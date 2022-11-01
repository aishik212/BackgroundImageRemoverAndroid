package com.simpleapps.imagebackgroundremover.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.simpleapps.imagebackgroundremover.activities.ImageViewerActivity
import com.simpleapps.imagebackgroundremover.databinding.ImageListRowLayoutBinding
import com.simpleapps.imagebackgroundremover.models.GalleryImages

class GalleryAdapter : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    var activity: FragmentActivity? = null
    var data: MutableList<GalleryImages> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ImageListRowLayoutBinding.inflate(LayoutInflater.from(parent.context)))
//        ViewHolder(LayoutInflater.from(parent.activity).inflate(R.layout.image_list_row_layout, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(data[position], activity)

    override fun getItemCount() = data.size

    class ViewHolder(var imageListRowLayoutBinding: ImageListRowLayoutBinding) :
        RecyclerView.ViewHolder(imageListRowLayoutBinding.root) {
        fun bind(item: GalleryImages, activity: FragmentActivity?) =
            with(imageListRowLayoutBinding) {
                val imageView1 = this.imageView
                imageView1.setImageURI(Uri.fromFile(item.file))
                imageView1.setOnClickListener {
                    val intent = Intent(itemView.context, ImageViewerActivity::class.java)
                    intent.putExtra("image", item.uri)
                    activity?.startActivity(intent)
                }
            }
    }
}