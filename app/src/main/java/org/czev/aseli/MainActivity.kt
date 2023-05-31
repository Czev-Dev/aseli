package org.czev.aseli

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var BASE_URL: String;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BASE_URL = resources.getString(R.string.base_url)
        setContentView(R.layout.activity_login)
    }
    fun onLogin(v: View) {
        v.isClickable = false
        val loading = findViewById<ProgressBar>(R.id.login_loading)
        loading.visibility = View.VISIBLE

        val client = OkHttpClient()
        val body = JSONObject()
        body.put("username", findViewById<EditText>(R.id.login_username).text.toString())
        body.put("password", findViewById<EditText>(R.id.login_password).text.toString())
        val req = Request.Builder()
            .url("$BASE_URL/user/login")
            .post(body.toString().toRequestBody("application/json".toMediaType())).build()
        val tview = findViewById<TextView>(R.id.login_alert)

        client.newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                this@MainActivity.runOnUiThread{ view("Terjadi kesalahan!") }
                e.printStackTrace()
            }
            fun view(msg: String){
                tview.visibility = View.VISIBLE
                tview.text = msg

                loading.visibility = View.GONE
                v.isClickable = true
            }

            override fun onResponse(call: Call, response: Response) {
                val data = JSONObject(response.body?.string())
                this@MainActivity.runOnUiThread { view( if(data.getBoolean("success")) "Login sukses" else data.getString("message")) }
            }
        })
    }
}