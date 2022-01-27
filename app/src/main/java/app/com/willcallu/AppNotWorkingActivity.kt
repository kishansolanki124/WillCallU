package app.com.willcallu

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_app_not_working.*

class AppNotWorkingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_not_working)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_back)


        bt_contact_us.setOnClickListener {
            giveFeedback()
        }
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
        intent.putExtra(Intent.EXTRA_SUBJECT, "App not working")
        if (!getDeviceName().isNullOrEmpty()) {
            val getDeviceOS = getDeviceOS()
            if (!getDeviceOS.isNullOrEmpty()) {
                intent.putExtra(
                    Intent.EXTRA_TEXT, "\n\n\nSent from " + getDeviceName()
                            + " - " + getDeviceOS
                )
            } else {
                intent.putExtra(Intent.EXTRA_TEXT, "\n\n\nSent via " + getDeviceName())
            }
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showSnackBar(getString(R.string.no_apps_found_to_send_mail), this)
        }
    }

    private fun showSnackBar(message: String, activity: Activity?) {
        if (activity != null) {
            val view =
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
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

    private fun capitalize(s: String?): String {
        if (s == null || s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first) + s.substring(1)
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}