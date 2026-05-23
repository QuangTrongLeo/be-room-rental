package tmdt.be_room_rental.service.impl.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
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

    private static final String TARGET_COLLECTION = "posts";

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

        // 🌟 KIỂM SOÁT ĐỒNG NHẤT LOGIC SỐ LƯỢNG: Áp dụng chung cho cả AI và Java Fallback
        int queryLimit = determineLimitCount(question);

        // 🌟 CHỐT CHẶN 1: Hội thoại xã giao
        if (isGeneralConversation(question)) {
            String quickSystemPrompt = "Bạn là Trợ lý Ảo thông minh của hệ thống Trọ Sinh Viên. Trả lời ngắn gọn, lịch sự, dưới 3 câu.";
            delayThread(1000);
            String quickResponse = callGeminiApi(quickSystemPrompt, question, history);

            String lowerQuick = quickResponse.toLowerCase();
            if (lowerQuick.contains("hạn ngạch") || lowerQuick.contains("gián đoạn") || lowerQuick.contains("cảnh báo")) {
                String fallbackGreeting = "Dạ, Trọ Sinh Viên xin chào bạn! Mình là Trợ lý ảo hỗ trợ tìm kiếm phòng trọ. Bạn cần mình giúp gì ạ?";
                history.add(Map.of("role", "user", "content", question));
                history.add(Map.of("role", "assistant", "content", fallbackGreeting));
                return fallbackGreeting;
            }

            history.add(Map.of("role", "user", "content", question));
            history.add(Map.of("role", "assistant", "content", quickResponse));
            return quickResponse;
        }

        // 🌟 CHỐT CHẶN 2: Gọi AI dịch câu hỏi tra cứu sang cấu trúc số thực Double
        delayThread(1200);
        String aiResponse = callGeminiApi(AIChatConstant.MONGO_SCHEMA, question, history);

        String lowerResponse = aiResponse.toLowerCase();
        if (lowerResponse.contains("hạn ngạch") || lowerResponse.contains("gián đoạn") || lowerResponse.contains("cảnh báo")) {
            log.warn("Mô hình bận hoặc hết hạn ngạch. Kích hoạt cứu hộ nâng cao.");
            return executeLocalFallback(question, history, queryLimit);
        }

        // 🌟 CHỐT CHẶN 3: Thực thi câu lệnh JSON từ AI
        if (aiResponse != null && aiResponse.trim().startsWith("{") && aiResponse.trim().endsWith("}")) {
            try {
                String cleanJson = aiResponse.trim();
                log.info("Lệnh truy vấn MongoDB nhận diện từ AI: {}", cleanJson);

                BasicQuery query = new BasicQuery(cleanJson);
                query.limit(queryLimit); // Đồng bộ số lượng giới hạn tại đây
                query.with(Sort.by(Sort.Direction.ASC, "price"));

                List<Document> databaseResults = mongoTemplate.find(query, Document.class, TARGET_COLLECTION);
                log.info("Số lượng bài đăng tìm thấy từ AI Query: {}", databaseResults.size());

                if (databaseResults.isEmpty()) {
                    log.warn("Kết quả từ AI bị rỗng, chuyển giao bộ cứu hộ Java xử lý.");
                    return executeLocalFallback(question, history, queryLimit);
                }

                return generateLocalAnswer(databaseResults, history, question);

            } catch (Exception e) {
                log.error("Lỗi thực thi truy vấn tự động: ", e);
                return executeLocalFallback(question, history, queryLimit);
            }
        }

        return executeLocalFallback(question, history, queryLimit);
    }

    /**
     * Bộ cứu hộ nâng cao bằng Java Regex - Tự động bóc tách ngôn ngữ nói thô thành số thực Double cho MongoDB
     */
    private Object executeLocalFallback(String question, List<Map<String, String>> history, int queryLimit) {
        log.info("[Java Tự Khợp] Đang phân tích chuỗi ngôn ngữ thô: {}", question);

        // Làm sạch chuỗi: chuyển chữ thường, bỏ dấu và khoảng trắng để dễ Match Regex viết tắt
        String cleanQuestion = question.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("\\s+", "");

        BasicQuery fallbackQuery = null;

        // ======================== PHẦN 1: TỰ ĐỘNG KHỚP DIỆN TÍCH (AREA) ========================
        if (cleanQuestion.contains("m2") || cleanQuestion.contains("metvuong")) {

            // 1.1: Diện tích khoảng / tầm (Tự động tính toán biên độ danh sách tập hợp X-5 đến X+5 dạng Double)
            Pattern areaApproxPattern = Pattern.compile("(?:khoang|tam)(\\d+(?:\\.\\d+)?)(?:m2|metvuong)");
            Matcher areaApproxMatcher = areaApproxPattern.matcher(cleanQuestion);
            if (areaApproxMatcher.find()) {
                double targetArea = Double.parseDouble(areaApproxMatcher.group(1));
                String json = String.format(Locale.US, "{ 'area': { '$gte': %.1f, '$lte': %.1f }, 'status': 'ACTIVE' }", targetArea - 5.0, targetArea + 5.0);
                fallbackQuery = new BasicQuery(json);
                log.info("[Cứu Hộ Area] Tự khớp biên độ khoảng danh sách: {}", json);
            }

            // 1.2: Diện tích TRÊN / RỘNG HƠN / LỚN HƠN
            if (fallbackQuery == null) {
                Pattern areaUpperPattern = Pattern.compile("(?:tren|ronghon|lonhon)(\\d+(?:\\.\\d+)?)(?:m2|metvuong)");
                Matcher areaUpperMatcher = areaUpperPattern.matcher(cleanQuestion);
                if (areaUpperMatcher.find()) {
                    double targetArea = Double.parseDouble(areaUpperMatcher.group(1));
                    String json = String.format(Locale.US, "{ 'area': { '$gte': %.1f }, 'status': 'ACTIVE' }", targetArea);
                    fallbackQuery = new BasicQuery(json);
                    log.info("[Cứu Hộ Area] Tự khớp điều kiện danh sách TRÊN: {}", json);
                }
            }

            // 1.3: Diện tích DƯỚI / NHỎ HƠN
            if (fallbackQuery == null) {
                Pattern areaLowerPattern = Pattern.compile("(?:duoi|nhohon)(\\d+(?:\\.\\d+)?)(?:m2|metvuong)");
                Matcher areaLowerMatcher = areaLowerPattern.matcher(cleanQuestion);
                if (areaLowerMatcher.find()) {
                    double targetArea = Double.parseDouble(areaLowerMatcher.group(1));
                    String json = String.format(Locale.US, "{ 'area': { '$lte': %.1f }, 'status': 'ACTIVE' }", targetArea);
                    fallbackQuery = new BasicQuery(json);
                    log.info("[Cứu Hộ Area] Tự khớp điều kiện danh sách DƯỚI: {}", json);
                }
            }
        }

        // ======================== PHẦN 2: TỰ ĐỘNG KHỚP GIÁ CẢ (PRICE) ========================
        if (fallbackQuery == null) {
            // 2.1: Khoảng giá "từ X đến Y" (Tự động nhân 1 triệu và ép đuôi .0)
            Pattern rangePattern = Pattern.compile("(?:tu|trongkhoang)(\\d+(?:\\.\\d+)?)(?:den|va)(\\d+(?:\\.\\d+)?)(?:trieu|tr)?");
            Matcher rangeMatcher = rangePattern.matcher(cleanQuestion);
            if (rangeMatcher.find()) {
                double fromPrice = Double.parseDouble(rangeMatcher.group(1)) * 1000000;
                double toPrice = Double.parseDouble(rangeMatcher.group(2)) * 1000000;
                String json = String.format(Locale.US, "{ 'price': { '$gte': %.1f, '$lte': %.1f }, 'status': 'ACTIVE' }", fromPrice, toPrice);
                fallbackQuery = new BasicQuery(json);
                log.info("[Cứu Hộ Price] Tự khớp khoảng giá danh sách 'từ... đến...': {}", json);
            }
        }

        if (fallbackQuery == null) {
            // 2.2: Giá lớn hơn / TRÊN / cao hơn / từ X trở lên
            Pattern upperPattern = Pattern.compile("(?:tren|lonhon|caohon)(\\d+(?:\\.\\d+)?)(?:trieu|tr)?|(?:tu)(\\d+(?:\\.\\d+)?)(?:trieu|tr)?(?:trolen)");
            Matcher upperMatcher = upperPattern.matcher(cleanQuestion);
            if (upperMatcher.find()) {
                String priceStr = upperMatcher.group(1) != null ? upperMatcher.group(1) : upperMatcher.group(2);
                double targetPrice = Double.parseDouble(priceStr) * 1000000;
                String json = String.format(Locale.US, "{ 'price': { '$gte': %.1f }, 'status': 'ACTIVE' }", targetPrice);
                fallbackQuery = new BasicQuery(json);
                log.info("[Cứu Hộ Price] Tự khớp điều kiện danh sách 'TRÊN': {}", json);
            }
        }

        if (fallbackQuery == null) {
            // 2.3: Giá nhỏ hơn / DƯỚI / thấp hơn
            Pattern lowerPattern = Pattern.compile("(?:duoi|nhohon|thaphon)(\\d+(?:\\.\\d+)?)(?:trieu|tr)?");
            Matcher lowerMatcher = lowerPattern.matcher(cleanQuestion);
            if (lowerMatcher.find()) {
                double targetPrice = Double.parseDouble(lowerMatcher.group(1)) * 1000000;
                String json = String.format(Locale.US, "{ 'price': { '$lte': %.1f }, 'status': 'ACTIVE' }", targetPrice);
                fallbackQuery = new BasicQuery(json);
                log.info("[Cứu Hộ Price] Tự khớp điều kiện danh sách 'DƯỚI': {}", json);
            }
        }

        // Biên độ phòng vệ cuối cùng (Mặc định khi người dùng gõ mỗi số vu vơ như "3 trieu", "3tr")
        if (fallbackQuery == null) {
            double extractedPrice = extractPriceFromString(question);
            if (extractedPrice > 0) {
                String json = String.format(Locale.US, "{ 'price': { '$gte': %.1f, '$lte': %.1f }, 'status': 'ACTIVE' }", extractedPrice - 500000, extractedPrice + 500000);
                fallbackQuery = new BasicQuery(json);
                log.info("[Cứu Hộ Phòng Vệ] Áp dụng biên độ mặc định danh sách: {}", json);
            } else {
                fallbackQuery = new BasicQuery("{ 'status': 'ACTIVE' }");
                log.info("[Cứu Hộ Toàn Cục] Không nhận diện được mốc, quét toàn bộ danh sách ACTIVE.");
            }
        }

        // Thực thi cấu hình số lượng giới hạn và sắp xếp đồng bộ
        fallbackQuery.limit(queryLimit);
        fallbackQuery.with(Sort.by(Sort.Direction.ASC, "price"));

        List<Document> databaseResults = mongoTemplate.find(fallbackQuery, Document.class, TARGET_COLLECTION);
        log.info("Số lượng bài đăng tìm thấy từ Cứu Hộ Local Query: {}", databaseResults.size());

        return generateLocalAnswer(databaseResults, history, question);
    }

    /**
     * Quyết định số lượng hiển thị dựa trên bộ lọc từ khóa độc quyền của người dùng
     */
    private int determineLimitCount(String question) {
        if (question == null) return 20;
        String lower = question.toLowerCase();

        // CHỈ KHI NÀO chứa các từ khóa chỉ định số lượng đơn lẻ tuyệt đối này mới ép về LIMIT = 1
        if (lower.contains("chỉ") || lower.contains("duy nhất") || lower.contains("đúng 1") || lower.contains("chỉ 1")) {
            log.info("[Logic Số Lượng] Phát hiện từ khóa ép số lượng độc quyền. Thiết lập LIMIT = 1");
            return 1;
        }

        // Tất cả các trường hợp tìm kiếm thông thường (khoảng, trên, dưới, tầm,...) mặc định lấy danh sách rộng (Tối đa 20 bài)
        return 20;
    }

    @SuppressWarnings("unchecked")
    private Object generateLocalAnswer(List<Document> data, List<Map<String, String>> history, String question) {
        if (data == null || data.isEmpty()) {
            return "Dạ hiện tại hệ thống Trọ Sinh Viên chưa tìm thấy dữ liệu phù hợp với yêu cầu tra cứu của bạn. Bạn vui lòng thử lại với tiêu chí khác nhé.";
        }

        List<Map<String, Object>> customPostList = new ArrayList<>();
        for (Document doc : data) {
            Map<String, Object> customPost = new LinkedHashMap<>();
            String idStr = doc.get("_id") != null ? doc.get("_id").toString() : doc.getString("id");
            customPost.put("id", idStr);
            customPost.put("title", doc.getString("title"));

            double finalPrice = 0.0;
            if (doc.get("price") != null) {
                Object priceObj = doc.get("price");
                if (priceObj instanceof Number) {
                    finalPrice = ((Number) priceObj).doubleValue();
                }
            }
            customPost.put("price", finalPrice);

            double finalArea = 0.0;
            if (doc.get("area") != null) {
                Object areaObj = doc.get("area");
                if (areaObj instanceof Number) {
                    finalArea = ((Number) areaObj).doubleValue();
                }
            }
            customPost.put("area", finalArea);

            customPost.put("address", doc.getString("address"));
            customPost.put("images", doc.get("images", List.class) != null ? doc.get("images", List.class) : new ArrayList<String>());

            customPostList.add(customPost);
        }

        String text = "Dạ, đây là một số phòng ở theo yêu cầu của bạn, bạn bấm vào để xem chi tiết nhé:";
        history.add(Map.of("role", "user", "content", question));
        history.add(Map.of("role", "assistant", "content", text));

        Map<String, Object> finalResultPayload = new LinkedHashMap<>();
        finalResultPayload.put("text", text);
        finalResultPayload.put("rooms", customPostList);

        return finalResultPayload;
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
                    "temperature", 0.1
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
            if (e.getStatusCode().value() == 429 || e.getStatusCode().value() == 503) {
                return "CẢNH BÁO: Hạn ngạch tài khoản AI thử nghiệm đã hết.";
            }
            return "Yêu cầu kết nối trí tuệ nhân tạo bị gián đoạn: " + e.getStatusText();
        } catch (Exception e) {
            return "Không thể kết nối với trí tuệ nhân tạo AI bị gián đoạn: " + e.getMessage();
        }
    }

    private double extractPriceFromString(String text) {
        if (text == null || text.isEmpty()) return 0;
        String clean = text.toLowerCase().replaceAll("[?.!,¿¡]", "").replaceAll("\\s+", "");
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)(triệu|triêu|tr)");
        Matcher matcher = pattern.matcher(clean);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1)) * 1000000;
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
                cleanText.contains("thank") || cleanText.equals("chào");
    }

    private void delayThread(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}