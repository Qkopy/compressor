package com.qkopy.imagecompressor

import android.content.Context
import android.graphics.Bitmap
import io.reactivex.Flowable

import java.io.File
import java.io.IOException
import java.util.concurrent.Callable

class Compressor(context: Context) {
    //max width and height values of the compressed image is taken as 612x816
    private var maxWidth = 612.0f
    private var maxHeight = 816.0f
    private var compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    private var quality = 80
    private var destinationDirectoryPath: String? = null

    init {
        destinationDirectoryPath = context.cacheDir.path + File.separator + "images"
    }

    fun setMaxWidth(maxWidth: Float): Compressor {
        this.maxWidth = maxWidth
        return this
    }

    fun setMaxHeight(maxHeight: Float): Compressor {
        this.maxHeight = maxHeight
        return this
    }

    fun setCompressFormat(compressFormat: Bitmap.CompressFormat): Compressor {
        this.compressFormat = compressFormat
        return this
    }

    fun setQuality(quality: Int): Compressor {
        this.quality = quality
        return this
    }

    fun setDestinationDirectoryPath(destinationDirectoryPath: String): Compressor {
        this.destinationDirectoryPath = destinationDirectoryPath
        return this
    }

    @Throws(IOException::class)
    fun compressToFile(imageFile: File): File {
        return compressToFile(imageFile, imageFile.name)
    }

    @Throws(IOException::class)
    private fun compressToFile(imageFile: File, compressedFileName: String): File {
        return ImageUtil.compressImage(
            imageFile, maxWidth, maxHeight, compressFormat, quality,
            destinationDirectoryPath + File.separator + compressedFileName
        )
    }

    @Throws(IOException::class)
    private fun compressToFile(imageFileList: ArrayList<File>):ArrayList<File>{
        val compressArrayList = ArrayList<File>()
        for (imageFile in imageFileList) {
            val compressed = ImageUtil.compressImage(imageFile, maxWidth, maxHeight, compressFormat, quality, destinationDirectoryPath + File.separator + imageFile.name)
            compressArrayList.add(compressed)
        }
        return compressArrayList
    }

    @Throws(IOException::class)
    private fun compressToBitmap(imageFile: File): Bitmap? {
        return ImageUtil.decodeSampledBitmapFromFile(imageFile, maxWidth, maxHeight)
    }

    fun compressToFileAsFlowable(imageFile: File): Flowable<File> {
        return compressToFileAsFlowable(imageFile, imageFile.name)
    }

    fun compressToFileAsFlowable(imageFileList:ArrayList<File>):Flowable<ArrayList<File>>{
        return compressToFileAsFlowableList(imageFileList)
    }

    private fun compressToFileAsFlowable(imageFile: File, compressedFileName: String): Flowable<File> {
        return Flowable.defer(Callable {
            try {
                return@Callable Flowable.just(compressToFile(imageFile, compressedFileName))
            } catch (e: IOException) {
                return@Callable Flowable.error<File>(e)
            }
        })
    }

    private fun compressToFileAsFlowableList(imageFileList: ArrayList<File>):Flowable<ArrayList<File>>{
        return Flowable.defer(Callable {
            try {
                return@Callable Flowable.just(compressToFile(imageFileList))
            }catch (e:IOException){
                return@Callable Flowable.error<ArrayList<File>>(e)
            }
        })
    }

    fun compressToBitmapAsFlowable(imageFile: File): Flowable<Bitmap> {
        return Flowable.defer(Callable {
            try {
                return@Callable Flowable.just(compressToBitmap(imageFile))
            } catch (e: IOException) {
                return@Callable Flowable.error<Bitmap>(e)
            }
        })
    }
}
