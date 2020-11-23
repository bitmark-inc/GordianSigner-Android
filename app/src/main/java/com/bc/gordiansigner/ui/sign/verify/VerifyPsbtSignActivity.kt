package com.bc.gordiansigner.ui.sign.verify

import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.BaseViewModel
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import kotlinx.android.synthetic.main.activity_verify_psbt_sign.*
import javax.inject.Inject

class VerifyPsbtSignActivity : BaseAppCompatActivity() {

    companion object {
        private const val SIGNING_KEY = "signing_key"
        private const val PARTICIPANTS = "participants"

        fun getBundle(keyInfo: KeyInfo, participants: List<KeyInfo>) = Bundle().apply {
            putParcelable(SIGNING_KEY, keyInfo)
            putParcelableArrayList(PARTICIPANTS, ArrayList(participants))
        }
    }

    private lateinit var signingKey: KeyInfo
    private lateinit var participants: List<KeyInfo>

    private val adapter = PsbtInfoRecyclerViewAdapter()

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes() = R.layout.activity_verify_psbt_sign

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        signingKey = intent?.getParcelableExtra(SIGNING_KEY) ?: error("missing signing key")
        participants =
            intent?.getParcelableArrayListExtra(PARTICIPANTS) ?: error("missing participants")

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(recyclerView) {
            this.adapter = this@VerifyPsbtSignActivity.adapter
            this.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        btnSave.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).finishActivityForResult()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}