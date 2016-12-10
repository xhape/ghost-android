package io.ideasquare.android.event;

import io.ideasquare.android.model.entity.Post;

public class PostCreatedEvent {

    public final Post newPost;

    public PostCreatedEvent(Post newPost) {
        this.newPost = newPost;
    }

}
