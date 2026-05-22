package tmdt.be_room_rental.service.interfaces.chat;

import tmdt.be_room_rental.dto.req.chat.AIChatRequest;

public interface IChatAIService {
    Object chatAI(AIChatRequest request);
}
