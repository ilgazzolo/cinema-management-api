package com.api.boleteria.service;

import com.api.boleteria.dto.dashboard.AdminDashboardDTO;
import com.api.boleteria.dto.dashboard.AdminDashboardSummaryDTO;
import com.api.boleteria.dto.dashboard.ProductSalesDTO;
import com.api.boleteria.dto.dashboard.TicketSalesDTO;
import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.model.OrderItems;
import com.api.boleteria.model.StoreOrder;
import com.api.boleteria.model.Ticket;
import com.api.boleteria.model.enums.StatusPayment;
import com.api.boleteria.repository.IStoreOrderRepository;
import com.api.boleteria.repository.ITicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final ITicketRepository ticketRepository;
    private final IStoreOrderRepository storeOrderRepository;

    public AdminDashboardDTO getDashboard(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = from != null ? from : today.withDayOfMonth(1);
        LocalDate endDate = to != null ? to : today;

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("La fecha hasta no puede ser anterior a la fecha desde.");
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Ticket> tickets = ticketRepository.findByPurchaseDateTimeBetween(start, end);
        List<StoreOrder> storeOrders = storeOrderRepository.findByStatusAndCreatedAtBetween(
                StatusPayment.APPROVED, start, end);

        List<TicketSalesDTO> ticketSales = buildTicketSales(tickets);
        List<ProductSalesDTO> productSales = buildProductSales(storeOrders);

        int ticketsSold = tickets.stream()
                .mapToInt(ticket -> ticket.getQuantity() != null ? ticket.getQuantity() : 0)
                .sum();

        BigDecimal ticketRevenue = tickets.stream()
                .map(ticket -> ticket.getTotalAmount() != null ? ticket.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int productsSold = productSales.stream()
                .mapToInt(ProductSalesDTO::quantitySold)
                .sum();

        BigDecimal storeRevenue = productSales.stream()
                .map(ProductSalesDTO::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal productCost = productSales.stream()
                .map(ProductSalesDTO::cost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = ticketRevenue.add(storeRevenue).subtract(productCost);

        AdminDashboardSummaryDTO summary = new AdminDashboardSummaryDTO(
                ticketsSold,
                ticketRevenue,
                productsSold,
                storeRevenue,
                productCost,
                netProfit
        );

        return new AdminDashboardDTO(startDate, endDate, summary, ticketSales, productSales);
    }

    private List<TicketSalesDTO> buildTicketSales(List<Ticket> tickets) {
        Map<Long, TicketAccumulator> sales = new LinkedHashMap<>();

        for (Ticket ticket : tickets) {
            Long movieId = ticket.getFunction().getMovie().getId();
            String movieName = ticket.getFunction().getMovieName();
            TicketAccumulator accumulator = sales.computeIfAbsent(
                    movieId,
                    id -> new TicketAccumulator(id, movieName)
            );

            accumulator.quantity += ticket.getQuantity() != null ? ticket.getQuantity() : 0;
            accumulator.revenue = accumulator.revenue.add(
                    ticket.getTotalAmount() != null ? ticket.getTotalAmount() : BigDecimal.ZERO
            );
        }

        return sales.values().stream()
                .map(acc -> new TicketSalesDTO(acc.movieId, acc.movieName, acc.quantity, acc.revenue))
                .sorted(Comparator.comparing(TicketSalesDTO::revenue).reversed())
                .toList();
    }

    private List<ProductSalesDTO> buildProductSales(List<StoreOrder> storeOrders) {
        Map<Long, ProductAccumulator> sales = new LinkedHashMap<>();

        for (StoreOrder order : storeOrders) {
            if (order.getItems() == null) continue;

            for (OrderItems item : order.getItems()) {
                if (item.getProduct() == null) continue;

                Long productId = item.getProduct().getId();
                String productName = item.getProduct().getName();
                ProductAccumulator accumulator = sales.computeIfAbsent(
                        productId,
                        id -> new ProductAccumulator(id, productName)
                );

                int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                BigDecimal revenue = item.getSubtotal() != null
                        ? BigDecimal.valueOf(item.getSubtotal())
                        : BigDecimal.valueOf(item.getHistoricalPrice() != null ? item.getHistoricalPrice() : 0.0)
                                .multiply(BigDecimal.valueOf(quantity));

                Double unitCostValue = item.getHistoricalUnitCost() != null
                        ? item.getHistoricalUnitCost()
                        : item.getProduct().getUnitCost();
                BigDecimal cost = BigDecimal.valueOf(unitCostValue != null ? unitCostValue : 0.0)
                        .multiply(BigDecimal.valueOf(quantity));

                accumulator.quantity += quantity;
                accumulator.revenue = accumulator.revenue.add(revenue);
                accumulator.cost = accumulator.cost.add(cost);
            }
        }

        return sales.values().stream()
                .map(acc -> new ProductSalesDTO(
                        acc.productId,
                        acc.productName,
                        acc.quantity,
                        acc.revenue,
                        acc.cost,
                        acc.revenue.subtract(acc.cost)
                ))
                .sorted(Comparator.comparing(ProductSalesDTO::profit).reversed())
                .toList();
    }

    private static class TicketAccumulator {
        private final Long movieId;
        private final String movieName;
        private int quantity;
        private BigDecimal revenue = BigDecimal.ZERO;

        private TicketAccumulator(Long movieId, String movieName) {
            this.movieId = movieId;
            this.movieName = movieName;
        }
    }

    private static class ProductAccumulator {
        private final Long productId;
        private final String productName;
        private int quantity;
        private BigDecimal revenue = BigDecimal.ZERO;
        private BigDecimal cost = BigDecimal.ZERO;

        private ProductAccumulator(Long productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }
    }
}
