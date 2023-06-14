package org.czev.aseli

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
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
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale


class ProfilActivity : AppCompatActivity() {
    private lateinit var BASE_URL: String
    private var file: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        BASE_URL = resources.getString(R.string.base_url)
        val pref = getSharedPreferences("user_data", MODE_PRIVATE)
        val req = Request.Builder()
            .url("$BASE_URL/user/profil/" + pref.getString("username", ""))
            .build()
        OkHttpClient().newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string()?.let { JSONObject(it).getJSONObject("data") }
                runOnUiThread {
                    findViewById<TextView>(R.id.profil_username).text = pref.getString("username", "")
                    findViewById<TextView>(R.id.profil_description).text = data?.getString("description")
                    findViewById<AppCompatButton>(R.id.profil_edit_btn).setOnClickListener { onProfilEdit(it) }
                    Glide.with(this@ProfilActivity).load("$BASE_URL/uploads/" + data?.getString("profil"))
                        .into(findViewById(R.id.profil_image))
                }
            }
        })
    }
    private val getResult = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        if(it == null) return@registerForActivityResult
        file = File.createTempFile("temp_profile", ".png", cacheDir)
        val inputStream = contentResolver.openInputStream(it)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        Log.d("FileName", file!!.absolutePath)
        Glide.with(this).load(file).into(findViewById(R.id.profil_image))
    }
    fun onProfilImage(v: View){
        getResult.launch(arrayOf(
            "image/jpeg",
            "image/png"
        ))
    }
    fun onProfilEdit(v: View){
        findViewById<ProgressBar>(R.id.profil_loading).visibility = View.VISIBLE
        v.isClickable = false
        val pref = getSharedPreferences("user_data", MODE_PRIVATE)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("user_id", pref.getString("id_user", "") ?: "")
            .addFormDataPart("description", findViewById<TextView>(R.id.profil_description).text.toString())
        if(file != null){
            val mimeType = getMimeType(file!!.toUri())
            body.addFormDataPart("profil", file!!.name, file!!.asRequestBody(mimeType!!.toMediaType()))
        }
        val req = Request.Builder()
            .url("$BASE_URL/user/profil")
            .post(body.build()).build()
        OkHttpClient().newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                file?.delete()
                onBackPressed()
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
        startActivity(Intent(this, UserActivity::class.java))
        finish()
    }
    fun onProfilBack(v: View){
        onBackPressed()
    }
}