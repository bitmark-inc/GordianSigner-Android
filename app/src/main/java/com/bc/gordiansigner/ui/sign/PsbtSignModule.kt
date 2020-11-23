package com.bc.gordiansigner.ui.sign

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.AccountService
import com.bc.gordiansigner.service.ContactService
import com.bc.gordiansigner.service.TransactionService
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import dagger.Module
import dagger.Provides

@Module
class PsbtSignModule {

    @ActivityScope
    @Provides
    fun provideViewModel(
        fragment: PsbtSignFragment,
        accountService: AccountService,
        contactService: ContactService,
        transactionService: TransactionService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = PsbtSignViewModel(
        fragment.lifecycle,
        accountService,
        contactService,
        transactionService,
        rxLiveDataTransformer
    )

    @ActivityScope
    @Provides
    fun provideNavigator(fragment: PsbtSignFragment) = Navigator(fragment)

    @ActivityScope
    @Provides
    fun provideDialogController(fragment: PsbtSignFragment) = DialogController(fragment.activity!!)
}