package com.bc.gordiansigner.ui.share_account

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.AccountMapService
import com.bc.gordiansigner.service.ContactService
import com.bc.gordiansigner.service.AccountService
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import dagger.Module
import dagger.Provides

@Module
class ShareAccountMapModule {

    @Provides
    @ActivityScope
    fun provideVM(
        fragment: ShareAccountMapFragment,
        accountMapService: AccountMapService,
        contactService: ContactService,
        accountService: AccountService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = ShareAccountMapViewModel(
        fragment.lifecycle,
        accountMapService,
        contactService,
        accountService,
        rxLiveDataTransformer
    )

    @ActivityScope
    @Provides
    fun provideNavigator(fragment: ShareAccountMapFragment) = Navigator(fragment)

    @ActivityScope
    @Provides
    fun provideDialogController(fragment: ShareAccountMapFragment) = DialogController(fragment.activity!!)
}