package tmdt.be_room_rental.dto.req.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportRequest {

    @NotBlank(message = "Người bị báo cáo không được để trống")
    private String targetId;

    @NotBlank(message = "Lý do báo cáo không được để trống")
    @Size(min = 10, max = 1000, message = "Lý do báo cáo phải có từ 10 đến 1000 ký tự")
    private String reason;
}
