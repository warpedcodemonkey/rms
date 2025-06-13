package com.krazykritterranch.rms.controller.livestock;

import com.krazykritterranch.rms.model.livestock.Vaccination;
import com.krazykritterranch.rms.repositories.livestock.VaccinationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vaccination")
public class VaccinationController {

    @Autowired
    private VaccinationRepository repository;

    @GetMapping
    public ResponseEntity<List<Vaccination>> getAll(){
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vaccination> getById(@PathVariable Long id){
        return repository.findById(id)
                .map(vaccination -> new ResponseEntity<>(vaccination, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Vaccination> save(@RequestBody Vaccination vaccination){
        return new ResponseEntity<>(repository.save(vaccination), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vaccination> update(@PathVariable Long id, @RequestBody Vaccination vaccination){
        return repository.findById(id)
                .map(existingVaccination -> {
                    vaccination.setId(existingVaccination.getId());
                    return new ResponseEntity<>(repository.save(vaccination), HttpStatus.OK);
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if (!repository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}