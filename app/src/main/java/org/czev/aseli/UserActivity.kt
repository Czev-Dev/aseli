package org.czev.aseli

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

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
        fun getFragment(position: Int): Fragment {
            return fragments[position]
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
        val req = Request.Builder().url("$BASE_URL/post?username=" + pref.getString("username", "")).get().build()
        OkHttpClient().newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string()?.let { JSONObject(it).getJSONArray("data") } ?: return
                val userName = activity.findViewById<TextView>(R.id.user_username).text
                val userImage = activity.findViewById<ImageView>(R.id.user_profil_image).drawable
                val userDetails = mainLayout.findViewById<LinearLayout>(R.id.fragment_user_details)
                for(i in 0 until data.length()){
                    val dat = data.getJSONObject(i)
                    val layout = inflater.inflate(R.layout.post_details, userDetails, false)
                    activity.runOnUiThread {
                        layout.findViewById<TextView>(R.id.post_details_ril).text = (dat.getInt("ril") - dat.getInt("fek")).toString()
                        layout.findViewById<TextView>(R.id.post_details_comment_total).text = dat.getJSONArray("comments").length().toString()
                        layout.findViewById<ImageView>(R.id.post_details_profil).setImageDrawable(userImage)
                        layout.findViewById<TextView>(R.id.post_details_username).text = userName
                        Glide.with(activity).load("$BASE_URL/uploads/" +
                                data.getJSONObject(i).getString("imageName")).into(layout.findViewById(R.id.post_details_image))
                        userDetails.addView(layout)
                    }
                }
            }
        })

//        val scroll = mainLayout.findViewById<ScrollView>(R.id.fragment_user_details_scroll)
//        val profilLayout = activity.findViewById<ConstraintLayout>(R.id.user_profil_layout)
//        scroll.setOnScrollChangeListener {
//            view: View, i: Int, i1: Int, i2: Int, i3: Int ->
//            if(scroll.scrollY > 0) {
//                profilLayout.translationY = -scroll.scrollY.toFloat()
//                val height = profilLayout.height - scroll.scrollY
//                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, if(height > 1) height else 0)
//                profilLayout.layoutParams = params
//            } else {
//                profilLayout.translationY = 0f
//                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                profilLayout.layoutParams = params
//            }
//        }
        return mainLayout
    }
}