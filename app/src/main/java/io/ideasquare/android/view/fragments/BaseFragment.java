package io.ideasquare.android.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import com.trello.rxlifecycle.components.support.RxFragment;
import io.ideasquare.android.event.BusProvider;
import io.ideasquare.android.view.BaseActivity;
import io.ideasquare.android.SpectreApplication;

public abstract class BaseFragment extends RxFragment {

    private static final String TAG = "BaseFragment";

    protected Bus getBus() {
        return BusProvider.getBus();
    }

    @SuppressWarnings("unused")
    protected Picasso getPicasso() {
        return SpectreApplication.getInstance().getPicasso();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        getBus().register(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        getBus().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /**
     * Called by the hosting {@link BaseActivity} to give the Fragment
     * a chance to handle the back press event. The Fragment must return true in order to prevent
     * the default action: {@link android.app.Activity#finish}.
     *
     * @return true if this Fragment has handled the event, false otherwise
     */
    public boolean onBackPressed() {
        return false;
    }

}
