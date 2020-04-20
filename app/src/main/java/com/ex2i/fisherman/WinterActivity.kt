package com.ex2i.fisherman

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_spring.*

class WinterActivity : AppCompatActivity(), OnItemClick {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_winter)

        val southSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("벵에돔", R.drawable.item_bangedom),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("쥐노래미", R.drawable.item_geenoraemi),
            RequestData("숭어", R.drawable.item_sunga),
            RequestData("돌돔", R.drawable.item_doldom),
            RequestData("참돔", R.drawable.item_chamdom),
            RequestData("호래기", R.drawable.item_horagi),
            RequestData("볼락", R.drawable.item_bolack),
            RequestData("열기", R.drawable.item_yeolgi),
            RequestData("붉은쏨뱅이", R.drawable.item_ssombangi),
            RequestData("청어", R.drawable.item_chunga)
        )

        val eastSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("각시가자미", R.drawable.item_gaksigajami),
            RequestData("황어", R.drawable.item_hwanga),
            RequestData("조피볼락", R.drawable.item_jopibolak),
            RequestData("쥐노래미", R.drawable.item_geenoraemi),
            RequestData("대구횟대", R.drawable.item_daeguhoedae),
            RequestData("대구", R.drawable.item_daegu),
            RequestData("홍감펭", R.drawable.item_hongampeng),
            RequestData("눈볼대", R.drawable.item_nonboldae),
            RequestData("황점볼락", R.drawable.item_hwangjumbolack),
            RequestData("개볼락", R.drawable.item_gaebolack),
            RequestData("농어", R.drawable.item_nonga)
        )
        val jejuSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("벵에돔", R.drawable.item_bangedom),
            RequestData("무늬오징어", R.drawable.item_muniojinga),
            RequestData("학꽁치", R.drawable.item_hakggongchi),
            RequestData("넙치농어", R.drawable.item_nupchinoga),
            RequestData("농어", R.drawable.item_nonga),
            RequestData("숭어", R.drawable.item_sunga)
        )

        val westSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("조피볼락", R.drawable.item_jopibolak),
            RequestData("쥐노래미", R.drawable.item_geenoraemi)
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
    override fun itemOnClick(name: String?, image: Int?) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("name",name)
        intent.putExtra("image",image)
        startActivity(intent)
    }
}
