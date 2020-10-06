package com.bc.gordiansigner.model

import com.bc.gordiansigner.helper.Network
import com.blockstream.libwally.Wally.*

class Psbt(base64: String, val network: Network) {

    private val psbt: Any = psbt_from_base64(base64)

    fun sign(privKey: ByteArray) {
        psbt_sign(psbt, privKey, 0)
    }

    fun sign(hdKey: HDKey) {
        psbt_sign(psbt, hdKey.privKey, 0)
    }

    fun toBase64(): String = psbt_to_base64(psbt, 0)
}