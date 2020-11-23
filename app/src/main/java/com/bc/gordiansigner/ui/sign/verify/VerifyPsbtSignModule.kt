package com.bc.gordiansigner.ui.sign.verify

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.ui.Navigator
import dagger.Module
import dagger.Provides

@Module
class VerifyPsbtSignModule {

    @ActivityScope
    @Provides
    fun provideNavigator(activity: VerifyPsbtSignActivity) = Navigator(activity)
}