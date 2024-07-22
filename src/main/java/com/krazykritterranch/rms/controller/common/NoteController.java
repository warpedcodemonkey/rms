package com.krazykritterranch.rms.controller.common;

import com.krazykritterranch.rms.model.common.Note;
import com.krazykritterranch.rms.repositories.common.NoteRepository;
import com.krazykritterranch.rms.service.common.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/note")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteService noteService;


    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes(){
        return new ResponseEntity<>(noteRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id){
        return new ResponseEntity<>(noteRepository.findById(id).get(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Note> saveNote(@RequestBody Note note){
        return new ResponseEntity<>(noteRepository.save(note), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note note){
        Note existingNote = noteRepository.findById(id).get();
        if (existingNote == null){
            return ResponseEntity.notFound().build();
        }
        note.setId(existingNote.getId());
        return new ResponseEntity<>(noteRepository.save(note), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id){
        Note existingNote = noteRepository.findById(id).get();
        if (existingNote == null){
            return ResponseEntity.notFound().build();
        }
        noteRepository.delete(existingNote);
        return ResponseEntity.noContent().build();
    }


}
