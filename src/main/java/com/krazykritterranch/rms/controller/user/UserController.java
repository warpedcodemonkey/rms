package com.krazykritterranch.rms.controller.user;

import com.krazykritterranch.rms.model.common.Address;
import com.krazykritterranch.rms.model.common.Email;
import com.krazykritterranch.rms.model.common.Phone;
import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.repositories.common.AddressRepository;
import com.krazykritterranch.rms.repositories.common.EmailRepository;
import com.krazykritterranch.rms.service.common.AddressService;
import com.krazykritterranch.rms.service.common.EmailService;
import com.krazykritterranch.rms.service.common.PhoneService;
import com.krazykritterranch.rms.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PhoneService phoneService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @GetMapping
    public ResponseEntity<List<User>> getAllCustomers(){
        return new ResponseEntity<>(userService.getAllCustomers(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getCustomerById(@PathVariable Long id){
        return new ResponseEntity<>(userService.findById(id), HttpStatus.OK);
    }

    @GetMapping("/search/{partialPhone}")
    public ResponseEntity<List<User>> searchCustomerByPhone(@PathVariable String partialPhone){
        return new ResponseEntity<>(userService.searchByPhone(partialPhone), HttpStatus.OK);
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<User> findByPhone(@PathVariable String phoneNumber){
        return new ResponseEntity<>(userService.findByPhone(phoneNumber), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<User> saveCustomer(@RequestBody User user){
        //Save or Update Phones
        user.setPhones(saveOrUpdatePhones(user.getPhones()));
        user.setEmails(saveOrUpdateEmails(user.getEmails()));
        user.setAddresses(saveOrUpdateAddresses(user.getAddresses()));
//        user.setPassWord(passwordEncoder.encode(user.getPassWord()));
        user.setActive(true);
        return new ResponseEntity<>(userService.saveCustomer(user), HttpStatus.OK);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<User> updateCustomer(@PathVariable Long customerId, @RequestBody User updatedUser) {
        User existingUser = userService.findById(customerId);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }

        updatedUser.setPhones(saveOrUpdatePhones(updatedUser.getPhones()));
        updatedUser.setEmails(saveOrUpdateEmails(updatedUser.getEmails()));
        updatedUser.setAddresses(saveOrUpdateAddresses(updatedUser.getAddresses()));

        // Save the updated customer
        User updated = userService.saveCustomer(updatedUser);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id){
        User existingUser = userService.findById(id);
        if (existingUser == null){
            return ResponseEntity.notFound().build();
        }
        existingUser.setActive(false);
        userService.saveCustomer(existingUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private List<Phone> saveOrUpdatePhones(List<Phone> phones){
        List<Phone> savedPhones = new ArrayList<>();
        for (Phone p : phones){
            savedPhones.add(phoneService.savePhone(p));
        }
        return savedPhones;
    }

    private List<Email> saveOrUpdateEmails(List<Email> emails){
        List<Email> savedEmails = new ArrayList<>();
        for(Email e: emails){
            savedEmails.add(emailRepository.save(e));
        }
        return savedEmails;
    }

    private List<Address> saveOrUpdateAddresses(List<Address> addresses){
        List<Address> savedAddress = new ArrayList<>();
        for(Address a : addresses){
            savedAddress.add(addressRepository.save(a));
        }
        return savedAddress;
    }


}
