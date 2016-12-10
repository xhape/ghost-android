package io.ideasquare.android.event;

public class LogoutEvent {

    public final boolean forceLogout;

    public LogoutEvent(boolean forceLogout) {
        this.forceLogout = forceLogout;
    }

}
