package com.jinsung.adoda.gpmon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.CookieManager;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private WebView mWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWebView = new WebView(getApplicationContext());
        setContentView(mWebView);

        mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebClient());
        mWebView.loadUrl("https://mlogin.plaync.com/login/signin");
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();

        mWebView.stopLoading();
        ViewGroup webParent = (ViewGroup)mWebView.getParent();
        if (null != webParent) {
            webParent.removeView(mWebView);
        }
        mWebView.destroy();
    }

    public class WebClient extends WebViewClient {

        @Override
        public  void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("https://mlogin.plaync.com")) {
                // fail case : "https://mlogin.plaync.com/login/error?login_error_code=0002"
                // success case : "https://mlogin.plaync.com/login/crosscookie?return_url=http%3A%2F%2Fkr.plaync.com&user_id=F5546C67-8F38-4710-B7E6-E92C0F344429&persist_session_id=22d556b2401298a9591ece915de56a448faa5233defdb4e2742dbe598717ee812490fcc9fbbd55640d79d08cd9eab0ad&persist_session_secret=d60a4bd571658e01dab803f10f62dc50fa8a11db26be7e2ba3d4a410142e4b39431a32a15e9e6676290f6351d243f7ad2490fcc9fbbd55640d79d08cd9eab0ad&persist_session_expire_at=f2297618ec6f3581477fadd704b49ba1e67a666aff78b13ccc49708a0869a597&persistent=true"
                if (url.contains("/login/error"))
                    return false;

                if (url.contains("/login/crosscookie")) {
                    view.loadData("로그인 성공",  "text/html; charset=UTF-8", null);
                    return true;
                }

                // 나머지는 로그인 페이지
                view.loadUrl(url);
                return true;
            }

            return true; // not render
        }
    }
}
