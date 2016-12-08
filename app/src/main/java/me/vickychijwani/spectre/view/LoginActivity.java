package me.vickychijwani.spectre.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Subscribe;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import me.vickychijwani.spectre.R;
import me.vickychijwani.spectre.error.LoginFailedException;
import me.vickychijwani.spectre.event.LoginDoneEvent;
import me.vickychijwani.spectre.event.LoginErrorEvent;
import me.vickychijwani.spectre.event.LoginStartEvent;
import me.vickychijwani.spectre.network.entity.ApiError;
import me.vickychijwani.spectre.network.entity.ApiErrorList;
import me.vickychijwani.spectre.pref.UserPrefs;
import me.vickychijwani.spectre.util.KeyboardUtils;
import me.vickychijwani.spectre.util.NetworkUtils;
import retrofit.RetrofitError;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements
        OnClickListener,
        TextView.OnEditorActionListener {

    private final String TAG = "LoginActivity";

    // UI references
    @Bind(R.id.email_layout)            TextInputLayout mEmailLayout;
    @Bind(R.id.email)                   EditText mEmailView;
    @Bind(R.id.password_layout)         TextInputLayout mPasswordLayout;
    @Bind(R.id.password)                EditText mPasswordView;
    @Bind(R.id.sign_in_btn)             Button mSignInBtn;
    @Bind(R.id.login_progress)          View mProgressView;
    @Bind(R.id.login_form)              View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout(R.layout.activity_login);

        mPasswordView.setOnEditorActionListener(this);

        UserPrefs prefs = UserPrefs.getInstance(this);
        mEmailView.setText(prefs.getString(UserPrefs.Key.USERNAME));
        mPasswordView.setText(prefs.getString(UserPrefs.Key.PASSWORD));

        mSignInBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        attemptLogin();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == getResources().getInteger(R.integer.ime_action_id_signin)
                || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
            attemptLogin();
            // don't consume the event, so the keyboard can also be hidden
            // http://stackoverflow.com/questions/2342620/how-to-hide-keyboard-after-typing-in-edittext-in-android#comment20849208_10184099
            return false;
        }
        return false;
    }

    private void attemptLogin() {
        // reset errors
        mEmailLayout.setError(null);
        mPasswordLayout.setError(null);

        // store values at the time of the login attempt
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean hasError = false;
        View focusView = null;

        // check for a valid password
        if (TextUtils.isEmpty(password)) {
            mPasswordLayout.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            hasError = true;
        } else if (! isPasswordValid(password)) {
            mPasswordLayout.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            hasError = true;
        }

        // check for a valid email address
        if (TextUtils.isEmpty(email)) {
            mEmailLayout.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            hasError = true;
        } else if (! isEmailValid(email)) {
            mEmailLayout.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            hasError = true;
        }

        if (hasError) {
            // there was an error; focus the first form field with an error
            focusView.requestFocus();
        } else {
            // actual login attempt
            showProgress(true);
            KeyboardUtils.defocusAndHideKeyboard(this);
            sendLoginRequest(email, password);
        }
    }

    private void sendLoginRequest(String username, String password) {
        getBus().post(new LoginStartEvent(username, password, true));
    }

    @Subscribe
    public void onLoginDoneEvent(LoginDoneEvent event) {
        UserPrefs prefs = UserPrefs.getInstance(this);
        prefs.setString(UserPrefs.Key.BLOG_URL, event.blogUrl);
        prefs.setString(UserPrefs.Key.USERNAME, event.username);
        prefs.setString(UserPrefs.Key.PASSWORD, event.password);
        finish();

        Intent intent = new Intent(this, PostListActivity.class);
        startActivity(intent);
    }

    @Subscribe
    public void onLoginErrorEvent(LoginErrorEvent event) {
        RetrofitError error = event.error;
        showProgress(false);
        try {
            ApiErrorList errorList = (ApiErrorList) error.getBodyAs(ApiErrorList.class);
            ApiError apiError = errorList.errors.get(0);
            EditText errorView = mPasswordView;
            TextInputLayout errorLayout = mPasswordLayout;
            if ("NotFoundError".equals(apiError.errorType)) {
                errorView = mEmailView;
                errorLayout = mEmailLayout;
            }
            errorView.requestFocus();
            errorLayout.setError(Html.fromHtml(apiError.message));
        } catch (Exception ignored) {
          Toast.makeText(this, getString(R.string.login_unexpected_error),
              Toast.LENGTH_SHORT).show();
        } finally {
            Crashlytics.logException(new LoginFailedException(error));        // report login failures to Crashlytics
            Log.e(TAG, Log.getStackTraceString(error));
        }
    }


    public static boolean isUserNetworkError(Throwable error) {
        // user provided a malformed / non-existent URL
        return error instanceof UnknownHostException || error instanceof MalformedURLException;
    }

    public static boolean isConnectionError(Throwable error) {
        return error instanceof ConnectException || error instanceof SocketTimeoutException;
    }

    private static boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private static boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }

    private static boolean hasScheme(String url) {
        return url.startsWith(NetworkUtils.SCHEME_HTTP) || url.startsWith(NetworkUtils.SCHEME_HTTPS);
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }


}
