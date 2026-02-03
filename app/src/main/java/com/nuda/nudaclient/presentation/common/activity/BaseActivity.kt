package com.nuda.nudaclient.presentation.common.activity

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nuda.nudaclient.R

open class BaseActivity : AppCompatActivity() {

    // 툴바 타이틀 설정 메소드
    fun setToolbarTitle(title: String) {
        findViewById<TextView>(R.id.tv_toolbar_title).text = title
    }

}
