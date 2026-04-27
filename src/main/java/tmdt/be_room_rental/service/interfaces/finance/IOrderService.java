package tmdt.be_room_rental.service.interfaces.finance;

import tmdt.be_room_rental.dto.req.finance.OrderRequest;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;
import tmdt.be_room_rental.dto.res.finance.OrderResponse;

import java.util.List;

public interface IOrderService {
    List<EnumResponse> getOrdersStatus();

    OrderResponse createOrder(OrderRequest orderRequest);
    OrderResponse updateOrder(String id, OrderRequest orderRequest);
    OrderResponse getOrderById(String id);
    List<OrderResponse> getOrders();
    List<OrderResponse> getPendingOrders();
    List<OrderResponse> getSuccessOrders();
    List<OrderResponse> getFailedOrders();
    List<OrderResponse> getMyOrders();
    void deleteOrder(String id);
}
