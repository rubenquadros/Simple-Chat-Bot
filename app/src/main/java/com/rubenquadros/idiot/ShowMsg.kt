package com.rubenquadros.idiot

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView


class ShowMsg(itemView: View) : RecyclerView.ViewHolder(itemView) {


    internal var leftText: TextView
    internal var rightText: TextView

    init {

        leftText = itemView.findViewById(R.id.botText) as TextView
        rightText = itemView.findViewById(R.id.myText) as TextView

    }
}