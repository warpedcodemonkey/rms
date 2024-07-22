package com.krazykritterranch.rms.service.livestock;

import com.krazykritterranch.rms.repositories.livestock.BreedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BreedService {
    @Autowired
    private BreedRepository breedRepository;

}
