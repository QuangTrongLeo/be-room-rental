package tmdt.be_room_rental.service.impl.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmdt.be_room_rental.config.VNPayConfig;
import tmdt.be_room_rental.dto.req.finance.OrderRequest;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;
import tmdt.be_room_rental.dto.res.finance.OrderResponse;
import tmdt.be_room_rental.entity.Order;
import tmdt.be_room_rental.entity.Packages;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.status.OrderStatus;
import tmdt.be_room_rental.enums.type.PackageType;
import tmdt.be_room_rental.mapper.enums.OrderEnumMapper;
import tmdt.be_room_rental.mapper.finance.OrderMapper;
import tmdt.be_room_rental.repository.auth.UserRepository;
import tmdt.be_room_rental.repository.finance.OrderRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.impl.auth.UserService;
import tmdt.be_room_rental.service.interfaces.finance.IOrderService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PackageService packageService;
    private final SecurityService securityService;
    private final OrderEnumMapper orderEnumMapper;
    private final OrderMapper orderMapper;
    private final VNPayConfig vnPayConfig;
    private final TaskScheduler taskScheduler;
    private static final int ORDER_EXPIRY_MINUTES = 10;

    @Override
    public List<EnumResponse> getOrdersStatus() {
        return orderEnumMapper.toStatusResponseList();
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        User user = securityService.getCurrentUser();
        Order order = buildOrder(user, request);
        Order savedOrder = orderRepository.save(order);

        // Lập lịch kiểm tra sau 10 phút
        taskScheduler.schedule(() -> {
            handleOrderTimeout(savedOrder.getId());
        }, java.sql.Timestamp.valueOf(LocalDateTime.now().plusMinutes(ORDER_EXPIRY_MINUTES)).toInstant());

        return orderMapper.toResponse(savedOrder);
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

    @Transactional
    public void successOrder(String vnpTxnRef) {
        Order order = findByVnpTxnRef(vnpTxnRef);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.SUCCESS);
            orderRepository.save(order);

            Packages pkg = packageService.findPackageById(order.getPackageId());
            User user = userService.findUserById(order.getUserId());

            // Đảm bảo không bị cộng dồn với giá trị null
            int limit = (pkg.getLimitQuota() != null) ? pkg.getLimitQuota() : 0;

            if (PackageType.POSTING.equals(pkg.getType())) {
                int current = (user.getPostQuota() != null) ? user.getPostQuota() : 0;
                user.setPostQuota(current + limit);
            } else if (PackageType.BOOSTING.equals(pkg.getType())) {
                int current = (user.getBoostQuota() != null) ? user.getBoostQuota() : 0;
                user.setBoostQuota(current + limit);
            }

            userRepository.save(user);
        }
    }

    public void failOrder(String vnpTxnRef) {
        Order order = findByVnpTxnRef(vnpTxnRef);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
        }
    }

    public Order findOrderById(String id){
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại với ID: " + id));
    }

    public Order findOrderOfLandlord(String landlordId, OrderStatus status){
        return orderRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(landlordId, status)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói dịch vụ hợp lệ bài đăng."));
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

    private Order findByVnpTxnRef(String vnpTxnRef) {
        return orderRepository.findByVnpTxnRef(vnpTxnRef)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã giao dịch: " + vnpTxnRef));
    }

    private void handleOrderTimeout(String orderId) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);

            // Nếu sau 10 phút đơn hàng vẫn là PENDING
            if (order != null && order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.FAILED);
                orderRepository.save(order);
                System.out.println("LOG: Đơn hàng " + orderId + " đã tự động chuyển thành FAILED do quá hạn 10 phút.");
            }
        } catch (Exception e) {
            System.err.println("LOG: Lỗi khi xử lý timeout đơn hàng: " + e.getMessage());
        }
    }
}
