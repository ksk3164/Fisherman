package com.ex2i.fisherman

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_spring.*

class AutumnActivity : AppCompatActivity(), OnItemClick {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autumn)

        val southSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("벵에돔", R.drawable.ic_fish_icon),
            RequestData("무늬오징어", R.drawable.ic_fish_icon),
            RequestData("긴꼬리벵에돔", R.drawable.ic_fish_icon),
            RequestData("쥐노래미", R.drawable.ic_fish_icon),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("농어", R.drawable.ic_fish_icon),
            RequestData("참돔", R.drawable.ic_fish_icon),
            RequestData("돌돔", R.drawable.item_doldom),
            RequestData("전갱이", R.drawable.item_jeongangi),
            RequestData("고등어", R.drawable.ic_fish_icon),
            RequestData("갈치", R.drawable.ic_fish_icon),
            RequestData("부시리", R.drawable.item_busiri)
        )

        val eastSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("가자미", R.drawable.ic_fish_icon),
            RequestData("황어", R.drawable.ic_fish_icon),
            RequestData("우럭", R.drawable.ic_fish_icon),
            RequestData("붕장어", R.drawable.ic_fish_icon),
            RequestData("부시리", R.drawable.ic_fish_icon),
            RequestData("연어병치", R.drawable.ic_fish_icon),
            RequestData("띠볼락", R.drawable.ic_fish_icon),
            RequestData("갈치", R.drawable.ic_fish_icon),
            RequestData("벵에돔", R.drawable.ic_fish_icon),
            RequestData("무늬오징어", R.drawable.ic_fish_icon),
            RequestData("대구", R.drawable.ic_fish_icon),
            RequestData("열기", R.drawable.ic_fish_icon),
            RequestData("농어", R.drawable.ic_fish_icon)
        )
        val jejuSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("벵에돔", R.drawable.ic_fish_icon),
            RequestData("무늬오징어", R.drawable.ic_fish_icon),
            RequestData("돌돔", R.drawable.item_doldom),
            RequestData("고등어", R.drawable.ic_fish_icon),
            RequestData("갈전갱이", R.drawable.item_jeongangi),
            RequestData("독가시치", R.drawable.ic_fish_icon),
            RequestData("갈치", R.drawable.ic_fish_icon),
            RequestData("보리멸", R.drawable.ic_fish_icon),
            RequestData("자바리", R.drawable.ic_fish_icon),
            RequestData("꼬치고기", R.drawable.item_kkochigogi)
        )

        val westSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("우럭", R.drawable.ic_fish_icon),
            RequestData("숭어", R.drawable.ic_fish_icon),
            RequestData("전어", R.drawable.ic_fish_icon),
            RequestData("삼치", R.drawable.ic_fish_icon),
            RequestData("쥐노래미", R.drawable.ic_fish_icon),
            RequestData("광어", R.drawable.ic_fish_icon),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("참돔", R.drawable.ic_fish_icon),
            RequestData("농어", R.drawable.ic_fish_icon),
            RequestData("대구", R.drawable.ic_fish_icon),
            RequestData("학꽁치", R.drawable.ic_fish_icon),
            RequestData("고등어", R.drawable.ic_fish_icon),
            RequestData("쭈꾸미", R.drawable.ic_fish_icon),
            RequestData("갑오징어", R.drawable.ic_fish_icon)
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
