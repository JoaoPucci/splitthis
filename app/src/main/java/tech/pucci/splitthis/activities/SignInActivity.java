package tech.pucci.splitthis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;

import androidx.appcompat.app.AppCompatActivity;
import tech.pucci.splitthis.R;
import tech.pucci.splitthis.model.SignInHandler;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    static class RequestCode {
        static final int RC_SIGN_IN = 100;
    }

    private SignInHandler signInHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        signInHandler = new SignInHandler(this);
        configureGoogleSignInButton();
    }

    private void configureGoogleSignInButton() {
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_DARK);
        signInButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInAccount account = signInHandler.getSignedInAccount();

        if (account != null) {
            Log.i("SignIn", account.getEmail());
        }
        //updateUI(account);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                startActivityForResult(signInHandler.signIn(), RequestCode.RC_SIGN_IN);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        getSignInResult(requestCode, data);
    }

    private void getSignInResult(int requestCode, Intent data) {
        if (requestCode == SignInActivity.RequestCode.RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            signInHandler.handleSignInResult(task);
        }
    }

}
