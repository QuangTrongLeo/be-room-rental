package tmdt.be_room_rental.mapper.finance;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.finance.OrderResponse;
import tmdt.be_room_rental.entity.Order;

import java.util.List;

@Component
public class OrderMapper {
    public OrderResponse toResponse(Order order) {
        if (order == null) return null;

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .packageId(order.getPackageId())
                .voucherId(order.getVoucherId())
                .vnpTxnRef(order.getVnpTxnRef())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public List<OrderResponse> toResponseList(List<Order> orders) {
        if (orders == null) return List.of();
        return orders.stream()
                .map(this::toResponse)
                .toList();
    }
}