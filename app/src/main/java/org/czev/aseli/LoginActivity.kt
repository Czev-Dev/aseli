package org.czev.aseli

import android.content.Intent
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

class LoginActivity : AppCompatActivity() {
    private lateinit var BASE_URL: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pref = getSharedPreferences("user_data", MODE_PRIVATE)
        if(pref.contains("id_user")){
            startActivity(Intent(this, UserActivity::class.java))
            finish()
        }

        BASE_URL = resources.getString(R.string.base_url)
        setContentView(R.layout.activity_login)
    }
    fun onLoginToRegister(v: View){
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }
    fun onLogin(v: View) {
        v.isClickable = false
        val loading = findViewById<ProgressBar>(R.id.login_loading)
        loading.visibility = View.VISIBLE

        val body = JSONObject()
        body.put("username", findViewById<EditText>(R.id.login_username).text.toString())
        body.put("password", findViewById<EditText>(R.id.login_password).text.toString())
        val req = Request.Builder()
            .url("$BASE_URL/user/login")
            .post(body.toString().toRequestBody("application/json".toMediaType())).build()
        val tview = findViewById<TextView>(R.id.login_alert)

        OkHttpClient().newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                this@LoginActivity.runOnUiThread{ view("Terjadi kesalahan!") }
                e.printStackTrace()
            }
            fun view(msg: String){
                tview.visibility = View.VISIBLE
                tview.text = msg

                loading.visibility = View.GONE
                v.isClickable = true
            }

            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string()?.let { JSONObject(it) } ?: return
                if(data.getBoolean("success")){
                    val pref = getSharedPreferences("user_data", MODE_PRIVATE).edit()
                    pref.putString("username", findViewById<EditText>(R.id.login_username).text.toString())
                    pref.putString("id_user", data.getJSONObject("data").getString("id_user"))
                    pref.apply()

                    startActivity(Intent(this@LoginActivity, UserActivity::class.java))
                    finish()
                } else {
                    this@LoginActivity.runOnUiThread { view( data.getString("message")) }
                }
            }
        })
    }
}