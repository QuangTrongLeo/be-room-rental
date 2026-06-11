package tmdt.be_room_rental.dto.res.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceResponse {
    private String label;      // Ví dụ: "Th. 5/2026", "Quý 2/2026", "Năm 2026"
    private Double revenue;    // Tổng doanh thu tính từ các đơn hàng SUCCESS
}