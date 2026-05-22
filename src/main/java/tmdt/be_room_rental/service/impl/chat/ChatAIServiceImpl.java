package tmdt.be_room_rental.service.impl.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import tmdt.be_room_rental.config.OpenAIConfig;
import tmdt.be_room_rental.dto.req.chat.AIChatRequest;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.chat.IChatAIService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatAIServiceImpl implements IChatAIService {

    private final RestClient aiRestClient;
    private final MongoTemplate mongoTemplate;
    private final SecurityService securityService;
    private final OpenAIConfig openAIConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, List<Map<String, String>>> chatHistoryCache = new ConcurrentHashMap<>();

    // Danh sách từ khóa chủ động để nhận diện thực thể Phòng ở / Nhà trọ / Bài đăng
    private static final List<String> ROOM_KEYWORDS = Arrays.asList(
            "phòng", "phong", "trọ", "tro", "nhà", "nha", "bài đăng", "bai dang", "căn hộ", "can ho", "chung cư", "chung cu"
    );

    @Override
    public Object chatAI(AIChatRequest request) {
        String question = request.getMessage();
        if (question == null || question.trim().isEmpty()) {
            return "Dạ, bạn đang để trống tin nhắn. Bạn cần mình trợ giúp gì không ạ?";
        }

        User currentUser = securityService.getCurrentUser();
        String cacheKey = (currentUser != null) ? currentUser.getId() : "anonymous_guest_session";

        List<Map<String, String>> history = chatHistoryCache.computeIfAbsent(cacheKey, k -> new ArrayList<>());
        if (history.size() > 4) {
            history.subList(0, history.size() - 4).clear();
        }

        // 🌟 CHỐT CHẶN 1: Xử lý hội thoại xã giao (Trả về văn bản văn xuôi thuần túy)
        if (isGeneralConversation(question)) {
            String quickSystemPrompt = "Bạn là Trợ lý Ảo thông minh của hệ thống Trọ Sinh Viên. " +
                    "Người dùng đang chào hỏi hoặc hỏi danh tính. Hãy trả lời ngắn gọn, lịch sự, lễ phép dưới 3 câu.";

            delayThread(1000);
            String quickResponse = callGeminiApi(quickSystemPrompt, question, history);

            String lowerQuick = quickResponse.toLowerCase();
            if (lowerQuick.contains("hạn ngạch") || lowerQuick.contains("gián đoạn") || lowerQuick.contains("cảnh báo")) {
                String fallbackGreeting = "Dạ, Trọ Sinh Viên xin chào bạn! Mình là Trợ lý ảo hỗ trợ tra cứu và tìm kiếm thông tin phòng trọ. Bạn cần mình giúp gì ạ?";
                history.add(Map.of("role", "user", "content", question));
                history.add(Map.of("role", "assistant", "content", fallbackGreeting));
                return fallbackGreeting;
            }

            history.add(Map.of("role", "user", "content", question));
            history.add(Map.of("role", "assistant", "content", quickResponse));
            return quickResponse;
        }

        // 🌟 CHỐT CHẶN 2: Nhận diện thực thể đích linh hoạt
        String targetCollection = "posts";
        String lowerQuestion = question.toLowerCase();

        boolean asksForRoom = ROOM_KEYWORDS.stream().anyMatch(lowerQuestion::contains);

        if (asksForRoom) {
            targetCollection = "posts";
            log.info("[Phân loại] Phát hiện từ khóa thuộc nhóm Phòng/Nhà trọ -> targetCollection = 'posts'");
        } else if (lowerQuestion.contains("tiện ích") || lowerQuestion.contains("tiện nghi") || lowerQuestion.contains("amenit")) {
            targetCollection = "amenities";
            log.info("[Phân loại] Phát hiện từ khóa thuộc nhóm Tiện ích -> targetCollection = 'amenities'");
        } else if (lowerQuestion.contains("gói") || lowerQuestion.contains("package")) {
            targetCollection = "packages";
            log.info("[Phân loại] Phát hiện từ khóa thuộc nhóm Gói dịch vụ -> targetCollection = 'packages'");
        } else if (lowerQuestion.contains("voucher") || lowerQuestion.contains("khuyến mãi") || lowerQuestion.contains("giảm giá")) {
            targetCollection = "vouchers";
            log.info("[Phân loại] Phát hiện từ khóa thuộc nhóm Voucher -> targetCollection = 'vouchers'");
        }

        // 3. Gọi AI dịch câu hỏi tra cứu sang cấu trúc JSON MongoDB
        delayThread(1200);
        String aiResponse = callGeminiApi(AIChatConstant.MONGO_SCHEMA, question, history);

        // Kích hoạt luồng cứu hộ cục bộ khi quá tải hạn ngạch API (Lỗi 429)
        String lowerResponse = aiResponse.toLowerCase();
        if (lowerResponse.contains("hạn ngạch") || lowerResponse.contains("gián đoạn") || lowerResponse.contains("cảnh báo")) {
            log.warn("Mô hình phản hồi trạng thái bận hoặc hết hạn ngạch. Kích hoạt bộ cứu hộ cục bộ.");
            return executeLocalFallback(targetCollection, question, history);
        }

        // 4. Thực thi câu lệnh JSON thành công từ AI
        if (aiResponse != null && aiResponse.trim().startsWith("{") && aiResponse.trim().endsWith("}")) {
            try {
                String cleanJson = aiResponse.trim();
                log.info("Lệnh truy vấn MongoDB nhận diện từ AI: {}", cleanJson);

                BasicQuery query = new BasicQuery(cleanJson);
                query.limit(4);

                List<Document> databaseResults = mongoTemplate.find(query, Document.class, targetCollection);

                // Tự động nới lỏng bộ lọc dùng trạng thái ACTIVE cho phòng trọ nếu rỗng kết quả
                if (databaseResults.isEmpty()) {
                    log.warn("Kết quả truy vấn theo tiêu chí bị rỗng trong DB. Tiến hành tự động nới lỏng điều kiện.");
                    BasicQuery relaxationQuery = "posts".equals(targetCollection)
                            ? new BasicQuery("{ 'status': 'ACTIVE' }")
                            : new BasicQuery("{}");
                    relaxationQuery.limit(4);
                    databaseResults = mongoTemplate.find(relaxationQuery, Document.class, targetCollection);
                }

                return generateLocalAnswer(targetCollection, databaseResults, history, question);

            } catch (Exception e) {
                log.error("Lỗi thực thi truy vấn tự động: ", e);
                return "Hệ thống gặp trục trặc khi truy xuất dữ liệu: " + e.getMessage();
            }
        }

        history.add(Map.of("role", "user", "content", question));
        history.add(Map.of("role", "assistant", "content", aiResponse));
        return aiResponse;
    }

    private Object executeLocalFallback(String collection, String question, List<Map<String, String>> history) {
        BasicQuery fallbackQuery = new BasicQuery("{}");

        if ("posts".equals(collection)) {
            double extractedPrice = extractPriceFromString(question);
            if (extractedPrice > 0) {
                double minPrice = Math.max(0, extractedPrice - 500000);
                double maxPrice = extractedPrice + 500000;
                fallbackQuery = new BasicQuery(String.format("{ 'price': { '$gte': %.0f, '$lte': %.0f }, 'status': 'ACTIVE' }", minPrice, maxPrice));
                log.info("Cứu hộ cục bộ thiết lập bộ lọc khoảng giá: {} - {}", minPrice, maxPrice);
            } else {
                fallbackQuery = new BasicQuery("{ 'status': 'ACTIVE' }");
            }
        } else if ("vouchers".equals(collection)) {
            fallbackQuery = new BasicQuery("{ 'isActive': true }");
        }

        fallbackQuery.limit(4);
        List<Document> databaseResults = mongoTemplate.find(fallbackQuery, Document.class, collection);

        if (databaseResults.isEmpty() && "posts".equals(collection)) {
            log.warn("Khung giá cứu hộ không tìm thấy kết quả phù hợp. Nới lỏng lấy toàn bộ danh sách phòng ACTIVE.");
            fallbackQuery = new BasicQuery("{ 'status': 'ACTIVE' }");
            fallbackQuery.limit(4);
            databaseResults = mongoTemplate.find(fallbackQuery, Document.class, collection);
        }

        return generateLocalAnswer(collection, databaseResults, history, question);
    }

    /**
     * NÂNG CẤP XỬ LÝ: Sinh cấu trúc hỗn hợp gồm Text dẫn dắt + List Object đối với phòng ở
     */
    @SuppressWarnings("unchecked")
    private Object generateLocalAnswer(String collection, List<Document> data, List<Map<String, String>> history, String question) {
        if (data == null || data.isEmpty()) {
            return "Dạ hiện tại hệ thống Trọ Sinh Viên chưa tìm thấy dữ liệu phù hợp với yêu cầu tra cứu của bạn. Bạn vui lòng thử lại với tiêu chí khác nhé.";
        }

        // 🌟 XỬ LÝ CHO BÀI ĐĂNG PHÒNG TRỌ: Trả về Object phức hợp gồm text và mảng các phòng
        if ("posts".equals(collection)) {
            List<Map<String, Object>> customPostList = new ArrayList<>();

            for (Document doc : data) {
                Map<String, Object> customPost = new LinkedHashMap<>();

                // Trích xuất id bản ghi an toàn
                String idStr = doc.get("_id") != null ? doc.get("_id").toString() : doc.getString("id");
                customPost.put("id", idStr);
                customPost.put("title", doc.getString("title"));

                // Trích xuất số thực qua trung gian lớp Number để trị dứt điểm ClassCastException
                Number priceNum = doc.get("price", Number.class);
                customPost.put("price", priceNum != null ? priceNum.doubleValue() : 0.0);

                Number areaNum = doc.get("area", Number.class);
                customPost.put("area", areaNum != null ? areaNum.doubleValue() : 0.0);

                customPost.put("address", doc.getString("address"));
                customPost.put("images", doc.get("images", List.class) != null ? doc.get("images", List.class) : new ArrayList<String>());

                customPostList.add(customPost);
            }

            String text = "Dạ, đây là một số phòng ở theo yêu cầu của bạn, bạn bấm vào để xem chi tiết nhé:";

            // Ghi nhận ngữ cảnh vào cache dưới dạng text ngắn gọn
            history.add(Map.of("role", "user", "content", question));
            history.add(Map.of("role", "assistant", "content", text));

            // Xây dựng JSON Payload phức hợp
            Map<String, Object> finalResultPayload = new LinkedHashMap<>();
            finalResultPayload.put("text", text);
            finalResultPayload.put("rooms", customPostList);

            return finalResultPayload;
        }

        // 🌟 XỬ LÝ CÁC LOẠI KHÁC (VOUCHER, AMENITIES...): Vẫn sinh dạng chuỗi văn bản thông dụng
        StringBuilder sb = new StringBuilder("Dạ, Trọ Sinh Viên xin gửi đến bạn thông tin thực tế được truy xuất trực tiếp từ hệ thống dữ liệu:\n\n");

        for (Document doc : data) {
            if ("amenities".equals(collection)) {
                sb.append("- Tiện nghi hệ thống: ").append(doc.getString("name")).append("\n");
            } else if ("packages".equals(collection)) {
                sb.append("- Gói dịch vụ: ").append(doc.getString("name"))
                        .append(" (Loại gói: ").append(doc.getString("type"))
                        .append(", Cấp độ: ").append(doc.getString("tier"))
                        .append(") - Giá niêm yết: ").append(String.format("%,.0f VNĐ", doc.getDouble("price"))).append("\n");
            } else if ("vouchers".equals(collection)) {
                sb.append("- Mã giảm giá chiết khấu: ").append(doc.get("discountPercentage")).append("%")
                        .append(" (Giảm tối đa: ").append(String.format("%,.0f VNĐ", doc.getDouble("maxDiscountAmount"))).append(")\n");
            } else {
                sb.append("- ").append(doc.toJson()).append("\n");
            }
        }
        sb.append("\n*(Thông tin được trích xuất tự động từ hệ thống quản trị nội bộ)*");

        String localAns = sb.toString();
        history.add(Map.of("role", "user", "content", question));
        history.add(Map.of("role", "assistant", "content", localAns));
        return localAns;
    }

    private String callGeminiApi(String systemPrompt, String userMessage, List<Map<String, String>> history) {
        try {
            List<Map<String, Object>> messagesList = new ArrayList<>();
            messagesList.add(Map.of("role", "system", "content", systemPrompt));

            for (Map<String, String> msg : history) {
                messagesList.add(Map.of("role", msg.get("role"), "content", msg.get("content")));
            }
            messagesList.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> requestBody = Map.of(
                    "model", openAIConfig.getModelName(),
                    "messages", messagesList,
                    "temperature", 0.2
            );

            String responseStr = aiRestClient.post()
                    .uri(openAIConfig.getCompletionsPath())
                    .header("Authorization", "Bearer " + openAIConfig.getApiKey())
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseStr);
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (HttpStatusCodeException e) {
            log.error("Lỗi kết nối từ API Gateway (Mã {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 429 || e.getStatusCode().value() == 503) {
                return "CẢNH BÁO: Hạn ngạch tài khoản AI thử nghiệm đã hết.";
            }
            return "Yêu cầu kết nối trí tuệ nhân tạo bị gián đoạn: " + e.getStatusText();
        } catch (Exception e) {
            log.error("Lỗi xử lý hệ thống: ", e);
            return "Không thể kết nối với trí tuệ nhân tạo AI bị gián đoạn: " + e.getMessage();
        }
    }

    private double extractPriceFromString(String text) {
        if (text == null || text.isEmpty()) return 0;

        String clean = text.toLowerCase()
                .replaceAll("[?.!,¿¡]", "")
                .replaceAll("\\s+", "");

        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)(triệu|triêu|tr)");
        Matcher matcher = pattern.matcher(clean);
        if (matcher.find()) {
            try {
                double value = Double.parseDouble(matcher.group(1));
                return value * 1000000;
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    private boolean isGeneralConversation(String text) {
        if (text == null) return false;
        String cleanText = text.toLowerCase().replaceAll("[?.!,¿¡]", "").trim();
        return cleanText.contains("bạn là ai") || cleanText.contains("ai đây") ||
                cleanText.contains("hello") || cleanText.contains("xin chào") ||
                cleanText.contains("chào bạn") || cleanText.contains("cảm ơn") ||
                cleanText.contains("thank") || cleanText.equals("chào") ||
                cleanText.contains("tên là gì") || cleanText.contains("mày là ai");
    }

    private void delayThread(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}