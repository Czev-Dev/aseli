package org.czev.aseli

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PostPreviewActivity : AppCompatActivity() {
    private lateinit var BASE_URL: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BASE_URL =  resources.getString(R.string.base_url)
        setContentView(R.layout.activity_post_preview)

        val img = findViewById<ImageView>(R.id.post_preview_image)
        Glide.with(this).load(intent.extras?.getString("image")).centerCrop().into(img)
    }

    override fun onBackPressed() {
        startActivity(Intent(this, PostActivity::class.java))
        finish()
    }
    fun onPostPreviewBack(v: View){
        onBackPressed()
    }
}