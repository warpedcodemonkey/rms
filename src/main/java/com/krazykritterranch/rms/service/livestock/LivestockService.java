package com.krazykritterranch.rms.service.livestock;

import com.krazykritterranch.rms.repositories.livestock.LivestockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LivestockService {
    @Autowired
    private LivestockRepository livestockRepository;


}
