package com.pollytronics.clique;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pollytronics.clique.lib.CliqueActivity;
import com.pollytronics.clique.lib.api_v02.ApiCallGetSalts;
import com.pollytronics.clique.lib.api_v02.ApiCallGetValidatekey;
import com.pollytronics.clique.lib.api_v02.ApiCallPostAccounts;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbRecreate;
import com.pollytronics.clique.lib.tools.MyAssortedTools;
import com.pollytronics.clique.lib.tools.MyCrypto;

import org.json.JSONException;

import java.io.IOException;

//import android.app.DialogFragment;

/**
 * TODO: when pressing "back" from login screen, shit happens
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
     * Any Clique acitvity will check for credentials in sharedpreferences in onResume()
     * using this method and start a Login_activity if none are found.
     * So were basically preventing an infinite loop here.
     */
    protected void assertAuthCredentials() { }

    /**
     * handle the GUI event of a login attempt, check formatting of fields and
     * suggest to user what to change
     */
    private void attemptLogin() {
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
            MyAssortedTools.hide_keyboard(this);
            try {
                mAuthTask = new UserLoginTask(username, password);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Error();
            }
            mAuthTask.execute();
        }
    }

    // TODO: why is this using parameters and attemptLogin isn't. it doesn't feel right.
    // TODO: also: duplicate code alert!
    private void attemptCreateAccount(String username, String pass, String nickname) {
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
            MyAssortedTools.hide_keyboard(this);
            mCreateAccountTask = new CreateAccountTask(username, pass);
            mCreateAccountTask.execute();
        }
    }

    private boolean isUsernameValid(String username) {
        return username.length() >= 5;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 5;
    }

    private enum ProgressBarState { LOGIN, WAIT, DONE }
    private void showProgress(final ProgressBarState show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show == ProgressBarState.WAIT || show == ProgressBarState.DONE  ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(show == ProgressBarState.WAIT || show == ProgressBarState.DONE ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show == ProgressBarState.WAIT || show == ProgressBarState.DONE ? View.GONE : View.VISIBLE);
            }
        });
        mProgressView.setVisibility(show == ProgressBarState.WAIT ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(show == ProgressBarState.WAIT ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show == ProgressBarState.WAIT ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Java thing: an enum is by definition a static class, and you can't declare static classes within a non-static member class,
     * so you can't use enums within member classes :(
     * UserLoginTask also can't be made static as it then wouldn't have access its parents attributes anymore.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, Integer> {
        private static final int AUTH_SUCCESS = 1;
        private static final int NEW_USERNAME = 2;
        private static final int WRONG_PASSWD = 3;
        private static final int  OTHER_ERROR = 4;

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
            showProgress(ProgressBarState.WAIT);
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
                this.key_b64 = MyCrypto.calcKey64(cs_b64, mPassword);
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return OTHER_ERROR;
        }

        @Override
        protected void onPostExecute(Integer loginResult) {
            mAuthTask = null;
            switch (loginResult) {
                case AUTH_SUCCESS:
                    getCliquePreferences().setAccountLogin(mUsername);
                    getCliquePreferences().setAccountKeyb64(key_b64);
                    getCliquePreferences().setAccountId(accountId);
                    showProgress(ProgressBarState.DONE);
                    finish();
                    break;
                case WRONG_PASSWD:
                    showProgress(ProgressBarState.LOGIN);
                    mPasswordView.setError(getString(R.string.error_password_incorrect));
                    mPasswordView.requestFocus();
                    break;
                case NEW_USERNAME:
                    showProgress(ProgressBarState.LOGIN);
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
            showProgress(ProgressBarState.LOGIN);
        }
    }

    private class CreateAccountTask extends AsyncTask<Void, Void, Integer> {
        private final int   CREATE_SUCCESS = 1;
        private final int CREATE_NOSUCCESS = 2;
        private final int      OTHER_ERROR = 3;

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
            showProgress(ProgressBarState.WAIT);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                cs_b64 = MyCrypto.generateSaltb64();
                key_b64 = MyCrypto.calcKey64(cs_b64, mPassword);
                cs2_b64 = MyCrypto.generateSaltb64();
                key2_b64 = MyCrypto.calcKey64(cs2_b64, mPassword);
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return OTHER_ERROR;
        }

        @Override
        protected void onPostExecute(Integer loginResult) {
            mCreateAccountTask = null;
            switch (loginResult) {
                case CREATE_SUCCESS:
                    getCliquePreferences().setAccountLogin(mUsername);
                    getCliquePreferences().setAccountKeyb64(key_b64);
                    getCliquePreferences().setAccountId(accountId);
                    showProgress(ProgressBarState.DONE);
                    finish();
                    break;
                default:
                    showProgress(ProgressBarState.LOGIN);
                    Log.i(TAG, "api call POST accounts failed, not doing anything");
            }
        }

        @Override
        protected void onCancelled() {
            mCreateAccountTask = null;
            showProgress(ProgressBarState.LOGIN);
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
                                    .attemptCreateAccount(
                                            loginEdit.getText().toString(),
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
}