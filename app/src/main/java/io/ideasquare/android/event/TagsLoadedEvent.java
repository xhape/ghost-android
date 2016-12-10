package io.ideasquare.android.event;

import io.ideasquare.android.model.entity.Tag;
import java.util.List;

public class TagsLoadedEvent {

    public final List<Tag> tags;

    public TagsLoadedEvent(List<Tag> tags) {
        this.tags = tags;
    }

}
