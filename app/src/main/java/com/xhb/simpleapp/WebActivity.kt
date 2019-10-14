package com.xhb.simpleapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebSettings
import com.ding.statusbarlib.StatusBarCompat
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.xhb.xhbwebview.AdvancedWebView
import com.xiaoxin.permission.PermissionListener
import com.xiaoxin.permission.PermissionsUtil
import kotlinx.android.synthetic.main.activity_web.*
import org.json.JSONObject


class WebActivity : AppCompatActivity(), AdvancedWebView.Listener {
    private lateinit var url: String
//    private val url = "https://ceping-test.xiaoheiban.cn/entrance-assessment?exam_id=52"
//    private val url = "http://h5.eqxiu.com/ls/WpYaintM"

    companion object {
        private const val KEY_URL = "KEY_URL"

        @JvmStatic
        fun launch(activity: Activity, url: String) {
            val intent = Intent(activity, WebActivity::class.java)
            intent.putExtra(KEY_URL, url)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        StatusBarCompat.translucentStatusBar(this, false, false)

        url = intent.getStringExtra(KEY_URL)

        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.mediaPlaybackRequiresUserGesture = false

        webView.setListener(this, this)
        webView.loadUrl(url)
        webView.registerHandler(
            "backToScan"
        ) { _, _ -> this@WebActivity.finish() }


        // download test
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            Log.d(
                "WebActivity", "download start [url:$url][userAgent:$userAgent]" +
                        "[contentDisposition:$contentDisposition][mimetype:$mimetype]" +
                        "[contentLength:$contentLength]"
            )

            // 预览 pdf 设置
            if (mimetype.contains("officedocument")) {
                Log.d("WebActivity", "office-document")
            }
        }
        PermissionsUtil.requestPermission(this, object : PermissionListener {
            override fun permissionGranted(p0: Array<out String>) {

            }

            override fun permissionDenied(p0: Array<out String>) {

            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    @SuppressLint("NewApi")
    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        webView.onActivityResult(requestCode, resultCode, intent)
    }

    override fun onBackPressed() {
        if (!webView.onBackPressed()) {
            return
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onPageStarted(url: String, favicon: Bitmap?) {}

    override fun onPageFinished(url: String) {}

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {}

    override fun onDownloadRequested(
        url: String,
        suggestedFilename: String,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {
    }

    override fun onExternalPageRequest(url: String) {}

    private class CPentranceHandler(val activity: WebActivity) : BridgeHandler {

        override fun handler(data: String?, function: CallBackFunction?) {
            if (data != null) {
                val jsonObject = JSONObject(data)

                val reqId = jsonObject.getString("reqId")
                val module = jsonObject.getString("module")
                val event = jsonObject.getString("event")
                val params = jsonObject.getJSONObject("params")

                // answer complete
                if (event != null && event == "backToScan") {
                    activity.finish()
                }
            }
        }
    }
}