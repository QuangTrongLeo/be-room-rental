package tmdt.be_room_rental.dto.req.post;

import lombok.Data;

@Data
public class ReviewRequest {
    private String landlordId;
    private int rating;
    private String comment;
}
