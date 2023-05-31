package org.czev.aseli

import android.annotation.SuppressLint
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.util.Log
import android.view.SurfaceView

class CameraCaptureController(
    private val cameraManager: CameraManager,
    private val imageReader: ImageReader
) {
    private val TAG = this.javaClass.simpleName
    private lateinit var csc: CameraCaptureDeviceCallback

    @SuppressLint("MissingPermission")
    fun startCameraPreview() {
        Log.i(TAG, "Starting Camera Preview")
        csc = CameraCaptureDeviceCallback(imageReader)

        //we want to use the backfacing camera
        val backfacingId = getBackfacingCameraId(cameraManager)
        if (backfacingId == null) {
            Log.e(TAG, "Can not open Camera because no backfacing Camera was found")
            return
        }

        //we have a backfacing camera, so we can start the preview
        try {
            cameraManager.openCamera(backfacingId, csc, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
    private fun getBackfacingCameraId(cameraManager: CameraManager): String? {
        return try {
            val ids = cameraManager.cameraIdList
            for (i in ids.indices) {
                Log.i(TAG, "Found Camera ID: " + ids[i])
                val characteristics = cameraManager.getCameraCharacteristics(ids[i])
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                if (cameraDirection == CameraCharacteristics.LENS_FACING_BACK) {
                    Log.i(TAG, "Found back facing camera")
                    return ids[i]
                }
            }
            null
        } catch (ce: CameraAccessException) {
            ce.printStackTrace()
            null
        }
    }
}