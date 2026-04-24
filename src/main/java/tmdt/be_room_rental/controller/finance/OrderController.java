package tmdt.be_room_rental.controller.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.finance.OrderRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;
import tmdt.be_room_rental.dto.res.finance.OrderResponse;
import tmdt.be_room_rental.service.interfaces.finance.IOrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('LANDLORD')")
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Khởi tạo đơn hàng thành công.")
                .data(orderService.createOrder(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponse> updateOrder(
            @PathVariable String id,
            @RequestBody OrderRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Cập nhật thông tin đơn hàng thành công.")
                .data(orderService.updateOrder(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đã xóa đơn hàng thành công.")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<OrderResponse>> getOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .code(200)
                .data(orderService.getOrders())
                .build();
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('LANDLORD')")
    public ApiResponse<List<OrderResponse>> getMyOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .code(200)
                .data(orderService.getMyOrders())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<OrderResponse> getOrder(@PathVariable String id) {
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .data(orderService.getOrderById(id))
                .build();
    }

    @GetMapping("/status")
    public ApiResponse<List<EnumResponse>> getOrderStatuses() {
        return ApiResponse.<List<EnumResponse>>builder()
                .code(200)
                .data(orderService.getOrdersStatus())
                .build();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<OrderResponse>> getPendingOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .code(200)
                .data(orderService.getPendingOrders())
                .build();
    }

    @GetMapping("/success")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<OrderResponse>> getSuccessOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .code(200)
                .data(orderService.getSuccessOrders())
                .build();
    }

    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<OrderResponse>> getFailedOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .code(200)
                .data(orderService.getFailedOrders())
                .build();
    }

    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<OrderResponse>> getExpiredOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .code(200)
                .data(orderService.getExpiredOrders())
                .build();
    }
}