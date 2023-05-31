package org.czev.aseli

import android.annotation.SuppressLint
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.util.Log
import android.view.SurfaceView

class CameraController(
    private val cameraManager: CameraManager,
    private val cameraPreview: SurfaceView,
    private val facingFront: Boolean
) {
    private val TAG = this.javaClass.simpleName
    private lateinit var csc: CameraDeviceCallback

    @SuppressLint("MissingPermission")
    fun startCameraPreview() {
        Log.i(TAG, "Starting Camera Preview")
        csc = CameraDeviceCallback(cameraPreview)

        val cameraId = getCameraId(cameraManager)
        if (cameraId == null) {
            Log.e(TAG, "Can not open Camera because no Camera was found")
            return
        }

        try {
            cameraManager.openCamera(cameraId, csc, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
    fun getCameraDevice(): CameraDevice? {
        return csc.cameraDevice
    }
    private fun getCameraId(cameraManager: CameraManager): String? {
        return try {
            val ids = cameraManager.cameraIdList
            for (i in ids.indices) {
                Log.i(TAG, "Found Camera ID: " + ids[i])
                val characteristics = cameraManager.getCameraCharacteristics(ids[i])
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                if (cameraDirection == CameraCharacteristics.LENS_FACING_BACK && !facingFront) {
                    Log.i(TAG, "Found back facing camera")
                    return ids[i]
                } else if (cameraDirection == CameraCharacteristics.LENS_FACING_FRONT && facingFront) {
                    Log.i(TAG, "Found front facing camera")
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