package com.api.boleteria.service;

import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.exception.NotFoundException;
import com.api.boleteria.model.Product;

import java.util.List;


import org.springframework.stereotype.Service;

import com.api.boleteria.dto.detail.ProductDetailDTO;
import com.api.boleteria.dto.list.ProductListDTO;
import com.api.boleteria.dto.request.ProductRequestDTO;
import com.api.boleteria.repository.IProductRepository;
import com.api.boleteria.validators.ProductValidator;
import com.api.boleteria.model.enums.ProductType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final IProductRepository repo;


    /* ------------------------------------- CREATE ------------------------------------------------------------- */

    public ProductDetailDTO create (ProductRequestDTO req){  
        
        ProductValidator.validateFields(req);

        if(repo.existsByName(req.getName())){
            throw new BadRequestException("Ya existe un Producto con el nombre " + req.getName()+".");
        }
        
        Product producto = mapToEntity(req);
        repo.save(producto);
        ProductDetailDTO productDetailDTO = mapToDetailDTO(producto);

        return productDetailDTO;                        
    }


   /* ------------------------------------- FIND ------------------------------------------------------------- */

    public List<ProductListDTO> findAll() {
        List<ProductListDTO> list = repo.findAll()
                .stream()
                .map(this::mapToListDTO)
                .toList();

        if (list.isEmpty()) {
            throw new NotFoundException("No hay productos registrados.");
        }

        return list;
    }


    public ProductDetailDTO findById(Long id) {
        ProductValidator.validateId(id);
        Product product = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("El producto con ID: " + id + " no fue encontrado. "));
        return mapToDetailDTO(product);
    }

    public List<ProductDetailDTO> findByName(String name) {
        ProductValidator.validateName(name);
        List<ProductDetailDTO> list = repo.findByName(name).stream()
                .map(this::mapToDetailDTO)
                .toList();

        if (list.isEmpty()) {
            throw new NotFoundException("No hay productos que coinicidan con la busqueda.");
        }    

        return list;
    }

    public List<ProductDetailDTO> findByAvailable(boolean available) {
        ProductValidator.validateAvailable(available);
        List<ProductDetailDTO> list = repo.findByAvailable(available).stream()
                .map(this::mapToDetailDTO)
                .toList();

        if (list.isEmpty()) {
            throw new NotFoundException("No hay productos disponibles.");
        }

        return list;
    }

    public List<ProductListDTO> findByProductType(ProductType productType) {
        ProductValidator.validateProductType(productType);
        List<ProductListDTO> list = repo.findByProductTypeAndAvailable(productType, true).stream()
                .map(this::mapToListDTO)
                .toList();

        if (list.isEmpty()) {
            throw new NotFoundException("No se encontraron productos de la categoria: " + productType);
        }

        return list;
    }

    /* ------------------------------------- UPDATE ------------------------------------------------------------- */

    public ProductDetailDTO updateByID(Long id, ProductRequestDTO req) {
        ProductValidator.validateId(id);
        ProductValidator.validateFields(req);

        if (repo.existsByNameAndIdNot(req.getName(), id)) { //
            throw new BadRequestException("Ya existe otro producto con el nombre: " + req.getName()); //
        }

        return repo.findById(id)
                .map(p -> {
                    p.setName(req.getName());
                    p.setUnitPrice(req.getUnitPrice());
                    p.setPriceInPoints(req.getPriceInPoints());
                    p.setStock(req.getStock());
                    p.setUnitCost(req.getUnitCost());
                    p.setImageURL(req.getImageURL());
                    p.setDescription(req.getDescription());
                    p.setAvailable(req.getAvailable());
                    p.setProductType(req.getProductType());

                    Product updated = repo.save(p);
                    return mapToDetailDTO(updated);
                })
                .orElseThrow(() -> new NotFoundException("El producto con ID: " + id + " no fue encontrado."));
    }


    /*-------------------------------------- DELETE ----------------------------------------------------------------------------- */

    public void deleteById(Long id) {
        ProductValidator.validateId(id);
        if (!repo.existsById(id)) {
            throw new NotFoundException("El producto con ID: " + id + " no fue encontrado. ");
        }
        repo.deleteById(id);
    }

    



    /*--------------------------------MAPS------------------------------------------------ */

    private ProductListDTO mapToListDTO(Product product) {
        return new ProductListDTO(
                product.getId(),
                product.getName(),
                product.getUnitPrice(),
                product.getPriceInPoints(),
                product.getImageURL()
        );
    }

    private ProductDetailDTO mapToDetailDTO(Product product) {
        return new ProductDetailDTO(
                product.getId(),
                product.getName(),
                product.getUnitPrice(),
                product.getPriceInPoints(),
                product.getStock(),
                product.getUnitCost(),
                product.getImageURL(),
                product.getDescription(),
                product.getAvailable(),
                product.getProductType()
        );
    }

    private Product mapToEntity(ProductRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setUnitPrice(dto.getUnitPrice());
        product.setPriceInPoints(dto.getPriceInPoints());
        product.setStock(dto.getStock());
        product.setUnitCost(dto.getUnitCost());
        product.setImageURL(dto.getImageURL());
        product.setDescription(dto.getDescription());
        product.setAvailable(dto.getAvailable());
        product.setProductType(dto.getProductType());
        
        return product;
    }

}
