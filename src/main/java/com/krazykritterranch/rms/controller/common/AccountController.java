package com.krazykritterranch.rms.controller.common;

import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.repositories.common.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping
    public ResponseEntity<List<Account>> getAllAddresses(){
        List<Account> accounts = accountRepository.findAll();
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> findById(@PathVariable Long id){
        return accountRepository.findById(id)
                .map(account -> new ResponseEntity<>(account, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Account> save(@RequestBody Account account){
        Account retAccount = accountRepository.save(account);
        return new ResponseEntity<>(retAccount, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> update(@PathVariable Long id, @RequestBody Account account){
        return accountRepository.findById(id)
                .map(existingAccount -> {
                    account.setId(existingAccount.getId());
                    Account updatedAccount = accountRepository.save(account);
                    return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Account existing = accountRepository.findById(id).get();
        if(existing == null){
            return ResponseEntity.notFound().build();
        }
        accountRepository.delete(existing);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
