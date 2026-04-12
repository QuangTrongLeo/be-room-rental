package tmdt.be_room_rental.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.ProviderType;
import tmdt.be_room_rental.enums.RoleEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "users")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @Builder.Default
    private String id = UUID.randomUUID().toString();
    @Indexed(unique = true)
    private String username;
    private String email;
    private String password;
    private String phone;
    private String avatar;
    private RoleEnum role;
    private boolean isVerified = false;
    private boolean isActive = true;
    private ProviderType provider;
    private LocalDateTime createdAt;
}