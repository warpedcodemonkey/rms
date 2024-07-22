package com.krazykritterranch.rms.controller.product;

import com.krazykritterranch.rms.model.product.Product;
import com.krazykritterranch.rms.repositories.product.ProductRepository;
import com.krazykritterranch.rms.service.product.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;


    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(){
        return new ResponseEntity<>( productRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id){
       return productRepository.findById(id)
                .map(stateOrProvince -> new ResponseEntity<>(stateOrProvince, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search/name/{search}")
    public ResponseEntity<List<Product>> searchByName(@PathVariable String search){
        return new ResponseEntity<>(productRepository.findByProductNameContains(search), HttpStatus.OK);
    }

    @GetMapping("/search/desc/{search}")
    public ResponseEntity<List<Product>> searchByDescription(@PathVariable String search){
        return new ResponseEntity<>(productRepository.findByProductDescriptionContains(search), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Product> saveProduct(@RequestBody Product product){
        return new ResponseEntity<>(productRepository.save(product), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product){
        return productRepository.findById(id)
                .map(existingProduct -> {
                    product.setId(existingProduct.getId());
                    return new ResponseEntity<>(productRepository.save(product), HttpStatus.OK);
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
