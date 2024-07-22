package com.krazykritterranch.rms.controller.livestock;

import com.krazykritterranch.rms.model.livestock.HeatCycle;
import com.krazykritterranch.rms.repositories.livestock.HeatCycleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/heatcycle")
public class HeatCycleController {

    @Autowired
    private HeatCycleRepository repository;

    @GetMapping
    public ResponseEntity<List<HeatCycle>> getAll(){
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeatCycle> getById(@PathVariable Long id){
        return new ResponseEntity<>(repository.findById(id).get(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<HeatCycle> save(@RequestBody HeatCycle heatCycle){
        return new ResponseEntity<>(repository.save(heatCycle), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HeatCycle> update(@PathVariable Long id, @RequestBody HeatCycle heatCycle){
        return repository.findById(id)
                .map(existingHeatCycle -> {
                    heatCycle.setId(existingHeatCycle.getId());
                    return new ResponseEntity<>(repository.save(heatCycle), HttpStatus.OK);
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
