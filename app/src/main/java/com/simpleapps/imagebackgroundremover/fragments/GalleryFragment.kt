package com.simpleapps.imagebackgroundremover.fragments

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.simpleapps.imagebackgroundremover.adapter.GalleryAdapter
import com.simpleapps.imagebackgroundremover.databinding.FragmentGalleryBinding
import com.simpleapps.imagebackgroundremover.models.GalleryImages
import java.io.File


class GalleryFragment : Fragment() {

    lateinit var inflate: FragmentGalleryBinding
    lateinit var registerForActivityResult: ActivityResultLauncher<String>
    lateinit var galleryAdapter: GalleryAdapter
    var imageList: MutableList<GalleryImages> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        inflate = FragmentGalleryBinding.inflate(layoutInflater)
        galleryAdapter = GalleryAdapter()
        galleryAdapter.data = imageList
        galleryAdapter.activity = activity
        inflate.imageListRv.adapter = galleryAdapter
        return inflate.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForActivityResult =
            registerForActivityResult(ActivityResultContracts.GetContent()) {
                getFiles()
            }
        getFiles()
    }

    override fun onResume() {
        super.onResume()
        getFiles()
    }

    private fun getFiles() {
        imageList.clear()
        val s =
            File(Environment.getExternalStorageDirectory().path + File.separator + Environment.DIRECTORY_PICTURES + File.separator + "BGRemover" + File.separator)
        val listFiles = s.listFiles()
        if (listFiles != null && listFiles.isNotEmpty()) {
            inflate.imageListRv.visibility = VISIBLE
            inflate.emptyTv.visibility = GONE
            listFiles.iterator().forEach {
                imageList.add(GalleryImages(it))
            }
            galleryAdapter.notifyDataSetChanged()
        } else {
            inflate.imageListRv.visibility = GONE
            inflate.emptyTv.visibility = VISIBLE
        }
    }

}