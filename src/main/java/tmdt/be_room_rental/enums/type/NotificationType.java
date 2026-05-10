package tmdt.be_room_rental.enums.type;

public enum NotificationType {
    BOOKING_CREATED,   // Tenant đặt phòng → Landlord nhận
    BOOKING_APPROVED,  // Landlord duyệt → Tenant nhận
    BOOKING_REJECTED,  // Landlord từ chối → Tenant nhận
    BOOKING_CANCELLED  // Tenant hủy → Landlord nhận
}
