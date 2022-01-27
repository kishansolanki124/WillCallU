package app.com.willcallu

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TimePicker
import app.com.willcallu.room_db.IconModel
import app.com.willcallu.room_db.Profile
import app.com.willcallu.room_db.SelectorModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_new_profile.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AddProfileActivity : AppCompatActivity(), TextWatcher {

    var formEdited = false
    private lateinit var permissionDialog: PermissionDialog

    internal var onCancelClickListener: DialogInterface.OnClickListener =
            DialogInterface.OnClickListener { dialogInterface, i -> permissionDialog.dismiss() }

    internal var onOkClickListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { dialogInterface, i ->
        permissionDialog.dismiss()
        appDetailIntent(this)
    }


    private lateinit var adapter: DemoRecyclerViewAdapter
    private lateinit var weekDaysAdapter: WeekDaysAdapter
    private lateinit var weekDaysArrayList: ArrayList<SelectorModel>
    private lateinit var selectedWeekDays: ArrayList<Int>
    private lateinit var iconArrayList: ArrayList<IconModel>
    private var previousPosition: Int = -1
    private var iconInitialScrollPosition: Int = 3

    private var rightNow = Calendar.getInstance()

    private var startHour = rightNow.get(Calendar.HOUR_OF_DAY) // return the hour in 24 hrs format (ranging from 0-23)
    private var startMin = rightNow.get(Calendar.MINUTE)
    private var endHour = rightNow.get(Calendar.HOUR_OF_DAY) // return the hour in 24 hrs format (ranging from 0-23)
    private var endMin = rightNow.get(Calendar.MINUTE)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_new_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_back)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val layoutManagerWeekDays = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rv_demo.layoutManager = layoutManager

        rv_week_days.layoutManager = layoutManagerWeekDays

        val halfScreenWidth = getWidth() / 2

        rv_demo.setPadding(halfScreenWidth, 0, halfScreenWidth, 0)

        iconArrayList = ArrayList()
        iconArrayList.add(IconModel(R.drawable.driveing, false))
        iconArrayList.add(IconModel(R.drawable.eating, false))
        iconArrayList.add(IconModel(R.drawable.workout, false))
        iconArrayList.add(IconModel(R.drawable.sleeping, false))
        iconArrayList.add(IconModel(R.drawable.shower, false))
        iconArrayList.add(IconModel(R.drawable.meeting, false))

        weekDaysArrayList = ArrayList()
        weekDaysArrayList.add(SelectorModel("S", false))
        weekDaysArrayList.add(SelectorModel("M", false))
        weekDaysArrayList.add(SelectorModel("T", false))
        weekDaysArrayList.add(SelectorModel("W", false))
        weekDaysArrayList.add(SelectorModel("T", false))
        weekDaysArrayList.add(SelectorModel("F", false))
        weekDaysArrayList.add(SelectorModel("S", false))

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rv_demo)

        adapter = DemoRecyclerViewAdapter(this, iconArrayList)

        weekDaysAdapter = WeekDaysAdapter(weekDaysArrayList, false) { selectedModel, position ->
            weekDaysArrayList[position].isSelected = !selectedModel.isSelected
            weekDaysAdapter.notifyDataSetChanged()
        }

        bt_apply.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkPermissionsForO()
            } else {
                checkPermissions()
            }

//            for (item in weekDaysArrayList) {
//                Log.d("weekDays: ", item.name + " " + item.isSelected.toString())
//            }
//
//            Log.d("icon: ", previousPosition.toString())
        }

        et_sart_time.addTextChangedListener(this)
        et_end_time.addTextChangedListener(this)
        et_profile_name.addTextChangedListener(this)
        et_message.addTextChangedListener(this)

        //input type action done for message
        //et_message.setHorizontallyScrolling(false)
        et_message.setImeOptions(EditorInfo.IME_ACTION_DONE)
        et_message.setRawInputType(InputType.TYPE_CLASS_TEXT)

        et_sart_time.setOnClickListener {

            val dialog = TimePickerDialog(this, { view: TimePicker?, hourOfDay: Int, minute: Int ->
                getTime(hourOfDay, minute, et_sart_time)
                startHour = hourOfDay
                startMin = minute
            }, startHour, startMin, false)
            dialog.show()
        }

        et_end_time.setOnClickListener {

            val dialog = TimePickerDialog(this, { view: TimePicker?, hourOfDay: Int, minute: Int ->
                getTime(hourOfDay, minute, et_end_time)
                endHour = hourOfDay
                endMin = minute
            }, endHour, endMin, false)
            dialog.show()

        }

        rv_week_days.adapter = weekDaysAdapter

        rv_demo.adapter = adapter
        (rv_demo.itemAnimator as SimpleItemAnimator)
                .supportsChangeAnimations = false//disable blink/spark when item refreshed by notifyitemchanged from ondatachange

        rv_demo.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(layoutManager)
                    val pos = layoutManager.getPosition(centerView)
                    if (previousPosition != -1) {
                        iconArrayList[previousPosition].isSelected = false
                        adapter.notifyItemChanged(previousPosition)
                    }

                    iconArrayList[pos].isSelected = true
                    adapter.notifyItemChanged(pos)
                    previousPosition = pos

                    Log.d("iconposition", pos.toString())
                    iconInitialScrollPosition = pos
                }
            }
        })

        rv_demo.smoothScrollToPosition(iconInitialScrollPosition)
        val vh = rv_demo.findViewHolderForLayoutPosition(iconInitialScrollPosition)
        if (vh != null) {
            // Target view is available, so just scroll to it.
            rv_demo.smoothScrollToPosition(iconInitialScrollPosition)
        } else {
            // Target view is not available. Scroll to it.
            rv_demo.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                // From the documentation:
                // This callback will also be called if visible item range changes after a layout
                // calculation. In that case, dx and dy will be 0.This callback will also be called
                // if visible item range changes after a layout calculation. In that case,
                // dx and dy will be 0.
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    rv_demo.removeOnScrollListener(this)
                    if (dx == 0) {
                        newScrollTo(iconInitialScrollPosition)
                    }
                }
            })
            rv_demo.scrollToPosition(iconInitialScrollPosition)
        }
    }

    private fun validFields(): Boolean {
        if (et_profile_name.text.toString().trim().isEmpty()) {
            showSnackBar(getString(R.string.enter_profile_name), this)
            return false
        } else if (et_sart_time.text.toString().trim().isEmpty()) {
            showSnackBar(getString(R.string.enter_start_time), this)
            return false
        } else if (et_end_time.text.toString().trim().isEmpty()) {
            showSnackBar(getString(R.string.select_end_time), this)
            return false
        } else if (et_message.text.toString().trim().isEmpty()) {
            showSnackBar(getString(R.string.enter_message_to_be_sent), this)
            return false
        } else if (selectedWeekDays.isEmpty()) {
            showSnackBar(getString(R.string.select_at_least_one_day), this)
            return false
        } else
            return true
    }

    private fun newScrollTo(pos: Int) {
        rv_demo.smoothScrollToPosition(pos)
        val vh = rv_demo.findViewHolderForLayoutPosition(pos)
        if (vh != null) {
            // Target view is available, so just scroll to it.
            rv_demo.smoothScrollToPosition(pos)
        } else {
            // Target view is not available. Scroll to it.
            rv_demo.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                // From the documentation:
                // This callback will also be called if visible item range changes after a layout
                // calculation. In that case, dx and dy will be 0.This callback will also be called
                // if visible item range changes after a layout calculation. In that case,
                // dx and dy will be 0.
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    rv_demo.removeOnScrollListener(this)
                    if (dx == 0) {
                        newScrollTo(pos)
                    }
                }
            })
            rv_demo.scrollToPosition(pos)
        }
    }

    private fun getWidth(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.heightPixels
        return displayMetrics.widthPixels
    }

    private fun getTime(hourOfDay: Int, min: Int, tv: TextView) {
        val isPM = hourOfDay >= 12
        tv.text = String.format("%02d:%02d %s", if (hourOfDay == 12 || hourOfDay == 0) 12 else hourOfDay % 12, min,
                if (isPM) "PM" else "AM")
    }

    private fun saveProfile() {

        val sbWeekDays = StringBuilder()
        for (i in 0 until selectedWeekDays.size) {
            sbWeekDays.append(selectedWeekDays[i]).append(",")
        }

        val startTime = getTimeStampFromTime(et_sart_time.text.toString().trim())
        val endTime = getTimeStampFromTime(et_end_time.text.toString().trim())
        if (startTime.isEmpty()) {
            showSnackBar("Error", this)
            return
        }

        val profile = Profile(et_profile_name.text.toString().trim(), iconInitialScrollPosition.toString(), startTime,
                endTime, sbWeekDays.toString(), et_message.text.toString().trim())

        setResult(RESULT_OK, Intent().putExtra("ProfileModel", Gson().toJson(profile)))
        //setResult(RESULT_OK, Intent().putExtra("ProfileModel", profile))
        finish()
    }

    fun showSnackBar(message: String, activity: Activity?) {
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


    override fun afterTextChanged(s: Editable?) {


    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s!!.isNotEmpty())
            formEdited = true
    }

    override fun onBackPressed() {
        if (formEdited) {
            editExitAlert()
        } else {
            finish()
        }
    }

    private fun editExitAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.alert_cancel_changes))
                .setTitle(getString(R.string.Alert))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.exit)) { dialog, id -> finish() }
                .setNegativeButton(getString(R.string.stay_here)) { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    private fun getTimeStampFromTime(time: String): String {

        val inputPattern = AppConstants.FORMAT_hh_mm_a
        val outputPattern = AppConstants.FORMAT_HH_mm
        val inputFormat = SimpleDateFormat(inputPattern, Locale.ENGLISH)
        val outputFormat = SimpleDateFormat(outputPattern, Locale.ENGLISH)

        val date: Date
        val str: String

        try {
            date = inputFormat.parse(time)
            str = outputFormat.format(date)
            return str
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (formEdited) {
            editExitAlert()
        } else {
            finish()
        }
        return true
    }

    fun appDetailIntent(activity: FragmentActivity?) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity!!.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }


//    override fun onRequestPermissionsResult(requestCode: Int, permissionsList: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            MULTIPLE_PERMISSIONS -> {
//                if (grantResults.isNotEmpty()) {
//                    var permissionsDenied = ""
//                    for (per in permissionsList) {
//                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                            permissionsDenied += "\n" + per
//
//                        }
//                    }
//                    if (!permissionsDenied.isEmpty()) {
//                        // Show permissionsDenied
//                        permissionDialog = PermissionDialog(this, onOkClickListener, onCancelClickListener,
//                                resources.getString(R.string.msg_permanent_denied_profile_permission_storage),
//                                resources.getString(R.string.app_name))
//                    }
//                }
//                return
//            }
//        }
//    }

    private fun checkPermissions() {
        EasyPermissions.requestPermissions(this, object : EasyPermissions.PermissionCallbacks {
            override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
                //  permissions  granted.
                selectedWeekDays = ArrayList()

                for ((index, item) in weekDaysArrayList.withIndex()) {
                    if (item.isSelected) {
                        selectedWeekDays.add(index)
                    }
                    Log.d("weekDays: ", item.name + " " + item.isSelected.toString())
                }

                if (validFields()) {
                    saveProfile()
                }
            }

            override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
                showSnackBar("Permission denied", this@AddProfileActivity)
            }

            override fun onPermissionsPermanentlyDeclined(requestCode: Int, perms: List<String>) {
                //Open Settings activated
                permissionDialog = PermissionDialog(this@AddProfileActivity, onOkClickListener, onCancelClickListener,
                        resources.getString(R.string.msg_permanent_denied_profile_permission_storage), resources.getString(R.string.app_name))

            }
        }, resources.getString(R.string.msg_allow_permission), 110,
                Manifest.permission.READ_CALL_LOG, Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_PHONE_STATE)


//        var result: Int;
//        val listPermissionsNeeded = ArrayList<String>()
//        for (p in permissions) {
//            result = ContextCompat.checkSelfPermission(this, p)
//            if (result != PackageManager.PERMISSION_GRANTED) {
//                listPermissionsNeeded.add(p);
//            }
//        }
//
//        if (!listPermissionsNeeded.isEmpty()) {
//            ActivityCompat.requestPermissions(this, permissions,
//                    MULTIPLE_PERMISSIONS)
//            return false;
//        }
//        return true;
    }

    private fun checkPermissionsForO() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            EasyPermissions.requestPermissions(this, object : EasyPermissions.PermissionCallbacks {
                override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
                    //  permissions  granted.
                    selectedWeekDays = ArrayList()

                    for ((index, item) in weekDaysArrayList.withIndex()) {
                        if (item.isSelected) {
                            selectedWeekDays.add(index)
                        }
                        Log.d("weekDays: ", item.name + " " + item.isSelected.toString())
                    }

                    if (validFields()) {
                        saveProfile()
                    }
                }

                override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
                    showSnackBar("Permission denied", this@AddProfileActivity)
                }

                override fun onPermissionsPermanentlyDeclined(requestCode: Int, perms: List<String>) {
                    //Open Settings activated
                    permissionDialog = PermissionDialog(this@AddProfileActivity, onOkClickListener, onCancelClickListener,
                            resources.getString(R.string.msg_permanent_denied_profile_permission_storage), resources.getString(R.string.app_name))

                }
            }, resources.getString(R.string.msg_allow_permission), 110,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.READ_PHONE_STATE)
        }


//        var result: Int;
//        val listPermissionsNeeded = ArrayList<String>()
//        for (p in permissions) {
//            result = ContextCompat.checkSelfPermission(this, p)
//            if (result != PackageManager.PERMISSION_GRANTED) {
//                listPermissionsNeeded.add(p);
//            }
//        }
//
//        if (!listPermissionsNeeded.isEmpty()) {
//            ActivityCompat.requestPermissions(this, permissions,
//                    MULTIPLE_PERMISSIONS)
//            return false;
//        }
//        return true;
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}