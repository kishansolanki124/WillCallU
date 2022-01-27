package app.com.willcallu

import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.com.willcallu.room_db.SelectorModel
import kotlinx.android.synthetic.main.week_days_profile_details_item.view.*

class WeekDaysAdapter(private val upcomingRequestsArraylist: ArrayList<SelectorModel>,
                      imageSmallSize: Boolean,
                      private val itemClick: (SelectorModel, Int) -> Unit) :
        RecyclerView.Adapter<WeekDaysAdapter.ViewHolder>() {

    private val imageSmall: Boolean = imageSmallSize//used to show this list as a small checkbox view

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.week_days_profile_details_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(imageSmall, position, upcomingRequestsArraylist[position])
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount() = upcomingRequestsArraylist.size

    class ViewHolder(view: View, private val itemClick: (SelectorModel, Int) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bindForecast(imageSmall: Boolean, position: Int, upcomingRequest: SelectorModel) {
            with(upcomingRequest) {

                itemView.cb_text.text = upcomingRequest.name
                itemView.cb_text.isChecked = upcomingRequest.isSelected

                if (imageSmall) {
                    val params = itemView.cb_text.layoutParams
                    params.height = 80
                    params.width = 80
                    itemView.cb_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    itemView.cb_text.layoutParams = params

                    //disabling on click effect of checkbox when showing it in list of home screen
                    itemView.cb_text.setOnCheckedChangeListener { _, isChecked ->
                        itemView.cb_text.isChecked = !isChecked
                    }
                }
                itemView.cb_text.setOnClickListener {
                    itemClick(this, position)
                }

                itemView.setOnClickListener {
                    itemClick(this, position)
                }
            }
        }
    }
}