package com.api.boleteria.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.boleteria.dto.detail.ProductDetailDTO;
import com.api.boleteria.dto.list.CinemaListDTO;
import com.api.boleteria.dto.list.ProductListDTO;
import com.api.boleteria.dto.request.ProductRequestDTO;
import com.api.boleteria.model.enums.ProductType;
import com.api.boleteria.model.enums.ScreenType;
import com.api.boleteria.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Validated
@CrossOrigin(origins = {"http://localhost:4200"})
public class ProductController {

    @Autowired
    ProductService service;

    /*----------------------------- POST --------------------------------------------------------------------------------- */

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDetailDTO> create(@Valid @RequestBody ProductRequestDTO req){
        return ResponseEntity.ok(service.create(req));
    }

    /*----------------------------- GET --------------------------------------------------------------------------------- */


    @GetMapping("")
    public ResponseEntity<List<ProductListDTO>> getList(){
        List<ProductListDTO> list = service.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ProductDetailDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/available/{available}")
    public ResponseEntity<List<ProductDetailDTO>> getByAvailableProduct(@PathVariable boolean available){
        List<ProductDetailDTO> list = service.findByAvailable(available);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/productType/{productType}")
    public ResponseEntity<List<ProductListDTO>> getByProductType(@PathVariable ProductType productType){
        List<ProductListDTO> list = service.findByProductType(productType);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<List<ProductDetailDTO>> getByName(@PathVariable String name){
        List<ProductDetailDTO> list = service.findByName(name);
        return ResponseEntity.ok(list);
    }

    /*----------------------------- UPDATE --------------------------------------------------------------------------------- */


    @PutMapping("update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDetailDTO> update(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO req){
        return ResponseEntity.ok(service.updateByID(id, req));
    }
      
    /*----------------------------- DELETE --------------------------------------------------------------------------------- */


    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }  
}
