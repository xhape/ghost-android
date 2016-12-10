package io.ideasquare.android.event;

import retrofit.RetrofitError;

@SuppressWarnings({"WeakerAccess", "unused"})
public class FileUploadErrorEvent {

    public final RetrofitError error;

    public FileUploadErrorEvent(RetrofitError error) {
        this.error = error;
    }

}
