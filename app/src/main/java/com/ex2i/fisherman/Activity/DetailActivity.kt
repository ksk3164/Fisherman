package com.ex2i.fisherman.Activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ex2i.fisherman.Model
import com.ex2i.fisherman.R
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    private val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val name = intent.getStringExtra("name")
        val image = intent.getIntExtra("image", 0)

        iv_fish.setImageResource(image)
        tv_fish_name.text = name

        val result: RealmResults<Model> = realm.where<Model>()
            .equalTo("title", name)
            .findAll()

        tv_size_result.text = result[0]?.size
        if (result[0]?.weight.equals("")) {
            view_weight.visibility = View.GONE
            weight_layout.visibility = View.GONE
        } else {
            view_weight.visibility = View.VISIBLE
            weight_layout.visibility = View.VISIBLE
            tv_weight_result.text = result[0]?.weight
        }
        tv_body_color_result.text = result[0]?.bodyColor
        tv_spawning_season_result.text = result[0]?.spawningSeason
        tv_habitat_result.text = result[0]?.habitat
        tv_distribution_area_result.text = result[0]?.distributionArea
        tv_description_result.text = result[0]?.description

    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }
}
