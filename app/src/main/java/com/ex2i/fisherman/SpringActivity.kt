package com.ex2i.fisherman

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_spring.*

class SpringActivity : AppCompatActivity(), OnItemClick {

    private var resultFishItems: ArrayList<RequestData>? = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spring)

        val southSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("볼락", R.drawable.item_bolack),
            RequestData("망상어", R.drawable.item_mangsang),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("도다리", R.drawable.item_dodari),
            RequestData("쥐노래미", R.drawable.item_geenoraemi),
            RequestData("농어", R.drawable.item_nonga),
            RequestData("참돔", R.drawable.item_chamdom)
        )

        val eastSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("각시가자미", R.drawable.item_gaksigajami),
            RequestData("임연수어", R.drawable.item_imyeonsua),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("강도다리", R.drawable.item_dodari),
            RequestData("노랑볼락", R.drawable.item_norangbolak)
        )
        val jejuSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("농어", R.drawable.item_nonga),
            RequestData("넙치농어", R.drawable.item_nupchinoga),
            RequestData("벵에돔", R.drawable.item_bangedom),
            RequestData("무늬오징어", R.drawable.item_muniojinga),
            RequestData("돌돔", R.drawable.item_doldom)
        )

        val westSeaItem: MutableList<RequestData> = mutableListOf(
            RequestData("조피볼락", R.drawable.item_jopibolak),
            RequestData("숭어", R.drawable.item_sunga),
            RequestData("쥐노래미", R.drawable.item_geenoraemi),
            RequestData("도다리", R.drawable.item_dodari),
            RequestData("광어", R.drawable.item_gwanga),
            RequestData("감성돔", R.drawable.item_gamsungdom),
            RequestData("학꽁치", R.drawable.item_hakggongchi)
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

    override fun itemOnClick(name: String?, image:Int?) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("name",name)
        intent.putExtra("image",image)
        startActivity(intent)
    }
}
