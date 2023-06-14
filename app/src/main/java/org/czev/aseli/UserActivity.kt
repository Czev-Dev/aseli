package org.czev.aseli

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executors

class UserActivity : AppCompatActivity() {
    private lateinit var BASE_URL: String
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        BASE_URL = resources.getString(R.string.base_url)

        val tab = findViewById<TabLayout>(R.id.user_tab)
        tab.addTab(tab.newTab().setIcon(R.drawable.icon_menu_grid))
        tab.addTab(tab.newTab().setIcon(R.drawable.icon_posts_detail))

        val pager = findViewById<ViewPager2>(R.id.user_pager)
        val pagerAdapter = UserPagerAdapter(supportFragmentManager, lifecycle)
        pager.adapter = pagerAdapter

        tab.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                pager.currentItem = tab?.position ?: 0
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tab.getTabAt(position)?.select()
            }
        })
        val scroll = findViewById<ScrollView>(R.id.user_scroll)
        scroll.setOnTouchListener(View.OnTouchListener { v, event ->
            return@OnTouchListener pager.currentItem == 0
        })
    }
    fun onUserNewPost(v: View){
        startActivity(Intent(this, PostActivity::class.java))
        finish()
    }
    fun onUserEditProfil(v: View){
        startActivity(Intent(this, ProfilActivity::class.java))
        finish()
    }
    override fun onStart() {
        super.onStart()
        val pref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        getUserDetails(pref)
    }
    private fun getUserDetails(pref: SharedPreferences){
        val req = Request.Builder().url("$BASE_URL/user/profil/" + pref.getString("username", "") /*+
                    "/" + pref.getString("id_user", "")*/).get().build()
        OkHttpClient().newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string()?.let { JSONObject(it).getJSONObject("data") }
                this@UserActivity.runOnUiThread {
                    Glide.with(this@UserActivity).load("$BASE_URL/uploads/" + data!!.getString("profil"))
                        .into(findViewById(R.id.user_profil_image))
                    findViewById<TextView>(R.id.user_username).text = pref.getString("username", "")
                    findViewById<TextView>(R.id.user_description).text = data.getString("description")
                    findViewById<TextView>(R.id.user_followers).text = data.getString("followers")
                    findViewById<TextView>(R.id.user_following).text = data.getString("following")
                    findViewById<TextView>(R.id.user_posts).text = data.getInt("posts").toString()
                }
            }
        })
    }

    class UserPagerAdapter(manager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(manager, lifecycle) {
        private val fragments = ArrayList<Fragment>()
        init {
            fragments.add(UserPostsFragment())
            fragments.add(UserDetailsFragment())
        }
        override fun getItemCount(): Int {
            return fragments.size
        }
        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}
class UserPostsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = requireActivity()
        val BASE_URL = activity.resources.getString(R.string.base_url)
        val pref = activity.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val layout = inflater.inflate(R.layout.fragment_user_posts, container, false)
        val req = Request.Builder().url("$BASE_URL/post?username=" + pref.getString("username", "")).get().build()
        OkHttpClient().newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string()?.let { JSONObject(it).getJSONArray("data") }
                val userPost = layout.findViewById<GridLayout>(R.id.fragment_user_posts)
                for(i in 0 until data!!.length()){
                    val img = inflater.inflate(R.layout.post_image, userPost, false)
                    activity.runOnUiThread {
                        Glide.with(activity).load("$BASE_URL/uploads/" +
                                data.getJSONObject(i).getString("imageName")).into(img.findViewById(R.id.post_image_view))
                        userPost.addView(img)
                    }
                }
            }
        })
        return layout
    }
}

class UserDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = requireActivity()
        val BASE_URL = activity.resources.getString(R.string.base_url)
        val pref = activity.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val mainLayout = inflater.inflate(R.layout.fragment_user_details, container, false)
        val userId = pref.getString("id_user","")

        Executors.newSingleThreadExecutor().execute {
            val reqPosts = Request.Builder().url("$BASE_URL/post?username=" + pref.getString("username", "")).get().build()
            val postsRes = OkHttpClient().newCall(reqPosts).execute()

            val reqUser = Request.Builder().url("$BASE_URL/user/profil/" + pref.getString("username", "") + "/$userId").get().build()
            val userRes = OkHttpClient().newCall(reqUser).execute()
            Handler(Looper.getMainLooper()).post {
                val data = postsRes.body?.string()?.let { JSONObject(it).getJSONArray("data") } ?: return@post
                val userData = userRes.body?.string()?.let { JSONObject(it).getJSONObject("data") } ?: return@post
                val userName = activity.findViewById<TextView>(R.id.user_username).text
                val userImage = activity.findViewById<ImageView>(R.id.user_profil_image).drawable
                val userDetails = mainLayout.findViewById<LinearLayout>(R.id.fragment_user_details)

                val ril = userData.getJSONArray("ril")
                val fek = userData.getJSONArray("fek")
                for(i in 0 until data.length()){
                    val dat = data.getJSONObject(i)
                    val layout = inflater.inflate(R.layout.post_details, userDetails, false)
                    val post_id = dat.getString("post_id")
                    val ril_btn = layout.findViewById<ImageButton>(R.id.post_details_ril_btn)
                    val fek_btn = layout.findViewById<ImageButton>(R.id.post_details_fek_btn)

                    activity.runOnUiThread {
                        for(j in 0 until ril.length()){
                            if(ril.getString(j).equals(post_id)){
                                ril_btn.setColorFilter(Color.GREEN)
                                break
                            }
                        }
                        for(j in 0 until fek.length()){
                            if(fek.getString(j).equals(post_id)){
                                fek_btn.setColorFilter(Color.RED)
                                break
                            }
                        }
                        ril_btn.setOnClickListener { it ->
                            it.isClickable = false
                            val rilBody = JSONObject()
                            rilBody.put("user_id", userId)
                            rilBody.put("post_id", post_id)
                            val rilReq = Request.Builder().url("$BASE_URL/post/ril")
                                .post(rilBody.toString().toRequestBody("application/json".toMediaType())).build()
                            OkHttpClient().newCall(rilReq).enqueue(object: Callback {
                                override fun onFailure(call: Call, e: IOException) {}
                                @SuppressLint("SetTextI18n")
                                override fun onResponse(call: Call, response: Response) {
                                    val rilRes = response.body?.string()?.let { JSONObject(it).getString("data") }
                                    val isAdd = !rilRes!!.contains("un")
                                    ril_btn.setColorFilter(if(!isAdd) Color.WHITE else Color.GREEN)
                                    val rilDetail = layout.findViewById<TextView>(R.id.post_details_ril)
                                    activity.runOnUiThread {
                                        rilDetail.text = (Integer.parseInt(rilDetail.text as String) + (if(isAdd) 1 else -1)).toString()
                                    }
                                    it.isClickable = true
                                }
                            })
                        }
                        fek_btn.setOnClickListener { it ->
                            it.isClickable = false
                            val fekBody = JSONObject()
                            fekBody.put("user_id", userId)
                            fekBody.put("post_id", post_id)
                            val fekReq = Request.Builder().url("$BASE_URL/post/fek")
                                .post(fekBody.toString().toRequestBody("application/json".toMediaType())).build()
                            OkHttpClient().newCall(fekReq).enqueue(object: Callback {
                                override fun onFailure(call: Call, e: IOException) {}
                                @SuppressLint("SetTextI18n")
                                override fun onResponse(call: Call, response: Response) {
                                    val fekRes = response.body?.string()?.let { JSONObject(it).getString("data") }
                                    val isAdd = !fekRes!!.contains("un")
                                    fek_btn.setColorFilter(if(!isAdd) Color.WHITE else Color.RED)
                                    val fekDetail = layout.findViewById<TextView>(R.id.post_details_fek)
                                    activity.runOnUiThread {
                                        fekDetail.text = (Integer.parseInt(fekDetail.text as String) + (if(isAdd) 1 else -1)).toString()
                                    }
                                    it.isClickable = true
                                }
                            })
                        }
                        layout.findViewById<TextView>(R.id.post_details_ril).text = dat.getInt("ril").toString()
                        layout.findViewById<TextView>(R.id.post_details_fek).text = dat.getInt("fek").toString()
                        layout.findViewById<ImageView>(R.id.post_details_profil).setImageDrawable(userImage)
                        layout.findViewById<TextView>(R.id.post_details_username).text = userName
                        val comments = dat.getJSONArray("comments")
                        var commentTotal = 0
                        for (k in 0 until comments.length()){
                            commentTotal += 1 + comments.getJSONObject(k).getJSONArray("replies").length()
                        }
                        layout.findViewById<TextView>(R.id.post_details_comment_total).text = commentTotal.toString()
                        layout.findViewById<ImageButton>(R.id.post_details_comment_btn).setOnClickListener {
                            startActivity(Intent(activity, CommentActivity::class.java)
                                .putExtra("comments", comments.toString())
                                .putExtra("post_id", post_id))
                            activity.finish()
                        }

                        Glide.with(activity).load("$BASE_URL/uploads/" +
                                data.getJSONObject(i).getString("imageName")).into(layout.findViewById(R.id.post_details_image))
                        userDetails.addView(layout)
                    }

                    val maxLength = 80
                    val oriDesc = dat.getString("description")
                    val splitted = oriDesc.split("\r\n|\n|\r")
                    var desc = splitted[0]
                    var spannable: SpannableStringBuilder? = null
                    if(splitted.size > 1 || desc.length > maxLength) {
                        if(desc.length > maxLength) desc = desc.substring(0, maxLength) + "... "
                        else desc += "\n"
                        desc += "Baca selengkapnya"
                        spannable = SpannableStringBuilder(desc)
                        val colorSpan = ForegroundColorSpan(Color.parseColor("#04C4CC"))
                        spannable.setSpan(colorSpan, desc.lastIndexOf("Baca selengkapnya") ,desc.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    val description = layout.findViewById<TextView>(R.id.post_details_description)
                    var isDetail = false
                    activity.runOnUiThread {
                        if(spannable != null) (description.parent as LinearLayout).setOnClickListener {
                            if(!isDetail) description.text = oriDesc
                            else description.text = spannable
                            isDetail = !isDetail
                        }
                        description.text = spannable ?: oriDesc
                    }
                }
            }
        }
        return mainLayout
    }
}