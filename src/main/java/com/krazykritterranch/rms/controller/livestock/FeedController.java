package com.krazykritterranch.rms.controller.livestock;

import com.krazykritterranch.rms.model.livestock.Feed;
import com.krazykritterranch.rms.repositories.livestock.FeedRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
public class FeedController {
    @Autowired
    private FeedRepository feedRepository;

    @GetMapping
    public ResponseEntity<List<Feed>> getAllFeed(){
        return new ResponseEntity<>(feedRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feed> getFeedById(@PathVariable Long id){
        return feedRepository.findById(id)
                .map(feed -> new ResponseEntity<>(feed, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Feed> saveFeed(@RequestBody Feed feed){
        return new ResponseEntity<>(feedRepository.save(feed), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Feed> updateFeed(@PathVariable Long id, @RequestBody Feed feed){
        return feedRepository.findById(id)
                .map(existingFeed -> {
                    feed.setId(existingFeed.getId());
                    return new ResponseEntity<>(feedRepository.save(feed), HttpStatus.OK);
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long id){
        if (!feedRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        feedRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}