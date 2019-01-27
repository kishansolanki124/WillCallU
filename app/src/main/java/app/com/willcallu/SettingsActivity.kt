package app.com.willcallu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_back)

        try {
            val pInfo = applicationContext.packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            tv_app_version.text = getString(R.string.version, version.toString())
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        et_ring_time.setText(WillCallUApplication.getRingTime().toString())
        et_ring_time.setSelection(et_ring_time.text.toString().length)//cursor position at end of the string

        intro_ring_time.setOnClickListener {
            alertDialog(getString(R.string.intro_call_seconds))
        }

        tv_privacy_policy.setOnClickListener {
            openPrivacyPolicy()
        }

        tv_rate_app.setOnClickListener {
            rateApp()
        }

        tv_feedback.setOnClickListener {
            giveFeedback()
        }

        tv_share_app.setOnClickListener {
            shareApp()
        }

        tv_app_not_working.setOnClickListener {
            openAppNotWorkingActivity()
        }

        tv_what_is_will_call_u.setOnClickListener {
            openAboutAppActivity()
        }

        swContacts.isChecked = WillCallUApplication.getOnlyContactsBoolEnable()

        swContacts.setOnCheckedChangeListener { _, isChecked ->
            run {
                val sharedPreferences = getSharedPreferences(AppConstants.PREF_APP, Context.MODE_PRIVATE)
                WillCallUApplication.setOnlyContactsBoolEnable(sharedPreferences, isChecked)
            }
        }

        et_ring_time.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (et_ring_time.text.toString().isNotEmpty()) {
                    val digitsOnly: Boolean = try {
                        java.math.BigDecimal(et_ring_time.text.toString())
                        true
                    } catch (e: Exception) {
                        false
                    }

                    if (digitsOnly) {
                        setRingTime(et_ring_time.text.toString())
                    } else {
                        setRingTime("25")
                    }
                } else {
                    setRingTime("25")
                }
            }
        })
    }

    private fun openAboutAppActivity() {
        startActivity(Intent(this, WhatIsWillCallUActivity::class.java))
    }

    private fun openAppNotWorkingActivity() {
        startActivity(Intent(this, AppNotWorkingActivity::class.java))
    }

    private fun setRingTime(seconds: String) {
        val sharedPreferences = getSharedPreferences(AppConstants.PREF_APP, Context.MODE_PRIVATE)
        WillCallUApplication.setRingTime(sharedPreferences, seconds.toInt())
    }

    private fun alertDialog(message: String) {
        val alertDilog = AlertDialog.Builder(this, R.style.AlertDialogStyle).create()
        alertDilog.setTitle(getString(R.string.app_name))
        alertDilog.setMessage(message)
        alertDilog.setCancelable(false)

        alertDilog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok)) { _, _ ->
            alertDilog.dismiss()
        }
        alertDilog.show()
    }

    private fun rateApp() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    private fun shareApp() {
        val appPackageName = packageName
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.share_content, "https://play.google.com/store/apps/details?id=$appPackageName"))
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    private fun openPrivacyPolicy() {
        val url = getString(R.string.privacyPolicyURL)
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun giveFeedback() {

//        val intent = Intent(Intent.ACTION_SEND)//common intent
//        intent.type = "text/plain"
//        intent.data = Uri.parse("mailto:" + AppConstants.EMAIL_OF_DEVELOPER) // only email apps should handle this
//        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
//
//        try {
//            startActivity(Intent.createChooser(intent, "Email via..."))
//        } catch (e: Exception) {
//            e.printStackTrace()
//            showSnackBar(getString(R.string.no_apps_found_to_send_mail), this)
//        }

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, Array(1) { AppConstants.EMAIL_OF_DEVELOPER })
        intent.putExtra(Intent.EXTRA_SUBJECT, "subject")
        if (getDeviceName() != null && getDeviceName().isNotEmpty()) {
            val getDeviceOS = getDeviceOS()
            if (getDeviceOS != null && getDeviceOS.isNotEmpty())
                intent.putExtra(Intent.EXTRA_TEXT, "\n\n\nSent from " + getDeviceName()
                        + " - " + getDeviceOS)
            else
                intent.putExtra(Intent.EXTRA_TEXT, "\n\n\nSent via " + getDeviceName())
        }


        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showSnackBar(getString(R.string.no_apps_found_to_send_mail), this)
        }
    }

    private fun showSnackBar(message: String, activity: Activity?) {
        if (activity != null) {
            val view = (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
            val mySnackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)

            //setting text color for snackbar
            val snackBarView = mySnackbar.view
            val tv = snackBarView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
            tv.maxLines = 4
            tv.setTextColor(Color.WHITE)
            tv.maxLines = 4

            mySnackbar.show()
        }
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    fun getDeviceOS(): String {
        val builder = StringBuilder()
        builder.append(Build.VERSION.RELEASE)

        val fields = Build.VERSION_CODES::class.java.fields
        for (field in fields) {
            val fieldName = field.name
            var fieldValue = -1

            try {
                fieldValue = field.getInt(Any())
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            if (fieldValue == Build.VERSION.SDK_INT) {
                builder.append(fieldName)
            }
        }
        return builder.toString()
    }


    private fun capitalize(s: String?): String {
        if (s == null || s.length == 0) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first) + s.substring(1)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}