package app.com.willcallu

import android.content.Context
import android.content.res.ColorStateList
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.com.willcallu.room_db.IconModel
import app.com.willcallu.room_db.Profile
import app.com.willcallu.room_db.SelectorModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ProfileListAdapter internal constructor(iconModels: ArrayList<IconModel>,
                                              context: Context,
                                              private val itemClick: (Profile, Int) -> Unit) :
        RecyclerView.Adapter<ProfileListAdapter.WordViewHolder>() {

    private val mContext: Context = context
    private val iconArrayList: ArrayList<IconModel> = iconModels
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mWords: List<Profile>? = null // Cached copy of words

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val itemView = mInflater.inflate(R.layout.profile_list_item, parent, false)
        return WordViewHolder(itemView, itemClick)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        if (mWords != null) {
            holder.bindForecast(position, mWords!![position])
        }
    }

    internal fun setProfiles(words: List<Profile>?) {
        mWords = words
        notifyDataSetChanged()
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    override fun getItemCount(): Int {
        return if (mWords != null)
            mWords!!.size
        else
            0
    }

    inner class WordViewHolder(itemView: View, private val itemClick: (Profile, Int) -> Unit) :
            RecyclerView.ViewHolder(itemView) {

        private val tv_profile_name: TextView = itemView.findViewById(R.id.tv_profile_name)
        private val card_view: CardView = itemView.findViewById(R.id.card_view)
        private val fab_profile: FloatingActionButton = itemView.findViewById(R.id.fab_profile)
        private val tv_profile_time: TextView = itemView.findViewById(R.id.tv_profile_time)
        private val tv_description: TextView = itemView.findViewById(R.id.tv_description)
        private val rv_week_days: RecyclerView = itemView.findViewById(R.id.rv_week_days)


        val layoutManagerWeekDays = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        private lateinit var weekDaysAdapter: WeekDaysAdapterForHomeList
        private lateinit var selectedWeekDays: ArrayList<Int>
        private lateinit var weekDaysArrayList: ArrayList<SelectorModel>

        fun bindForecast(position: Int, profile: Profile) {


            with(profile) {

                weekDaysArrayList = ArrayList()
                weekDaysArrayList.add(SelectorModel("S", false))
                weekDaysArrayList.add(SelectorModel("M", false))
                weekDaysArrayList.add(SelectorModel("T", false))
                weekDaysArrayList.add(SelectorModel("W", false))
                weekDaysArrayList.add(SelectorModel("T", false))
                weekDaysArrayList.add(SelectorModel("F", false))
                weekDaysArrayList.add(SelectorModel("S", false))

                tv_profile_name.text = profile.getMName()
                tv_profile_time.text = changeTimeFormat(profile.getMStartTime(), AppConstants.FORMAT_HH_mm, AppConstants.FORMAT_hh_mm_a) +
                        "-" + changeTimeFormat(profile.getMEndTime(), AppConstants.FORMAT_HH_mm, AppConstants.FORMAT_hh_mm_a)
                tv_description.text = profile.getMMessage()

                fab_profile.setImageDrawable(ContextCompat.getDrawable(mContext, iconArrayList.get(profile.mImage.toInt()).name))

                ViewCompat.setBackgroundTintList(fab_profile,
                        ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorControlHighlight)))

                fab_profile.isEnabled = false
                weekDaysAdapter = WeekDaysAdapterForHomeList(weekDaysArrayList)

                rv_week_days.adapter = weekDaysAdapter

                rv_week_days.layoutManager = layoutManagerWeekDays

                val sbWeekDays = profile.mWeekDays

                val weekList = sbWeekDays.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                selectedWeekDays = ArrayList()

                for (name in weekList) {
                    selectedWeekDays.add(name.toInt())

                    weekDaysArrayList[name.toInt()].isSelected = true
                }

                weekDaysAdapter.notifyDataSetChanged()

//                itemView.setOnClickListener {
//                    itemClick(this, position)
//                }

                card_view.setOnClickListener {
                    itemClick(this, position)
                }
            }
        }

        private fun changeTimeFormat(time: String, inputFormat: String, outputFormat: String): String {

            val inputFormatLocal = SimpleDateFormat(inputFormat, Locale.ENGLISH)
            val outputFormatLocal = SimpleDateFormat(outputFormat, Locale.ENGLISH)

            val date: Date
            val str: String

            try {
                date = inputFormatLocal.parse(time)
                str = outputFormatLocal.format(date)
                return str
            } catch (e: ParseException) {
                e.printStackTrace()
                return ""
            }
        }
    }
}