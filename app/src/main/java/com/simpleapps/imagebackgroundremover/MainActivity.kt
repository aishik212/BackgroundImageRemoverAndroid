package com.simpleapps.imagebackgroundremover

import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.simpleapps.admaster.AdUtils.Companion.showAdFromChoices
import com.simpleapps.imagebackgroundremover.databinding.ActivityMainBinding
import com.simpleapps.imagebackgroundremover.databinding.IntersAdLoadingLayoutBinding
import com.simpleapps.imagebackgroundremover.fragments.ConverterFragment
import com.simpleapps.imagebackgroundremover.fragments.GalleryFragment
import com.simpleapps.imagebackgroundremover.utilities.AdUtils.Companion.downloadInterstitialAd
import com.simpleapps.imagebackgroundremover.utilities.utils
import com.suddenh4x.ratingdialog.AppRating
import com.suddenh4x.ratingdialog.preferences.MailSettings
import com.suddenh4x.ratingdialog.preferences.RatingThreshold
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    var adLoadingDialog: AlertDialog? = null
    override fun onBackPressed() {
//        super.onBackPressed()
        val builder2 = AlertDialog.Builder(this, R.style.DialogTheme)
        val adLoadingLayoutBinding =
            IntersAdLoadingLayoutBinding.inflate(layoutInflater)
        builder2.setView(adLoadingLayoutBinding.root)
        adLoadingDialog = builder2.create()
        adLoadingDialog?.show()
        Handler(Looper.getMainLooper()).postDelayed({
            if (downloadInterstitialAd != null) {
                downloadInterstitialAd?.fullScreenContentCallback = object :
                    FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        finish()
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        adLoadingDialog?.dismiss()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        finish()
                    }
                }
                downloadInterstitialAd?.show(this) {}
            } else {
                adLoadingDialog?.dismiss()
                finish()
            }
        }, 2000)
    }

    companion object {
        var ratingDialog: AppRating.Builder? = null

        lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
        fun hasPermissions(s: String, context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context,
                s) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = ActivityMainBinding.inflate(layoutInflater)
        setContentView(inflate.root)
        if (!BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d("texts", "getPermission: GRANTED")
                } else {
                    Log.d("texts", "getPermission: REJECTED")
                }
            }
//        AdUtils.showNativeAd(inflate.adView,
//            this@MainActivity)
        showAdFromChoices(inflate.adView, this, lifecycle)
        val tabLayout = inflate.tabLayout
        val viewPager = inflate.viewpager
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.icon = getDrawable(iconList[position])
//            tab.text = titleList[position]
        }.attach()
        val mailSettings =
            MailSettings("simpleappsofficial@gmail.com",
                "Bug Report BGRemover", "Please Enter Your Feedback Here \n")
        val bundle = Bundle()
        ratingDialog = AppRating.Builder(this)
            .setMinimumLaunchTimes(1)
            .setMinimumDays(0)
            .setMinimumLaunchTimesToShowAgain(0)
            .setMinimumDaysToShowAgain(0)
            .setShowOnlyFullStars(true)
            .setMailSettingsForFeedbackDialog(mailSettings)
            .setRateLaterButtonClickListener {
                bundle.putString(utils.Companion.EventKeys.RATING.name, "RATE LATER")
                utils.logEvent(this, utils.Companion.EventKeys.RATING, bundle)
            }
            .setNoFeedbackButtonClickListener {
                bundle.putString(utils.Companion.EventKeys.RATING.name, "NO FEEDBACK")
                utils.logEvent(this, utils.Companion.EventKeys.RATING, bundle)
            }
            .setConfirmButtonClickListener { userRating ->
                bundle.putString(utils.Companion.EventKeys.RATING.name,
                    "RATE - ${userRating.roundToInt()}")
                utils.logEvent(this, utils.Companion.EventKeys.RATING, bundle)
            }.setGoogleInAppReviewCompleteListener {
                bundle.putString(utils.Companion.EventKeys.RATING.name,
                    "REVIEWED_DIALOG")
                utils.logEvent(this, utils.Companion.EventKeys.RATING, bundle)
            }
            .setRatingThreshold(RatingThreshold.FOUR)
            .setCustomTheme(R.style.MyAlertDialogTheme)
    }

    var iconList = listOf(R.drawable.ic_baseline_home_24, R.drawable.ic_baseline_photo_library_24)
    var titleList = listOf("Home", "Saved Images")


    public class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return ConverterFragment()
                1 -> return GalleryFragment()
            }
            return ConverterFragment()
        }
    }

}