package com.krazykritterranch.rms.service.livestock;

import com.krazykritterranch.rms.repositories.livestock.FeedingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedService {

    @Autowired
    private FeedingRepository feedingRepository;
}
