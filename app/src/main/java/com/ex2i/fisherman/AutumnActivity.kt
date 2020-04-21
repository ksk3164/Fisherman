package com.ex2i.fisherman

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_spring.*

class AutumnActivity : AppCompatActivity(), OnItemClick {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autumn)

        val southSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("벵에돔", R.drawable.item_bangedom),
            RequestData("무늬오징어", R.drawable.item_muniojinga),
            RequestData("긴꼬리벵에돔", R.drawable.item_kinggoribangedom),
            RequestData("쥐노래미", R.drawable.item_geenoraemi),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("농어", R.drawable.item_nonga),
            RequestData("참돔", R.drawable.item_chamdom),
            RequestData("돌돔", R.drawable.item_doldom),
            RequestData("전갱이", R.drawable.item_jeongangi),
            RequestData("고등어", R.drawable.item_godoenga),
            RequestData("갈치", R.drawable.item_galchi),
            RequestData("부시리", R.drawable.item_busiri)
        )

        val eastSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("각시가자미", R.drawable.item_gaksigajami),
            RequestData("황어", R.drawable.item_hwanga),
            RequestData("조피볼락", R.drawable.item_jopibolak),
            RequestData("붕장어", R.drawable.item_bungjanga),
            RequestData("부시리", R.drawable.item_busiri),
            RequestData("연어병치", R.drawable.item_yeonabyuongchi),
            RequestData("띠볼락", R.drawable.item_ddibolak),
            RequestData("갈치", R.drawable.item_galchi),
            RequestData("벵에돔", R.drawable.item_bangedom),
            RequestData("무늬오징어", R.drawable.item_muniojinga),
            RequestData("대구", R.drawable.item_daegu),
            RequestData("열기", R.drawable.item_yeolgi),
            RequestData("농어", R.drawable.item_nonga)
        )
        val jejuSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("벵에돔", R.drawable.item_bangedom),
            RequestData("무늬오징어", R.drawable.item_muniojinga),
            RequestData("돌돔", R.drawable.item_doldom),
            RequestData("고등어", R.drawable.item_godoenga),
            RequestData("갈전갱이", R.drawable.item_jeongangi),
            RequestData("독가시치", R.drawable.item_dockgasichi),
            RequestData("갈치", R.drawable.item_galchi),
            RequestData("보리멸", R.drawable.item_borimyul),
            RequestData("자바리", R.drawable.item_jabari),
            RequestData("꼬치고기", R.drawable.item_kkochigogi)
        )

        val westSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("조피볼락", R.drawable.item_jopibolak),
            RequestData("숭어", R.drawable.item_sunga),
            RequestData("전어", R.drawable.item_jeona),
            RequestData("삼치", R.drawable.item_samchi),
            RequestData("쥐노래미", R.drawable.item_geenoraemi),
            RequestData("광어", R.drawable.item_gwanga),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("참돔", R.drawable.item_chamdom),
            RequestData("농어", R.drawable.item_nonga),
            RequestData("대구", R.drawable.item_daegu),
            RequestData("학꽁치", R.drawable.item_hakggongchi),
            RequestData("고등어", R.drawable.item_godoenga),
            RequestData("쭈꾸미", R.drawable.item_jjuggumi),
            RequestData("갑오징어", R.drawable.item_gapojinga)
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }
}
