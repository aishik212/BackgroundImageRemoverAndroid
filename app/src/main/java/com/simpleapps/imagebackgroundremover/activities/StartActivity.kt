package com.simpleapps.imagebackgroundremover.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simpleapps.admaster.AdUtils
import com.simpleapps.imagebackgroundremover.BuildConfig
import com.simpleapps.imagebackgroundremover.MainActivity
import com.simpleapps.imagebackgroundremover.R
import com.simpleapps.imagebackgroundremover.databinding.StartActivityLayoutBinding

class StartActivity : AppCompatActivity() {
    private var startActivityLayoutBinding: StartActivityLayoutBinding? = null
    private var loadingTv: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivityLayoutBinding = StartActivityLayoutBinding.inflate(layoutInflater)
        val root = startActivityLayoutBinding?.root
        loadingTv = startActivityLayoutBinding?.loadingTv
        setContentView(root)
        val bannerAdmobAds = listOf(
            AdUtils.Companion.bannerAdObject(R.string.native_ad_id, "NATIVE_ALL")
        )
        val appOpenAdMobAds = listOf(R.string.appOpenHighID, R.string.appOpenMedID)
        AdUtils.initializeMobileAds(this, "", appOpenAdMobAds, bannerAdmobAds)
        {
            if (BuildConfig.DEBUG) {
                goToHomeAct()
            } else {
                AdUtils.showAppOpenAd(this, object : AdUtils.Companion.AppOpenListener {
                    override fun moveNext() {
                        val listOf = listOf(
                            AdUtils.Companion.SplashAdObject(R.string.rins_high_ad_id, "HIGH"),
                            AdUtils.Companion.SplashAdObject(R.string.rins_med_ad_id, "MED"),
                            AdUtils.Companion.SplashAdObject(R.string.rins_all_ad_id, "LOW")
                        )
                        AdUtils.loadSplashAD(this@StartActivity,
                            listOf,
                            object : AdUtils.Companion.SplashAdListener {
                                override fun moveNext() {
                                    goToHomeAct()
                                }
                            })
                    }
                })
            }
        }
    }

    private fun goToHomeAct() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


}