package com.pratiksahu.dokify.`interface`

import ImageUtils
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Matrix4f
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicColorMatrix
import androidx.annotation.RequiresApi


class ToBlackWhite {

    @RequiresApi(Build.VERSION_CODES.P)
    fun convertToBW(context: Context, colorUri: Uri): Bitmap? {

//        val colorUriBitMap =
//            ImageUtils.instant?.getCompressedBitmap("/storage/emulated/0/Android/data/com.pratiksahu.dokify/${colorUri.path}")
        val colorUriBitMap =
            ImageUtils.instant?.getCompressedBitmap(colorUri.path)
        //Transformation Matrix
        val redVal = 0.299f
        val greenVal = 0.587f
        val blueVal = 0.114f
        val matrix = Matrix4f(
            floatArrayOf(
                redVal, redVal, redVal, 0.0F,   //R
                greenVal, greenVal, greenVal, 0.0F,  //G
                blueVal, blueVal, blueVal, 0.0F,   //B
                0.0F, 0.0F, 0.0F, 0.0F
            )
        )
        //Render Script
        val renderScript = RenderScript.create(context)
        val input = Allocation.createFromBitmap(
            renderScript,
            colorUriBitMap,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )
        val output = Allocation.createTyped(renderScript, input.type)

        //Inverts the bitmap
        //U8_4 : Grayscale can be done in 8bit
        val invert = ScriptIntrinsicColorMatrix.create(renderScript)
        invert.setColorMatrix(matrix)
        invert.forEach(input, output)
        output.copyTo(colorUriBitMap)
        renderScript.destroy()


        return colorUriBitMap
    }

}