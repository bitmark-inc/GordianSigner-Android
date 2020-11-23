package com.bc.gordiansigner.model

import com.blockstream.libwally.Wally.*

class Bip39Mnemonic {

    val mnemonic: String

    constructor(words: String) {
        bip39_mnemonic_validate(bip39_get_wordlist(null), words)
        mnemonic = words
    }

    constructor(entropy: ByteArray) {
        mnemonic = bip39_mnemonic_from_bytes(bip39_get_wordlist(null), entropy)
    }

    fun seed(passphrase: String? = null) = ByteArray(BIP39_SEED_LEN_512).apply {
        bip39_mnemonic_to_seed(mnemonic, passphrase, this)
    }

    fun seedHex(passphrase: String? = null): String = hex_from_bytes(seed(passphrase))
}