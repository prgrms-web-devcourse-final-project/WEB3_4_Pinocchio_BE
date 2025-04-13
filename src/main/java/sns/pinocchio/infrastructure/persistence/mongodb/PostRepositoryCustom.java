package sns.pinocchio.infrastructure.persistence.mongodb;

public interface PostRepositoryCustom {
    void incrementLikesCount(String postId, int delta);
}