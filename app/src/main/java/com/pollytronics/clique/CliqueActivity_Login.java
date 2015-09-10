package com.pollytronics.clique;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pollytronics.clique.lib.CliqueActivity;
import com.pollytronics.clique.lib.api_v02.ApiCallGetSalts;
import com.pollytronics.clique.lib.api_v02.ApiCallGetValidatekey;
import com.pollytronics.clique.lib.api_v02.ApiCallPostAccounts;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbRecreate;

import org.json.JSONException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//import android.app.DialogFragment;

/**
 * TODO: when pressing "back" from login screen, shit happens
 * TODO: move the hide keyboard subroutine to a more appropriate place in the project
 * TODO: make sure the optional message json-entry in always checked before read, as it is optional
 * TODO: error handling!
 * TODO: initial sync after logging in, not sure if that has to happen in this class though
 */
public class CliqueActivity_Login extends CliqueActivity {

    private static final String TAG = "CliqueActivity_Login";

    private EditText mUserNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private UserLoginTask mAuthTask = null;
    private CreateAccountTask mCreateAccountTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            new CliqueDbRecreate().execute();
        } catch (CliqueDbException e) {
            e.printStackTrace();
        }
        getCliquePreferences().setAccountId(0);
        getCliquePreferences().setAccountKeyb64(null);

        setContentView(R.layout.cliqueactivity_login);

        mUserNameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mProgressView = findViewById(R.id.login_progress);
        mLoginFormView = findViewById(R.id.login_form);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.login || actionId == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        String username = getCliquePreferences().getAccountLogin();
        if (username != null) mUserNameView.setText(username);
    }

    @Override
    /**
     * must override this cliqueActivity function to as this login activity is started when theres no credentials
     */
    protected void assertAuthCredentials() {
    }

    // yuck! the following lines are to hide the keyboard (http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard)
    // yuck android, thanks rmirabelle
    private static void hide_keyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if(view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // TODO: shouldn't this be called with username/pass as parameters or smth?
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mUserNameView.setError(null);
        mPasswordView.setError(null);

        String username = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_password_invalid));
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(username)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUserNameView.setError(getString(R.string.error_username_invalid));
            focusView = mUserNameView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            try {
                mAuthTask = new UserLoginTask(username, password);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Error();
            }
            hide_keyboard(this);
            mAuthTask.execute();
        }
    }

    // TODO: why is this using parameters and attemptLogin isn't. it doesn't feel right.
    // TODO: duplicate code alert!
    public void attemptCreateAccount(String username, String pass, String nickname) {
        if (mCreateAccountTask != null) {
            return;
        }
        mUserNameView.setError(null);
        mPasswordView.setError(null);
        mUserNameView.setText(username);
        mPasswordView.setText(pass);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(pass)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(pass)) {
            mPasswordView.setError(getString(R.string.error_password_invalid));
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(username)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUserNameView.setError(getString(R.string.error_username_invalid));
            focusView = mUserNameView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            try {
                mCreateAccountTask = new CreateAccountTask(username, pass);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Error();
            }
            hide_keyboard(this);
            mCreateAccountTask.execute();
        }
    }



    private boolean isUsernameValid(String username) {
        return username.length() >= 5;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 5;
    }

    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private class UserLoginTask extends AsyncTask<Void, Void, Integer> {
        private final int AUTH_SUCCESS = 1;
        private final int NEW_USERNAME = 2;
        private final int WRONG_PASSWD = 3;
        private final int  OTHER_ERROR = 4;

        private final String mUsername;
        private final String mPassword;

        private String cs_b64 = null;
        private String key_b64 = null;
        private int accountId = 0;

        public UserLoginTask(String mUsername, String mPassword) {
            this.mUsername = mUsername;
            this.mPassword = mPassword;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // get salt for username
            //    if no salt -> trigger account creation dialog and STOP
            //    if salt    -> calculate key and validate key
            //       if key valid   -> trigger username/key storage and STOP
            //       if key invalid -> trigger wrong password hint and STOP
            try {
                ApiCallGetSalts getSalts = new ApiCallGetSalts(mUsername);
                getSalts.callAndParse();
                if(!getSalts.getCallSuccess()) {
                    return NEW_USERNAME;
                }
                this.cs_b64 = getSalts.getCs();
                this.key_b64 = calcKey64(cs_b64, mPassword);
                ApiCallGetValidatekey validate = new ApiCallGetValidatekey(mUsername,key_b64);
                validate.callAndParse();
                if(validate.isCallSuccess()) {
                    this.accountId = validate.getAccountId();
                    return AUTH_SUCCESS;
                } else {
                    Log.i(TAG, "GET validatekey returned success:false");
                    String m = validate.getCallMessage();
                    if(m != null) Log.i(TAG, "api message= " + m);
                    return WRONG_PASSWD;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return OTHER_ERROR;
        }

        @Override
        protected void onPostExecute(Integer loginResult) {
            mAuthTask = null;
            showProgress(false);
            switch (loginResult) {
                case AUTH_SUCCESS:
                    getCliquePreferences().setAccountLogin(mUsername);
                    getCliquePreferences().setAccountKeyb64(key_b64);
                    getCliquePreferences().setAccountId(accountId);
                    finish();
                    break;
                case WRONG_PASSWD:
                    mPasswordView.setError(getString(R.string.error_password_incorrect));
                    mPasswordView.requestFocus();
                    break;
                case NEW_USERNAME:
                    Log.i(TAG, "unknown username, lets suggest creating a new account with it.");
                    CreateNewAccountDialog createNewAccountDialog =
                            CreateNewAccountDialog.newInstance(mUsername, mPassword, "anon");
                    createNewAccountDialog.show(getSupportFragmentManager(), "createNewAccountDialog");
                    break;
                case OTHER_ERROR:
                    Log.i(TAG, "something went wrong while logging in... what is it? well let's find out shall we?");
                default:
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private class CreateAccountTask extends AsyncTask<Void, Void, Integer> {
        private final int CREATE_SUCCESS = 1;
        private final int CREATE_NOSUCCESS = 2;
        private final int  OTHER_ERROR = 4;

        private final String mUsername;
        private final String mPassword;

        private String cs_b64 = null;
        private String cs2_b64 = null;
        private String key_b64 = null;
        private String key2_b64 = null;
        private int accountId = 0;

        public CreateAccountTask(String mUsername, String mPassword) {
            this.mUsername = mUsername;
            this.mPassword = mPassword;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                cs_b64 = generateSaltb64();
                key_b64 = calcKey64(cs_b64, mPassword);
                cs2_b64 = generateSaltb64();
                key2_b64 = calcKey64(cs2_b64, mPassword);
                ApiCallPostAccounts createAccount = new ApiCallPostAccounts(mUsername,cs_b64,key_b64,cs2_b64,key2_b64);
                createAccount.callAndParse();
                if(createAccount.getCallSuccess()) {
                    this.accountId = createAccount.getAccountId();
                    return CREATE_SUCCESS;
                } else {
                    Log.i(TAG, "POST ACCOUNTS returned success:false");
                    String m = createAccount.getCallMessage();
                    if(m != null) Log.i(TAG, "api message= " + m);
                    return CREATE_NOSUCCESS;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return OTHER_ERROR;
        }

        @Override
        protected void onPostExecute(Integer loginResult) {
            mCreateAccountTask = null;
            showProgress(false);
            switch (loginResult) {
                case CREATE_SUCCESS:
                    getCliquePreferences().setAccountLogin(mUsername);
                    getCliquePreferences().setAccountKeyb64(key_b64);
                    getCliquePreferences().setAccountId(accountId);
                    finish();
                    break;
                default:
                    Log.i(TAG, "api call POST accounts failed, not doing anything");
            }
        }

        @Override
        protected void onCancelled() {
            mCreateAccountTask = null;
            showProgress(false);
        }
    }


    public static class CreateNewAccountDialog extends DialogFragment {
        static CreateNewAccountDialog newInstance(String login, String pass, String nick) {
            CreateNewAccountDialog dialogFrag = new CreateNewAccountDialog();
            Bundle args = new Bundle();
            args.putString("login", login);
            args.putString("pass", pass);
            args.putString("nick", nick);
            dialogFrag.setArguments(args);
            return dialogFrag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String login = getArguments().getString("login");
            final String pass = getArguments().getString("pass");
            final String nick = getArguments().getString("nick");
            final View dialogContentView = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_new_account, null);
            final EditText loginEdit = (EditText) dialogContentView.findViewById(R.id.createaccount_login);
            loginEdit.setText(login);
            final EditText passEdit = (EditText) dialogContentView.findViewById(R.id.createaccount_password);
            passEdit.setText(pass);
            final EditText nickEdit = (EditText) dialogContentView.findViewById(R.id.createaccount_nickname);
            nickEdit.setText(nick);
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setView(dialogContentView)
                    .setMessage("Create new account with these credentials?")
                    .setPositiveButton("sure", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "account creation confirmed, lets try this");
                            ((CliqueActivity_Login) getActivity())
                                    .attemptCreateAccount(loginEdit.getText().toString(),
                                            passEdit.getText().toString(),
                                            nickEdit.getText().toString());
                        }
                    })
                    .setNegativeButton("nope", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "account creation canceled, letting dialog finish");
                        }
                    }).create();
            return dialog;
        }
    }

    private String generateSaltb64() {
        Log.i(TAG,"WARNING GENERATING A RANDOM SALT USING A CONSTANT!!!!!!");
        return "VX4IAeia5bX/jwe15x0s5SwpfgIB5mXbaea5hVDVDjfNBMmf+HUYzqfFCQE8dqoOEK5SowWRo+IjTnrOwvH4Lg==";
    }

    private String calcKey64(String salt64, String pass) {
        byte[] salt = Base64.decode(salt64, Base64.DEFAULT);
        byte[] passbytes = pass.getBytes();
        byte[] concat = new byte[salt.length + passbytes.length];
        byte[] key = null;
        System.arraycopy(salt, 0, concat, 0, salt.length);
        System.arraycopy(passbytes, 0, concat, salt.length, passbytes.length);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            key = md.digest(concat);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(key, Base64.NO_WRAP);
    }

}