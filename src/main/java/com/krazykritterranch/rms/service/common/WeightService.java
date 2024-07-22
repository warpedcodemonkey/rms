package com.krazykritterranch.rms.service.common;

import com.krazykritterranch.rms.repositories.common.WeightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeightService {
    @Autowired
    private WeightRepository weightRepository;
}
