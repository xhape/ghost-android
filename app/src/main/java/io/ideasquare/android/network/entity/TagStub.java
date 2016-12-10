package io.ideasquare.android.network.entity;

import android.support.annotation.NonNull;

import io.ideasquare.android.model.entity.Tag;

@SuppressWarnings({"WeakerAccess", "unused"})
public class TagStub {

    public final String name;

    public TagStub(@NonNull Tag tag) {
        this.name = tag.getName();
    }

}
