package com.simpleapps.admaster

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.browser.customtabs.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.google.firebase.perf.metrics.Trace
import kotlin.math.floor

@SuppressLint("MissingPermission")
class AdUtils {
    companion object {
        interface adShown {
            fun goNext()
            fun adFailed()
        }

        interface AdInterface {
            fun AdDismissed()
        }


        var bannerAdmobAds: List<bannerAdObject>? = null
        var appOpenAdMobAds: List<Int>? = null

        fun initializeMobileAds(
            activity: Activity,
            testAdId: String,
            appOpenAdMobAds: List<Int>,
            bannerAdmobAds: List<bannerAdObject>,
            onInitializationCompleteListener: OnInitializationCompleteListener,
        ) {
            MobileAds.initialize(activity, onInitializationCompleteListener)
            val testDeviceIds = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(testAdId)).build()
            MobileAds.setRequestConfiguration(testDeviceIds)
            this.appOpenAdMobAds = appOpenAdMobAds
            this.bannerAdmobAds = bannerAdmobAds
        }


        @JvmField
        var bought = 0

        @JvmField
        var TestRewardedInterstitialAd: RewardedInterstitialAd? = null
        var adKinowaTimeinSecs: Long = 30

        fun loadStartAdByType(activity: Activity, adList: List<Int>) {
            loadRInterAd(activity, 0, adList)
        }


        private fun loadRInterAd(activity: Activity, i: Int, adList: List<Int>) {
            if (bought == 0 && (adList.size < i)) {
                when (i) {
                    0 -> {
                        loadRAD(
                            activity,
                            activity.getString(adList[i]),
                            (i + 1),
                            "RINS_HIGH",
                            adList
                        )
                    }
                    1 -> {
                        loadRAD(
                            activity,
                            activity.getString(adList[i]),
                            (i + 1),
                            "RINS_MED",
                            adList
                        )
                    }
                    2 -> {
                        loadRAD(
                            activity,
                            activity.getString(adList[i]),
                            (i + 1),
                            "RINS_ALL",
                            adList
                        )
                    }
                }
            }
        }

        private fun loadRAD(
            activity: Activity,
            adId: String,
            i: Int,
            type: String,
            adList: List<Int>,
        ) {
            RewardedInterstitialAd.load(
                activity,
                adId,
                AdRequest.Builder().build(),
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                        super.onAdLoaded(rewardedInterstitialAd)
                        logAdResult(type + "_load", null, null, activity)
                        TestRewardedInterstitialAd = rewardedInterstitialAd
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        logAdResult(null, type, loadAdError.message, activity)
                        TestRewardedInterstitialAd = null
                        loadRInterAd(activity, i, adList)
                    }
                })
        }

        fun loadSimpleInters(
            activity: Activity?,
            adLoadingView: View?,
            adList: List<Int>,
        ) {
            if (bought == 0 && activity != null) {
                if (adLoadingView != null) {
                    adLoadingView.visibility = VISIBLE
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        if (adLoadingView != null) {
                            adLoadingView.visibility = GONE
                        }
                        val shown =
                            activity.window.decorView.rootView.isShown
                        if (shown) {
                            TestRewardedInterstitialAd?.show(activity) { }
                            loadStartAdByType(
                                activity,
                                adList
                            )
                        }
                        if (adLoadingView != null) {
                            adLoadingView.visibility = GONE
                        }
                    } catch (e: java.lang.Exception) {
                        TestRewardedInterstitialAd?.show(activity) { }
                    }
                }, 2000)
            } else {
                if (adLoadingView != null) {
                    adLoadingView.visibility = GONE
                }
            }
        }


        fun adLoadingMessage(frameLayout: FrameLayout, activity: Activity) {
            activity.runOnUiThread {
                frameLayout.removeAllViews()
                val inflate =
                    activity.layoutInflater.inflate(R.layout.bottom_ad_loading_layout, null)
                inflate.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                frameLayout.addView(
                    inflate
                )
                frameLayout.visibility = VISIBLE
            }
        }

        fun showInGrownBannerAd(
            frameLayout: FrameLayout,
            activity: Activity,
        ) {
            val imageView = ImageView(activity)
            var inGrownNotify = R.drawable.in_grown_notify
            val roundNum = floor(Math.random() * 6.0).toInt()
            var url = "https://notificationshortcuts.page.link/notifApp"
            var s = "NOTIF"
            val s1 = "INGROWN_BANNER"
//            checkSpread()
            frameLayout.visibility = VISIBLE
            adLoadingMessage(frameLayout, activity)
            when (roundNum) {
                1 -> {
                    inGrownNotify = R.drawable.in_grown_notify
                    url = "https://notificationshortcuts.page.link/notifApp"
                    loadAdImage(activity, inGrownNotify, imageView, frameLayout, url, s1, s)
                }
                2 -> {
                    inGrownNotify = R.drawable.in_grown_notify2
                    url = "https://notificationshortcuts.page.link/notifApp"
                    loadAdImage(activity, inGrownNotify, imageView, frameLayout, url, s1, s)
                }
                3 -> {
                    s = "GAME_ZOP"
                    loadAdLayout(
                        activity,
                        R.layout.game_layout_1,
                        frameLayout,
                        s1,
                        s
                    )
                }
                4 -> {
                    s = "GAME_ZOP"
                    loadAdLayout(
                        activity,
                        R.layout.game_layout_2,
                        frameLayout,
                        s1,
                        s
                    )
                }
                5 -> {
                    s = "GAME_ZOP"
                    loadAdLayout(
                        activity,
                        R.layout.game_layout_3,
                        frameLayout,
                        s1,
                        s
                    )
                }
            }
        }

        private fun loadAdLayout(
            activity: Activity,
            layout: Int,
            frameLayout: FrameLayout,
            s1: String,
            s: String,
        ) {

            val inflate = activity.layoutInflater.inflate(layout, null)
            frameLayout.removeAllViews()
            val view = inflate.rootView.findViewById<CardView>(R.id.game_button)
            view.setOnClickListener {
                logAdResult("Click_" + s1 + "_" + s, null, null, activity)
                startGamezopActivity(activity)
            }
            logAdResult(s1 + "_" + s, null, null, activity)
            frameLayout.addView(view)
        }

        val gameZopUrl = "https://www.gamezop.com/?id=3759"
        var builder1: CustomTabsIntent.Builder? = null

        private fun startGamezopActivity(activity: Activity) {
            if (builder1 == null) {
                initCustomtab(activity)
            }
            if (builder1 != null) {
                try {
                    val customTabsIntent: CustomTabsIntent = builder1!!.build()
                    customTabsIntent.launchUrl(activity, Uri.parse(gameZopUrl))
                } catch (e: Exception) {
                    val uri = Uri.parse(gameZopUrl)
                    val i1 = Intent(Intent.ACTION_VIEW)
                    i1.data = uri
                    startA(activity, i1)
                }
            } else {
                val uri = Uri.parse(gameZopUrl)
                val i1 = Intent(Intent.ACTION_VIEW)
                i1.data = uri
                startA(activity, i1)
            }
        }


        fun loadAdImage(
            activity: Activity,
            inGrownNotify: Int,
            imageView: ImageView,
            frameLayout: FrameLayout,
            url: String,
            s1: String,
            s: String,
        ) {
            Glide.with(activity)
                .load(ContextCompat.getDrawable(activity, inGrownNotify))
                .into(imageView)
            frameLayout.removeAllViews()
            imageView.setOnClickListener {
                logAdResult("Click_" + s1 + "_" + s, null, null, activity)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data =
                    Uri.parse(url)
                startA(activity, intent)
            }
            imageView.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            logAdResult(s1 + "_" + s, null, null, activity)
            frameLayout.addView(imageView)
        }


        private fun checkSpread() {
            val hmap: HashMap<Int, Long> = hashMapOf()
            var i = 0
            while (i < 12000) {
                val key = (floor(Math.random() * 6.0).toInt())
                val l = hmap[key] ?: 0
                hmap[key] = l + 1
                i++
            }
        }

        @SuppressLint("MissingPermission")
        fun logAdResult(
            adType: String?,
            errorAdType: String?,
            error: String?,
            activity: Activity?,
        ) {
//            Log.d("texts", "logAdResult: "+adType+" "+errorAdType)
            var error = error
            val b = Bundle()
            try {
                error = error!!.substring(0, 25)
            } catch (e: java.lang.Exception) {
            }
            if (adType != null) {
                b.putString("AdType", adType)
            }
            if (error != null && errorAdType != null) {
                b.putString("Error", errorAdType)
                b.putString("ErrorMessage", "$errorAdType $error")
            }
//            Log.d("texts", "logAdResult: $AdType $errorAdType $error ")
            if (activity != null) {
                FirebaseAnalytics.getInstance(activity).logEvent("AdLog", b)
                if (adType != null && adType == "AdKinowa") {
                    val bundle = Bundle()
                    bundle.putString("AdKinowa", "Success")
                    FirebaseAnalytics.getInstance(activity).logEvent("AdKinowa", bundle)
                }
            }
        }


        var mClient: CustomTabsClient? = null

        private fun initCustomtab(activity: Activity) {
            CustomTabsClient.bindCustomTabsService(
                activity,
                "com.android.chrome",
                object : CustomTabsServiceConnection() {
                    override fun onCustomTabsServiceConnected(
                        name: ComponentName,
                        client: CustomTabsClient,
                    ) {
                        // mClient is now valid.
                        try {
                            mClient = client
                            mClient!!.warmup(0)
                            val session = mClient!!.newSession(CustomTabsCallback())
                            builder1 = initBuilder(activity, session)
                            session!!.mayLaunchUrl(Uri.parse(gameZopUrl), null, null)
                        } catch (e: Exception) {
                            mClient = null
                        }

                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        mClient = null
                    }
                })
            CustomTabsClient.connectAndInitialize(activity, activity.packageName)

        }

        private fun initBuilder(
            activity: Activity,
            session: CustomTabsSession?,
        ): CustomTabsIntent.Builder {
            val builder = CustomTabsIntent.Builder()
            if (session != null) {
                builder.setSession(session)
            }
            builder.setStartAnimations(
                activity,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            builder.setExitAnimations(
                activity,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            val colorInt = ContextCompat.getColor(activity, R.color.primaryColor) //red
            val defaultColors = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(colorInt)
                .build()
            builder.setDefaultColorSchemeParams(defaultColors)
            return builder
        }


        fun startA(activity: Activity, i: Intent) {
            if (i.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(i)
            } else {
                toast(activity, "Cannot Find Suitable Application")
            }
        }

        fun toast(activity: Activity?, s: String?) {
            if (activity != null && !activity.isFinishing) {
                activity.runOnUiThread(Runnable {
                    try {
                        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                    }
                })
            }
        }


        var num = 0
        var appOpenCTD: CountDownTimer? = null
        var intersCTD: CountDownTimer? = null
        val l: Long = 7500
        var showAppOpenAd = true
        var showSplashAd = true

        interface AppOpenListener {
            fun moveNext()
        }

        fun showAppOpenAd(activity: Activity, openListener: AppOpenListener, trace: Trace? = null) {
            val preferences = activity.getSharedPreferences("options", 0)
            val appOpenCount = preferences.getInt("appOpen", 0)
            if (bought == 0) {
                trace?.start()
                try {
                    if (appOpenCTD == null) {
                        appOpenCTD = object : CountDownTimer(l, 500) {
                            override fun onTick(millisUntilFinished: Long) {

                            }

                            override fun onFinish() {
                                if (showAppOpenAd) {
                                    logAppOpen(null, "SKIP", "TIMEOUT", activity)
                                    showAppOpenAd = false
                                    trace?.putAttribute("AdType", "TIMEOUT")
                                    trace?.stop()
                                    openListener.moveNext()
                                    appOpenCTD = null
                                }
                            }
                        }
                    }
                    appOpenCTD?.cancel()
                    appOpenCTD?.start()
                } catch (e: Exception) {

                }
                try {
                    val adMobAds1 = appOpenAdMobAds
                    if (adMobAds1 != null && adMobAds1.size > num) {
                        val request = AdRequest.Builder().build()
                        when (num) {
                            0 -> {
                                trace?.putAttribute("AdType", "HIGH")
                                AppOpenAd.load(
                                    activity, activity.getString(adMobAds1[num]), request,
                                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                                    appOpenAdLoadCallback(activity, openListener, trace)
                                )
                            }
                            1 -> {
                                trace?.putAttribute("AdType", "MED")
                                AppOpenAd.load(
                                    activity,
                                    activity.getString(adMobAds1[num]),
                                    request,
                                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                                    appOpenAdLoadCallback(
                                        activity,
                                        openListener,
                                        trace
                                    )
                                )
                            }
                            2 -> {
                                trace?.putAttribute("AdType", "ALL")
                                AppOpenAd.load(
                                    activity,
                                    activity.getString(adMobAds1[num]),
                                    request,
                                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                                    appOpenAdLoadCallback(
                                        activity,
                                        openListener,
                                        trace
                                    )
                                )
                            }
                            3 -> {
                                trace?.putAttribute("AdType", "NONE")
                                trace?.stop()
                                openListener.moveNext()
                            }
                        }

                    } else {
                        appOpenCTD?.cancel()
                        openListener.moveNext()
                    }
                    num++
                } catch (e: Exception) {
                    openListener.moveNext()
                }
            } else {
                openListener.moveNext()
            }
            preferences.edit().putInt("appOpen", appOpenCount + 1).apply()
        }

        private fun appOpenAdLoadCallback(
            activity: Activity,
            openListener: AppOpenListener,
            trace: Trace?,
        ) =
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    logAppOpen("LOAD", null, null, activity)
                    if (showAppOpenAd) {
                        trace?.stop()
                        ad.show(activity)
                        appOpenCTD?.cancel()
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                logAppOpen(null, "SHOW_FAIL", p0.message, activity)
                                showAppOpenAd(
                                    activity,
                                    openListener,
                                    trace
                                )
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                logAppOpen("SHOWN", null, null, activity)
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                openListener.moveNext()
                            }
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    if (showAppOpenAd) {
                        logAppOpen(null, "FAIL", loadAdError.message, activity)
                        showAppOpenAd(
                            activity,
                            openListener,
                            trace
                        )
                    }
                }
            }


        fun logAppOpen(success: String?, fail: String?, failReason: String?, activity: Activity) {
            when (num - 1) {
                0 -> {
                    if (success != null) {
                        logAdResult("APPOPEN_HIGH_$success", null, null, activity)
                    } else if (fail != null) {
                        logAdResult(null, "APPOPEN_HIGH_$fail", failReason, activity)
                    }
                }
                1 -> {
                    if (success != null) {
                        logAdResult("APPOPEN_MED_$success", null, null, activity)
                    } else if (fail != null) {
                        logAdResult(null, "APPOPEN_MED_$fail", failReason, activity)
                    }
                }
                2 -> {
                    if (success != null) {
                        logAdResult("APPOPEN_ALL_$success", null, null, activity)
                    } else if (fail != null) {
                        logAdResult(null, "APPOPEN_ALL_$fail", failReason, activity)
                    }
                }
            }
        }

        interface SplashAdListener {
            fun moveNext()
        }

        data class SplashAdObject(
            val id: Int,
            val type: String,
        )

        data class bannerAdObject(
            val id: Int,
            val type: String,
        )


        fun loadSplashAD(
            activity: Activity,
            adList: List<SplashAdObject>,
            splashAdListener: SplashAdListener,
            num: Int = 0,
        ) {
            val trace = Firebase.performance.newTrace("SplashAdLoad")
            if (bought == 0) {
                try {
                    trace.start()
                    if (intersCTD == null) {
                        intersCTD = object : CountDownTimer((l / 2), 500) {
                            override fun onTick(millisUntilFinished: Long) {
                            }

                            override fun onFinish() {
                                if (showSplashAd) {
                                    logAppOpen(null, "SKIP", "TIMEOUT", activity)
                                    showSplashAd = false
                                    trace.putAttribute("AdType", "TIMEOUT")
                                    trace.stop()
                                    splashAdListener.moveNext()
                                    intersCTD = null
                                }
                            }
                        }
                    }
                    intersCTD?.cancel()
                    intersCTD?.start()
                } catch (e: Exception) {
                }
                if (num <= (adList.size - 1)) {
                    val splashAdObject = adList[num]
                    trace.putAttribute("AdType", splashAdObject.type)
                    if (showSplashAd) {
                        loadAndShowSplashAd(activity, splashAdObject, object : SplashAdListener {
                            override fun moveNext() {
                                intersCTD?.cancel()
                                if (showSplashAd) {
                                    loadSplashAD(
                                        activity,
                                        adList,
                                        splashAdListener,
                                        (num + 1)
                                    )
                                } else {
                                    intersCTD = null
                                    showSplashAd = false
                                    intersCTD?.cancel()
                                    splashAdListener.moveNext()
                                }
                            }
                        }, trace)
                    } else {
                        if (showSplashAd) {
                            trace.stop()
                            intersCTD?.cancel()
                            splashAdListener.moveNext()
                        }

                    }
                } else {
                    if (showSplashAd) {
                        trace.stop()
                        intersCTD?.cancel()
                        showSplashAd = false
                        splashAdListener.moveNext()
                    }

                }
            } else {
                if (showSplashAd) {
                    trace.stop()
                    intersCTD?.cancel()
                    splashAdListener.moveNext()
                }
            }

        }

        private fun loadAndShowSplashAd(
            activity: Activity,
            splashAdObject: SplashAdObject,
            splashAdListener: SplashAdListener,
            trace: Trace,
        ) {
            RewardedInterstitialAd.load(
                activity,
                activity.getString(splashAdObject.id),
                AdRequest.Builder().build(),
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                        super.onAdLoaded(rewardedInterstitialAd)
                        if (showSplashAd) {
                            intersCTD?.cancel()
                            logAdResult(
                                "INTERS_" + splashAdObject.type + "_load",
                                null,
                                null,
                                activity
                            )
                            showSplashAd = false
                            trace.putAttribute(
                                "AdType",
                                "SPLASH_" + splashAdObject.type + "_LOADED"
                            )
                            trace.stop()
                            rewardedInterstitialAd.show(activity) {

                            }
                            rewardedInterstitialAd.fullScreenContentCallback = object :
                                FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    splashAdListener.moveNext()
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    showSplashAd = true
                                    splashAdListener.moveNext()
                                }
                            }
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        if (showSplashAd) {
                            trace.putAttribute(
                                "AdType",
                                "SPLASH_" + splashAdObject.type + "_FAILED"
                            )
                            trace?.stop()
                            logAdResult(
                                null,
                                "INTERS_" + splashAdObject.type,
                                loadAdError.message,
                                activity
                            )
                            splashAdListener.moveNext()
                        }
                    }
                })

        }


        private fun showNativeAd(
            frameLayout: FrameLayout,
            activity: Activity,
            adId: String,
            type: Int?,
            adTypeLog: String,
        ) {
            val adLoader = AdLoader.Builder(activity, adId)
                .forNativeAd { nativeAd: NativeAd? ->
                    activity.runOnUiThread {
                        logAdResult(adTypeLog, null, null, activity)
                        val styles = NativeTemplateStyle.Builder().build()
                        val template = activity.layoutInflater.inflate(
                            R.layout.template_small_layout,
                            null
                        ).rootView as TemplateView
                        template.setStyles(styles)
                        template.setNativeAd(nativeAd)
                        frameLayout.removeAllViews()
                        frameLayout.visibility = VISIBLE
                        frameLayout.addView(template)
                    }
                }.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        logAdResult(null, adTypeLog, loadAdError.message, activity)
                        if (type != null) {
                            showRestOfAds(frameLayout, activity, type)
                        }
                    }
                })
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }

        fun showBannerAd(
            frameLayout: FrameLayout?,
            activity: Activity?,
            adId: String,
            type: Int?,
            logName: String,
        ) {
            if (frameLayout != null && activity != null) {
                if (bought == 0) {
                    val adView = AdView(activity.applicationContext)
                    adView.adUnitId = adId
                    loadBanner(adView, activity, frameLayout, type, logName)
                } else {
                    if (type != null) {
                        showRestOfAds(frameLayout, activity, type)
                    } else {
                        frameLayout.visibility = GONE
                    }
                }
            }
        }

        fun getAdSize(activity: Activity): AdSize {
            val display = activity.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val widthPixels = outMetrics.widthPixels.toFloat()
            val density = outMetrics.density
            val adWidth = (widthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
        }

        fun loadBanner(
            bannerAdView: AdView,
            activity: Activity?,
            frameLayout: FrameLayout,
            type: Int?,
            logName: String,
        ) {
            if (bought == 0) {
                val adRequest = AdRequest.Builder()
                    .build()
                val adSize = getAdSize(activity!!)
                try {
                    bannerAdView.setAdSize(adSize)
                    bannerAdView.loadAd(adRequest)
                    bannerAdView.adListener = object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            logAdResult(null, logName, loadAdError.message + "", activity)
                            if (type != null) {
                                showRestOfAds(frameLayout, activity, type)
                            }
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            logAdResult(logName + "_Clicked", null, null, activity)
                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            frameLayout.visibility = VISIBLE
                            frameLayout.removeAllViews()
                            frameLayout.addView(bannerAdView)
                            logAdResult(logName, null, null, activity)
                        }
                    }
                } catch (e: java.lang.Exception) {
                    logAdResult(null, logName, e.localizedMessage + "", activity)
                    if (type != null) {
                        showRestOfAds(frameLayout, activity, type)
                    }
                }
            }
        }

        fun showRestOfAds(frameLayout: FrameLayout, activity: Activity?, type: Int) {
            val bannerAdmobAds1 = bannerAdmobAds
            if (bought == 0 && activity != null && bannerAdmobAds1 != null) {
                if (bannerAdmobAds1.size > type) {
                    val bannerAdObject = bannerAdmobAds1[type]
                    when {
                        bannerAdObject.type.contains("BAN") -> {
                            showBannerAd(
                                frameLayout,
                                activity,
                                activity.getString(bannerAdObject.id),
                                type + 1,
                                bannerAdObject.type
                            )
                        }
                        bannerAdObject.type.contains("NATIVE") -> {
                            showNativeAd(
                                frameLayout,
                                activity,
                                activity.getString(bannerAdObject.id),
                                type + 1,
                                bannerAdObject.type
                            )
                        }
                    }
                } else {
                    showInGrownBannerAd(
                        frameLayout,
                        activity
                    )
                    logAdResult(null, "END", "BBH", activity)
                }
            } else {
                frameLayout.removeAllViews()
                frameLayout.visibility = GONE
            }
        }


    }
}