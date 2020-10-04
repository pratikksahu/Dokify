package com.pratiksahu.dokify.homepage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.databinding.DashboardItemCardsBinding
import com.pratiksahu.dokify.model.CardData
import kotlinx.android.synthetic.main.dashboard_item_cards.view.*

class HomePageItemAdapter(
    items: List<CardData>,
    private val itemClick: (view: View, position: Int, cardData: CardData?) -> Unit
) : RecyclerView.Adapter<HomePageItemAdapter.ViewHolder>() {

    var items = items
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.dashboard_item_cards,
                parent,
                false
            )
        )
        val params = view.itemView.cardItemImageView.layoutParams
        params.width = (parent.measuredWidth / 2)
        view.itemView.cardItemImageView.layoutParams = params
        return view
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
        holder.itemView.setOnClickListener(holder)
        holder.itemView.cardView.setBackgroundResource(R.drawable.dashboard_card_background)
        holder.itemView.cardItemImageView.setImageResource(items[position].cardImage)
    }

    inner class ViewHolder(private val binding: DashboardItemCardsBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        override fun onClick(v: View?) {
            v?.let {
                itemClick.invoke(v, adapterPosition, items[adapterPosition])
            }
        }

        fun bind(
            position: Int
        ) {
            binding.card = items[position]
            binding.executePendingBindings()
        }
    }
}