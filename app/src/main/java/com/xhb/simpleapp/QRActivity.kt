package com.xhb.simpleapp

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.ding.statusbarlib.StatusBarCompat
import com.xfan.scannerlibrary.codec.CodeFormat
import com.xfan.scannerlibrary.utils.CameraUtil
import com.xfan.scannerlibrary.view.ScannerView
import com.xiaoxin.permission.PermissionsUtil
import kotlinx.android.synthetic.main.activity_scan_code.*

class QRActivity : AppCompatActivity(), ScannerView.OnScanListener {

    private var isLightingOn: Boolean = false
    private var isDecodeImage: Boolean = false//是否是要解码本地图片

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_code)
        StatusBarCompat.translucentStatusBar(this, false, false)

        scanner_view.setOnClickListener {
            setEnableScan(true)
            //摄头已经释放，需要重新获取
            scanner_view.handleCamera(CameraUtil.getDefaultCameraId()) { result ->
                if (result) {
                    scanner_view.startScan()
                    switchLight(isLightingOn)
                }
            }
            isDecodeImage = false
        }

        scanner_view.setOnScanResultListener(this)
        btn_light.setOnClickListener { switchLight(!isLightingOn) }

        tv_img_reg_result.visibility = View.GONE
        scanner_view.isClickable = false
        scanner_view.isContinuous = true
    }

    override fun onScanResult(result: String?, format: CodeFormat?) {
        onScanSuccess(result ?: "")
    }

    override fun onBrightnessIsDark(isDark: Boolean) {
        if (isDark) {
            btn_light.visibility = View.VISIBLE
        } else if (!isLightingOn) {
            btn_light.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (isDecodeImage) return //如果是解码本地图片，不开启摄像头扫描
        if (PermissionsUtil.hasPermission(this, Manifest.permission.CAMERA)) {
            scanner_view.handleCamera(CameraUtil.getDefaultCameraId()) { result ->
                if (result) {
                    scanner_view.startScan()
                    switchLight(isLightingOn)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        scanner_view.stopScan()
        scanner_view.releaseCamera()
    }

    private fun switchLight(isLightOn: Boolean) {
        isLightingOn = isLightOn
        if (isLightingOn) {
            scanner_view.turnOnLight()
            (btn_light.getChildAt(0) as ImageView).setImageResource(R.drawable.light_open)
            (btn_light.getChildAt(1) as TextView).text = getString(R.string.flash_light_close)
        } else {
            scanner_view.turnOffLight()
            (btn_light.getChildAt(0) as ImageView).setImageResource(R.drawable.light_close)
            (btn_light.getChildAt(1) as TextView).text = getString(R.string.flash_light_open)

            btn_light.visibility = View.GONE
        }
    }

    /**
     * 二维码扫描成功
     *
     * @param result
     */
    private fun onScanSuccess(result: String) {
        vibrate()
        Log.d("QRActivity", "onScanSuccess:$result")
        for (s in BuildConfig.URL_ARRAY) {
            if (result.contains(s)) {
                WebActivity.launch(this, result)
                return
            }
        }

        Toast.makeText(this, "不是正确有效的二维码，请重新扫码！", Toast.LENGTH_SHORT).show()
    }

    private fun setEnableScan(isEnable: Boolean) {
        tv_img_reg_result.visibility = if (!isEnable) View.VISIBLE else View.GONE
        scanner_view.isClickable = !isEnable
        btn_light.visibility = View.GONE
    }

    /**
     * 扫码成功后，震动提示
     */
    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        vibrator?.vibrate(200)
    }
}