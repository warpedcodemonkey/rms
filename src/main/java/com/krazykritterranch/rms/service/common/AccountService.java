package com.krazykritterranch.rms.service.common;

import com.krazykritterranch.rms.repositories.common.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
}
