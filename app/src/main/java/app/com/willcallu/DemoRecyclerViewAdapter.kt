package app.com.willcallu

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.com.willcallu.room_db.IconModel
import kotlinx.android.synthetic.main.profile_icon_item.view.*

class DemoRecyclerViewAdapter(
    private val context: Context,
    private val upcomingRequestsArraylist: ArrayList<IconModel>
) :
    RecyclerView.Adapter<DemoRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.profile_icon_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(upcomingRequestsArraylist[position], context)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount() = upcomingRequestsArraylist.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindForecast(upcomingRequest: IconModel, context: Context) {
            itemView.fab.setImageDrawable(ContextCompat.getDrawable(context, upcomingRequest.name))

            if (upcomingRequest.isSelected) {

                itemView.rb_test.isSelected = true
                itemView.fab.background.setColorFilter(
                    ContextCompat.getColor(context, R.color.colorControlHighlight),
                    PorterDuff.Mode.MULTIPLY
                )
//                    ViewCompat.setBackgroundTintList(itemView.fab,
//                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorControlHighlight)))
            } else {

                itemView.rb_test.isSelected = false

                itemView.fab.background.setColorFilter(
                    ContextCompat.getColor(context, R.color.gray),
                    PorterDuff.Mode.MULTIPLY
                )

//                    ViewCompat.setBackgroundTintList(itemView.fab,
//                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gray)))
            }
        }
    }
}