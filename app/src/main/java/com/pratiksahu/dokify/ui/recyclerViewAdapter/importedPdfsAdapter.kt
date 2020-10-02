package com.pratiksahu.dokify.ui.recyclerViewAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.databinding.ImportedDocksPdfItemBinding
import com.pratiksahu.dokify.model.DocInfo
import kotlinx.android.synthetic.main.imported_docks_item.view.singleItemCheckBox
import kotlinx.android.synthetic.main.imported_docks_pdf_item.view.*

class ImportedPdfsAdapter(
    items: List<DocInfo>,
    private val itemShareClick: (view: View, position: Int, dockItem: DocInfo?) -> Unit,
    private val itemOpenClick: (view: View, position: Int, dockItem: DocInfo?) -> Unit,
    private val itemCheckBoxClick: (dockItem: DocInfo?, position: Int, isChecked: Boolean) -> Unit,
    private val itemLongClick: (view: View, position: Int, dockItem: DocInfo?) -> Unit
) :
    RecyclerView.Adapter<ImportedPdfsAdapter.ViewHolder>() {

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
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.imported_docks_pdf_item,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
        holder.itemView.longPressArea.setOnClickListener(holder)
        holder.itemView.longPressArea.setOnLongClickListener(holder)
        holder.itemView.shareButton.setOnClickListener(holder)
        holder.itemView.singleItemCheckBox.setOnCheckedChangeListener(null)

        holder.itemView.singleItemCheckBox.visibility =
            if (isLongClicked) View.VISIBLE else View.GONE

        //Should not be visible when user is trying to delete pdf
        holder.itemView.shareButton.visibility =
            if (isLongClicked) View.GONE else View.VISIBLE
        holder.itemView.singleItemCheckBox.isChecked = selectedItems.contains(position)
        holder.itemView.singleItemCheckBox.setOnCheckedChangeListener(({ view, isChecked ->
            itemCheckBoxClick.invoke(items[position], position, isChecked)
        }))
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    inner class ViewHolder(private val binding: ImportedDocksPdfItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        override fun onClick(v: View?) {
            when (v) {
                binding.longPressArea -> itemOpenClick.invoke(
                    v,
                    adapterPosition,
                    items[adapterPosition]
                )
                binding.shareButton -> itemShareClick.invoke(
                    v,
                    adapterPosition,
                    items[adapterPosition]
                )
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
            binding.docs = items[position]
            binding.executePendingBindings()
        }
    }
}
