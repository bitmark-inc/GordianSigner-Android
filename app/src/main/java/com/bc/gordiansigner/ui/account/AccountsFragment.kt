package com.bc.gordiansigner.ui.account

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.KeyStoreHelper
import com.bc.gordiansigner.helper.ext.enrollDeviceSecurity
import com.bc.gordiansigner.helper.view.ContactDialog
import com.bc.gordiansigner.ui.BaseSupportFragment
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import com.bc.gordiansigner.ui.account.add_account.AddAccountActivity
import com.bc.gordiansigner.ui.account.contact.ContactsActivity
import kotlinx.android.synthetic.main.activity_accounts.*
import javax.inject.Inject

class AccountsFragment : BaseSupportFragment() {

    companion object {
        private const val TAG = "AccountsActivity"
    }

    @Inject
    internal lateinit var viewModel: AccountsViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    private lateinit var adapter: AccountRecyclerViewAdapter

    private var deletePrivateKeyOnly = false
    private lateinit var deletedAccountFingerprint: String

    override fun layoutRes() = R.layout.activity_accounts

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        (activity as? AppCompatActivity)?.supportActionBar?.let { supportActionBar ->
            supportActionBar.title = ""
            supportActionBar.setDisplayHomeAsUpEnabled(false)
        }

        setHasOptionsMenu(true)

        adapter = AccountRecyclerViewAdapter { keyInfo ->
            deletedAccountFingerprint = keyInfo.fingerprint

            if (keyInfo.isSaved) {
                dialogController.confirm(
                    R.string.delete_signer,
                    R.string.do_you_want_to_delete_the_whole_key,
                    cancelable = true,
                    positive = R.string.delete,
                    positiveEvent = {
                        deletePrivateKeyOnly = false
                        viewModel.deleteKeyInfo(deletedAccountFingerprint)
                    },
                    neutral = R.string.delete_seed,
                    neutralEvent = {
                        deletePrivateKeyOnly = true
                        viewModel.deleteHDKey(deletedAccountFingerprint)
                    }
                )
            } else {
                dialogController.confirm(
                    R.string.delete_signer,
                    R.string.this_action_is_undoable_the_signer_will_be_gone_forever,
                    cancelable = true,
                    positive = R.string.delete,
                    positiveEvent = {
                        deletePrivateKeyOnly = false
                        viewModel.deleteKeyInfo(deletedAccountFingerprint)
                    }
                )
            }
        }

        adapter.setItemSelectedListener { keyInfo ->
            val dialog = ContactDialog(keyInfo) { newKeyInfo ->
                viewModel.updateKeyInfo(newKeyInfo)
            }
            dialog.show(fragmentManager!!, ContactDialog.TAG)
        }

        with(recyclerView) {
            this.adapter = this@AccountsFragment.adapter
            this.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchKeysInfo()
    }

    override fun onPause() {
        if (adapter.isEditing) {
            adapter.isEditing = false
            activity?.invalidateOptionsMenu()
        }
        super.onPause()
    }

    override fun observe() {
        super.observe()

        viewModel.keyInfoLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { keysInfo ->
                        adapter.set(keysInfo)
                    }
                }

                res.isError() -> {
                    dialogController.alert(
                        R.string.error,
                        R.string.fetching_accounts_failed
                    )
                }
            }
        })

        viewModel.deleteKeysLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { _ ->
                        viewModel.fetchKeysInfo()
                    }
                }

                res.isError() -> {
                    if (!KeyStoreHelper.handleKeyStoreError(
                            context!!,
                            res.throwable()!!,
                            dialogController,
                            navigator,
                            authRequiredCallback = {
                                KeyStoreHelper.biometricAuth(
                                    activity!!,
                                    R.string.auth_required,
                                    R.string.auth_for_deleting_account,
                                    successCallback = {
                                        if (deletePrivateKeyOnly) {
                                            viewModel.deleteHDKey(deletedAccountFingerprint)
                                        } else {
                                            viewModel.deleteKeyInfo(deletedAccountFingerprint)
                                        }
                                    },
                                    failedCallback = { code ->
                                        if (code == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                                            navigator.anim(RIGHT_LEFT).enrollDeviceSecurity()
                                        } else {
                                            Log.e(
                                                TAG,
                                                "Biometric auth failed with code: $code"
                                            )
                                        }
                                    })
                            })
                    ) {
                        dialogController.alert(
                            R.string.error,
                            R.string.could_not_delete_account
                        )
                    }
                }
            }
        })

        viewModel.updateKeysLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { keyInfo ->
                        adapter.update(keyInfo)
                    }
                }

                res.isError() -> {
                    dialogController.alert(res.throwable())
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.accounts_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_edit)
            ?.setTitle(if (adapter.isEditing) R.string.done else R.string.edit)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
            R.id.action_contact -> {
                navigator.anim(RIGHT_LEFT).startActivity(ContactsActivity::class.java)
            }
            R.id.action_add -> {
                navigator.anim(RIGHT_LEFT).startActivity(AddAccountActivity::class.java)
            }
            R.id.action_edit -> {
                adapter.isEditing = !adapter.isEditing
                activity?.invalidateOptionsMenu()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_CANCELED && requestCode == KeyStoreHelper.ENROLLMENT_REQUEST_CODE) {
            // resultCode is 3 after biometric is enrolled
            if (deletePrivateKeyOnly) {
                viewModel.deleteHDKey(deletedAccountFingerprint)
            } else {
                viewModel.deleteKeyInfo(deletedAccountFingerprint)
            }
        }
    }
}