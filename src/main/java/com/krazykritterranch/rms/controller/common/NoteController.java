package com.krazykritterranch.rms.controller.common;

import com.krazykritterranch.rms.model.common.Note;
import com.krazykritterranch.rms.service.common.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/api/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Note>> getAllNotes() {
        try {
            List<Note> notes = noteService.getAllNotes();
            return ResponseEntity.ok(notes);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        try {
            return noteService.findById(id)
                    .map(note -> ResponseEntity.ok(note))
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<Note> saveNote(@RequestBody Note note) {
        try {
            Note savedNote = noteService.createNote(note);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedNote);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note note) {
        try {
            Note updatedNote = noteService.updateNote(id, note);
            return ResponseEntity.ok(updatedNote);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        try {
            noteService.deleteNote(id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Note>> searchNotes(@RequestParam String subject) {
        try {
            if (subject == null || subject.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            List<Note> notes = noteService.searchBySubject(subject.trim());
            return ResponseEntity.ok(notes);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Note>> getNotesByDateRange(
            @RequestParam Date startDate,
            @RequestParam Date endDate) {
        try {
            if (startDate == null || endDate == null) {
                return ResponseEntity.badRequest().build();
            }
            if (startDate.after(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            List<Note> notes = noteService.getNotesByDateRange(startDate, endDate);
            return ResponseEntity.ok(notes);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<Long> countNotes() {
        try {
            long count = noteService.countNotes();
            return ResponseEntity.ok(count);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}