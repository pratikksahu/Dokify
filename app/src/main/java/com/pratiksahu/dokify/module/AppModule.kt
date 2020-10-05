package com.pratiksahu.dokify.module

import com.pratiksahu.dokify.MainActivityViewModel
import com.pratiksahu.dokify.`interface`.ToBlackWhite
import com.pratiksahu.dokify.ui.viewPagerHome.imagePager.ImagePagerViewModel
import com.pratiksahu.dokify.ui.viewPagerHome.pdfPager.PdfViewPagerFragmentViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    //ImagePagerViewModel
    @Singleton
    @Provides
    fun provideVMIMG(): ImagePagerViewModel = ImagePagerViewModel()

    //PdfFragmentViewModel
    @Singleton
    @Provides
    fun profideVMPDF(): PdfViewPagerFragmentViewModel = PdfViewPagerFragmentViewModel()

    //MainActivityViewModel
    @Singleton
    @Provides
    fun profideVMMA(): MainActivityViewModel = MainActivityViewModel()

    @Provides
    fun provideGrayscaleconverter(): ToBlackWhite = ToBlackWhite()

}