package com.example.mvp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object PerformanceOptimizer {
    private const val MAX_IMAGE_WIDTH = 1920
    private const val MAX_IMAGE_HEIGHT = 1920
    private const val COMPRESSION_QUALITY = 85
    
    /**
     * Compress and resize image for better performance
     */
    fun compressImage(imageFile: File): File? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imageFile.absolutePath, options)
            
            val scale = calculateInSampleSize(
                options.outWidth,
                options.outHeight,
                MAX_IMAGE_WIDTH,
                MAX_IMAGE_HEIGHT
            )
            
            options.inJustDecodeBounds = false
            options.inSampleSize = scale
            
            var bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, options)
            
            // Handle orientation
            bitmap = handleImageOrientation(imageFile, bitmap)
            
            // Resize if still too large
            if (bitmap.width > MAX_IMAGE_WIDTH || bitmap.height > MAX_IMAGE_HEIGHT) {
                val width = bitmap.width.coerceAtMost(MAX_IMAGE_WIDTH)
                val height = bitmap.height.coerceAtMost(MAX_IMAGE_HEIGHT)
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            }
            
            // Compress
            val compressedFile = File(imageFile.parent, "compressed_${imageFile.name}")
            val outputStream = FileOutputStream(compressedFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
            outputStream.close()
            bitmap.recycle()
            
            compressedFile
        } catch (e: Exception) {
            null
        }
    }
    
    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    private fun handleImageOrientation(imageFile: File, bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }
            
            if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }
    
    /**
     * Paginate list for lazy loading
     */
    fun <T> paginateList(
        list: List<T>,
        page: Int,
        pageSize: Int = 20
    ): List<T> {
        val startIndex = page * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(list.size)
        return if (startIndex < list.size) {
            list.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }
    
    /**
     * Check if more pages are available
     */
    fun <T> hasMorePages(
        list: List<T>,
        currentPage: Int,
        pageSize: Int = 20
    ): Boolean {
        val startIndex = (currentPage + 1) * pageSize
        return startIndex < list.size
    }
}

