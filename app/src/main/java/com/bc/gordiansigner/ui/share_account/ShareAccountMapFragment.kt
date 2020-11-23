package com.bc.gordiansigner.ui.share_account

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.Error.ACCOUNT_MAP_ALREADY_FILLED_ERROR
import com.bc.gordiansigner.helper.Error.ACCOUNT_MAP_COMPLETED_ERROR
import com.bc.gordiansigner.helper.Error.BAD_DESCRIPTOR_ERROR
import com.bc.gordiansigner.helper.KeyStoreHelper
import com.bc.gordiansigner.helper.ext.copyToClipboard
import com.bc.gordiansigner.helper.ext.enrollDeviceSecurity
import com.bc.gordiansigner.helper.ext.pasteFromClipBoard
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.helper.view.ChooseAccountDialog
import com.bc.gordiansigner.helper.view.ExportBottomSheetDialog
import com.bc.gordiansigner.helper.view.QRCodeBottomSheetDialog
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.ui.BaseSupportFragment
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import com.bc.gordiansigner.ui.account.AccountsFragment
import com.bc.gordiansigner.ui.account.add_account.AddAccountActivity
import com.bc.gordiansigner.ui.scan.QRScannerActivity
import kotlinx.android.synthetic.main.activity_share_account_map.*
import javax.inject.Inject

class ShareAccountMapFragment : BaseSupportFragment() {

    companion object {
        private const val TAG = "ShareAccountMapActivity"
        private const val REQUEST_CODE_QR_ACCOUNT_MAP = 0x01
        private const val REQUEST_CODE_INPUT_KEY = 0x02
    }

    @Inject
    internal lateinit var viewModel: ShareAccountMapViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    private lateinit var keysInfo: List<KeyInfo>
    private var export = false
    private var selectedFingerprint = ""

    override fun layoutRes() = R.layout.activity_share_account_map

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        (activity as? AppCompatActivity)?.supportActionBar?.let { supportActionBar ->
            supportActionBar.title = ""
            supportActionBar.setDisplayHomeAsUpEnabled(false)
        }

        setHasOptionsMenu(true)

        buttonFill.setSafetyOnclickListener {
            val accountMapJson = editText.text.toString()

            if (!export) {
                if (accountMapJson.isNotEmpty()) {
                    val dialog = ChooseAccountDialog(keysInfo) {
                        if (it.isSaved) {
                            selectedFingerprint = it.fingerprint
                            viewModel.getSeed(it.fingerprint)
                        } else {
                            val bundle = AddAccountActivity.getBundle(it)
                            navigator.anim(RIGHT_LEFT).startActivityForResult(
                                AddAccountActivity::class.java,
                                REQUEST_CODE_INPUT_KEY,
                                bundle
                            )
                        }
                    }
                    dialog.show(fragmentManager!!, ChooseAccountDialog.TAG)
                } else {
                    dialogController.alert(R.string.error, R.string.invalid_account_map)
                }
            } else {
                val dialog = ExportBottomSheetDialog(
                    isFileVisible = false,
                    listener = object : ExportBottomSheetDialog.OnItemSelectedListener {
                        override fun onCopy() {
                            context!!.copyToClipboard(accountMapJson)
                            Toast.makeText(
                                context!!,
                                R.string.copied,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        override fun onShowQR() {
                            val qrDialog = QRCodeBottomSheetDialog(editText.text.toString())
                            qrDialog.show(fragmentManager!!, QRCodeBottomSheetDialog.TAG)
                        }

                        override fun onSaveFile() {
                            //Not supported
                        }
                    })
                dialog.show(fragmentManager!!, ExportBottomSheetDialog.TAG)
            }
        }

        viewModel.fetchKeysInfo()
    }

    override fun observe() {
        super.observe()

        viewModel.fillAccountMapLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    editText.setText(res.data())
                    export = true
                    buttonFill.setText(R.string.export)
                    dialogController.alert(
                        R.string.success,
                        R.string.you_may_now_export_it_by_tapping_the_export_button
                    )
                }

                res.isError() -> {
                    val msg = when (res.throwable()) {
                        ACCOUNT_MAP_COMPLETED_ERROR -> R.string.account_map_completed
                        ACCOUNT_MAP_ALREADY_FILLED_ERROR -> R.string.account_map_filled
                        BAD_DESCRIPTOR_ERROR -> R.string.bad_descriptor
                        else -> R.string.unsupported_format
                    }
                    dialogController.alert(R.string.error, msg)
                }
            }
        })

        viewModel.checkAccountMapStatusLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { (joinedSigners, descriptor) ->
                        dialogController.alert(
                            getString(R.string.valid_account_map),
                            getString(
                                R.string.account_map_info,
                                descriptor.mOfNType,
                                descriptor.format,
                                if (joinedSigners.isNotEmpty()) joinedSigners.joinToString {
                                    "\n\t\uD83D\uDD11 ${if (it.alias.isNotEmpty()) {
                                        getString(
                                            R.string.fingerprint_alias_format,
                                            it.fingerprint,
                                            it.alias
                                        )
                                    } else {
                                        it.fingerprint
                                    }}"
                                } else "<none>"
                            )
                        )
                    }
                }

                res.isError() -> {
                    Log.d(TAG, res.throwable()?.message ?: "")
                    editText.setText("")
                    dialogController.alert(R.string.error, R.string.invalid_account_map)
                }
            }
        })

        viewModel.getKeyInfoLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let {
                        keysInfo = it
                    }
                }

                res.isError() -> {
                    dialogController.alert(res.throwable())
                }
            }
        })

        viewModel.getSeedLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let {
                        updateAccountMap(it)
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
                                    R.string.auth_for_updating_account_map,
                                    successCallback = {
                                        viewModel.getSeed(selectedFingerprint)
                                    },
                                    failedCallback = { code ->
                                        if (code == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                                            navigator.anim(RIGHT_LEFT).enrollDeviceSecurity()
                                        } else {
                                            Log.e(TAG, "Biometric auth failed with code: $code")
                                        }
                                    })
                            })
                    ) {
                        dialogController.alert(res.throwable())
                    }
                }
            }
        })
    }

    private fun updateAccountMap(seed: String) {
        val accountMapJson = editText.text.toString()
        viewModel.updateAccountMap(accountMapJson, seed)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.account_map_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
            R.id.action_paste -> {
                context!!.pasteFromClipBoard()?.let {
                    checkAccountMap(it)
                } ?: dialogController.alert(R.string.error, R.string.clipboard_is_empty)
            }
            R.id.action_scan -> {
                navigator.anim(RIGHT_LEFT).startActivityForResult(
                    QRScannerActivity::class.java,
                    REQUEST_CODE_QR_ACCOUNT_MAP
                )
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_QR_ACCOUNT_MAP -> {
                    data?.let {
                        val accountMap = QRScannerActivity.extractResultData(it)
                        checkAccountMap(accountMap)
                    }
                }

                REQUEST_CODE_INPUT_KEY -> {
                    data?.let {
                        val (keyInfo, seed) = AddAccountActivity.extractResultData(it)
                        selectedFingerprint = keyInfo?.fingerprint ?: return
                        updateAccountMap(seed!!)
                    }
                }

                else -> {
                    error("unknown request code: $requestCode")
                }
            }
        } else if (resultCode != Activity.RESULT_CANCELED && requestCode == KeyStoreHelper.ENROLLMENT_REQUEST_CODE) {
            // resultCode is 3 after biometric is enrolled
            viewModel.getSeed(selectedFingerprint)
        }
    }

    private fun checkAccountMap(string: String) {
        editText.setText(string)
        export = false
        buttonFill.setText(R.string.fill)
        viewModel.checkValidAccountMap(string)
    }
}