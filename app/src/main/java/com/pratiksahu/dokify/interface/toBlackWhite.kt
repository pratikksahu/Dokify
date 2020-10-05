package com.pratiksahu.dokify.`interface`

import ImageUtils
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Matrix4f
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicColorMatrix


class ToBlackWhite {


    fun convertToBW(context: Context, colorUri: Uri): Bitmap? {

        val path = colorUri.path
        val file = BitmapFactory.decodeFile(path)
        val width = file.width.toFloat()
        val height = file.height.toFloat()
        val colorUriBitMap =
            ImageUtils.instant?.getCompressedBitmap(path, width, height)
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
        //U8_4 : Black and white can be done in 8bit
        val invert = ScriptIntrinsicColorMatrix.create(renderScript)
        invert.setColorMatrix(matrix)
        invert.forEach(input, output)
        output.copyTo(colorUriBitMap)
        renderScript.destroy()


        return colorUriBitMap
    }

}