package org.czev.aseli

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import java.io.File


class PostActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private var cameraController: CameraController? = null
    private lateinit var surfaceView: SurfaceView
    private var reinit: Boolean = false
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES), 1)
        } else {
            setGalleryImages()
            surfaceView = findViewById(R.id.post_camera_view)
            surfaceView.holder.addCallback(this)

            val facingFront = intent.getBooleanExtra("reinit", false)
            reinit = facingFront
            val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            cameraController = CameraController(cameraManager, surfaceView, facingFront)
        }
    }
    fun onPostChangeFacingCamera(_v: View){
        cameraController?.getCameraDevice()?.close()
        startActivity(Intent(this, this@PostActivity.javaClass).putExtra("reinit", !reinit))
        finish()
    }
    fun onPostTakePicture(v: View){}
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setGalleryImages(){
        try {
            val images = getAllFilesInDirectoriesSortedByDate()
            val grid = findViewById<GridLayout>(R.id.post_images)
            for (image in images){
                val layout = LayoutInflater.from(this).inflate(R.layout.post_image, grid, false) as ConstraintLayout
                val imgView = layout.findViewById<ImageView>(R.id.post_image_view)
                Log.d("Image Path", image.absolutePath)
                Glide.with(this).load(image).centerCrop().into(imgView)
                grid.addView(layout)
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAllFilesInDirectoriesSortedByDate(): List<File> {
        val files = mutableListOf<File>()
        val paths = listOf(Environment.DIRECTORY_DCIM, Environment.DIRECTORY_DOCUMENTS, Environment.DIRECTORY_DCIM,
        Environment.DIRECTORY_DOWNLOADS, Environment.DIRECTORY_SCREENSHOTS, Environment.DIRECTORY_PICTURES)
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
//    fun onTakePicture(v: View){
//        cameraController?.getCameraCaptureSession()?.close()
//        val imageView = findViewById<ImageView>(R.id.post_image)
//        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
//        val imageReader = ImageReader.newInstance(surfaceView.width, surfaceView.height, ImageFormat.JPEG, 1)
//        val cameraCaptureController = CameraCaptureController(cameraManager, imageReader)
//        cameraCaptureController.startCameraPreview()
//
//        imageReader.setOnImageAvailableListener({it
//            val buffer: ByteBuffer = it.acquireLatestImage().planes[0].buffer
//            val bytes = ByteArray(buffer.capacity())
//            buffer[bytes]
//            val bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
//
//            val matrix = Matrix()
//            matrix.postRotate(90f)
//            val rotatedBitmap = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.width, bitmapImage.height, matrix, true)
//            imageView.setImageBitmap(rotatedBitmap)
//
//            bitmapImage.recycle()1
//            rotatedBitmap.recycle()
//            it.acquireLatestImage().close()
//        }, null)
//    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for(result in grantResults){
            if(result == PackageManager.PERMISSION_DENIED) finish()
            else recreate()
        }
    }
    override fun surfaceCreated(holder: SurfaceHolder) {
        cameraController?.startCameraPreview()
    }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}
}