package com.krazykritterranch.rms.controller.livestock;


import com.krazykritterranch.rms.model.livestock.Livestock;
import com.krazykritterranch.rms.repositories.livestock.LivestockRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livestock")

public class LivestockController {

    @Autowired
    private LivestockRepository repository;

    @GetMapping
    public ResponseEntity<List<Livestock>> getAll(){
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livestock> getById(@PathVariable Long id){
        return new ResponseEntity<>(repository.findById(id).get(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Livestock> save(@RequestBody Livestock livestock){
        return new ResponseEntity<>(repository.save(livestock), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livestock> update(@PathVariable Long id, @RequestBody Livestock livestock){
        return repository.findById(id)
                .map(existingHeatCycle -> {
                    livestock.setId(existingHeatCycle.getId());
                    return new ResponseEntity<>(repository.save(livestock), HttpStatus.OK);
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

