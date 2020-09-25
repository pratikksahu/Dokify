package com.pratiksahu.dokify.ui.recyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.databinding.ImportedDocksItemBinding
import com.pratiksahu.dokify.model.DocInfo
import kotlinx.android.synthetic.main.imported_docks_item.view.*

class ImportedImagesAdapter(
    items: List<DocInfo>,
    private val itemClick: (view: View, position: Int, dockItem: DocInfo?) -> Unit
) :
    RecyclerView.Adapter<ImportedImagesAdapter.ViewHolder>() {

    // refresh items
    var items = items
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.imported_docks_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
        holder.itemView.setOnClickListener(holder)
        if (items[position].imageUri != null) {
            Glide.with(holder.itemView.context)
                .applyDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                )
                .load(items[position].imageUri)
                .into(holder.itemView.importedImage)
        }
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    inner class ViewHolder(private val binding: ImportedDocksItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        override fun onClick(v: View?) {
            v?.let {
                itemClick.invoke(it, adapterPosition, items[adapterPosition])
            }
        }

        fun bind(
            position: Int
        ) {
            binding.docs = items[position]
            binding.executePendingBindings()
        }
    }
}
