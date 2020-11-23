package com.bc.gordiansigner.di

import com.bc.gordiansigner.ui.account.add_account.AddAccountActivity
import com.bc.gordiansigner.ui.account.add_account.AddAccountModule
import com.bc.gordiansigner.ui.account.contact.ContactsActivity
import com.bc.gordiansigner.ui.account.contact.ContactsModule
import com.bc.gordiansigner.ui.main.MainActivity
import com.bc.gordiansigner.ui.main.MainModule
import com.bc.gordiansigner.ui.scan.QRScannerActivity
import com.bc.gordiansigner.ui.scan.QRScannerModule
import com.bc.gordiansigner.ui.sign.verify.VerifyPsbtSignActivity
import com.bc.gordiansigner.ui.sign.verify.VerifyPsbtSignModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector(modules = [AddAccountModule::class])
    @ActivityScope
    internal abstract fun bindAddAccountActivity(): AddAccountActivity

    @ContributesAndroidInjector(modules = [QRScannerModule::class])
    @ActivityScope
    internal abstract fun bindQRScannerActivity(): QRScannerActivity

    @ContributesAndroidInjector(modules = [ContactsModule::class])
    @ActivityScope
    internal abstract fun bindContactsActivity(): ContactsActivity

    @ContributesAndroidInjector(modules = [VerifyPsbtSignModule::class])
    @ActivityScope
    internal abstract fun bindVerifyPsbtSignActivity(): VerifyPsbtSignActivity

    @ContributesAndroidInjector(modules = [MainModule::class])
    @ActivityScope
    internal abstract fun bindMainActivity(): MainActivity
}