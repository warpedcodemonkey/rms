package com.krazykritterranch.rms.service.livestock;

import com.krazykritterranch.rms.repositories.livestock.HeatCycleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HeatCycleService {
    @Autowired
    private HeatCycleRepository repository;
}
