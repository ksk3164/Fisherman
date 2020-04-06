package com.ex2i.fisherman

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_spring.*

class SpringActivity : AppCompatActivity(), OnItemClick {

    private var resultFishItems: MutableList<RequestData>? = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spring)

        val southSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("볼락", R.drawable.ic_fish_icon),
            RequestData("망상어", R.drawable.ic_fish_icon),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("도다리", R.drawable.ic_fish_icon),
            RequestData("쥐노래미", R.drawable.ic_fish_icon),
            RequestData("농어", R.drawable.ic_fish_icon),
            RequestData("참돔", R.drawable.ic_fish_icon)
        )

        val eastSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("가자미", R.drawable.ic_fish_icon),
            RequestData("임연수어", R.drawable.ic_fish_icon),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("강도다리", R.drawable.ic_fish_icon),
            RequestData("노랑볼락", R.drawable.ic_fish_icon)
        )
        val jejuSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("농어", R.drawable.ic_fish_icon),
            RequestData("넙치농어", R.drawable.ic_fish_icon),
            RequestData("벵에돔", R.drawable.ic_fish_icon),
            RequestData("무늬오징어", R.drawable.ic_fish_icon),
            RequestData("돌돔", R.drawable.item_doldom)
        )

        val westSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("우럭", R.drawable.ic_fish_icon),
            RequestData("숭어", R.drawable.ic_fish_icon),
            RequestData("쥐노래미", R.drawable.ic_fish_icon),
            RequestData("도다리", R.drawable.ic_fish_icon),
            RequestData("광어", R.drawable.ic_fish_icon),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("학꽁치", R.drawable.ic_fish_icon)
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
