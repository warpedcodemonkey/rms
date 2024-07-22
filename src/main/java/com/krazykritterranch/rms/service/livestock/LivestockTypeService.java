package com.krazykritterranch.rms.service.livestock;

import com.krazykritterranch.rms.repositories.livestock.LivestockTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LivestockTypeService {

    @Autowired
    private LivestockTypeRepository livestockTypeRepository;
}
