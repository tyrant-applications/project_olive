package com.tyrantapp.olive;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.helper.RESTHelper;
import com.tyrantapp.olive.services.SyncNetworkService;

/**
 * A sign up screen that offers sign up via email/password.
 */
public class SignUpActivity extends BaseActivity {

	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };
	/**
	 * Keep track of the sign up task to ensure we can cancel it if requested.
	 */
	//private UserSignUpTask mAuthTask = null;

	// UI references.
	private AutoCompleteTextView mUsernameView;
	private EditText mPasswordView;
	private EditText mPasswordCheckView;
	private View mProgressView;
	private View mSignUpFormView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		// Set up the  form.
		mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordCheckView = (EditText) findViewById(R.id.confirm_password);
		mPasswordView
		.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if (id == R.id.signup || id == EditorInfo.IME_NULL) {
					attemptSignUp();
					return true;
				}
				return false;
			}
		});
		mPasswordCheckView
		.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if (id == R.id.signup || id == EditorInfo.IME_NULL) {
					attemptSignUp();
					return true;
				}
				return false;
			}
		});
		
		Button mSignUpButton = (Button) findViewById(R.id.sign_up_button);
		mSignUpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptSignUp();
			}
		});

		mSignUpFormView = findViewById(R.id.signup_form);
		mProgressView = findViewById(R.id.signup_progress);
	}

	public void attemptSignUp() {
		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the sign up attempt.
		String username = mUsernameView.getText().toString();
		String password = mPasswordView.getText().toString();
		String passwordCheck = mPasswordCheckView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}
		
		if (!password.equals(passwordCheck)) {
			mPasswordView.setError(getString(R.string.error_invalid_password2));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(username)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt sign up and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user sign up attempt.
			showProgress(true);			
			int eError = mRESTHelper.signUp(username, password);
			if (eError == RESTHelper.OLIVE_SUCCESS) {
				Toast.makeText(getApplicationContext(), R.string.toast_succeed_to_create_account, Toast.LENGTH_SHORT);
				showProgress(false);
				finish();
			} else
			if (eError == RESTHelper.OLIVE_FAIL_INVALID_ID_PW) {
				Toast.makeText(getApplicationContext(), R.string.toast_invalid_email_address, Toast.LENGTH_SHORT).show();
			} else {
				String message = String.format(getResources().getString(R.string.toast_failed_to_create_account), eError);
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
			
			showProgress(false);
		}
	}
	
	private boolean isPasswordValid(String password) {
		// TODO: Replace this with your own logic
		return password.length() > 4;
	}

	/**
	 * Shows the progress UI and hides the sign up form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mSignUpFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mSignUpFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mProgressView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous sign up/registration task used to authenticate
	 * the user.
	 */
	public class UserSignUpTask extends AsyncTask<Void, Void, Boolean> {

		private final String mEmail;
		private final String mPassword;

		UserSignUpTask(String email, String password) {
			mEmail = email;
			mPassword = password;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}

			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				if (pieces[0].equals(mEmail)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mPassword);
				}
			}

			// TODO: register the new account here.
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			//mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();
				
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			//mAuthTask = null;
			showProgress(false);
		}
	}
}
