package com.krazykritterranch.rms.service.common;

import com.krazykritterranch.rms.model.common.StateOrProvince;
import com.krazykritterranch.rms.repositories.common.StateOrProvinceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StateOrProvinceService {
    @Autowired
    private StateOrProvinceRepository stateOrProvinceRepository;

}
