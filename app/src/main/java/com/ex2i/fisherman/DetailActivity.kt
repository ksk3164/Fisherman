package com.ex2i.fisherman

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        tv_weight_result.text =result[0]?.weight
        tv_body_color_result.text = result[0]?.bodyColor
        tv_spawning_season_result.text =result[0]?.spawningSeason
        tv_habitat_result.text = result[0]?.habitat
        tv_distribution_area_result.text =result[0]?.distributionArea


    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
