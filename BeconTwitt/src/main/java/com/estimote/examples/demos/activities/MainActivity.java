package com.estimote.examples.demos.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.examples.demos.R;
import com.estimote.examples.demos.utils.ConnectionManger;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String PREF_NAME = "sample_twitter_pref";
    private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    private static final String PREF_KEY_TWITTER_LOGIN = "is_twitter_loggedin";
    private static final String PREF_USER_NAME = "twitter_user_name";

    public static final int WEBVIEW_REQUEST_CODE = 100;

    private ProgressDialog pDialog;

    private static Twitter twitter;
    private static RequestToken requestToken;

    private static SharedPreferences sharedPreferences;

    private TextView userName, baconData;
    private View loginLayout;
    private View shareLayout;

    private String consumerKey = null;
    private String consumerSecret = null;
    private String callbackUrl = null;
    private String oAuthVerifier = null;
    private List<Beacon> beaconsData = new ArrayList<Beacon>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTwitterConfigs();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        initViews();
        initListners();
        getBeaconData();
        setData();
        if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            Toast.makeText(this, "Twitter key or secret not configured",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        checkTwitterLogin();
    }

    private void setData() {

        baconData.setText("MAC :" + beaconsData.get(0).getMacAddress().toStandardString() + "\n" + " RSSI :" + beaconsData.get(0).getRssi() + "\n" + " MPower :" + beaconsData.get(0).getMeasuredPower() + "\n" + " Minor :" + beaconsData.get(0).getMinor());
    }

    /**
     * This method is used to check twitter account login
     */
    private void checkTwitterLogin() {

        sharedPreferences = getSharedPreferences(PREF_NAME, 0);
        boolean isLoggedIn = sharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
        if (isLoggedIn) {
            loginLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.VISIBLE);

            String username = sharedPreferences.getString(PREF_USER_NAME, "");
            userName.setText(getResources().getString(R.string.hello) + " " + username);
        } else {
            loginLayout.setVisibility(View.VISIBLE);
            shareLayout.setVisibility(View.GONE);

            Uri uri = getIntent().getData();

            if (uri != null && uri.toString().startsWith(callbackUrl)) {

                String verifier = uri.getQueryParameter(oAuthVerifier);

                try {

                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                    long userId = accessToken.getUserId();
                    final User user = twitter.showUser(userId);
                    final String username = user.getName();

                    saveTwitterInfo(accessToken);

                    loginLayout.setVisibility(View.GONE);
                    shareLayout.setVisibility(View.VISIBLE);
                    userName.setText(getString(R.string.hello) + " " + username);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method is used to get Beacon Data
     */
    private void getBeaconData() {
        beaconsData = getIntent().getParcelableArrayListExtra("BEACONDATA");
    }

    /**
     * This method is used to initialize Listeners
     */

    private void initListners() {
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
    }

    /**
     * This method is used to initialize views
     */
    private void initViews() {
        loginLayout = (RelativeLayout) findViewById(R.id.login_layout);
        shareLayout = (LinearLayout) findViewById(R.id.share_layout);
        baconData = (TextView) findViewById(R.id.beacon_data);
        userName = (TextView) findViewById(R.id.user_name);
    }

    /**
     * This method is used to initialize twitter Configurations
     */

    private void initTwitterConfigs() {
        consumerKey = getString(R.string.twitter_consumer_key);
        consumerSecret = getString(R.string.twitter_consumer_secret);
        callbackUrl = getString(R.string.twitter_callback);
        oAuthVerifier = getString(R.string.twitter_oauth_verifier);
    }


    /**
     * This method is used to Save Twitter Information in Shared Prefrence
     */
    private void saveTwitterInfo(AccessToken accessToken) {

        long userId = accessToken.getUserId();
        User user;
        try {
            user = twitter.showUser(userId);
            String username = user.getName();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            e.putString(PREF_USER_NAME, username);
            e.commit();

        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to Login In Twitter
     */
    private void loginToTwitter() {

        boolean isLoggedIn = sharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);

        if (!isLoggedIn) {
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(consumerKey);
            builder.setOAuthConsumerSecret(consumerSecret);

            final Configuration configuration = builder.build();
            final TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter.getOAuthRequestToken(callbackUrl);

                final Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
                startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            loginLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            String verifier = data.getExtras().getString(oAuthVerifier);

            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                long userId = accessToken.getUserId();
                final User user = twitter.showUser(userId);
                String username = user.getName();

                saveTwitterInfo(accessToken);

                loginLayout.setVisibility(View.GONE);
                shareLayout.setVisibility(View.VISIBLE);

                userName.setText(MainActivity.this.getResources().getString(R.string.hello)
                        + " " + username);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login: {
                if (ConnectionManger.hasInternet(getApplicationContext())) {
                    loginToTwitter();
                } else {
                    Toast.makeText(this, "You Don't Have Working Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case R.id.btn_share:
                if (ConnectionManger.hasInternet(getApplicationContext())) {
                    Beacon beacon = beaconsData.get(0);
                    String status = "MAC :" + beacon.getMacAddress().toStandardString() + "\n" + " RSSI :" + beacon.getRssi() + "\n" + " MPower :" + beacon.getMeasuredPower() + "\n" + " Minor :" + beacon.getMinor();

                    if (status.trim().length() > 0) {
                        new updateTwitterStatus().execute(status);
                    } else {
                        Toast.makeText(this, "Message is empty!!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "You Don't Have Working Internet Connection", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * This method is used to Twitt the Beacons data
     */
    class updateTwitterStatus extends AsyncTask<String, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Posting to Twitter...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            String status = params[0];

            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(consumerKey);
                builder.setOAuthConsumerSecret(consumerSecret);
                String access_token = sharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                String acces_token_secret = sharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");
                AccessToken accessToken = new AccessToken(access_token, acces_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
                StatusUpdate statusUpdate = new StatusUpdate(status);
                twitter4j.Status response = twitter.updateStatus(statusUpdate);

            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            pDialog.dismiss();
            Toast.makeText(MainActivity.this, "Posted to Twitter!", Toast.LENGTH_SHORT);
            finish();
        }
    }
}
