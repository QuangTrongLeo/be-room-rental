package tmdt.be_room_rental.service.impl.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.config.VNPayConfig;
import tmdt.be_room_rental.dto.req.finance.OrderRequest;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;
import tmdt.be_room_rental.dto.res.finance.OrderResponse;
import tmdt.be_room_rental.entity.Order;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.status.OrderStatus;
import tmdt.be_room_rental.mapper.enums.OrderEnumMapper;
import tmdt.be_room_rental.mapper.finance.OrderMapper;
import tmdt.be_room_rental.repository.finance.OrderRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.finance.IOrderService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final SecurityService securityService;
    private final OrderEnumMapper orderEnumMapper;
    private final OrderMapper orderMapper;
    private final VNPayConfig vnPayConfig;

    @Override
    public List<EnumResponse> getOrdersStatus() {
        return orderEnumMapper.toStatusResponseList();
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        User user = securityService.getCurrentUser();
        Order order = buildOrder(user, request);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse updateOrder(String id, OrderRequest request) {
        Order order = findOrderById(id);
        if (request.getPackageId() != null) order.setPackageId(request.getPackageId());
        if (request.getVoucherId() != null) order.setVoucherId(request.getVoucherId());
        if (request.getTotalPrice() != null) order.setTotalPrice(request.getTotalPrice());
        if (request.getStatus() != null) order.setStatus(request.getStatus());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse getOrderById(String id) {
        return orderMapper.toResponse(findOrderById(id));
    }

    @Override
    public List<OrderResponse> getOrders() {
        return orderMapper.toResponseList(orderRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public List<OrderResponse> getPendingOrders() {
        return orderMapper.toResponseList(orderRepository.findAllByStatusOrderByCreatedAtDesc(OrderStatus.PENDING));
    }

    @Override
    public List<OrderResponse> getSuccessOrders() {
        return orderMapper.toResponseList(orderRepository.findAllByStatusOrderByCreatedAtDesc(OrderStatus.SUCCESS));
    }

    @Override
    public List<OrderResponse> getFailedOrders() {
        return orderMapper.toResponseList(orderRepository.findAllByStatusOrderByCreatedAtDesc(OrderStatus.FAILED));
    }

    @Override
    public List<OrderResponse> getExpiredOrders() {
        return orderMapper.toResponseList(orderRepository.findAllByStatusOrderByCreatedAtDesc(OrderStatus.EXPIRED));
    }

    @Override
    public List<OrderResponse> getMyOrders() {
        User user = securityService.getCurrentUser();
        List<Order> orders = orderRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
        return orderMapper.toResponseList(orders);
    }

    @Override
    public void deleteOrder(String id) {
        Order order = findOrderById(id);
        orderRepository.delete(order);
    }

    public Order findOrderById(String id){
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại với ID: " + id));
    }

    private Order buildOrder(User user, OrderRequest request) {
        String vnpTxnRef = vnPayConfig.getRandomNumber(10);
        return Order.builder()
                .userId(user.getId())
                .packageId(request.getPackageId())
                .voucherId(request.getVoucherId() != null ? request.getVoucherId() : null)
                .vnpTxnRef(vnpTxnRef)
                .totalPrice(request.getTotalPrice())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
