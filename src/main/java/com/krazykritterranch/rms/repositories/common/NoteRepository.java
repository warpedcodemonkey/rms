package com.krazykritterranch.rms.repositories.common;

import com.krazykritterranch.rms.model.common.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
}
