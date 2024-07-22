package com.krazykritterranch.rms.controller.common;

import com.krazykritterranch.rms.model.common.StateOrProvince;
import com.krazykritterranch.rms.repositories.common.StateOrProvinceRepository;
import com.krazykritterranch.rms.service.common.StateOrProvinceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stateorprovince")

public class StateOrProvinceController {
    @Autowired
    private StateOrProvinceService stateOrProvinceService;

    @Autowired
    private StateOrProvinceRepository stateOrProvinceRepository;

    @GetMapping
    public ResponseEntity<List<StateOrProvince>> getAllStateOrProvinces() {
        List<StateOrProvince> stateOrProvinces = stateOrProvinceRepository.findAll();
        return new ResponseEntity<>(stateOrProvinces, HttpStatus.OK);
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<StateOrProvince>> search(@PathVariable String keyword){
        List<StateOrProvince> stateOrProvinces = stateOrProvinceRepository.findByNameContainingIgnoreCase(keyword);
        return new ResponseEntity<>(stateOrProvinces, HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<StateOrProvince> getStateOrProvinceById(@PathVariable Long id) {
        return stateOrProvinceRepository.findById(id)
                .map(stateOrProvince -> new ResponseEntity<>(stateOrProvince, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<StateOrProvince> createStateOrProvince(@RequestBody StateOrProvince stateOrProvince) {
        StateOrProvince savedStateOrProvince = stateOrProvinceRepository.save(stateOrProvince);
        return new ResponseEntity<>(savedStateOrProvince, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StateOrProvince> updateStateOrProvince(@PathVariable Long id, @RequestBody StateOrProvince stateOrProvince) {
        return stateOrProvinceRepository.findById(id)
                .map(existingStateOrProvince -> {
                    stateOrProvince.setId(existingStateOrProvince.getId());
                    StateOrProvince updatedStateOrProvince = stateOrProvinceRepository.save(stateOrProvince);
                    return new ResponseEntity<>(updatedStateOrProvince, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStateOrProvince(@PathVariable Long id) {
        stateOrProvinceRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //TODO: Add null checks and item not found, or no results
}
