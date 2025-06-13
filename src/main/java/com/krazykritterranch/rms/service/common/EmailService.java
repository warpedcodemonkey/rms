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
    private EmailRepository emailRepository;

    public List<Email> getAllEmails() {
        return emailRepository.findAll();
    }

    public Optional<Email> getEmailById(Long id) {
        return emailRepository.findById(id);
    }

    public Email getEmailByAddress(String emailAddress) {
        Optional<Email> email = emailRepository.findByConstructedEmailAddress(emailAddress);
        return email.orElse(null);
    }

    public List<Email> searchEmailsByPartial(String partialEmail) {
        return emailRepository.findByPartialEmailAddress(partialEmail);
    }

    public boolean emailExists(String emailAddress) {
        return emailRepository.existsByConstructedEmailAddress(emailAddress);
    }

    public Email saveEmail(Email email) {
        // Ensure the email address is properly parsed into components
        if (email.getEmailAddress() != null &&
                (email.getEmailAccount() == null || email.getDomain() == null || email.getTld() == null)) {
            email.setEmailAddress(email.getEmailAddress()); // This will parse the components
        }
        return emailRepository.save(email);
    }

    public Email updateEmail(Long id, Email email) {
        Optional<Email> existingEmail = emailRepository.findById(id);
        if (existingEmail.isPresent()) {
            email.setId(existingEmail.get().getId());
            return saveEmail(email);
        }
        return null;
    }

    public void deleteEmail(Long id) {
        emailRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return emailRepository.existsById(id);
    }
}