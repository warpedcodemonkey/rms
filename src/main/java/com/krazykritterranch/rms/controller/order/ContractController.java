package com.krazykritterranch.rms.controller.order;

import com.krazykritterranch.rms.model.order.Contract;
import com.krazykritterranch.rms.repositories.order.ContractRepository;
import com.krazykritterranch.rms.service.order.ContractService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contract")
public class ContractController {
    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractRepository contractRepository;

    @GetMapping
    public ResponseEntity<List<Contract>> getAllContracts(){
        return new ResponseEntity<>(contractRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContractById(@PathVariable Long id){
        return contractRepository.findById(id)
                .map(contract -> new ResponseEntity<>(contract, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Contract>> getContractByCustomerId(@PathVariable Long customerId){
        return new ResponseEntity<>(contractRepository.findByCustomerId(customerId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Contract> createContract(@RequestBody Contract contract){
        return new ResponseEntity<>(contractRepository.save(contract), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contract> updateContract(@PathVariable Long id, @RequestBody Contract contract){
        return contractRepository.findById(id)
                .map(exitstigContract -> {
                    contract.setId(exitstigContract.getId());
                    return new ResponseEntity<>(contractRepository.save(contract), HttpStatus.OK);
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateContract(@PathVariable Long id){
        contractService.deactivateContract(id);
        return ResponseEntity.noContent().build();
    }


}
