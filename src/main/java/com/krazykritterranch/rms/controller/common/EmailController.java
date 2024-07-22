package com.krazykritterranch.rms.controller.common;

import com.krazykritterranch.rms.model.common.Email;
import com.krazykritterranch.rms.repositories.common.EmailRepository;
import com.krazykritterranch.rms.service.common.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailRepository emailRepository;

    @GetMapping
    public ResponseEntity<List<Email>> findAll(){
        return new ResponseEntity<>(emailRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Email> getEmailById(@PathVariable Long id){
        return emailRepository.findById(id)
                .map(email -> new ResponseEntity<>(email, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Email> saveEmail(@RequestBody Email email){
        return new ResponseEntity<>(emailRepository.save(email), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Email> updateEmail(@PathVariable Long id, @RequestBody Email email) {
        return emailRepository.findById(id)
                .map(existingAddress -> {
                    email.setId(existingAddress.getId());
                    Email updatedAddress = emailRepository.save(email);
                    return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmailAddress(@PathVariable Long id){
        emailRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //TODO: Add null checks and item not found, or no results
}
