package app.com.willcallu

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.com.willcallu.room_db.SelectorModel
import kotlinx.android.synthetic.main.week_days_home_list_item.view.*


class WeekDaysAdapterForHomeList(private val upcomingReqestsArraylist: ArrayList<SelectorModel>) :
        RecyclerView.Adapter<WeekDaysAdapterForHomeList.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.week_days_home_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(upcomingReqestsArraylist[position])
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount() = upcomingReqestsArraylist.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindForecast(upcomingRequest: SelectorModel) {
            itemView.tv_day.text = upcomingRequest.name
            if (upcomingRequest.isSelected) {
                itemView.tv_day.setBackgroundResource(R.drawable.blue_circle)
            } else {
                itemView.tv_day.setBackgroundResource(R.drawable.gray_circle)
            }

//                val params = itemView.tv_day.layoutParams
//                params.height = 80
//                params.width = 80
//                itemView.tv_day.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
//                itemView.tv_day.layoutParams = params
        }
    }
}