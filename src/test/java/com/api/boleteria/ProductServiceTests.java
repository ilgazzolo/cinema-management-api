package com.api.boleteria;

import com.api.boleteria.model.enums.ProductType;
import com.api.boleteria.repository.IProductRepository;
import com.api.boleteria.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @Mock
    private IProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void collectionQueriesReturnEmptyListsWhenThereAreNoProducts() {
        when(productRepository.findAll()).thenReturn(List.of());
        when(productRepository.findByName("inexistente")).thenReturn(Optional.empty());
        when(productRepository.findByAvailable(true)).thenReturn(List.of());
        when(productRepository.findByProductTypeAndAvailable(ProductType.COMIDA, true))
                .thenReturn(List.of());

        assertAll(
                () -> assertTrue(productService.findAll().isEmpty()),
                () -> assertTrue(productService.findByName("inexistente").isEmpty()),
                () -> assertTrue(productService.findByAvailable(true).isEmpty()),
                () -> assertTrue(productService.findByProductType(ProductType.COMIDA).isEmpty())
        );
    }
}
