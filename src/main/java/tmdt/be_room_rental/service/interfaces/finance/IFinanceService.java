package tmdt.be_room_rental.service.interfaces.finance;

import tmdt.be_room_rental.dto.res.finance.FinanceResponse;

import java.util.List;

public interface IFinanceService {
    List<FinanceResponse> getFinanceStatsByMonth();
    List<FinanceResponse> getFinanceStatsByQuarter();
    List<FinanceResponse> getFinanceStatsByYear();
}
