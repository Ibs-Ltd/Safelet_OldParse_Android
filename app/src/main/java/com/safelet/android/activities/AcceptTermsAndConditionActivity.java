package com.safelet.android.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;

/**
 * Activity for ForgotPassword Screen
 *
 * @author alin
 */
public class AcceptTermsAndConditionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_terms_and_condition_layout);

        Toolbar toolbar = findViewById(R.id.safelet_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Accept terms and conditions");
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        WebView wvLoadURL = findViewById(R.id.wvLoadURL);
        wvLoadURL.getSettings().setJavaScriptEnabled(true);
        final Activity activity = this;
        wvLoadURL.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });

//        wvLoadURL.loadUrl("https://safelet.com/terms-of-service/");
        wvLoadURL.loadUrl("https://d11j0nlr3gbblw.cloudfront.net/");
    }
}