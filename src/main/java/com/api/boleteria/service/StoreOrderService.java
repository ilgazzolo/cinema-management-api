package com.api.boleteria.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.api.boleteria.dto.detail.OrderItemsDetailDTO;
import com.api.boleteria.dto.detail.StoreOrderDetailDTO;
import com.api.boleteria.dto.list.StoreOrderListDTO;
import com.api.boleteria.dto.request.OrderItemsRequestDTO;
import com.api.boleteria.model.OrderItems;
import com.api.boleteria.model.Product;
import com.api.boleteria.model.StoreOrder;
import com.api.boleteria.model.User;
import com.api.boleteria.model.enums.StatusPayment;
import com.api.boleteria.repository.IOrderItemsRepository;
import com.api.boleteria.repository.IProductRepository;
import com.api.boleteria.repository.IStoreOrderRepository;
import com.api.boleteria.validators.StoreOrderValidator;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreOrderService {
    private final IStoreOrderRepository orderRepository;
    private final IProductRepository productRepository;
    private final IOrderItemsRepository orderItemsRepository;
    private final UserService userService;




    @Transactional
    public StoreOrderDetailDTO addProductToCart(OrderItemsRequestDTO request) {
        // 2. Magia pura: Obtenemos el usuario con tu método
        User user = userService.findAuthenticatedUser(); 
        
        Product product = productRepository.findById(request.getProductId()).orElseThrow();

        // 3. Validamos (usando el método estático que vimos antes)
        StoreOrderValidator.validateStock(product, request.getQuantity());

        // 4. Obtener carrito activo (PENDING) o crear uno nuevo
        StoreOrder cart = orderRepository.findByUserAndStatus(user, StatusPayment.PENDING)
                .orElseGet(() -> createNewCart(user));

        if (product.getStock() < request.getQuantity()) {
            // Puedes lanzar una BadRequestException o IllegalArgumentException
            throw new IllegalArgumentException("No hay suficiente stock. Stock disponible: " + product.getStock()); 
        }

        // 2. Descontar el stock
        product.setStock(product.getStock() - request.getQuantity());

        // 5. Crear el item y agregarlo al carrito
        OrderItems newItem = new OrderItems();
        newItem.setStoreOrder(cart);
        newItem.setProduct(product);
        newItem.setQuantity(request.getQuantity());
        newItem.setHistoricalPrice(product.getUnitPrice()); 
        newItem.setHistoricalPriceInPoints(product.getPriceInPoints());
        newItem.setSubtotal(product.getUnitPrice() * request.getQuantity());
        newItem.setSubtotalInPoints(product.getPriceInPoints() * request.getQuantity());

        cart.getItems().add(newItem);
        
        double total = cart.getItems().stream()
            .mapToDouble(item -> item.getQuantity() * item.getHistoricalPrice())
            .sum();
        cart.setTotalAmount(total);

        int totalPuntos = cart.getItems().stream()
            .mapToInt(item -> item.getQuantity() * item.getProduct().getPriceInPoints())
            .sum();
        cart.setTotalAmountInPoints(totalPuntos);


        StoreOrder savedCart = orderRepository.save(cart);
        return mapToDetailDTO(savedCart);
    }

    @Transactional(readOnly = true)
    public StoreOrderDetailDTO getActiveCart() {
        // 1. Obtenemos el usuario autenticado
        User user = userService.findAuthenticatedUser();
        
        // 2. Buscamos si tiene un carrito PENDING
        return orderRepository.findByUserAndStatus(user, StatusPayment.PENDING)
                .map(this::mapToDetailDTO) // Si existe, lo mapeamos a DTO
                .orElseGet(() -> {
                    // 3. Como es un RECORD, le pasamos los valores directamente.
                    // Lo más importante es pasar el 'new ArrayList<>()' en la posición de los items.
                    return new StoreOrderDetailDTO(
                            null,                   
                            null,                   
                            null,                   
                            null,                   
                            new ArrayList<>(),      
                            0.0,                    
                            0                     
                    );
                });
    }

    @Transactional(readOnly = true)
    public List<StoreOrderListDTO> getPurchaseHistory() {
        User user = userService.findAuthenticatedUser();
    
        List<StoreOrder> history = orderRepository.findAllByUserAndStatusOrderByCreatedAtDesc(user, StatusPayment.APPROVED);

        return history.stream()
            .map(this::mapToListDTO)
            .collect(Collectors.toList());
    }

    private StoreOrder createNewCart(User user) {
        StoreOrder order = new StoreOrder();
        order.setUser(user);
        order.setStatus(StatusPayment.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    @Transactional
    public StoreOrderDetailDTO removeItemFromCart(Long itemId) {
        // 1. Identificar al usuario conectado
        User user = userService.findAuthenticatedUser();
        
        // 2. Buscar su carrito activo
        StoreOrder cart = orderRepository.findByUserAndStatus(user, StatusPayment.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un carrito activo para este usuario"));

        // 3. Buscar el ítem DENTRO de ese carrito (Seguridad)
        OrderItems itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El ítem no pertenece a tu carrito de compras"));

        // 4. DEVOLVER EL STOCK al producto original
        Product product = itemToRemove.getProduct();
        product.setStock(product.getStock() + itemToRemove.getQuantity());
        // productRepository.save(product); // Opcional, JPA lo actualiza solo por el @Transactional

        // 5. Eliminar el ítem de la lista del carrito y de la base de datos
        cart.getItems().remove(itemToRemove);
        orderItemsRepository.delete(itemToRemove); 

        // 6. RECALCULAR los totales del carrito con los ítems que quedaron
        double nuevoTotal = cart.getItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getHistoricalPrice())
                .sum();
        cart.setTotalAmount(nuevoTotal);

        int nuevoTotalPuntos = cart.getItems().stream()
                .mapToInt(item -> item.getQuantity() * item.getProduct().getPriceInPoints())
                .sum();
        cart.setTotalAmountInPoints(nuevoTotalPuntos);

        // 7. Guardar el estado actualizado del carrito y retornar el DTO fresquito
        StoreOrder savedCart = orderRepository.save(cart);
        return mapToDetailDTO(savedCart);
    }

    private StoreOrderListDTO mapToListDTO(StoreOrder storeOrder) {
        return new StoreOrderListDTO(
                storeOrder.getId(),
                storeOrder.getCreatedAt().toLocalDate().toString(),
                storeOrder.getCreatedAt().toLocalTime().toString(),
                storeOrder.getTotalAmount(),
                storeOrder.getTotalAmountInPoints()
        );
    }

    // Este es el método que ya estás llamando desde getActiveCart y addProductToCart
    private StoreOrderDetailDTO mapToDetailDTO(StoreOrder order) {
    
        // 1. Acá pegás el bloque que me pasaste para transformar los items
        List<OrderItemsDetailDTO> itemDTOs = order.getItems() != null ? 
            order.getItems().stream()
                .map(item -> new OrderItemsDetailDTO(
                    item.getId(),
                    item.getProduct().getName(),
                    item.getProduct().getImageURL(),
                    item.getQuantity(),
                    item.getHistoricalPrice(),
                    item.getHistoricalPriceInPoints(),
                    item.getSubtotal(),
                    item.getSubtotalInPoints()
                ))
                .collect(Collectors.toList()) 
                : new ArrayList<>(); // Por si el carrito recién se crea y no tiene items

        // 2. Armás y retornás el DTO final del carrito, inyectando la lista que acabás de mapear
        return new StoreOrderDetailDTO(
            order.getId(),
            order.getStatus(),
            order.getCreatedAt().toString(), // Ajustá el .toString() si ya es String
            order.getCreatedAt().toString(),
            itemDTOs,                            // <-- Tu lista procesada entra acá
            order.getTotalAmount(),
            order.getTotalAmountInPoints()
        );
    }
}
