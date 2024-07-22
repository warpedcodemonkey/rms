package com.krazykritterranch.rms.controller.livestock;

import com.krazykritterranch.rms.model.livestock.Breed;
import com.krazykritterranch.rms.model.livestock.LivestockType;
import com.krazykritterranch.rms.repositories.livestock.LivestockTypeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livestocktype")
public class LivestockTypeController {

    @Autowired
    private LivestockTypeRepository livestockTypeRepository;

    @GetMapping
    public ResponseEntity<List<LivestockType>> getAllLivestockTypes(){
        return new ResponseEntity<>(livestockTypeRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivestockType> getBreedById(@PathVariable Long id){
        return livestockTypeRepository.findById(id)
                .map(livestockType -> new ResponseEntity<>(livestockType, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<LivestockType> saveBreed(@RequestBody LivestockType livestockType){
        return new ResponseEntity<>(livestockTypeRepository.save(livestockType), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LivestockType> updateBreed(@PathVariable Long id, @RequestBody LivestockType livestockType){
        return livestockTypeRepository.findById(id)
                .map(existingType -> {
                    livestockType.setId(existingType.getId());
                    return new ResponseEntity<>(livestockTypeRepository.save(livestockType), HttpStatus.OK);
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBreed(@PathVariable Long id){
        livestockTypeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
