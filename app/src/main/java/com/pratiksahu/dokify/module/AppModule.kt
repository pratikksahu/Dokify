package com.pratiksahu.dokify.module

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

    @Singleton
    @Provides
    fun provideVMIMG(): ImagePagerViewModel = ImagePagerViewModel()

    @Singleton
    @Provides
    fun profideVMPDF(): PdfViewPagerFragmentViewModel = PdfViewPagerFragmentViewModel()

    @Provides
    fun provideGrayscaleconverter(): ToBlackWhite = ToBlackWhite()
}