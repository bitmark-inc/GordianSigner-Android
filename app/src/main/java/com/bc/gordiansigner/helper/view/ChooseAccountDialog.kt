package com.bc.gordiansigner.helper.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.model.KeyInfo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_choose_account.*
import kotlinx.android.synthetic.main.item_choose_account.view.*

class ChooseAccountDialog(
    private val keysInfo: List<KeyInfo>,
    private val onItemClicked: (KeyInfo) -> Unit
) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ChooseAccountDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_account, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(recyclerView) {
            this.adapter = AccountsRecyclerViewAdapter(keysInfo) {
                onItemClicked.invoke(it)
                dismiss()
            }
            this.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    class AccountsRecyclerViewAdapter(
        private val items: List<KeyInfo>,
        private val onItemClicked: (KeyInfo) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return AccountViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_choose_account,
                    parent,
                    false
                ),
                onItemClicked
            )
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as? AccountViewHolder)?.bind(items[position])
        }

        class AccountViewHolder(view: View, onClicked: (KeyInfo) -> Unit) :
            RecyclerView.ViewHolder(view) {

            private lateinit var keyInfo: KeyInfo

            init {
                itemView.setSafetyOnclickListener {
                    onClicked.invoke(keyInfo)
                }
            }

            fun bind(keyInfo: KeyInfo) {
                this.keyInfo = keyInfo
                with(itemView) {
                    tvName.text = context.getString(
                        if (keyInfo.isSaved) R.string.account_with_key_format else R.string.account_without_key_format,
                        keyInfo.alias,
                        keyInfo.fingerprint
                    )
                }
            }
        }
    }
}