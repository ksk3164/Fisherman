package com.ex2i.fisherman

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_spring.*

class WinterActivity : AppCompatActivity(), OnItemClick {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_winter)

        val southSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("벵에돔", R.drawable.ic_fish_icon),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("쥐노래미", R.drawable.ic_fish_icon),
            RequestData("숭어", R.drawable.ic_fish_icon),
            RequestData("돌돔", R.drawable.item_doldom),
            RequestData("참돔", R.drawable.ic_fish_icon),
            RequestData("호래기", R.drawable.ic_fish_icon),
            RequestData("볼락", R.drawable.ic_fish_icon),
            RequestData("열기", R.drawable.ic_fish_icon),
            RequestData("붉은쏨뱅이", R.drawable.ic_fish_icon),
            RequestData("청어", R.drawable.ic_fish_icon)
        )

        val eastSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("가자미", R.drawable.ic_fish_icon),
            RequestData("황어", R.drawable.ic_fish_icon),
            RequestData("우럭", R.drawable.ic_fish_icon),
            RequestData("쥐노래미", R.drawable.ic_fish_icon),
            RequestData("대구횟대", R.drawable.ic_fish_icon),
            RequestData("대구", R.drawable.ic_fish_icon),
            RequestData("홍감펭", R.drawable.ic_fish_icon),
            RequestData("눈볼대", R.drawable.ic_fish_icon),
            RequestData("황점볼락", R.drawable.ic_fish_icon),
            RequestData("개볼락", R.drawable.ic_fish_icon),
            RequestData("농어", R.drawable.ic_fish_icon)
        )
        val jejuSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("벵에돔", R.drawable.ic_fish_icon),
            RequestData("무늬오징어", R.drawable.ic_fish_icon),
            RequestData("학꽁치", R.drawable.ic_fish_icon),
            RequestData("넙치농어", R.drawable.ic_fish_icon),
            RequestData("농어", R.drawable.ic_fish_icon),
            RequestData("숭어", R.drawable.ic_fish_icon)
        )

        val westSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("우럭", R.drawable.ic_fish_icon),
            RequestData("쥐노래미", R.drawable.ic_fish_icon)
        )

        rv_south_sea.adapter =
            Adapter(this, southSeaItem, this)
        rv_south_sea.layoutManager = GridLayoutManager(this, 3)

        rv_east_sea.adapter =
            Adapter(this, eastSeaItem, this)
        rv_east_sea.layoutManager = GridLayoutManager(this, 3)

        rv_jeju_sea.adapter =
            Adapter(this, jejuSeaItem, this)
        rv_jeju_sea.layoutManager = GridLayoutManager(this, 3)

        rv_west_sea.adapter =
            Adapter(this, westSeaItem, this)
        rv_west_sea.layoutManager = GridLayoutManager(this, 3)

    }
    override fun itemOnClick(value: String?, item: List<RequestData>) {
        TODO("Not yet implemented")
    }
}
