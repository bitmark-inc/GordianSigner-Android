package com.bc.gordiansigner.ui.sign.verify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bc.gordiansigner.R

class PsbtInfoRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PsbtInfoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_psbt_info,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    class PsbtInfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }
}