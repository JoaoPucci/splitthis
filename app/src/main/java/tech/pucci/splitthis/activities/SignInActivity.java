package tech.pucci.splitthis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import tech.pucci.splitthis.R;
import tech.pucci.splitthis.enums.FacebookPermission;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mFacebookCallbackManager;
    private FirebaseAuth mAuth;

    private static class RequestCode {
        static final int RC_SIGN_IN = 100;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        createClient();
        configureViews();
    }

    private void createClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void configureViews() {
        configureGoogleSignInButton();
        configureFacebookSignInButton();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
            Snackbar.make(findViewById(android.R.id.content), mAuth.getCurrentUser().getEmail(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void configureGoogleSignInButton() {
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_AUTO);
        signInButton.setOnClickListener(this);
    }

    private void configureFacebookSignInButton() {
        mFacebookCallbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(FacebookPermission.EMAIL.toString(), FacebookPermission.PUBLIC_PROFILE.toString());
        loginButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                authenticate(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.w("SignIn", "onCancel: Facebook login canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.i("SignIn", "onError: " + error.getLocalizedMessage());
                Snackbar.make(findViewById(android.R.id.content), R.string.login_failed, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RequestCode.RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case RequestCode.RC_SIGN_IN:
                handleGoogleSignIn(data);
                break;
        }
    }

    private void handleGoogleSignIn(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                authenticate(account);
            }
        } catch (ApiException e) {
            Snackbar.make(findViewById(android.R.id.content), R.string.login_failed, Snackbar.LENGTH_LONG).show();
            Log.w("SignIn", "Google sign in failed", e);
        }
    }

    private void authenticate(GoogleSignInAccount acct) {
        Log.d("SignIn", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                                Snackbar.make(findViewById(android.R.id.content), mAuth.getCurrentUser().getEmail(), Snackbar.LENGTH_LONG).show();
                                Log.i("SignIn", "onComplete: Login successful");
                            }
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), R.string.login_failed, Snackbar.LENGTH_LONG).show();
                            Log.w("SignIn", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void authenticate(AccessToken token) {
        Log.d("SignIn", "firebaseAuthWithFacebook:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("SignIn", "signInWithCredential:success");

                            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                                Snackbar.make(findViewById(android.R.id.content), mAuth.getCurrentUser().getEmail(), Snackbar.LENGTH_LONG).show();
                            }

                        } else {
                            Snackbar.make(findViewById(android.R.id.content), R.string.login_failed, Snackbar.LENGTH_LONG).show();
                            Log.w("SignIn", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

}
