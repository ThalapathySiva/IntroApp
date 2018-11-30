package com.example.sivaram.introapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.util.HashMap;

public class MainScreen extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TwitterLogin";


    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    private TwitterAuthClient mLoginButton;
    public static final int RequestSignInCode = 7;
    public FirebaseAuth firebaseAuth;
    public GoogleApiClient googleApiClient;
    Button signin;
    private Button btnConnect;
    private InstagramApp mApp;
    private InstagramSession mSession;
    private HashMap<String, String> userInfoHashmap = new HashMap<String, String>();
    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == InstagramApp.WHAT_FINALIZE) {
                userInfoHashmap = mApp.getUserInfo();
            } else if (msg.what == InstagramApp.WHAT_FINALIZE) {
                Toast.makeText(MainScreen.this, "Check your network.",
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(
                getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret));

        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .twitterAuthConfig(authConfig)
                .build();

        Twitter.initialize(twitterConfig);
        setContentView(R.layout.activity_main_screen);
        btnConnect = (Button) findViewById(R.id.button3);
        mSession = new InstagramSession(getApplicationContext());
        btnConnect.setOnClickListener(this);
        setUpViews();
        mAuth = FirebaseAuth.getInstance();
        signin = (Button) findViewById(R.id.button1);
        firebaseAuth = FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(MainScreen.this)
                .enableAutoManage(MainScreen.this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainScreen.this,"error",Toast.LENGTH_LONG).show();

                    }
                } )
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UserSignInMethod();

            }
        });
   /*     mLoginButton = findViewById(R.id.customtwitter);
        mLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                updateUI(null);
            }
        });*/
        final TwitterAuthClient mTwitterAuthClient= new TwitterAuthClient();

        Button twitter_custom_button = (Button) findViewById(R.id.customtwitter);
        twitter_custom_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                mTwitterAuthClient.authorize(MainScreen.this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {

                    @Override
                    public void success(Result<TwitterSession> twitterSessionResult) {
                        // Success
                        Log.d(TAG, "twitterLogin:success" + twitterSessionResult);
                        handleTwitterSession(twitterSessionResult.data);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        e.printStackTrace();
                        updateUI(null);
                    }
                });


            }
        });




    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainScreen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            Log.e("","");
        } else {
            Log.e("","");
        }
    }

    @VisibleForTesting

    public ProgressDialog mProgressDialog;
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void setUpViews() {
        mApp = new InstagramApp(this, Constants.INSTA_CLIENT_ID,
                Constants.INSTA_CLIENT_SECRET, Constants.INSTA_CALLBACK_URL);
        mApp.setListener(new InstagramApp.OAuthAuthenticationListener() {

            @Override
            public void onSuccess() {
                // tvSummary.setText("Connected as " + mApp.getUserName());
                // userInfoHashmap = mApp.
                mApp.fetchUserName(handler);
                setInstragramData(true);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT)
                        .show();
            }
        });


        if (mApp.hasAccessToken()) {
            // tvSummary.setText("Connected as " + mApp.getUserName());
            mApp.fetchUserName(handler);
/*
            Log.w("Id",mApp.getId());
            Log.w("Name",mApp.getName());
            Log.w("Token",mApp.getTOken());
            Log.w("UserName",mApp.getUserName());
            Log.w("Profile",mApp.getProfilePicture());*/
            setInstragramData(true);
        }

    }
    public void setInstragramData(boolean isLogin)
    {
        if (isLogin && mSession.getLoginType().equals("MainScreen")) {
//        Log.w("InstraData",userInfoHashmap.get(InstagramApp.TAG_ID));
            Log.w("ID",mApp.getName());
           /* userId.setText(mApp.getId());
            userName.setText(mApp.getName());
            Picasso.with(this).load(mApp.getProfilePicture()).into(userProfile);*/
        }

    }
    @Override
    public void onBackPressed()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        }
    }
    @Override
    public void onClick(View view) {

        if (view == btnConnect) {
            connectOrDisconnectUser();
        }
    }
    private void connectOrDisconnectUser() {
        if (mSession.getAccessToken() != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainScreen.this);
            builder.setMessage("Logout ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    mApp.resetAccessToken();
                                    // btnConnect.setVisibility(View.VISIBLE);
                                    // tvSummary.setText("Not connected");
                                }
                            })
                    .setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });
            final AlertDialog alert = builder.create();
            alert.show();
        } else {
            mApp.authorize();
        }
    }

    public void UserSignInMethod(){
        Intent AuthIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(AuthIntent, RequestSignInCode);
    }
    int twitter=2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RequestSignInCode){

            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (googleSignInResult.isSuccess()){

                GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();

                FirebaseUserAuth(googleSignInAccount);
            }

        }

        if (requestCode==twitter){
            mLoginButton.onActivityResult(requestCode, resultCode, data);

        }

    }


    public void FirebaseUserAuth(GoogleSignInAccount googleSignInAccount) {

        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        Toast.makeText(MainScreen.this,"Logged in",Toast.LENGTH_LONG).show();

        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(MainScreen.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> AuthResultTask) {

                        if (AuthResultTask.isSuccessful()){
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                           /* LoginUserEmail.setVisibility(View.VISIBLE);
                            LoginUserName.setVisibility(View.VISIBLE);
                            LoginUserName.setText("NAME = "+ firebaseUser.getDisplayName().toString());
                            LoginUserEmail.setText("Email = "+ firebaseUser.getEmail().toString());*/
                            Log.e("hh",firebaseUser.getDisplayName().toString());

                        }else {
                            Toast.makeText(MainScreen.this,"Something Went Wrong",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }



    /*public void google(View view) {
        Toast.makeText(MainScreen.this,"Google",Toast.LENGTH_LONG).show();
    }*/

   /* public void twitter(View view) {
        Toast.makeText(MainScreen.this,"Twitter",Toast.LENGTH_LONG).show();
    }*/

    public void facebook(View view) {
        Toast.makeText(MainScreen.this,"Facebook",Toast.LENGTH_LONG).show();

    }





}
