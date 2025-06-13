package com.krazykritterranch.rms.controller.common;

import com.krazykritterranch.rms.model.common.Weight;
import com.krazykritterranch.rms.repositories.common.WeightRepository;
import com.krazykritterranch.rms.service.common.WeightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weight")
public class WeightController {

    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    private WeightService weightService;

    @GetMapping
    public ResponseEntity<List<Weight>> getAll(){
        return new ResponseEntity<>(weightRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Weight> getWeightById(@PathVariable Long id){
        return weightRepository.findById(id)
                .map(weight -> new ResponseEntity<>(weight, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Weight> saveWeight(@RequestBody Weight weight){
        return new ResponseEntity<>(weightRepository.save(weight), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Weight> updateWeight(@PathVariable Long id, @RequestBody Weight weight){
        return weightRepository.findById(id)
                .map(existingWeight -> {
                    weight.setId(existingWeight.getId());
                    return new ResponseEntity<>(weightRepository.save(weight), HttpStatus.OK);
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWeight(@PathVariable Long id){
        if (!weightRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        weightRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}