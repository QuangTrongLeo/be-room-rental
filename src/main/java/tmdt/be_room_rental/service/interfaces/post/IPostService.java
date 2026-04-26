package tmdt.be_room_rental.service.interfaces.post;

import tmdt.be_room_rental.dto.req.post.PostRequest;
import tmdt.be_room_rental.dto.res.post.PostResponse;
import java.util.List;

public interface IPostService {
    PostResponse createPost(PostRequest request);
    PostResponse updatePost(String id, PostRequest request);
    PostResponse approveActivePost(String id);
    PostResponse rejectActivePost(String id);
    PostResponse republishPost(String id);
    PostResponse getPostById(String id);
    List<PostResponse> getMyPosts();
    List<PostResponse> getPosts();
    List<PostResponse> getPostsByAmenityId(String amenityId);
    List<PostResponse> getPendingPosts();
    List<PostResponse> getActivePosts();
    List<PostResponse> getHiddenPosts();
    List<PostResponse> getRejectPosts();
    void deletePost(String id);
}