package com.krazykritterranch.rms.service.common;

import com.krazykritterranch.rms.model.common.Note;
import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.user.VetPermissionType;
import com.krazykritterranch.rms.repositories.common.NoteRepository;
import com.krazykritterranch.rms.repositories.common.AccountRepository;
import com.krazykritterranch.rms.service.security.TenantContext;
import com.krazykritterranch.rms.service.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TenantContext tenantContext;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Get all notes for the current account
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public List<Note> getAllNotes() {
        if (tenantContext.isAdmin()) {
            return noteRepository.findAll();
        } else if (tenantContext.isAccountUser()) {
            Long accountId = tenantContext.getCurrentAccountId();
            if (accountId == null) {
                throw new SecurityException("No account context available");
            }
            return noteRepository.findByAccountId(accountId);
        } else if (tenantContext.isVeterinarian()) {
            Long vetId = tenantContext.getCurrentUserId();
            Long accountId = tenantContext.getCurrentAccountId();
            if (accountId != null && securityService.canAccessAccount(accountId)) {
                return noteRepository.findByAccountId(accountId);
            }
            throw new SecurityException("Veterinarian does not have access to this account");
        }
        throw new SecurityException("Access denied");
    }

    /**
     * Get a specific note by ID
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public Optional<Note> findById(Long id) {
        Optional<Note> note = noteRepository.findById(id);
        if (note.isEmpty()) {
            return Optional.empty();
        }

        // Security check
        if (!canAccessNote(note.get())) {
            throw new SecurityException("Access denied");
        }

        return note;
    }

    /**
     * Create a new note
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public Note createNote(Note note) {
        User currentUser = securityService.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("No authenticated user");
        }

        // Set the author
        note.setNoteAuthor(currentUser);

        // Set the account
        if (tenantContext.isAdmin()) {
            // Admin must specify account
            if (note.getAccount() == null) {
                throw new IllegalArgumentException("Account must be specified");
            }
        } else if (tenantContext.isAccountUser()) {
            Long accountId = tenantContext.getCurrentAccountId();
            if (accountId == null) {
                throw new SecurityException("No account context available");
            }
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            note.setAccount(account);
        } else if (tenantContext.isVeterinarian()) {
            Long accountId = tenantContext.getCurrentAccountId();
            if (accountId == null || !securityService.canAccessAccount(accountId)) {
                throw new SecurityException("Veterinarian does not have access to this account");
            }
            // Check if vet has edit permission
            if (!securityService.hasVetPermission(accountId, VetPermissionType.EDIT_MEDICAL_RECORDS)) {
                throw new SecurityException("Veterinarian does not have permission to create notes");
            }
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            note.setAccount(account);
        }

        // Set current date if not provided
        if (note.getNoteDate() == null) {
            note.setNoteDate(new Date(System.currentTimeMillis()));
        }

        return noteRepository.save(note);
    }

    /**
     * Update an existing note
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public Note updateNote(Long id, Note noteDetails) {
        Note existingNote = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        User currentUser = securityService.getCurrentUser();

        // Check if user can edit
        if (!canEditNote(existingNote, currentUser)) {
            throw new SecurityException("Not authorized to edit this note");
        }

        // Update fields
        existingNote.setNoteSubject(noteDetails.getNoteSubject());
        existingNote.setNote(noteDetails.getNote());
        // Note date and author should not be changed

        return noteRepository.save(existingNote);
    }

    /**
     * Delete a note
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public void deleteNote(Long id) {
        Note existingNote = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        User currentUser = securityService.getCurrentUser();

        // Check if user can delete
        if (!canDeleteNote(existingNote, currentUser)) {
            throw new SecurityException("Not authorized to delete this note");
        }

        noteRepository.delete(existingNote);
    }

    /**
     * Search notes by subject
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public List<Note> searchBySubject(String subject) {
        if (tenantContext.isAdmin()) {
            // Admin could search all, but let's limit to current context
            Long accountId = tenantContext.getCurrentAccountId();
            if (accountId != null) {
                return noteRepository.findBySubjectContainingAndAccountId(subject, accountId);
            }
            return List.of();
        } else if (tenantContext.isAccountUser()) {
            Long accountId = tenantContext.getCurrentAccountId();
            if (accountId == null) {
                throw new SecurityException("No account context available");
            }
            return noteRepository.findBySubjectContainingAndAccountId(subject, accountId);
        } else if (tenantContext.isVeterinarian()) {
            Long accountId = tenantContext.getCurrentAccountId();
            if (accountId != null && securityService.canAccessAccount(accountId)) {
                return noteRepository.findBySubjectContainingAndAccountId(subject, accountId);
            }
            throw new SecurityException("Access denied");
        }
        throw new SecurityException("Access denied");
    }

    /**
     * Get notes by date range
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public List<Note> getNotesByDateRange(Date startDate, Date endDate) {
        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null) {
            throw new SecurityException("No account context available");
        }

        if (tenantContext.isVeterinarian() && !securityService.canAccessAccount(accountId)) {
            throw new SecurityException("Access denied");
        }

        return noteRepository.findByDateRangeAndAccountId(startDate, endDate, accountId);
    }

    /**
     * Count notes for current account
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public long countNotes() {
        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null) {
            throw new SecurityException("No account context available");
        }

        return noteRepository.countByAccountId(accountId);
    }

    // Helper methods
    private boolean canAccessNote(Note note) {
        if (tenantContext.isAdmin()) {
            return true;
        }

        if (tenantContext.isAccountUser()) {
            return note.getAccount() != null &&
                    note.getAccount().getId().equals(tenantContext.getCurrentAccountId());
        }

        if (tenantContext.isVeterinarian()) {
            return note.getAccount() != null &&
                    securityService.canAccessAccount(note.getAccount().getId());
        }

        return false;
    }

    private boolean canEditNote(Note note, User currentUser) {
        if (tenantContext.isAdmin()) {
            return true;
        }

        // Author can edit their own notes
        if (note.getNoteAuthor() != null &&
                note.getNoteAuthor().getId().equals(currentUser.getId())) {
            return true;
        }

        // Account users with proper permissions
        if (tenantContext.isAccountUser() && canAccessNote(note)) {
            return true;
        }

        // Veterinarians with edit permission
        if (tenantContext.isVeterinarian() && note.getAccount() != null) {
            return securityService.hasVetPermission(note.getAccount().getId(),
                    VetPermissionType.EDIT_MEDICAL_RECORDS);
        }

        return false;
    }

    private boolean canDeleteNote(Note note, User currentUser) {
        if (tenantContext.isAdmin()) {
            return true;
        }

        // Author can delete their own notes
        if (note.getNoteAuthor() != null &&
                note.getNoteAuthor().getId().equals(currentUser.getId())) {
            return true;
        }

        // Account administrators
        if (tenantContext.isAccountUser() && canAccessNote(note)) {
            // Could add additional check for account admin role here
            return true;
        }

        return false;
    }
}