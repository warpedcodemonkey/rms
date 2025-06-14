package com.krazykritterranch.rms.model.common;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.user.User;

import jakarta.persistence.*;
import java.sql.Date;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "note_id"))
public class Note extends BaseVO {

    private Date noteDate;

    private String noteSubject;

    @Column(name = "note", length = 250)
    private String note;

    @ManyToOne
    @JoinColumn(name = "note_author_id", nullable = false)
    private User noteAuthor;

    // CRITICAL: Add account relationship for multi-tenancy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Constructors
    public Note() {}

    public Note(Account account, User noteAuthor, String noteSubject, String note) {
        this.account = account;
        this.noteAuthor = noteAuthor;
        this.noteSubject = noteSubject;
        this.note = note;
        this.noteDate = new Date(System.currentTimeMillis());
    }

    // Getters and Setters
    public Date getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(Date noteDate) {
        this.noteDate = noteDate;
    }

    public String getNoteSubject() {
        return noteSubject;
    }

    public void setNoteSubject(String noteSubject) {
        this.noteSubject = noteSubject;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public User getNoteAuthor() {
        return noteAuthor;
    }

    public void setNoteAuthor(User noteAuthor) {
        this.noteAuthor = noteAuthor;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Note.class.getSimpleName() + "[", "]")
                .add("id=" + getId())
                .add("noteDate=" + noteDate)
                .add("noteSubject='" + noteSubject + "'")
                .add("note='" + note + "'")
                .add("noteAuthor=" + (noteAuthor != null ? noteAuthor.getUsername() : "null"))
                .add("account=" + (account != null ? account.getAccountNumber() : "null"))
                .toString();
    }
}