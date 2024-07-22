package com.krazykritterranch.rms.service.common;

import com.krazykritterranch.rms.model.common.Address;
import com.krazykritterranch.rms.model.common.StateOrProvince;
import com.krazykritterranch.rms.repositories.common.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;



}
