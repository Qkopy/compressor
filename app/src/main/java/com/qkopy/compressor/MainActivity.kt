package com.qkopy.compressor

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.qkopy.imagecompressor.Compressor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.IOException
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 100
    private var actualImage: File? = null
    private var compressedImage: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clearImage()
    }

    fun chooseImage(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    fun compressImage(view: View) {
        if (actualImage == null) {
            toast("Please choose an image!")
        } else {

            // Compress image in main thread
            //compressedImage = new Compressor(this).compressToFile(actualImage);
            //setCompressedImage();

            // Compress image to bitmap in main thread
            //compressedImageView.setImageBitmap(new Compressor(this).compressToBitmap(actualImage));

            // Compress image using RxJava in background thread
            Compressor(this@MainActivity)
                .compressToFileAsFlowable(actualImage!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ file ->
                    compressedImage = file
                    setCompressedImage()
                }, { throwable ->
                    throwable.printStackTrace()
                    toast("$throwable.message")
                })

            doAsync {
                Compressor(this@MainActivity).compressToFileAsFlowable(actualImage!!)
                uiThread {

                }

            }


        }
    }

    private fun setCompressedImage() {
        compressed_image.setImageBitmap(BitmapFactory.decodeFile(compressedImage?.absolutePath))
        compressed_size.text = String.format("Size : %s", getReadableFileSize(compressedImage?.length()))
        //Toast.makeText(this, "Compressed image save in " + compressedImage?.path, Toast.LENGTH_LONG).show()
        Log.d("Compressor", "Compressed image save in " + compressedImage?.path)
    }

    private fun clearImage() {
        compressed_image.setImageDrawable(null)
        compressed_size.text = getString(R.string.size)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                toast("Failed to open picture!")
                return
            }
            try {
                actualImage = FileUtil.from(this, data.data)
                if (actualImage!!.absolutePath != null) {
                    actual_image.setImageBitmap(BitmapFactory.decodeFile(actualImage!!.absolutePath))
                    actual_size.text = String.format("Size : %s", getReadableFileSize(actualImage!!.length()))
                    clearImage()
                }

            } catch (e: IOException) {
                toast("Failed to read picture data!")
                e.printStackTrace()
            }

        }
    }


    private fun getReadableFileSize(size: Long?): String {
        return if (size != null && size <= 0) {
            "0"
        } else {
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size!!.toDouble()) / Math.log10(1024.0)).toInt()
            DecimalFormat("#,##0.#").format(
                size / Math.pow(
                    1024.0,
                    digitGroups.toDouble()
                )
            ) + " " + units[digitGroups]
        }
    }

}
