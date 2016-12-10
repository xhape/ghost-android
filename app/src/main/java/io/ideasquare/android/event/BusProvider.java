package io.ideasquare.android.event;

import com.squareup.otto.Bus;

public class BusProvider {

    private static final Bus mBus = new Bus();

    private BusProvider() {}

    public static Bus getBus() { return mBus; }

}
