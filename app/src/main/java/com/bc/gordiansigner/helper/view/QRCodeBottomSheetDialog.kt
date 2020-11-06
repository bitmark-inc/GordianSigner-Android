package com.bc.gordiansigner.helper.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.ext.*
import com.bc.ur.UR
import com.bc.ur.UREncoder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_qr_code.*
import java.util.*


class QRCodeBottomSheetDialog(
    private val base64: String,
    private val animateEnabled: Boolean = false
) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "QrCodeBottomSheetDialog"
        private const val QR_CODE_SIZE = 500
    }

    private val handler = Handler(Looper.getMainLooper())
    private val parts = mutableListOf<String>()
    private var partIndex = 0
    private var isAnimating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qr_code, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (animateEnabled) {
            val data = Base64.decode(base64, Base64.NO_WRAP)
            val ur = UR.create("psbt", data)

            val qrBitmap = UREncoder.encode(ur).toQrCode(QR_CODE_SIZE)
            ivQRCode.setImageBitmap(qrBitmap)

            val encoder = UREncoder(ur, 250, 0, 10)
            while (encoder.seqNum < encoder.seqLen) {
                val part = encoder.nextPart()
                parts.add(part.toUpperCase(Locale.ENGLISH))
            }

            btnAnimate.visible()
            btnAnimate.setSafetyOnclickListener {
                isAnimating = !isAnimating
                if (isAnimating) {
                    progressBar.visible()
                    btnAnimate.setText(R.string.no_animate)
                    animate()
                } else {
                    handler.removeCallbacksAndMessages(null)
                    progressBar.invisible()
                    btnAnimate.setText(R.string.animate)
                    ivQRCode.setImageBitmap(qrBitmap)
                }
            }
        } else {
            btnAnimate.gone()
            ivQRCode.setImageBitmap(base64.toQrCode(QR_CODE_SIZE))
        }
    }

    private fun animate() {
        val qrCode = parts[partIndex].toQrCode(QR_CODE_SIZE)
        ivQRCode.setImageBitmap(qrCode)
        progressBar.progress = (partIndex + 1) * 100 / parts.size
        if (partIndex < parts.size - 1) partIndex += 1 else partIndex = 0
        handler.postDelayed({ animate() }, 200)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}