package tmdt.be_room_rental.mapper.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.post.BookingResponse;
import tmdt.be_room_rental.entity.Booking;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.repository.auth.UserRepository;
import tmdt.be_room_rental.repository.post.PostRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) return null;

        Post post = postRepository.findById(booking.getPostId()).orElse(null);

        // Lấy thông tin tenant để landlord thấy tên người đặt
        User tenant = userRepository.findById(booking.getUserId()).orElse(null);

        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .landlordId(booking.getLandlordId())
                .postId(booking.getPostId())
                .post(post != null ? postMapper.toResponse(post) : null)
                .tenantName(tenant != null ? tenant.getUsername() : null)
                .tenantAvatar(tenant != null ? tenant.getAvatar() : null)
                .bookingTime(booking.getBookingTime())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    public List<BookingResponse> toResponseList(List<Booking> bookings) {
        if (bookings == null) return List.of();
        return bookings.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
