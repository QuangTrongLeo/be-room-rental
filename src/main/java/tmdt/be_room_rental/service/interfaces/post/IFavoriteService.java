package tmdt.be_room_rental.service.interfaces.post;

import tmdt.be_room_rental.dto.res.post.FavoriteResponse;

import java.util.List;

public interface IFavoriteService {
    FavoriteResponse addFavorite(String postId);
    void removeFavorite(String postId);
    List<FavoriteResponse> getMyFavorites();
    boolean isFavorited(String postId);
}
