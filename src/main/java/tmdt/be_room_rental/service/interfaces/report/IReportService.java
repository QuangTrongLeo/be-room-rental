package tmdt.be_room_rental.service.interfaces.report;

import tmdt.be_room_rental.dto.req.report.ReportRequest;
import tmdt.be_room_rental.dto.res.report.ReportResponse;

import java.util.List;

public interface IReportService {
    ReportResponse createReport(ReportRequest request);

    List<ReportResponse> getMyReports();

    List<ReportResponse> getAllReports();

    ReportResponse resolveReport(String id);
}
