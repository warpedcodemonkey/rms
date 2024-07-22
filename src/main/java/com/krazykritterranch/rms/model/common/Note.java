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
    @OneToOne
    @JoinColumn(name = "customer_id")
    private User noteAuthor;

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

    @Override
    public String toString() {
        return new StringJoiner(", ", Note.class.getSimpleName() + "[", "]")
                .add("noteDate=" + noteDate)
                .add("noteSubject='" + noteSubject + "'")
                .add("note='" + note + "'")
                .add("noteAuthor=" + noteAuthor)
                .toString();
    }
}
