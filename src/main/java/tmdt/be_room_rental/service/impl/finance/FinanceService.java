package tmdt.be_room_rental.service.impl.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.res.finance.FinanceResponse;
import tmdt.be_room_rental.repository.finance.OrderRepository;
import tmdt.be_room_rental.service.interfaces.finance.IFinanceService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceService implements IFinanceService {

    private final OrderRepository orderRepository;

    @Override
    public List<FinanceResponse> getFinanceStatsByMonth() {
        return orderRepository.getFinanceByMonth();
    }

    @Override
    public List<FinanceResponse> getFinanceStatsByQuarter() {
        return orderRepository.getFinanceByQuarter();
    }

    @Override
    public List<FinanceResponse> getFinanceStatsByYear() {
        return orderRepository.getFinanceByYear();
    }
}