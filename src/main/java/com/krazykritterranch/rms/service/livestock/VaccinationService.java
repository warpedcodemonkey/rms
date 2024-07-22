package com.krazykritterranch.rms.service.livestock;

import com.krazykritterranch.rms.repositories.livestock.VaccinationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VaccinationService {

    @Autowired
    private VaccinationRepository vaccinationRepository;
}
