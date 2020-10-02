import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Image
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class ImageUtils {
    val TAG_IMAGE_RESOLUTION = "IMAGE_RESOLUTION"
    fun createPdf(imageList: ArrayList<Uri>, filePath: String): Boolean {
        val list = imageList
        if (list.isEmpty())
            return false
        var width: Float
        var height: Float
        // Creating a PdfDocument object
        val pdfOut = FileOutputStream(filePath)
        val writerProperties = WriterProperties().setFullCompressionMode(true)
        writerProperties.useSmartMode()
        val pdfWriter = PdfWriter(pdfOut, writerProperties)
        val pdfDocument =
            PdfDocument(pdfWriter)
        //Getting height and width
        var bm = BitmapFactory.decodeFile(list[0].path)
        width = bm.width.toFloat()
        height = bm.height.toFloat()
        //Creating first page outside loop to avoid first page being blank
        val document = Document(pdfDocument, PageSize(width, height))
        document.setMargins(2F, 2F, 2F, 2F)

        //For first page
        try {
            val imgIn = FileInputStream(list[0].path)
            val byteOut = ByteArrayOutputStream()

            val data = ByteArray(1024)
            while (imgIn.read(data, 0, data.size) != -1) {
                byteOut.write(data)
            }
            byteOut.flush()
            imgIn.close()

            //ImageData
            val imgData = ImageDataFactory.create(byteOut.toByteArray())
            byteOut.close()
            val pdfImage = Image(imgData)
            document.add(pdfImage)
            list.removeAt(0)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        //For rest of page if items greater than 1
        if (list.isNotEmpty()) {
            try {
                for (it in list.indices) {
                    bm = BitmapFactory.decodeFile(list[it].path)
                    width = bm.width.toFloat()
                    height = bm.height.toFloat()
                    val rectangleImage = Rectangle(width, height)
                    println("TESTING $rectangleImage")
                    document.add(AreaBreak(PageSize(rectangleImage)))
                    document.setMargins(2F, 2F, 2F, 2F)

                    val imgIn = FileInputStream(list[it].path)
                    val byteOut = ByteArrayOutputStream()

                    val data = ByteArray(1024)
                    while (imgIn.read(data, 0, data.size) != -1) {
                        byteOut.write(data)
                    }
                    byteOut.flush()
                    imgIn.close()

                    //ImageData
                    val imgData = ImageDataFactory.create(byteOut.toByteArray())
                    byteOut.close()
                    val pdfImage = Image(imgData)
                    document.add(pdfImage)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }
        document.close()
        return true
    }

    fun getCompressedBitmap(imagePath: String?): Bitmap {
        val maxWidth = 1920.0f
        val maxHeight = 1080.0f
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(imagePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth
        Log.d(TAG_IMAGE_RESOLUTION, "WIDTH : $actualWidth , HEIGHT : $actualHeight ")
        var imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
        val maxRatio = maxWidth / maxHeight
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
        options.inJustDecodeBounds = false
        options.inTempStorage = ByteArray(16 * 1024)
        try {
            bmp = BitmapFactory.decodeFile(imagePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            null
        )
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(imagePath)
            val orientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90F)
            } else if (orientation == 3) {
                matrix.postRotate(180F)
            } else if (orientation == 8) {
                matrix.postRotate(270F)
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                scaledBitmap.width,
                scaledBitmap.height,
                matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val out = ByteArrayOutputStream()
        scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
        val byteArray: ByteArray = out.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = width * height.toFloat()
        val totalReqPixelsCap = reqWidth * reqHeight * 2.toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }


    companion object {
        var mInstant: ImageUtils? = null
        val instant: ImageUtils?
            get() {
                if (mInstant == null) {
                    mInstant = ImageUtils()
                }
                return mInstant
            }
    }
}