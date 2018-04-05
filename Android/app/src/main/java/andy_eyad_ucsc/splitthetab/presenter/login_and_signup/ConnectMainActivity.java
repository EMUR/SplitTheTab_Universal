package andy_eyad_ucsc.splitthetab.presenter.login_and_signup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import andy_eyad_ucsc.splitthetab.R;

public class ConnectMainActivity extends AppCompatActivity {
    String user_stripe;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_main);
        final Intent data = new Intent();

        WebView myWebView = findViewById(R.id.webview);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                int startIndex = url.indexOf("ac_");

                if (startIndex > 0) {
                    int endIndex = url.indexOf("&state=");

                    if (endIndex > 0) {
                        user_stripe = url.substring(startIndex, endIndex);
                        data.putExtra("userStripe",user_stripe);
                        setResult(RESULT_OK,data);
                        finish();

                    } else {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return false;
            }
        });

        myWebView.loadUrl("https://connect.stripe.com/express/oauth/authorize?redirect_uri=https://stripe.com/connect/default/oauth/test&client_id=ca_Bo2VATsQS7g1EMN5B5ZewpTDJvkhrKih&state={STATE_VALUE}");
    }
}
