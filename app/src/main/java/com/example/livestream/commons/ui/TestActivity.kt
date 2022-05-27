package com.example.livestream.commons.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.livestream.databinding.ActivityTestBinding
import io.github.ponnamkarthik.richlinkpreview.MetaData
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
import io.github.ponnamkarthik.richlinkpreview.RichPreview
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception

class TestActivity : AppCompatActivity() {

    lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        with(binding) {
            val linkPreview = RichPreview(object : ResponseListener {
                override fun onData(metaData: MetaData) {
                    with(metaData) {
                        Timber.d("asdf: $url, $imageurl, $title, $description, $sitename, $mediatype, $favicon")
                    }
                }

                override fun onError(e: Exception?) {
                    Timber.e(e)
                }
            })
//            val someUrl = "https://www.google.com/maps/place/R+Mall/@19.1840208,72.9491658,17z/data=!3m1!4b1!4m5!3m4!1s0x3be7b8fe01f3485d:0xada5a22795f3336a!8m2!3d19.1840208!4d72.9513545"
            val someUrl = "https://maps.app.goo.gl/oiFiwvm9abwBBAvG9"
            linkPreview.getPreview(someUrl)
        }
    }

    companion object {

        fun Context.launchTestActivity() {
            Intent(this, TestActivity::class.java).run {
                startActivity(this)
            }
        }
    }
}
