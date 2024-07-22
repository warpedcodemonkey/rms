package com.krazykritterranch.rms.controller.livestock;

import com.krazykritterranch.rms.model.livestock.Feed;
import com.krazykritterranch.rms.model.livestock.Feeding;
import com.krazykritterranch.rms.repositories.livestock.FeedingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feeding")
public class FeedingController {
    @Autowired
    private FeedingRepository feedingRepository;

    @GetMapping
    public ResponseEntity<List<Feeding>> getAll(){
        return new ResponseEntity<>(feedingRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feeding> getById(@PathVariable Long id){
        return new ResponseEntity<>(feedingRepository.findById(id).get(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Feeding> save(@RequestBody Feeding feeding){
        return new ResponseEntity<>(feedingRepository.save(feeding), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Feeding> update(@PathVariable Long id, @RequestBody Feeding feeding){
        return feedingRepository.findById(id)
                .map(existingFeeding -> {
                    feeding.setId(existingFeeding.getId());
                    return new ResponseEntity<>(feedingRepository.save(feeding), HttpStatus.OK);
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete (@PathVariable Long id){
        feedingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
