package org.czev.aseli

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.AUTHORITY
import android.provider.ContactsContract.DisplayPhoto
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1)
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            setGalleryImages()
        }
    }
    private lateinit var imageUri: Uri
    private val getResult = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if(!it) return@registerForActivityResult
        val photo = contentResolver.openInputStream(imageUri) ?: return@registerForActivityResult
        val date = SimpleDateFormat("ss-mm-hh dd-MM-yyyy", Locale.getDefault())
        val movedPhoto = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "aseli-image " + date.format(Date()) + ".png")
        photo.copyTo(FileOutputStream(movedPhoto))
        startActivity(Intent(this, PostPreviewActivity::class.java)
            .putExtra("image", movedPhoto.absolutePath))
        photo.close()
        finish()
    }
    fun onPostTakePhoto(v: View){
        try {
            val file = File.createTempFile("IMG_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
            imageUri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            getResult.launch(imageUri)
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
    private fun setGalleryImages(){
        try {
            val images = getAllFilesInDirectoriesSortedByDate()
            val grid = findViewById<GridLayout>(R.id.post_images)
            for (image in images){
                val layout = LayoutInflater.from(this).inflate(R.layout.post_image, grid, false) as ConstraintLayout
                val imgView = layout.findViewById<ImageView>(R.id.post_image_view)
                Glide.with(this).load(image).centerCrop().into(imgView)
                layout.setOnClickListener {
                    startActivity(Intent(this, PostPreviewActivity::class.java)
                        .putExtra("image", image.absolutePath))
                    finish()
                }
                grid.addView(layout)
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
    fun getAllFilesInDirectoriesSortedByDate(): List<File> {
        val files = mutableListOf<File>()
        val paths = listOf(Environment.DIRECTORY_DCIM, Environment.DIRECTORY_DOCUMENTS,
        Environment.DIRECTORY_DOWNLOADS, Environment.DIRECTORY_PICTURES)
        for (path in paths){
            val allowed = arrayOf(".jpg", ".png", ".jpeg", ".gif")
            val file = Environment.getExternalStoragePublicDirectory(path).listFiles {
                    filter -> allowed.any { filter.absolutePath.endsWith(it) }
            }
            if (file != null) files.addAll(file)
        }


        files.sortWith { file1, file2 ->
            val lastModified1 = file1.lastModified()
            val lastModified2 = file2.lastModified()
            lastModified2.compareTo(lastModified1)
        }

        return files
    }
}