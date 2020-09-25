package com.pratiksahu.dokify.module

import com.pratiksahu.dokify.`interface`.ToBlackWhite
import com.pratiksahu.dokify.ui.viewPagerHome.imagePager.ImagePagerViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideVM(): ImagePagerViewModel = ImagePagerViewModel()

    @Provides
    fun provideGrayscaleconverter(): ToBlackWhite = ToBlackWhite()
}