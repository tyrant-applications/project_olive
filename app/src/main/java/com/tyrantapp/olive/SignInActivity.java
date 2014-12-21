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
import com.tyrantapp.olive.types.UserInfo;

/**
 * A sign in screen that offers sign in via email/password.
 */
public class SignInActivity extends BaseActivity {

	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };
	/**
	 * Keep track of the sign in task to ensure we can cancel it if requested.
	 */
	//private UserSignInTask mAuthTask = null;

	// UI references.
	private AutoCompleteTextView mUsernameView;
	private EditText mPasswordView;
	private View mProgressView;
	private View mSignInFormView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);

		// Set up the sign in form.
		mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.signin || id == EditorInfo.IME_NULL) {
							attemptSignIn();
							return true;
						}
						return false;
					}
				});

		Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
		mSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptSignIn();
			}
		});
		
		Button mSignUpButton = (Button) findViewById(R.id.sign_up_button);
		mSignUpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		});

		mSignInFormView = findViewById(R.id.signin_form);
		mProgressView = findViewById(R.id.signin_progress);
	}

	/**
	 * Attempts to sign in or register the account specified by the sign in form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual sign in attempt is made.
	 */
	public void attemptSignIn() {
		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the sign in attempt.
		String username = mUsernameView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
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
			// There was an error; don't attempt sign in and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user sign in attempt.
			showProgress(true);
			int eError = mRESTHelper.signIn(username, password);
			if (eError == RESTHelper.OLIVE_SUCCESS) {
				UserInfo info = mRESTHelper.getUserProfile();					
				
				finish();					
				Intent intent = new Intent(getApplicationContext(), MainActivity.class).putExtra("username", info.mUsername);
				startActivityForPasscode(intent);
			} else
			if (eError == RESTHelper.OLIVE_FAIL_INVALID_ID_PW){
				Toast.makeText(getApplicationContext(), R.string.toast_invalid_id_or_password, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), R.string.toast_failed_to_sign_in, Toast.LENGTH_SHORT).show();
			}
			
			showProgress(false);
		}
	}
	
	private boolean isPasswordValid(String password) {
		// TODO: Replace this with your own logic
		return password.length() > 4;
	}

	/**
	 * Shows the progress UI and hides the sign in form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mSignInFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mSignInFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mSignInFormView.setVisibility(show ? View.GONE
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
			mSignInFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous sign in/registration task used to authenticate
	 * the user.
	 */
	public class UserSignInTask extends AsyncTask<Void, Void, Boolean> {

		private final String mEmail;
		private final String mPassword;

		UserSignInTask(String email, String password) {
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
