package app.com.willcallu

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class WhatIsWillCallUActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_what_is_will_call_u)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_back)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}