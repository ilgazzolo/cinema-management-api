package com.api.boleteria;

import com.api.boleteria.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void availableProductsArePublic() throws Exception {
        when(productService.findByAvailable(true)).thenReturn(List.of());

        mockMvc.perform(get("/api/products/available/true"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void activeCartRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/store/cart"))
                .andExpect(status().isUnauthorized());
    }
}
