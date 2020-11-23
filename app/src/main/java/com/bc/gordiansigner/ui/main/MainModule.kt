package com.bc.gordiansigner.ui.main

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.ui.Navigator
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @ActivityScope
    @Provides
    fun provideNavigator(activity: MainActivity) = Navigator(activity)

}