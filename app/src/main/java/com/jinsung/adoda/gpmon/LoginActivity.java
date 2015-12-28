package com.jinsung.adoda.gpmon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
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

import org.apache.http.cookie.Cookie;

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
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.setWebViewClient(new WebClient());

        // 쿠키 즉시 싱크를 위한 싱크매니저 등록.
        CookieSyncManager.createInstance(getApplicationContext());
        CookieManager.getInstance().setAcceptCookie(true);

        LogCookie("onCreate", null);

        // 세션 체크를 한다.
        mWebView.postUrl(
                "https://mlogin.plaync.com/login/refresh",
                (
                        "return_url=http%3A%2F%2F127.0.0.1%2Flogin%2Fcheck%2Fsuccess" +
                                "&err_return_url=http%3A%2F%2F127.0.0.1%2Flogin%2Fcheck%2Ferror"
                ).getBytes()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 쿠키 즉시 싱크 시작
        CookieSyncManager.getInstance().startSync();

        LogCookie("onResume", null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LogCookie("onPause", null);

        // 쿠키 즉시 싱크 중지
        CookieSyncManager.getInstance().stopSync();
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

    private void LogCookie (String where, String url) {
        String tag = String.format(
            "%s-%s",
            "LoginWebCookie", where
        );
        String cookie = CookieManager.getInstance().getCookie(
            "https://mlogin.plaync.com"
        ).toString();
        if (null != url)
            Log.v(tag, "URL=" + url);
        Log.v(tag, cookie);
    }

    public class WebClient extends WebViewClient {

        @Override
        public  void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogCookie("shouldOverrideUrlLoading", url);

            if (url.startsWith("https://mlogin.plaync.com")) {
                if (url.contains("/login/error")) {
                    // 로그인 페이지의 명시적인 오류 처리ㄴ
                    return false;
                }

                // 나머지는 로그인웹에서 핸들링하도록 맡긴다.
                return false;
            }
            else if (url.startsWith("http://127.0.0.1")) {
                // 여기로 들어온 것은, 앱에서 핸들링하겠다는 의미이다. 127.0.0.1은 아무런 의미가 없다.

                if (url.contains("/login/check/success")) {
                    // 세션 체크에 성공했다. 다음 화면으로 넘어간다.
                    Toast.makeText(getApplicationContext(), "Already logged in.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MachinesActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                else if (url.contains("/login/success")) {
                    // 로그인에 성공했다. 다음 화면으로 넘어간다.
                    Toast.makeText(getApplicationContext(), "Logged in.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MachinesActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                else if (url.contains("/login/check/error")) {
                    // 로그인이 필요하다는 안내 토스트를 띄운다.
                    Toast.makeText(getApplicationContext(), "Please login.", Toast.LENGTH_SHORT).show();
                }

                // 로그인 실패 혹은 비정상적인 접근이다. 로그인 화면으로 넘어간다.
                view.loadUrl("https://mlogin.plaync.com/login/signin?return_url=http%3A%2F%2F127.0.0.1%2Flogin%2Fsuccess");
                return true;
            }

            // 그 외에는 웹에서 핸들링하도록 맡긴다.
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // 하나의 페이지 전환이 일어날 때마다, 쿠키를 즉시 싱크하도록 한다.
            CookieSyncManager.getInstance().sync();

            LogCookie("onPageFinished", url);
        }
    }
}
