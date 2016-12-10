package io.ideasquare.android.view.fragments;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import me.vickychijwani.spectre.BuildConfig;
import me.vickychijwani.spectre.R;
import io.ideasquare.android.view.BundleKeys;

/**
 * NOTE: Always use the {@link #newInstance} factory method to create an instance of this fragment.
 */
public class WebViewFragment extends BaseFragment {

    public interface OnWebViewCreatedListener {
        void onWebViewCreated();
    }

    private WebView mWebView;
    private String mUrl;
    private OnWebViewCreatedListener mOnWebViewCreatedListener = null;

    /**
     * Returns a new WebViewFragment which will load the desired URL.
     * @param url - URL to load
     * @return A new instance of WebViewFragment
     */
    public static WebViewFragment newInstance(String url) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(BundleKeys.URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    public WebViewFragment() {}

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        mWebView = (WebView) view.findViewById(R.id.web_view);
        mUrl = getArguments().getString(BundleKeys.URL);
        if (TextUtils.isEmpty(mUrl)) {
            throw new IllegalArgumentException("Empty URL passed to WebViewFragment!");
        }

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        // enable remote debugging
        if (0 != (getActivity().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mWebView.setWebViewClient(new DefaultWebViewClient());
        mWebView.loadUrl(mUrl);

        return view;
    }

    public void setOnWebViewCreatedListener(OnWebViewCreatedListener listener) {
        mOnWebViewCreatedListener = listener;
    }

    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mOnWebViewCreatedListener != null) {
            mOnWebViewCreatedListener.onWebViewCreated();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // don't hold on to the listener (which could potentially be an Activity)
        mOnWebViewCreatedListener = null;
        // destroy the WebView completely
        if (mWebView != null) {
            // the WebView must be removed from the view hierarchy before calling destroy
            // to prevent a memory leak (#75)
            // See https://developer.android.com/reference/android/webkit/WebView.html#destroy%28%29
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
    }

    // our custom methods
    @Nullable
    public String getCurrentUrl() {
        if (mWebView == null) {
            return null;
        }
        String currentLoadedUrl = mWebView.getOriginalUrl();
        if (currentLoadedUrl == null) {
            currentLoadedUrl = mUrl;
        }
        return currentLoadedUrl;
    }

    @Nullable
    public String getCurrentTitle() {
        if (mWebView == null) {
            return null;
        }
        return mWebView.getTitle();
    }

    public void evaluateJavascript(@Nullable String javascript) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(javascript, null);
        } else {
            mWebView.loadUrl("javascript:" + javascript);
        }
    }

    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    public void setJSInterface(Object jsInterface, String name) {
        mWebView.addJavascriptInterface(jsInterface, name);
    }

    public <T extends DefaultWebViewClient> void setWebViewClient(@NonNull T webViewClient) {
        mWebView.setWebViewClient(webViewClient);
    }

    @Override
    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    public static class DefaultWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onReceivedSslError(WebView view, @NonNull SslErrorHandler handler, SslError error) {
            if (BuildConfig.DEBUG) {
                handler.proceed();      // ignore in debug builds
            } else {

            }
        }
    }

}
