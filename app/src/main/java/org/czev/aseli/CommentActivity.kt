package org.czev.aseli

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CommentActivity : AppCompatActivity() {
    private lateinit var BASE_URL: String
    private lateinit var post_id: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        BASE_URL = resources.getString(R.string.base_url)

        post_id = intent.extras?.getString("post_id").toString()
        val comments = JSONArray(intent.extras?.getString("comments"))
        val main = findViewById<LinearLayout>(R.id.comment_layout)
        var commentTotal = 0
        for (i in 0 until comments.length()){
            val comment = comments.getJSONObject(i)
            val replies = comment.getJSONArray("replies")
            val layout = LayoutInflater.from(this).inflate(R.layout.comment_view, main, false) as LinearLayout
            commentTotal += 1 + replies.length()
            layout.findViewById<TextView>(R.id.comment_username).text = comment.getString("username")
            layout.findViewById<TextView>(R.id.comment_time).text = comment.getString("time")
            val replyTotal = layout.findViewById<TextView>(R.id.comment_show_reply_total)
            if(replies.length() > 0) replyTotal.text = replies.length().toString() + " balasan"
            else {
                layout.findViewById<LinearLayout>(R.id.comment_show_reply_btn).visibility = View.GONE
                layout.findViewById<TextView>(R.id.comment_reply_btn).setPadding(20, 0, 0, 0)
            }
            setOnClickLongText(layout.findViewById(R.id.comment_value), comment.getString("comment"))
            Glide.with(this).load("$BASE_URL/user/profil/image/" + comment.getString("username"))
                .into(layout.findViewById(R.id.comment_profile))

            var isShow = true
            val repliesLayout = layout.findViewById<LinearLayout>(R.id.comment_replies)
            val commentArrow = layout.findViewById<ImageView>(R.id.comment_arrow)
            layout.findViewById<LinearLayout>(R.id.comment_show_reply_btn).setOnClickListener {
                if(isShow){
                    commentArrow.setImageResource(R.drawable.icon_keyboard_arrow_down)
                    repliesLayout.visibility = View.VISIBLE
                } else {
                    commentArrow.setImageResource(R.drawable.icon_keyboard_arrow_up)
                    repliesLayout.visibility = View.GONE
                }
                isShow = !isShow
            }

            for(j in 0 until replies.length()){
                val reply = replies.getJSONObject(j)
                val replayout = LayoutInflater.from(this).inflate(R.layout.comment_reply, layout, false)
                replayout.findViewById<TextView>(R.id.reply_username).text = reply.getString("username")
                replayout.findViewById<TextView>(R.id.reply_time).text = reply.getString("time")
                setOnClickLongText(replayout.findViewById(R.id.reply_value), reply.getString("comment"))
                Glide.with(this).load("$BASE_URL/user/profil/image/" + reply.getString("username"))
                    .into(replayout.findViewById(R.id.reply_profile))
                repliesLayout.addView(replayout)
            }
            main.addView(layout)
        }
        findViewById<TextView>(R.id.comment_total).text = "Komentar ($commentTotal)"
    }
    fun onCommentSend(v: View){
        val input = findViewById<EditText>(R.id.comment_input)
        val comment = input.text.toString()
        if(comment.isEmpty()) Toast.makeText(this, "Komentar tidak boleh kosong!", Toast.LENGTH_SHORT).show()
        v.isClickable = false
        input.clearFocus()
        input.text.clear()

        val pref = getSharedPreferences("user_data", MODE_PRIVATE)
        val body = JSONObject()
        body.put("user_id", pref.getString("id_user", ""))
        body.put("post_id", post_id)
        body.put("comment", comment)
        val req = Request.Builder()
            .url("$BASE_URL/post/comment")
            .post(body.toString().toRequestBody("application/json".toMediaType())).build()
        OkHttpClient().newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()?.let { JSONObject(it) }
                if(res == null || !res.getBoolean("success")){
                    Toast.makeText(this@CommentActivity, res?.getString("message"), Toast.LENGTH_SHORT).show()
                    return
                }
                val layout = findViewById<LinearLayout>(R.id.comment_layout)
                val comlayout = LayoutInflater.from(this@CommentActivity).inflate(R.layout.comment_view, layout, false)
                runOnUiThread {
                    comlayout.findViewById<TextView>(R.id.comment_username).text = pref.getString("username", "")
                    comlayout.findViewById<TextView>(R.id.comment_time).text = "Baru saja..."
                    comlayout.findViewById<TextView>(R.id.comment_value).text = comment
                    comlayout.findViewById<LinearLayout>(R.id.comment_show_reply_btn).visibility = View.GONE
                    comlayout.findViewById<TextView>(R.id.comment_reply_btn).setPadding(20, 0, 0, 0)
                    Glide.with(this@CommentActivity).load("$BASE_URL/user/profil/image/" + pref.getString("username", ""))
                        .into(comlayout.findViewById(R.id.comment_profile))
                    layout.addView(comlayout)
                    v.isClickable = true
                }
            }
        })
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        startActivity(Intent(this, UserActivity::class.java))
        finish()
    }
    fun onCommentBack(v: View){
        onBackPressed()
    }
    private fun setOnClickLongText(textView: TextView, text: String){
        val maxLength = 100
        val splitted = text.split("\r\n|\n|\r")
        var txt = splitted[0]
        var spannable: SpannableStringBuilder? = null
        if(splitted.size > 1 || txt.length > maxLength) {
            if(txt.length > maxLength) txt = txt.substring(0, maxLength) + "... "
            else txt += "\n"
            txt += "Baca selengkapnya"
            spannable = SpannableStringBuilder(txt)
            val colorSpan = ForegroundColorSpan(Color.parseColor("#04C4CC"))
            spannable.setSpan(colorSpan, txt.lastIndexOf("Baca selengkapnya"), txt.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        var isDetail = false
        if(spannable != null) textView.setOnClickListener {
            if(!isDetail) textView.text = text
            else textView.text = spannable
            isDetail = !isDetail
        }
        textView.text = spannable ?: text
    }
}