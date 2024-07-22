package com.krazykritterranch.rms.service.common;

import com.krazykritterranch.rms.model.common.Phone;
import com.krazykritterranch.rms.repositories.common.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhoneService {

    @Autowired
    private PhoneRepository phoneRepository;

    public Phone getPhoneById(Long id){
        return Phone.formatPhone(phoneRepository.findById(id).get());
    }

    private List<Phone> formatPhones(List<Phone> phones){
        return phones.stream()
                .map(
                        p -> {
                            Phone.formatPhone(p);
                            return p;
                        }
                )
                .collect(Collectors.toList());
    }

    public List<Phone> getAllPhones(){
        return this.formatPhones(phoneRepository.findAll());
    }

    public List<Phone> getByAreaCode(String areaCode){
        return this.formatPhones(phoneRepository.findByAreaCode(areaCode));
    }

    public Phone savePhone(Phone phone){
        return Phone.formatPhone(phoneRepository.save(phone));
    }

    public void deletePhone(Long id){
        phoneRepository.deleteById(id);
    }



}
