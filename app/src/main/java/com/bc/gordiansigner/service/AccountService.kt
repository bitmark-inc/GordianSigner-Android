package com.bc.gordiansigner.service

import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.helper.ext.SIMPLE_DATE_TIME_FORMAT
import com.bc.gordiansigner.helper.ext.fromJson
import com.bc.gordiansigner.helper.ext.newGsonInstance
import com.bc.gordiansigner.helper.ext.toString
import com.bc.gordiansigner.model.Bip39Mnemonic
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.storage.file.FileStorageApi
import com.bc.gordiansigner.service.storage.file.rxCompletable
import com.bc.gordiansigner.service.storage.file.rxSingle
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.security.SecureRandom
import java.util.*
import javax.inject.Inject

class AccountService @Inject constructor(
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : BaseService(sharedPrefApi, fileStorageApi) {

    companion object {
        private const val XPRIV_KEY_FILE = "xpriv.secret"
        private const val KEY_INFO_FILE = "keyinfo.secret"
    }

    fun importHDKeyWallet(mnemonic: String, network: Network = Network.TEST): Single<HDKey> {
        return Single.fromCallable {
            val seed = Bip39Mnemonic(mnemonic).seed
            HDKey(seed, network)
        }.subscribeOn(Schedulers.computation())
    }

    fun generateHDKeyWallet(network: Network): Single<HDKey> {
        return Single.fromCallable {
            val entropy = ByteArray(16)
            SecureRandom().nextBytes(entropy)
            val seed = Bip39Mnemonic(entropy).seed
            HDKey(seed, network)
        }.subscribeOn(Schedulers.computation())
    }

    fun getHDKey(fingerprint: String) =
        getHDKeys()
            .map { keys -> keys.first { it.fingerprintHex == fingerprint } }
            .flatMap { updateKeyInfoLastUsed(it.fingerprintHex).andThen(Single.just(it)) }

    fun getHDKeys() =
        fileStorageApi.SUPER_SECURE.rxSingle { gateway ->
            val privs = gateway.readOnFilesDir(XPRIV_KEY_FILE)
            if (privs.isEmpty()) {
                emptyList()
            } else {
                newGsonInstance().fromJson<List<String>>(String(privs))
            }
        }.map { set -> set.map { HDKey(it) } }

    fun getKeysInfo() = fileStorageApi.SECURE.rxSingle { gateway ->
        val json = gateway.readOnFilesDir(KEY_INFO_FILE)
        if (json.isEmpty()) {
            emptyList()
        } else {
            newGsonInstance().fromJson<List<KeyInfo>>(String(json))
        }
    }

    fun saveKey(keyInfo: KeyInfo, hdKey: HDKey) = if (keyInfo.isSaved) {
        saveHDKey(hdKey)
    } else {
        Single.just(hdKey)
    }.flatMap { saveKeyInfo(keyInfo).map { hdKey } }

    fun deleteHDKey(fingerprintHex: String): Completable = getHDKeys().flatMapCompletable { keys ->
        if (keys.any { it.fingerprintHex == fingerprintHex }) {
            val newKeys = keys.filterNot { it.fingerprintHex == fingerprintHex }.toSet()
            saveHDKeys(newKeys).andThen(updateKeyInfoSavingState(fingerprintHex))
        } else {
            Completable.complete()
        }
    }

    fun deleteKeyInfo(fingerprintHex: String) = getKeysInfo().flatMapCompletable { keysInfo ->
        val currentKey = keysInfo.firstOrNull { it.fingerprint == fingerprintHex }
        currentKey?.let { keyInfo ->
            val newKeys = keysInfo.filterNot { it == keyInfo }.toSet()
            saveKeysInfo(newKeys).andThen(if (keyInfo.isSaved) deleteHDKey(fingerprintHex) else Completable.complete())
        } ?: Completable.complete()
    }

    private fun updateKeyInfoSavingState(fingerprint: String): Completable =
        getKeysInfo().flatMapCompletable { keysInfo ->
            val keyInfoSet = keysInfo.toMutableSet()
            keyInfoSet.firstOrNull { it.fingerprint == fingerprint }?.let {
                it.isSaved = false
                saveKeysInfo(keyInfoSet)
            } ?: Completable.complete()
        }

    private fun updateKeyInfoLastUsed(fingerprint: String): Completable =
        getKeysInfo().flatMapCompletable { keysInfo ->
            val keyInfoSet = keysInfo.toMutableSet()
            keyInfoSet.firstOrNull { it.fingerprint == fingerprint }?.let {
                it.lastUsed = Date().toString(SIMPLE_DATE_TIME_FORMAT)
                saveKeysInfo(keyInfoSet)
            } ?: Completable.complete()
        }

    private fun saveHDKey(hdKey: HDKey) =
        getHDKeys().flatMap { keys ->
            val keySet = keys.toMutableSet()
            if (keySet.add(hdKey)) {
                saveHDKeys(keySet).andThen(Single.just(hdKey))
            } else {
                Single.just(hdKey)
            }
        }

    fun saveKeyInfo(keyInfo: KeyInfo) = getKeysInfo().flatMap { keysInfo ->
        val keyInfoSet = keysInfo.toMutableSet()
        keyInfoSet.firstOrNull { it == keyInfo }?.let {
            if (keyInfo.alias.isNotEmpty()) {
                it.alias = keyInfo.alias
            }
            it.isSaved = keyInfo.isSaved
            it.lastUsed = keyInfo.lastUsed
        } ?: let {
            keyInfoSet.add(keyInfo)
        }

        saveKeysInfo(keyInfoSet).andThen(Single.just(keyInfo))
    }

    private fun saveHDKeys(keys: Set<HDKey>) =
        fileStorageApi.SUPER_SECURE.rxCompletable { gateway ->
            gateway.writeOnFilesDir(
                XPRIV_KEY_FILE,
                newGsonInstance().toJson(keys.map { it.xprv }).toByteArray()
            )
        }

    private fun saveKeysInfo(keysInfo: Set<KeyInfo>) =
        fileStorageApi.SECURE.rxCompletable { gateway ->
            gateway.writeOnFilesDir(
                KEY_INFO_FILE,
                newGsonInstance().toJson(keysInfo).toByteArray()
            )
        }

}