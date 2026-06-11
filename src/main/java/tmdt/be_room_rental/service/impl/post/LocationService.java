package tmdt.be_room_rental.service.impl.post;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class LocationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Double[]> getProvincePolygon(String provinceName) {
        try {
            // polygon_geojson=1 giúp lấy ranh giới thực tế của tỉnh
            String url = String.format(Locale.US,
                    "https://nominatim.openstreetmap.org/search?q=%s&format=json&polygon_geojson=1&limit=1",
                    provinceName);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "RoomRentalApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class).getBody();

            if (response != null && !response.isEmpty()) {
                Map<String, Object> firstResult = (Map<String, Object>) response.get(0);
                Map<String, Object> geojson = (Map<String, Object>) firstResult.get("geojson");

                if ("Polygon".equals(geojson.get("type"))) {
                    List<List<List<Double>>> coords = (List<List<List<Double>>>) geojson.get("coordinates");
                    List<Double[]> polygon = new ArrayList<>();
                    for (List<Double> point : coords.get(0)) {
                        polygon.add(new Double[]{point.get(0), point.get(1)});
                    }
                    return polygon;
                } else if ("MultiPolygon".equals(geojson.get("type"))) {
                    // Xử lý trường hợp thành phố có nhiều vùng rời nhau (như đảo)
                    List<List<List<List<Double>>>> coords = (List<List<List<List<Double>>>>) geojson.get("coordinates");
                    List<Double[]> polygon = new ArrayList<>();
                    for (List<Double> point : coords.get(0).get(0)) {
                        polygon.add(new Double[]{point.get(0), point.get(1)});
                    }
                    return polygon;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy ranh giới tỉnh: " + e.getMessage());
        }
        return null;
    }
}