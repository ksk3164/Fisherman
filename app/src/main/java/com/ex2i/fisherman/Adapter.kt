package com.ex2i.fisherman

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_fish.view.*

class Adapter(
    private var context: Context,
    private var items: List<RequestData>,
    mCallback: OnItemClick
) :
    RecyclerView.Adapter<Adapter.MainViewHolder>() {

    private var mCallback: OnItemClick? = mCallback

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = MainViewHolder(parent)

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {

        items[position].let { item ->
            with(holder) {

                tvName.text = item.name
                ivFish.setImageResource(item.image)

                holder.itemView.setOnClickListener {
                    mCallback?.itemOnClick(item.name,item.image)
                }

            }
        }

    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_fish, parent, false)
    ) {

        val tvName: TextView = itemView.tv_fish_name
        val ivFish: ImageView = itemView.iv_fish

    }

}

