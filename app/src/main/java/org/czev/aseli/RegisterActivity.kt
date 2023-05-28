package org.czev.aseli

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RegisterActivity : AppCompatActivity() {
    private lateinit var BASE_URL: String;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BASE_URL = resources.getString(R.string.base_url)
        setContentView(R.layout.activity_register)
    }
    fun onRegister(v: View){
        v.isClickable = false

        val pass = findViewById<EditText>(R.id.register_password)
        val cpass = findViewById<EditText>(R.id.register_confirm)
        val tview = findViewById<TextView>(R.id.register_alert)

        if(pass.text.toString() != cpass.text.toString()) {
            tview.visibility = View.VISIBLE
            tview.text = "Verifikasi password salah!"
            return
        }

        val loading = findViewById<ProgressBar>(R.id.register_loading)
        loading.visibility = View.VISIBLE

        val client = OkHttpClient()
        val body = JSONObject()
        body.put("username", findViewById<EditText>(R.id.register_username).text.toString())
        body.put("password", pass.text.toString())
        body.put("confirm_password", cpass.text.toString())
        val req = Request.Builder()
            .url("$BASE_URL/user/register")
            .post(body.toString().toRequestBody("application/json".toMediaType())).build()

        client.newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                this@RegisterActivity.runOnUiThread{ view("Terjadi kesalahan!") }
                e.printStackTrace()
            }
            fun view(msg: String){
                tview.visibility = View.VISIBLE
                tview.text = msg

                loading.visibility = View.GONE
                v.isClickable = false
            }

            override fun onResponse(call: Call, response: Response) {
                val data = JSONObject(response.body?.string())
                this@RegisterActivity.runOnUiThread { view( if(data.getBoolean("success")) "Login sukses" else data.getString("message")) }
            }
        })
    }
}