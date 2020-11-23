package com.bc.gordiansigner.di

import com.bc.gordiansigner.ui.account.AccountsFragment
import com.bc.gordiansigner.ui.account.AccountsModule
import com.bc.gordiansigner.ui.share_account.ShareAccountMapFragment
import com.bc.gordiansigner.ui.share_account.ShareAccountMapModule
import com.bc.gordiansigner.ui.sign.PsbtSignFragment
import com.bc.gordiansigner.ui.sign.PsbtSignModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector(modules = [PsbtSignModule::class])
    @ActivityScope
    internal abstract fun bindPsbtSignActivity(): PsbtSignFragment


    @ContributesAndroidInjector(modules = [AccountsModule::class])
    @ActivityScope
    internal abstract fun bindAccountsActivity(): AccountsFragment

    @ContributesAndroidInjector(modules = [ShareAccountMapModule::class])
    @ActivityScope
    internal abstract fun bindShareAccountMapActivity(): ShareAccountMapFragment
}