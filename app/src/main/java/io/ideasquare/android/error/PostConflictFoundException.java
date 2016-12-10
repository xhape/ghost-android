package io.ideasquare.android.error;

public class PostConflictFoundException extends RuntimeException {

    public PostConflictFoundException() {
        super("POST CONFLICT FOUND: see logs for details");
    }

}
