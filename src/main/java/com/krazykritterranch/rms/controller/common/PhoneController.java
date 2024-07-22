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
        return new ResponseEntity<>(phoneRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Phone> getPhoneById(@PathVariable  Long id){
        return phoneRepository.findById(id)
                .map(phone -> new ResponseEntity<>(phone, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @PostMapping
    public ResponseEntity<Phone> createPhone(@RequestBody Phone phone){
        return new ResponseEntity<>(phoneRepository.save(phone), HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity<Phone> updatePhone(@PathVariable Long id, @RequestBody Phone phone){
        Phone existingPhone = phoneRepository.findById(id).get();
        if(existingPhone == null){
            return ResponseEntity.notFound().build();
        }
        phone.setId(existingPhone.getId());
        return new ResponseEntity<>(phoneRepository.save(phone), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhone(@PathVariable Long id){
        phoneRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //TODO: Add null checks and item not found, or no results
}
