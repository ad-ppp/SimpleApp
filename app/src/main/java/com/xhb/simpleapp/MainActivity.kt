package com.xhb.simpleapp

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.xiaoxin.permission.PermissionListener
import com.xiaoxin.permission.PermissionsUtil

class MainActivity : AppCompatActivity(), PermissionListener {
    override fun permissionDenied(p0: Array<out String>) {
        AlertDialog.Builder(this)
            .setMessage("请授权摄像头权限")
            .setCancelable(false)
            .setPositiveButton(
                "确定"
            ) { _, _ ->
                PermissionsUtil
                    .requestPermission(this@MainActivity, this@MainActivity, Manifest.permission.CAMERA)
            }.show()
    }

    override fun permissionGranted(p0: Array<out String>) {
        startActivity(Intent(this, QRActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isTaskRoot) {
            val intent = intent
            if (intent != null) {
                val action = intent.action
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == action) {
                    finish()
                }
            }
        } else {
            PermissionsUtil.requestPermission(this, this, Manifest.permission.CAMERA)
        }
    }
}