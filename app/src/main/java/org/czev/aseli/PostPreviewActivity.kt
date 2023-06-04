package org.czev.aseli

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.Locale


class PostPreviewActivity : AppCompatActivity() {
    private lateinit var BASE_URL: String
    private lateinit var imgPath: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BASE_URL =  resources.getString(R.string.base_url)
        setContentView(R.layout.activity_post_preview)

        val img = findViewById<ImageView>(R.id.post_preview_image)
        imgPath = intent.extras?.getString("image").toString()
        Glide.with(this).load(imgPath).centerCrop().into(img)
    }
    fun onPostPreviewUpload(v: View){
        v.isClickable = false
        findViewById<ProgressBar>(R.id.post_preview_progress).visibility = View.VISIBLE
        val description = findViewById<EditText>(R.id.post_preview_description).text.toString()
        val imgFile = File(imgPath)
        val mimeType = getMimeType(imgFile.toUri())
        val id_user = getSharedPreferences("user_data", MODE_PRIVATE).getString("id_user", "")!!
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("user_id", id_user)
            .addFormDataPart("description", description)
            .addFormDataPart("image", imgFile.name, imgFile.asRequestBody(mimeType!!.toMediaType()))
            .build()

        val req = Request.Builder().url("$BASE_URL/post").post(body).build()
        OkHttpClient().newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()?.let { JSONObject(it) }!!
                if(!body.getBoolean("success")) runOnUiThread {
                    Toast.makeText(this@PostPreviewActivity, body.getString("message"), Toast.LENGTH_LONG).show() }
                startActivity(Intent(this@PostPreviewActivity, UserActivity::class.java))
                finish()
            }
        })
    }
    private fun getMimeType(uri: Uri): String? {
        val mimeType: String? = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cr: ContentResolver = contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase(Locale.getDefault())
            )
        }
        return mimeType
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        onBack()
    }
    private fun onBack(){
        startActivity(Intent(this, PostActivity::class.java))
        finish()
    }
    fun onPostPreviewBack(v: View){
        onBack()
    }
}