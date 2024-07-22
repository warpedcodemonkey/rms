package com.krazykritterranch.rms.controller.livestock;

import com.krazykritterranch.rms.model.livestock.Breed;
import com.krazykritterranch.rms.repositories.livestock.BreedRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/breed")
public class BreedController {

    @Autowired
    private BreedRepository breedRepository;

    @GetMapping
    public ResponseEntity<List<Breed>> getAllBreeds(){
        return new ResponseEntity<>(breedRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Breed> getBreedById(@PathVariable Long id){
        return breedRepository.findById(id)
                .map(breed -> new ResponseEntity<>(breed, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Breed> saveBreed(@RequestBody Breed breed){
        return new ResponseEntity<>(breedRepository.save(breed), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Breed> updateBreed(@PathVariable Long id, @RequestBody Breed breed){
        return breedRepository.findById(id)
                .map(exitstigBreed -> {
                    breed.setId(exitstigBreed.getId());
                    return new ResponseEntity<>(breedRepository.save(breed), HttpStatus.OK);
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBreed(@PathVariable Long id){
        breedRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


}
