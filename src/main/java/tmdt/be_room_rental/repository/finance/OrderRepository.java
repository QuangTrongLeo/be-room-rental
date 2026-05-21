package tmdt.be_room_rental.repository.finance;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.dto.res.finance.FinanceResponse;
import tmdt.be_room_rental.entity.Order;
import tmdt.be_room_rental.enums.status.OrderStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    Optional<Order> findByVnpTxnRef(String vnpTxnRef);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findAllByStatusOrderByCreatedAtDesc(OrderStatus status);
    List<Order> findAllByUserIdOrderByCreatedAtDesc(String userId);

    // Thống kê tài chính theo tháng
    @Aggregation(pipeline = {
            "{ '$match': { 'status': 'SUCCESS' } }",
            "{ '$group': { " +
                    "    '_id': { 'month': { '$month': '$createdAt' }, 'year': { '$year': '$createdAt' } }, " +
                    "    'revenue': { '$sum': '$totalPrice' } " +
                    "} }",
            "{ '$sort': { '_id.year': 1, '_id.month': 1 } }",
            "{ '$project': { " +
                    "    'label': { '$concat': [ 'Th. ', { '$toString': '$_id.month' }, '/', { '$toString': '$_id.year' } ] }, " +
                    "    'revenue': 1, " +
                    "    '_id': 0 " +
                    "} }"
    })
    List<FinanceResponse> getFinanceByMonth();

    // Thống kê tài chính theo Quý
    @Aggregation(pipeline = {
            "{ '$match': { 'status': 'SUCCESS' } }",
            "{ '$group': { " +
                    "    '_id': { " +
                    "       'quarter': { '$ceil': { '$divide': [ { '$month': '$createdAt' }, 3 ] } }, " +
                    "       'year': { '$year': '$createdAt' } " +
                    "    }, " +
                    "    'revenue': { '$sum': '$totalPrice' } " +
                    "} }",
            "{ '$sort': { '_id.year': 1, '_id.quarter': 1 } }",
            "{ '$project': { " +
                    "    'label': { '$concat': [ 'Quý ', { '$toString': '$_id.quarter' }, '/', { '$toString': '$_id.year' } ] }, " +
                    "    'revenue': 1, " +
                    "    '_id': 0 " +
                    "} }"
    })
    List<FinanceResponse> getFinanceByQuarter();

    // Thống kê tài chính theo Năm
    @Aggregation(pipeline = {
            "{ '$match': { 'status': 'SUCCESS' } }",
            "{ '$group': { " +
                    "    '_id': { 'year': { '$year': '$createdAt' } }, " +
                    "    'revenue': { '$sum': '$totalPrice' } " +
                    "} }",
            "{ '$sort': { '_id.year': 1 } }",
            "{ '$project': { " +
                    "    'label': { '$concat': [ 'Năm ', { '$toString': '$_id.year' } ] }, " +
                    "    'revenue': 1, " +
                    "    '_id': 0 " +
                    "} }"
    })
    List<FinanceResponse> getFinanceByYear();
}
