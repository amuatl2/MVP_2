package com.example.mvp.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

object PhotoManager {
    private val storage = FirebaseStorage.getInstance()
    
    /**
     * Upload photo to Firebase Storage
     */
    suspend fun uploadPhoto(context: Context, uri: Uri, ticketId: String): String? {
        return try {
            val storageRef = storage.reference
            val imageRef = storageRef.child("tickets/$ticketId/${System.currentTimeMillis()}.jpg")
            
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File.createTempFile("upload", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            val uploadTask = imageRef.putFile(Uri.fromFile(file))
            uploadTask.await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            // Fallback to local storage path
            uri.toString()
        }
    }
    
    /**
     * Upload multiple photos
     */
    suspend fun uploadPhotos(context: Context, uris: List<Uri>, ticketId: String): List<String> {
        return uris.mapNotNull { uri ->
            uploadPhoto(context, uri, ticketId)
        }
    }
    
    /**
     * Delete photo from storage
     */
    suspend fun deletePhoto(photoUrl: String) {
        try {
            if (photoUrl.startsWith("gs://") || photoUrl.contains("firebasestorage")) {
                val storageRef = storage.getReferenceFromUrl(photoUrl)
                storageRef.delete().await()
            }
        } catch (e: Exception) {
            // Ignore deletion errors
        }
    }
    
    /**
     * Get photo file name from URI
     */
    fun getPhotoFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.let { File(it).name } ?: "photo_${System.currentTimeMillis()}.jpg"
        }
        return result
    }
}

