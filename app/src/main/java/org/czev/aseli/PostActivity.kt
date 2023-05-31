package org.czev.aseli

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class PostActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private var cameraController: CameraController? = null
    private lateinit var surfaceView: SurfaceView
    private var reinit: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
        } else {
            surfaceView = findViewById(R.id.post_camera_view)
            surfaceView.holder.addCallback(this)

            val facingFront = intent.getBooleanExtra("reinit", false)
            reinit = facingFront
            val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            cameraController = CameraController(cameraManager, surfaceView, facingFront)
        }
    }
    fun onPostChangeFacingCamera(v: View){
        cameraController?.getCameraDevice()?.close()
        startActivity(Intent(this, this@PostActivity.javaClass).putExtra("reinit", !reinit))
        finish()
    }
    fun onPostTakePicture(v: View){}
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
//            bitmapImage.recycle()
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
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) recreate()
        else finish()
    }
    override fun surfaceCreated(holder: SurfaceHolder) {
        cameraController?.startCameraPreview()
    }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}
}