# Image Compressor Library [ ANDROID ]
Compressor is an android image compression library. Compressor will allow you to compress large photos into smaller sized photos.

### CODE

```
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
                })
```
