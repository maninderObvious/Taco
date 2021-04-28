package com.swiggy.taco

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swiggy.taco.card.card

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        card(this, attrs = null, R.attr.materialCardViewStyle)
    }
}