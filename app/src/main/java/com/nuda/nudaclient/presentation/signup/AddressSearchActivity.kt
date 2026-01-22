package com.nuda.nudaclient.presentation.signup

import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader
import com.nuda.nudaclient.R

class AddressSearchActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_address_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        webView = findViewById(R.id.webView)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        // WebView 설정
        webView.settings.apply {
            javaScriptEnabled = true  // JavaScript 활성화 필수
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
        }

        // Webview 터치 설정
        webView.isClickable = true
        webView.isFocusable = true
        webView.isFocusableInTouchMode = true

        webView.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                android.util.Log.d("AddressWebView", "JS: ${consoleMessage?.message()}")
                return true
            }
        }

        // JavaScript Interface 연결
        webView.addJavascriptInterface(WebAppInterface(), "Android")

        // WebViewClient 설정 (페이지 로딩 처리)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: android.webkit.WebResourceRequest
            ): android.webkit.WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }

        // HTML 로드
        webView.loadUrl("https://appassets.androidplatform.net/assets/daum_address.html")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 시 취소 처리
                setResult(RESULT_CANCELED)
                finish()
            }
        })
    }

    // JavaScript에서 호출할 Interface
    inner class WebAppInterface {
        @JavascriptInterface
        fun onAddressSelected(zonecode: String, address: String, buildingName: String) {
            // 주소 데이터를 Intent로 반환
            val intent = Intent().apply {
                putExtra("zonecode", zonecode)
                putExtra("address", address)
                putExtra("buildingName", buildingName)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }


}