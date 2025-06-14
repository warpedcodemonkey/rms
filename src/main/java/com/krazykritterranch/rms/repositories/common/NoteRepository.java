package com.krazykritterranch.rms.repositories.common;

import com.krazykritterranch.rms.model.common.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // Find all notes for a specific account (tenant filtering)
    @Query("SELECT n FROM Note n WHERE n.account.id = :accountId ORDER BY n.noteDate DESC")
    List<Note> findByAccountId(@Param("accountId") Long accountId);

    // Find note by ID with account check (security)
    @Query("SELECT n FROM Note n WHERE n.id = :id AND n.account.id = :accountId")
    Optional<Note> findByIdAndAccountId(@Param("id") Long id, @Param("accountId") Long accountId);

    // Find notes by author within an account
    @Query("SELECT n FROM Note n WHERE n.noteAuthor.id = :authorId AND n.account.id = :accountId ORDER BY n.noteDate DESC")
    List<Note> findByAuthorIdAndAccountId(@Param("authorId") Long authorId, @Param("accountId") Long accountId);

    // Find notes by subject (case-insensitive search) within an account
    @Query("SELECT n FROM Note n WHERE LOWER(n.noteSubject) LIKE LOWER(CONCAT('%', :subject, '%')) AND n.account.id = :accountId ORDER BY n.noteDate DESC")
    List<Note> findBySubjectContainingAndAccountId(@Param("subject") String subject, @Param("accountId") Long accountId);

    // Find notes containing text (case-insensitive search) within an account
    @Query("SELECT n FROM Note n WHERE LOWER(n.note) LIKE LOWER(CONCAT('%', :text, '%')) AND n.account.id = :accountId ORDER BY n.noteDate DESC")
    List<Note> findByNoteContainingAndAccountId(@Param("text") String text, @Param("accountId") Long accountId);

    // Find notes within a date range for an account
    @Query("SELECT n FROM Note n WHERE n.noteDate BETWEEN :startDate AND :endDate AND n.account.id = :accountId ORDER BY n.noteDate DESC")
    List<Note> findByDateRangeAndAccountId(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("accountId") Long accountId);

    // Count notes by account
    @Query("SELECT COUNT(n) FROM Note n WHERE n.account.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);

    // Find recent notes (last N days) for an account
    @Query("SELECT n FROM Note n WHERE n.noteDate >= :sinceDate AND n.account.id = :accountId ORDER BY n.noteDate DESC")
    List<Note> findRecentNotesByAccountId(@Param("sinceDate") Date sinceDate, @Param("accountId") Long accountId);

    // Delete all notes for an account (useful for account deletion)
    @Query("DELETE FROM Note n WHERE n.account.id = :accountId")
    void deleteByAccountId(@Param("accountId") Long accountId);

    // Check if a note exists for a specific account
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Note n WHERE n.id = :id AND n.account.id = :accountId")
    boolean existsByIdAndAccountId(@Param("id") Long id, @Param("accountId") Long accountId);
}