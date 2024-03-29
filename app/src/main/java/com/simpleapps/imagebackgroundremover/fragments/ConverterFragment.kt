package com.simpleapps.imagebackgroundremover.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.simpleapps.imagebackgroundremover.MainActivity.Companion.hasPermissions
import com.simpleapps.imagebackgroundremover.MainActivity.Companion.ratingDialog
import com.simpleapps.imagebackgroundremover.MainActivity.Companion.requestPermissionLauncher
import com.simpleapps.imagebackgroundremover.R
import com.simpleapps.imagebackgroundremover.databinding.DownloadConfirmationLayoutBinding
import com.simpleapps.imagebackgroundremover.databinding.FragmentConverterBinding
import com.simpleapps.imagebackgroundremover.databinding.IntersAdLoadingLayoutBinding
import com.simpleapps.imagebackgroundremover.utilities.AdUtils.Companion.downloadInterstitialAd
import com.simpleapps.imagebackgroundremover.utilities.AdUtils.Companion.loadTestStartAd
import com.simpleapps.imagebackgroundremover.utilities.utils
import com.slowmac.autobackgroundremover.BackgroundRemover
import com.slowmac.autobackgroundremover.DownloadListener
import com.slowmac.autobackgroundremover.OnBackgroundChangeListener
import com.slowmac.autobackgroundremover.SaveBitmapTask
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt


class ConverterFragment : Fragment() {

    lateinit var inflate: FragmentConverterBinding
    var scale: Bitmap? = null
    lateinit var target: File
    lateinit var FragContext: Context
    lateinit var FragActivity: Activity
    var tolerance: Int = 50
    var bgColor: Int = Color.WHITE
    var bgColorHex: String = "#FFFFFF"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        inflate = FragmentConverterBinding.inflate(layoutInflater)
        val context: Context? = context
        if (context != null) {
            FragContext = context
        }
        val activity: Activity? = activity
        if (activity != null) {
            FragActivity = activity
        }
        return inflate.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                try {
                    val fileFromUri = getFileFromUri(FragContext, uri)
                    target =
                        File("${FragContext.cacheDir.absoluteFile}/images/${fileFromUri?.name}")
                    target.parentFile.mkdirs()
                    target.delete()
                    hideAddImageView()
                    inflate.newimg.visibility = View.GONE
                    if (target.createNewFile()) {
                        Glide.with(inflate.oldimg)
                            .load(Uri.fromFile(fileFromUri?.copyTo(target, true)))
                            .into(inflate.oldimg)
                    } else {
                        Toast.makeText(context, "Some Error Occured", Toast.LENGTH_SHORT).show()
                        Glide.with(inflate.oldimg)
                            .load(Uri.fromFile(fileFromUri?.copyTo(target, true)))
                            .into(inflate.oldimg)
                    }
                    if (target.exists()) {
                        Glide.with(inflate.oldimg).load(Uri.fromFile(target)).into(inflate.oldimg)
                        inflate.oldimg.setImageURI(Uri.fromFile(target))
                        val decodeFile = BitmapFactory.decodeFile(target.absolutePath)
                        var width = decodeFile.width
                        var height = decodeFile.height
                        val i = 1000
                        while (max(width, height) > i) {
                            width /= 2
                            height /= 2
                        }
                        scale = decodeFile.scale(width, height, true)
                        scale = reduceOutputImageSize(decodeFile, 1000)
                        hideAddImageView()
                        inflate.newimg.setImageBitmap(scale)
                        tolerance = 255 / 2
                        val seekBar = inflate.seekBar
                        seekBar.visibility = View.VISIBLE
                        seekBar.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                                inflate.progressTv.text =
                                    "Detection Sensitivity ${((p0?.progress ?: 0) * 100F / 255).roundToInt()}%"
                            }

                            override fun onStartTrackingTouch(p0: SeekBar?) {
                                //NOT NEEDED
                            }

                            override fun onStopTrackingTouch(p0: SeekBar?) {
                                inflate.progressTv.visibility = View.VISIBLE
                                inflate.progressTv.text =
                                    "Detection Sensitivity ${((p0?.progress ?: 0) * 100F / 255).roundToInt()}%"
                                tolerance = p0?.progress ?: 50
                                removeBG(inflate)
                            }
                        })
                        seekBar.progress = tolerance
                        removeBG(inflate)
                    }
                } catch (e: Exception) {
                    Log.d("texts", "onViewCreated: " + e.localizedMessage)
                }
                // Use the uri to load the image
            }
        }
        inflate.addView.setOnClickListener {
            ImagePicker.with(FragActivity)
                .provider(ImageProvider.BOTH) //Or bothCameraGallery()
                .maxResultSize(100, 100, true)
                .createIntentFromDialog { launcher.launch(it) }
        }
        inflate.switchImage.setOnClickListener {
            ImagePicker.with(FragActivity)
                .provider(ImageProvider.BOTH) //Or bothCameraGallery()
                .maxResultSize(100, 100, true)
                .createIntentFromDialog { launcher.launch(it) }
        }
    }

    private fun hideAddImageView() {
        inflate.addView.visibility = View.GONE
        inflate.oldimg.visibility = View.VISIBLE
        inflate.procView.visibility = View.VISIBLE
    }

    private fun removeBG(
        inflate: FragmentConverterBinding,
    ) {
        inflate.progressTv.visibility = View.VISIBLE
        saveImgGone(inflate)
        val decodeFile = BitmapFactory.decodeFile(target.absolutePath)
        var width = decodeFile.width
        var height = decodeFile.height
        val i = 1500
        while (max(width, height) > i) {
            width /= 2
            height /= 2
        }
        scale = decodeFile.copy(Bitmap.Config.ARGB_8888, true).scale(width, height, true)
        showProgressDialog(inflate)
        val image = scale
        if (image != null) {
            utils.logProcessingEvent(context, "START")
            BackgroundRemover.bitmapForProcessing(image,
                object : OnBackgroundChangeListener {
                    override fun onFailed(exception: java.lang.Exception) {
                        Log.d("texts", "onFailed: " + exception.localizedMessage)
                        hideProgressDialog(inflate)
                        utils.logProcessingEvent(context, "FAIL_${exception.localizedMessage}")
                    }

                    override fun onChange(bitmap: Bitmap) {
                        val l = System.currentTimeMillis() - lastChange
                        if (l > 1000) {
                            FragActivity.runOnUiThread {
                                inflate.procView.visibility = View.VISIBLE
                                saveImgGone(inflate)
                            }
                            lastChange = System.currentTimeMillis()
                        }
                    }

                    override fun onSuccess(bitmap: Bitmap) {
                        utils.logProcessingEvent(context, "SUCCESS")
                        inflate.procView.visibility = View.GONE
                        inflate.newimg.setImageBitmap(bitmap)
                        saveImgVisible(inflate)
                        val context = context
                        if (context != null && !hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                context)
                        ) {
                            getPermission()
                        }
                        inflate.changeBg.setOnClickListener {
                            activity?.let {
                                ColorPickerDialog
                                    .Builder(requireActivity())
                                    .setTitle("Pick background Color")            // Default "Choose Color"
                                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                                    .setDefaultColor("#FFFFFF")     // Pass Default Color
                                    .setColorListener { color, colorHex ->
                                        Log.d("texts", "onSuccess: " + color + " " + colorHex)
                                        bgColor = color
                                        bgColorHex = colorHex
                                        removeBG(inflate)
                                    }
                                    .show()
                            }
                        }
                        inflate.saveImg.setOnClickListener {
                            showSaveDialog(bitmap, object : SaveResult {
                                override fun saveBitmap(divideBy: Int) {
                                    utils.logSaveEvent(context,
                                        if (divideBy == 2) "Low" else "High", bgColorHex)
                                    var outputImage = bitmap.scale(
                                        bitmap.width / divideBy,
                                        bitmap.height / divideBy
                                    )
                                    if (divideBy == 2) {
                                        outputImage = reduceOutputImageSize(bitmap)
                                        outputImage =
                                            utils.mark(outputImage, "SimpleAppsOfficial", context)
                                    }
                                    val saveBitmapTask = SaveBitmapTask(FragContext,
                                        target.name,
                                        outputImage,
                                        "BGRemover",
                                        object : DownloadListener {
                                            override fun onSuccess(path: String) {
                                                Toast.makeText(FragContext,
                                                    "Saved to $path",
                                                    Toast.LENGTH_SHORT)
                                                    .show()
                                                ratingDialog?.showIfMeetsConditions()
                                            }

                                            override fun onFailure(error: String) {
                                                Log.d("texts", "onFailure: $error")
                                            }
                                        })
                                    saveBitmapTask.execute()
                                }
                            })
                        }
                        hideProgressDialog(inflate)
                    }
                }, tolerance, bgColor)
        }
    }

    private fun reduceOutputImageSize(
        bitmap: Bitmap,
        maxSize: Int = 200,
    ): Bitmap {
        var outputImage1 = bitmap
        while (outputImage1.width > maxSize && outputImage1.height > maxSize) {
            Log.d("texts",
                "reduceOutputImageSize: " + outputImage1.width + " " + outputImage1.height)
            outputImage1 = bitmap.scale(
                outputImage1.width * 90 / 100,
                outputImage1.height * 90 / 100
            )
        }
        return outputImage1
    }

    //After Processing Completed
    private fun saveImgVisible(inflate: FragmentConverterBinding) {
//        ratingDialog?.showIfMeetsConditions()
        inflate.newimg.visibility = View.VISIBLE
        inflate.saveImg.visibility = View.VISIBLE
        inflate.changeBg.visibility = View.VISIBLE
    }

    private fun saveImgGone(inflate: FragmentConverterBinding) {
        inflate.saveImg.visibility = View.GONE
        inflate.newimg.visibility = View.GONE
        inflate.changeBg.visibility = View.GONE
    }

    interface SaveResult {
        fun saveBitmap(divideBy: Int)
    }

    var adLoadingDialog: AlertDialog? = null

    lateinit var outputImage: Bitmap
    private fun showSaveDialog(bitmap: Bitmap, saveResult: SaveResult) {
        val builder = AlertDialog.Builder(context, R.style.DialogTheme)
        val inflate1 = DownloadConfirmationLayoutBinding.inflate(layoutInflater)
        builder.setView(inflate1.root)
        var create: AlertDialog? = null
        outputImage = bitmap
        while (outputImage.width > 200) {
            outputImage = bitmap.scale(
                outputImage.width * 90 / 100,
                outputImage.height * 90 / 100
            )
        }
        outputImage = utils.mark(outputImage, "SimpleAppsOfficial", context)
        inflate1.outputPreview.setImageBitmap(outputImage)
        inflate1.rgroup.setOnCheckedChangeListener { _, i ->
            if (i == R.id.radioButton) {
                inflate1.outputPreview.setImageBitmap(outputImage)
            } else {
                inflate1.outputPreview.setImageBitmap(bitmap)
            }
        }
        inflate1.radioButton2.performClick()
        inflate1.downloadButton.setOnClickListener {
            if (inflate1.radioButton.isChecked) {
                create?.dismiss()
                saveResult.saveBitmap(2)
            } else {
                val activity = activity
                create?.dismiss()
                if (activity != null) {
                    val builder2 = AlertDialog.Builder(context, R.style.DialogTheme)
                    val adLoadingLayoutBinding =
                        IntersAdLoadingLayoutBinding.inflate(layoutInflater)
                    builder2.setView(adLoadingLayoutBinding.root)
                    adLoadingDialog = builder2.create()
                    adLoadingDialog?.show()
                    if (downloadInterstitialAd != null) {
                        downloadInterstitialAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    adLoadingDialog?.dismiss()
                                    saveResult.saveBitmap(1)
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    adLoadingDialog?.dismiss()
                                    saveResult.saveBitmap(2)
                                }
                            }
                        downloadInterstitialAd?.show(activity) {}
                        loadTestStartAd(activity)
                    } else {
                        showDownloadAd(activity, saveResult)
                    }
                } else {
                    saveResult.saveBitmap(2)
                }
            }
        }
        inflate1.cancelButton.setOnClickListener {
            utils.logClickEvent(context, "SAVE_CANCEL")
            create?.dismiss()
        }
        create = builder.create()
        create?.show()
    }

    private fun showDownloadAd(
        activity: FragmentActivity,
        saveResult: SaveResult,
    ) {
        RewardedInterstitialAd.load(activity,
            activity.getString(R.string.download_rins_all_ad_id),
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    adLoadingDialog?.dismiss()
                    saveResult.saveBitmap(2)
                }

                override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                    super.onAdLoaded(rewardedInterstitialAd)
                    rewardedInterstitialAd.fullScreenContentCallback = object :
                        FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            saveResult.saveBitmap(1)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            super.onAdFailedToShowFullScreenContent(adError)
                            Log.d("texts",
                                "onAdFailedToShowFullScreenContent: " + adError.message)
                            saveResult.saveBitmap(2)
                        }
                    }
                    adLoadingDialog?.dismiss()
                    rewardedInterstitialAd.show(activity) { }
                }
            })
    }


    private fun showProgressDialog(inflate: FragmentConverterBinding) {
        FragActivity.runOnUiThread {
            inflate.seekBar.isEnabled = false
            inflate.switchImage.isEnabled = false
        }
    }

    private fun hideProgressDialog(inflate: FragmentConverterBinding) {
        FragActivity.runOnUiThread {
            inflate.seekBar.isEnabled = true
            inflate.switchImage.isEnabled = true
        }
    }

    fun getFileFromUri(context: Context, uri: Uri?): File? {
        uri ?: return null
        uri.path ?: return null

        var newUriString = uri.toString()
        newUriString = newUriString.replace(
            "content://com.android.providers.downloads.documents/",
            "content://com.android.providers.media.documents/"
        )
        newUriString = newUriString.replace(
            "/msf%3A", "/image%3A"
        )
        val newUri = Uri.parse(newUriString)

        var realPath = String()
        val databaseUri: Uri
        val selection: String?
        val selectionArgs: Array<String>?
        if (newUri.path?.contains("/document/image:") == true) {
            databaseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            selection = "_id=?"
            selectionArgs = arrayOf(DocumentsContract.getDocumentId(newUri).split(":")[1])
        } else {
            databaseUri = newUri
            selection = null
            selectionArgs = null
        }
        try {
            val column = "_data"
            val projection = arrayOf(column)
            val cursor = context.contentResolver.query(
                databaseUri,
                projection,
                selection,
                selectionArgs,
                null
            )
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(column)
                    realPath = cursor.getString(columnIndex)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.i("GetFileUri Exception:", e.message ?: "")
        }
        val path = realPath.ifEmpty {
            when {
                newUri.path?.contains("/document/raw:") == true -> newUri.path?.replace(
                    "/document/raw:",
                    ""
                )
                newUri.path?.contains("/document/primary:") == true -> newUri.path?.replace(
                    "/document/primary:",
                    "/storage/emulated/0/"
                )
                else -> return null
            }
        }
        return if (path.isNullOrEmpty()) null else File(path)
    }

    var manager: DownloadManager? = null

    private fun getPermission() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                FragContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }


    var lastChange = System.currentTimeMillis()

}