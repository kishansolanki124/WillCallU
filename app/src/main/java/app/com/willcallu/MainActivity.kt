package app.com.willcallu

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import app.com.willcallu.room_db.IconModel
import app.com.willcallu.room_db.Profile
import app.com.willcallu.room_db.ProfileViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private var secondsArrayList = ArrayList<Double>()
    private var foundedTimeArrayList = ArrayList<Profile>()
    private var mProfileViewModel: ProfileViewModel? = null
    private lateinit var iconArrayList: java.util.ArrayList<IconModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iconArrayList = java.util.ArrayList()
        iconArrayList.add(IconModel(R.drawable.driveing, false))
        iconArrayList.add(IconModel(R.drawable.eating, false))
        iconArrayList.add(IconModel(R.drawable.workout, false))
        iconArrayList.add(IconModel(R.drawable.sleeping, false))
        iconArrayList.add(IconModel(R.drawable.shower, false))
        iconArrayList.add(IconModel(R.drawable.meeting, false))

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val rlEmptylist = findViewById<RelativeLayout>(R.id.rl_emptylist)

        val adapter = ProfileListAdapter(iconArrayList, this) { profile, _ ->

            Log.d("profile", profile.mStartTime)

            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            val allPRofiles = mProfileViewModel!!.extractProfileByDay(dayOfWeek - 1)

            secondsArrayList = ArrayList()
            foundedTimeArrayList = ArrayList()

            if (allPRofiles != null) {
                for (prof in allPRofiles) {
                    if (isTimeBetweenTwoTime(prof.mStartTime, prof.mEndTime,
                                    SimpleDateFormat(AppConstants.FORMAT_HH_mm, Locale.ENGLISH).format(Date()))) {
                        findTimeDifference(prof, prof.mStartTime, prof.mEndTime)
                    }

                    //TODO IMPORTANT, this will select only last profile that was saved
//                    if (isTimeBetweenTwoTime(prof.mStartTime, prof.mEndTime, "22:59")) {
//                        showSnackBar("time to alarm", this)
//                        break
//                    } else {
//                        showSnackBar("no alarm please", this)
//                    }
                }

                if (secondsArrayList.size > 0) {
                    Log.d("lowest is ", Collections.min(secondsArrayList).toString())
                    val minIndex = secondsArrayList.indexOf(Collections.min(secondsArrayList))
                    Log.d("lowest minIndex ", minIndex.toString())
                    Log.d("profile name ", foundedTimeArrayList[minIndex].mName)
                }
            }

            //open edit profile that is already added
            startActivityForResult(Intent(this, EditProfileActivity::class.java)
                    .putExtra("ProfileModel", Gson().toJson(profile)), EDIT_PROFILE_ACTIVITY_REQUEST_CODE)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        mProfileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        fab.setOnClickListener {
            val intent = Intent(this, AddProfileActivity::class.java)
            startActivityForResult(intent, NEW_PROFILE_ACTIVITY_REQUEST_CODE)
        }

        mProfileViewModel!!.getmAllProfiles().observe(this, Observer { profiles ->
            // Update the cached copy of the profiles in the adapter.
            if (profiles != null && profiles.isNotEmpty()) {
                rlEmptylist.visibility = View.GONE
            } else {
                rlEmptylist.visibility = View.VISIBLE
            }
            adapter.setProfiles(profiles)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle item selection
        if (item != null) {
            when (item.itemId) {
                R.id.settings -> {
                    openSettings()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (requestCode == NEW_PROFILE_ACTIVITY_REQUEST_CODE) {

                    //profile added
                    if (data.getStringExtra("ProfileModel") != null) {

                        val gson = Gson()
                        val strObj = data.getStringExtra("ProfileModel")
                        val responseModel = gson.fromJson(strObj, Profile::class.java)

                        mProfileViewModel!!.insert(responseModel)

                        val weekDays = responseModel.mWeekDays.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        for (weekDay in weekDays) {
                            Log.d("mWeekDays", weekDay)
                        }
                    } else {
                        Toast.makeText(applicationContext, R.string.empty_not_saved, Toast.LENGTH_LONG).show()
                    }
                } else if (requestCode == EDIT_PROFILE_ACTIVITY_REQUEST_CODE) {
                    //profile edited
                    if (data.getStringExtra("ProfileModel") != null) {

                        val gson = Gson()
                        val strObj = data.getStringExtra("ProfileModel")
                        val responseModel = gson.fromJson(strObj, Profile::class.java)

                        mProfileViewModel!!.update(responseModel)

                        val weekDays = responseModel.mWeekDays.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        for (weekDay in weekDays) {
                            Log.d("mWeekDays", weekDay)
                        }
                    } else {
                        Toast.makeText(applicationContext, R.string.empty_not_saved, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    companion object {
        //room db
        val NEW_PROFILE_ACTIVITY_REQUEST_CODE = 1
        val EDIT_PROFILE_ACTIVITY_REQUEST_CODE = 2
    }

    //https://xd.adobe.com/spec/ddb59c67-f540-43dc-9f23-18a259d5c463/screen/1754017d-118d-4857-91e4-fbd3c73b27de/Home/
    private fun isTimeBetweenTwoTime(initialTime: String, finalTime: String, currentTimeStr: String): Boolean {

        val reg = Regex("^([0-1][0-9]|2[0-3]):([0-5][0-9])$")

        if (initialTime.matches(reg) && finalTime.matches(reg)
                && currentTimeStr.matches(reg)) {
            val valid: Boolean
            // Start Time
            var startTime = SimpleDateFormat(AppConstants.FORMAT_HH_mm, Locale.ENGLISH)
                    .parse(initialTime)
            val startCalendar = Calendar.getInstance()
            startCalendar.time = startTime

            // Current Time
            var currentTime = SimpleDateFormat(AppConstants.FORMAT_HH_mm, Locale.ENGLISH)
                    .parse(currentTimeStr)
            val currentCalendar = Calendar.getInstance()
            currentCalendar.time = currentTime

            // End Time
            var endTime = SimpleDateFormat(AppConstants.FORMAT_HH_mm, Locale.ENGLISH)
                    .parse(finalTime)
            val endCalendar = Calendar.getInstance()
            endCalendar.time = endTime

            //
            if (currentTime.compareTo(endTime) < 0) {

                currentCalendar.add(Calendar.DATE, 1)
                currentTime = currentCalendar.time

            }

            if (startTime.compareTo(endTime) < 0) {

                startCalendar.add(Calendar.DATE, 1)
                startTime = startCalendar.time

            }
            //
            if (currentTime.before(startTime)) {
                println(" Time is Lesser ")
                valid = false
            } else {

                if (currentTime.after(endTime)) {
                    endCalendar.add(Calendar.DATE, 1)
                    endTime = endCalendar.time

                }

                println("Comparing , Start Time /n " + startTime)
                println("Comparing , End Time /n " + endTime)
                println("Comparing , Current Time /n " + currentTime)

                if (currentTime.before(endTime)) {
                    println("RESULT, Time lies b/w")
                    valid = true
                } else {
                    valid = false
                    println("RESULT, Time does not lies b/w")
                }
            }
            return valid
        } else {
            return false
        }
    }


    private fun findTimeDifference(mProfileViewModel: Profile, startTime: String, endTime: String) {

        val start = Integer.parseInt(startTime.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        val end = Integer.parseInt(endTime.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        val startMinute = Integer.parseInt(startTime.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
        val endMinute = Integer.parseInt(endTime.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])

        var totalhours: Int
        val totalminutes: Int

        if (start == end) {
            if (endMinute - startMinute > 0) {
                totalhours = end - start
                if (endMinute - startMinute >= 0) {
                    totalminutes = endMinute - startMinute
                } else {
                    totalhours -= 1
                    totalminutes = (endMinute + 60) - startMinute
                }
            } else {
                totalhours = (24 - start) + end

                if (endMinute - startMinute >= 0) {
                    totalminutes = endMinute - startMinute
                } else {
                    totalhours -= 1
                    totalminutes = (endMinute + 60) - startMinute
                }
            }
        } else if (start < end) {
            totalhours = end - start
            if (endMinute - startMinute >= 0) {
                totalminutes = endMinute - startMinute
            } else {
                totalhours -= 1
                totalminutes = (endMinute + 60) - startMinute
            }
        } else {
            totalhours = (24 - start) + end
            if (endMinute - startMinute >= 0) {
                totalminutes = endMinute - startMinute
            } else {
                totalhours -= 1
                totalminutes = (endMinute + 60) - startMinute
            }
        }

        val printString = String.format("%02d:%02d", totalhours, totalminutes)

        Log.d("difference", "difference between " + startTime + " and " + endTime + " is " + printString)

        val temp: Int = 60 * totalminutes + 3600 * totalhours

        Log.d("seconds", temp.toString())
        secondsArrayList.add(temp.toDouble())
        foundedTimeArrayList.add(mProfileViewModel)
    }
}