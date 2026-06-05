package com.api.boleteria.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.api.boleteria.dto.detail.StoreOrderDetailDTO;
import com.api.boleteria.dto.list.StoreOrderListDTO;
import com.api.boleteria.dto.request.OrderItemsRequestDTO;
import com.api.boleteria.service.StoreOrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200"})
public class StoreOrderController {

    private final StoreOrderService storeOrderService;

    @PostMapping("/cart/items")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<StoreOrderDetailDTO> addToCart(@RequestBody OrderItemsRequestDTO request) {
        StoreOrderDetailDTO cart = storeOrderService.addProductToCart(request);
        return ResponseEntity.ok(cart);
    }


    @GetMapping("/cart")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<StoreOrderDetailDTO> getActiveCart() {
        StoreOrderDetailDTO activeCart = storeOrderService.getActiveCart();
        return ResponseEntity.ok(activeCart);
    }


    @GetMapping("/history")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<StoreOrderListDTO>> getHistory() {
        List<StoreOrderListDTO> history = storeOrderService.getPurchaseHistory();
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/cart/items/{itemId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<StoreOrderDetailDTO> removeItemFromCart(@PathVariable Long itemId) {
        StoreOrderDetailDTO updatedCart = storeOrderService.removeItemFromCart(itemId);
        return ResponseEntity.ok(updatedCart);
    }
}