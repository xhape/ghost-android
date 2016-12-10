package io.ideasquare.android.event;

public class PostDeletedEvent {

    public final int postId;

    public PostDeletedEvent(int postId) {
        this.postId = postId;
    }
}
