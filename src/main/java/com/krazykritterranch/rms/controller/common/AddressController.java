package com.krazykritterranch.rms.controller.common;

import com.krazykritterranch.rms.model.common.Address;
import com.krazykritterranch.rms.model.common.StateOrProvince;
import com.krazykritterranch.rms.repositories.common.AddressRepository;
import com.krazykritterranch.rms.repositories.common.StateOrProvinceRepository;
import com.krazykritterranch.rms.service.common.AddressService;
import com.krazykritterranch.rms.service.common.StateOrProvinceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {
    @Autowired
    private AddressService addressService;
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private StateOrProvinceRepository stateOrProvinceRepository;

    @Autowired
    private StateOrProvinceService stateOrProvinceService;

    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses(){
        List<Address> addresses = addressRepository.findAll();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/postalCode/{postalCode}")
    public ResponseEntity<List<Address>> findAllByPostalCode(@PathVariable String postalCode){
        List<Address> addresses = addressRepository.findByPostalCode(postalCode);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/state/{stateId}")
    public ResponseEntity<List<Address>> listAddressByState(@PathVariable Long stateId){
        return stateOrProvinceRepository.findById(stateId)
                .map(stateOrProvince -> {
                    List<Address> addresses = addressRepository.listByState(stateOrProvince);
                    return new ResponseEntity<>(addresses, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> findById(@PathVariable Long id){
        return addressRepository.findById(id)
                .map(address -> new ResponseEntity<>(address, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Address> saveAddress(@RequestBody Address address){
        Address retAddress = addressRepository.save(address);
        return new ResponseEntity<>(retAddress, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long id, @RequestBody Address address){
        return addressRepository.findById(id)
                .map(existingAddress -> {
                    address.setId(existingAddress.getId());
                    Address updatedAddress = addressRepository.save(address);
                    return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        if (!addressRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        addressRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}