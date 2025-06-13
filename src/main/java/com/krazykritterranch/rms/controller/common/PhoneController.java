package com.krazykritterranch.rms.controller.common;

import com.krazykritterranch.rms.model.common.Phone;
import com.krazykritterranch.rms.repositories.common.PhoneRepository;
import com.krazykritterranch.rms.service.common.PhoneService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phone")
public class PhoneController {

    @Autowired
    private PhoneService phoneService;

    @Autowired
    private PhoneRepository phoneRepository;

    @GetMapping
    public ResponseEntity<List<Phone>> getAllPhones(){
        return new ResponseEntity<>(phoneService.getAllPhones(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Phone> getPhoneById(@PathVariable Long id){
        return phoneRepository.findById(id)
                .map(phone -> new ResponseEntity<>(Phone.formatPhone(phone), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/area-code/{areaCode}")
    public ResponseEntity<List<Phone>> getPhonesByAreaCode(@PathVariable String areaCode){
        return new ResponseEntity<>(phoneService.getByAreaCode(areaCode), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Phone> createPhone(@RequestBody Phone phone){
        return new ResponseEntity<>(phoneService.savePhone(phone), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Phone> updatePhone(@PathVariable Long id, @RequestBody Phone phone){
        return phoneRepository.findById(id)
                .map(existingPhone -> {
                    phone.setId(existingPhone.getId());
                    return new ResponseEntity<>(phoneService.savePhone(phone), HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhone(@PathVariable Long id){
        if (!phoneRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        phoneService.deletePhone(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}