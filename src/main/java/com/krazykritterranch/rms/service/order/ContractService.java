package com.krazykritterranch.rms.service.order;

import com.krazykritterranch.rms.model.order.Contract;
import com.krazykritterranch.rms.repositories.order.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService {
    @Autowired
    private ContractRepository contractRepository;



    public void deactivateContract(Long id){
        Contract existingContract = contractRepository.findById(id).get();
        existingContract.setActive(false);
        contractRepository.save(existingContract);
    }

}
