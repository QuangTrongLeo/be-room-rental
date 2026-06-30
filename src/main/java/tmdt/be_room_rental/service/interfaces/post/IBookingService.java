package tmdt.be_room_rental.service.interfaces.post;

import tmdt.be_room_rental.dto.req.post.BookingRequest;
import tmdt.be_room_rental.dto.res.post.BookingResponse;

import java.util.List;

public interface IBookingService {

    // Tenant
    BookingResponse createBooking(BookingRequest request);
    List<BookingResponse> getMyBookings();
    BookingResponse cancelBooking(String bookingId);
    BookingResponse confirmRented(String bookingId);

    // Landlord
    List<BookingResponse> getBookingsForLandlord();
    BookingResponse approveBooking(String bookingId);
    BookingResponse rejectBooking(String bookingId);

    List<BookingResponse> getBookings();

    // Chung
    BookingResponse getBookingById(String bookingId);
}
