package org.czev.aseli

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.os.Handler
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import java.util.Vector

class CameraDeviceCallback(private val cameraPreview: SurfaceView) :
    CameraDevice.StateCallback() {
    private val TAG = this.javaClass.simpleName
    var cameraCaptureSession: CameraCaptureSession? = null
    var cameraDevice: CameraDevice? = null

    override fun onOpened(cameraDevice: CameraDevice) {
        Log.i(TAG, "CameraDevice.StateCallback onOpened()")

        this.cameraDevice = cameraDevice
        val csc: CameraCaptureSession.StateCallback =
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    Log.i(TAG, "CameraCaptureSession.StateCallback onConfigured()")
                    try {
                        val builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                        builder.addTarget(cameraPreview.holder.surface)
                        cameraCaptureSession.setRepeatingRequest(builder.build(), null, null)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Log.e(TAG, "CameraCaptureSession.StateCallback onConfigureFailed()")
                }
            }
        val v = Vector<Surface>()
        v.add(cameraPreview.holder.surface)
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