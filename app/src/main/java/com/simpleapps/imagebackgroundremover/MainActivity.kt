package com.simpleapps.imagebackgroundremover

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayoutMediator
import com.simpleapps.imagebackgroundremover.databinding.ActivityMainBinding
import com.simpleapps.imagebackgroundremover.fragments.ConverterFragment
import com.simpleapps.imagebackgroundremover.fragments.GalleryFragment
import com.simpleapps.imagebackgroundremover.utilities.adUtils


class MainActivity : AppCompatActivity() {


    companion object {
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
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d("texts", "getPermission: GRANTED")
                } else {
                    Log.d("texts", "getPermission: REJECTED")
                }
            }
        MobileAds.initialize(applicationContext) {
            adUtils.showNativeAd(inflate.adView,
                this@MainActivity)
        }
        val tabLayout = inflate.tabLayout
        val viewPager = inflate.viewpager
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val i = iconList[position]
            tab.icon = getDrawable(iconList[position])
            tab.text = titleList[position]
        }.attach()
    }

    var iconList = listOf(R.drawable.ic_baseline_home_24,
        com.github.drjacky.imagepicker.R.drawable.ic_photo_black_48dp)
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