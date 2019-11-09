package com.example.timeline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_asset.*

class AssetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset)


        if(intent.hasExtra("asset_data")){
            val container = intent.getParcelableExtra<AssetContainer>("asset_data")!!
            textView1.text = container.description
        }
    }
}
