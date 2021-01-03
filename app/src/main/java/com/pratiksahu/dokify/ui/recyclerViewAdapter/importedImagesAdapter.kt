package com.pratiksahu.dokify.ui.recyclerViewAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.databinding.ImportedDocksItemBinding
import com.pratiksahu.dokify.model.DocInfo
import kotlinx.android.synthetic.main.imported_docks_item.view.*

class ImportedImagesAdapter(
    items: List<DocInfo>,
    val progressCircle: CircularProgressDrawable,
    private val itemClick: (view: View, position: Int, dockItem: DocInfo?) -> Unit,
    private val itemCheckBoxClick: (dockItem: DocInfo?, position: Int, isChecked: Boolean) -> Unit,
    private val itemLongClick: (view: View, position: Int, dockItem: DocInfo?) -> Unit
) :
    RecyclerView.Adapter<ImportedImagesAdapter.ViewHolder>() {

    val TAG_SELECTED = "SELECTED_IMAGES"

    // refresh items
    var isLongClicked = false
    var selectedItems = ArrayList<Int>()
    var items = items
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setIsLongClicked(bol: Boolean) {
        isLongClicked = bol
        notifyDataSetChanged()
    }

    @JvmName("setSelectedItems1")
    fun setSelectedItems(selectedItemsList: ArrayList<Int>) {
        selectedItems = selectedItemsList
        Log.d(TAG_SELECTED, selectedItems.toString())
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.imported_docks_item,
                parent,
                false
            )
        )
        val params = view.itemView.importedImage.layoutParams
        params.width = (parent.measuredWidth / 2)
        params.height = (parent.measuredHeight / 4)
        view.itemView.importedImage.layoutParams = params
        return view
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
        holder.itemView.importedImage.setOnClickListener(holder)
//        holder.itemView.importedImage.setOnLongClickListener(holder)
        holder.itemView.singleItemCheckBox.setOnCheckedChangeListener(null)
        holder.itemView.singleItemCheckBox.visibility = if (isLongClicked) VISIBLE else GONE
        holder.itemView.singleItemCheckBox.isChecked = selectedItems.contains(position)
        holder.itemView.singleItemCheckBox.setOnCheckedChangeListener(({ view, isChecked ->
            itemCheckBoxClick.invoke(items[position], position, isChecked)
        }))

        Glide.with(holder.itemView.context)
            .applyDefaultRequestOptions(
                RequestOptions()
                    .placeholder(progressCircle)
            )
            .load(items[position].imageUri)
            .centerCrop()
            .into(holder.itemView.importedImage)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    inner class ViewHolder(private val binding: ImportedDocksItemBinding) :
        RecyclerView.ViewHolder(binding.root), OnClickListener, OnLongClickListener {

        override fun onClick(v: View?) {
            v?.let {
                itemClick.invoke(v, adapterPosition, items[adapterPosition])
            }
        }

        override fun onLongClick(v: View?): Boolean {
            v?.let {
                itemLongClick.invoke(it, adapterPosition, items[adapterPosition])
            }
            return true
        }


        fun bind(
            position: Int
        ) {
            binding.pgCount.text = "(" + (position + 1).toString() + ")"
            binding.docs = items[position]
            binding.executePendingBindings()
        }
    }
}
