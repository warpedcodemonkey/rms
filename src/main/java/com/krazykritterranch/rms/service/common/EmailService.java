package com.krazykritterranch.rms.service.common;

import com.krazykritterranch.rms.model.common.Email;
import com.krazykritterranch.rms.repositories.common.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class EmailService {
    @Autowired
    public EmailRepository emailRepository;


}
