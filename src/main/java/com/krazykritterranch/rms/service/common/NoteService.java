package com.krazykritterranch.rms.service.common;

import com.krazykritterranch.rms.repositories.common.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoteService {
    @Autowired
    NoteRepository noteRepository;
}
