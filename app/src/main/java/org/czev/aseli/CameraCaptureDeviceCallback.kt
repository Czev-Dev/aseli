package org.czev.aseli

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.media.ImageReader
import android.util.Log
import android.view.Surface
import java.util.Vector

class CameraCaptureDeviceCallback(private val imageReader: ImageReader) :
    CameraDevice.StateCallback() {
    private val TAG = this.javaClass.simpleName
    var cameraCaptureSession: CameraCaptureSession? = null

    override fun onOpened(cameraDevice: CameraDevice) {
        Log.i(TAG, "CameraDevice.StateCallback onOpened()")

        val csc: CameraCaptureSession.StateCallback =
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    Log.i(TAG, "CameraCaptureSession.StateCallback onConfigured()")
                    this@CameraCaptureDeviceCallback.cameraCaptureSession = cameraCaptureSession
                    try {
                        val builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                        builder.addTarget(imageReader.surface)
                        cameraCaptureSession.capture(builder.build(), null, null)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Log.e(TAG, "CameraCaptureSession.StateCallback onConfigureFailed()")
                }
            }
        val v = Vector<Surface>()
        v.add(imageReader.surface)
        try {
            cameraDevice.createCaptureSession(v, csc, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            return
        }
    }

    override fun onDisconnected(cameraDevice: CameraDevice) {
        Log.i(TAG, "CameraDevice.StateCallback onDisconnected()")
    }

    override fun onError(cameraDevice: CameraDevice, i: Int) {
        Log.i(TAG, "CameraDevice.StateCallback onError()")
    }
}