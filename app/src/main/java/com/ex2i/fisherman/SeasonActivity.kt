package com.ex2i.fisherman

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ex2i.fisherman.Constant.APP_KEY
import com.ex2i.fisherman.Constant.PM_APP_KEY
import com.ex2i.fisherman.retrofit.*
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_season.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLDecoder

class SeasonActivity : AppCompatActivity() {

    private val realm = Realm.getDefaultInstance()

    private var retrofitInit: RetrofitInit? = null
    private var service: Service? = null
    private var service_pm: Service? = null
    private var name: String? = null
    private var decodeServiceKey: String? = null

    private var gpsTracker: GpsTracker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_season)

        setRetrofit()

        gpsTracker = GpsTracker(this)
        val latitude: String = gpsTracker!!.getLatitude().toString()
        val longitude: String = gpsTracker!!.getLongitude().toString()

        setWeather(latitude, longitude)

        //날씨 클릭
        iv_refresh.setOnClickListener {
            gpsTracker = GpsTracker(this)
            val latitude: String = gpsTracker!!.getLatitude().toString()
            val longitude: String = gpsTracker!!.getLongitude().toString()

            Log.e("TAG", latitude + longitude)
            setWeather(latitude, longitude)

            val currentDegree = iv_refresh.rotation
            ObjectAnimator.ofFloat(iv_refresh, View.ROTATION, currentDegree, currentDegree + -360f)
                .setDuration(300)
                .start()

            Toast.makeText(this, "날씨정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
        }

        spring_layout.setOnClickListener {
            val intent = Intent(this, SpringActivity::class.java)
            startActivity(intent)
        }

        summer_layout.setOnClickListener {
            val intent = Intent(this, SummerActivity::class.java)
            startActivity(intent)
        }

        autumn_lyaout.setOnClickListener {
            val intent = Intent(this, AutumnActivity::class.java)
            startActivity(intent)
        }

        winter_layout.setOnClickListener {
            val intent = Intent(this, WinterActivity::class.java)
            startActivity(intent)
        }

        insertModel(
            "벵에돔", "최대60cm", "", "어두운 흑갈색", "2월~6월", "암초 또는 자갈이 많은 연안의 해초가 무성한 곳"
            , "우리나라의 동해와 남해, 제주도 연안 해역, 일본 중부 이남, 동중국해, 타이완 근해"
        )

        insertModel(
            "벤자리", "최대40cm", "", "겨울에는 등쪽 희흑색이며 배족은 연한 빛, 봄과 여름에는 녹색을 띤 연한 갈색 바탕에 3줄의 폭이 넓은 황갈색 세로줄"
            , "6월~8월", "연안의 깊은 곳이나 해조류가 많은 곳"
            , "타이완, 동중국해, 일본 남부, 한국 연근해"
        )

        insertModel(
            "보구치", "약 30cm", "", "등쪽 연한 갈색, 측선을 경계로 밝아져 배쪽 은백색"
            , "5월~8월", "수심 40~100m의 바닥이 모래나 뻘로 된 근해"
            , "일본 남부, 동중국해, 타이완, 한국 연안"
        )

        insertModel(
            "볼락", "최대35cm", "0.8kg", "회갈색, 몸 옆구리 불분명한 검은색 가로무늬가 5~6줄(한국)"
            , "1월~2월", "암초로 된 연안"
            , "한국, 일본 등 북서태평양의 아열대 해역"
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private fun insertModel(
        title: String,
        size: String,
        weight: String,
        bodyColor: String,
        spawningSeason: String,
        habitat: String,
        distributionArea: String
    ) {
        realm.beginTransaction()

        val newItem = realm.createObject<Model>(nextId())

        newItem.title = title
        newItem.size = size
        newItem.weight = weight
        newItem.bodyColor = bodyColor
        newItem.spawningSeason = spawningSeason
        newItem.habitat = habitat
        newItem.distributionArea = distributionArea

        realm.commitTransaction()

    }

    //다음 id를 반환
    private fun nextId(): Int {//Realm 은 기본키 자동 증가 기능을 제공하지않아 가장 큰 id값을 얻고 1을 더한 값을 반환하는 메소드
        val maxId = realm.where<Model>()
            .max("id")// where 테이블 모든 값 얻기, 이메소드는 RealmQuery 객체를 반환, max는 가장 큰 값 얻음
        if (maxId != null) {
            return maxId.toInt() + 1
        }
        return 0
    }

    private fun setWeather(latitude: String, longitude: String) {
        //날씨API
        service?.getModel(APP_KEY, "1", latitude, longitude)
            ?.enqueue(object : Callback<Model_weather?> {
                override fun onResponse(
                    call: Call<Model_weather?>,
                    response: Response<Model_weather?>
                ) {
                    if (response.isSuccessful) {
                        val model = response.body()!!

                        if (model.weather?.hourlyList?.isEmpty()!!) {
                            Toast.makeText(this@SeasonActivity, "현재 위치를 받아올수 없습니다", Toast.LENGTH_LONG)
                                .show()
                            return
                        }
                        val tc =
                            model.weather?.hourlyList?.get(0)?.temperature?.tc?.toFloat()?.toInt()
                        val village = model.weather?.hourlyList?.get(0)?.grid?.village
                        val county = model.weather?.hourlyList?.get(0)?.grid?.county

                        name = model.weather?.hourlyList?.get(0)?.sky?.name

                        setImageView()

                        tv_temp.text = "$tc℃"
                        tv_city.text = "$county"
                        tv_village.text = "$village"

                        //키 상태 디코드
                        //이미 인코딩된것을 retrofit에서 한번 더 인코딩해서 보내기 때문에, 디코딩 한번 해줌
                        decodeServiceKey = URLDecoder.decode(PM_APP_KEY, "UTF-8")

                        //미세먼지API
                        service_pm?.getModel_PM(
                            decodeServiceKey,
                            "1",
                            "1",
                            county,
                            "DAILY",
                            "1.3",
                            "json"
                        )
                            ?.enqueue(object : Callback<Model_Item?> {
                                override fun onResponse(
                                    call: Call<Model_Item?>,
                                    response: Response<Model_Item?>
                                ) {
                                    if (response.isSuccessful) {

                                        val modelItem = response.body()!!

                                        if (modelItem.List?.isEmpty()!!) return
                                        val pm10Grade = modelItem.List?.get(0)?.pm10Grade

                                        if (pm10Grade.equals("1")) {
                                            tv_pm_result.text = "좋음"
                                        } else if (pm10Grade.equals("2")) {
                                            tv_pm_result.text = "보통"
                                        } else if (pm10Grade.equals("3")) {
                                            tv_pm_result.text = "나쁨"
                                        } else if (pm10Grade.equals("4")) {
                                            tv_pm_result.text = "매우나쁨"
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<Model_Item?>, t: Throwable) {}

                            })
                    }
                }

                override fun onFailure(call: Call<Model_weather?>, t: Throwable) {}
            })
    }


    private fun setRetrofit() {
        retrofitInit = RetrofitInit().instance
        service = retrofitInit!!.getService()
        service_pm = retrofitInit!!.getService_pm()
    }

    private fun setImageView() {
        when {
            (name == "맑음") ->
                iv_weather_icon.setImageResource(R.drawable.weather_sunny)
            (name == "구름조금") ->
                iv_weather_icon.setImageResource(R.drawable.weather_cloud_sunny)
            (name == "구름많음") ->
                iv_weather_icon.setImageResource(R.drawable.weather_cloud_sunny)
            (name == "구름많고 비") ->
                iv_weather_icon.setImageResource(R.drawable.weather_sunny_rainy)
            (name == "구름많고 비 또는 눈") ->
                iv_weather_icon.setImageResource(R.drawable.weather_sunny_rainy)
            (name == "흐림") ->
                iv_weather_icon.setImageResource(R.drawable.weather_cloud)
            (name == "흐리고 비") ->
                iv_weather_icon.setImageResource(R.drawable.weather_sunny_rainy)
            (name == "흐리고 눈") ->
                iv_weather_icon.setImageResource(R.drawable.weather_snow)
            (name == "흐리고 비 또는 눈") ->
                iv_weather_icon.setImageResource(R.drawable.weather_snow)
            (name == "흐리고 낙뢰") ->
                iv_weather_icon.setImageResource(R.drawable.weather_cloud_thunder)
            (name == "뇌우/비") ->
                iv_weather_icon.setImageResource(R.drawable.weather_cloud_thunder)
            (name == "뇌우/눈") ->
                iv_weather_icon.setImageResource(R.drawable.weather_cloud_thunder)
            (name == "뇌우/비 또는 눈") ->
                iv_weather_icon.setImageResource(R.drawable.weather_cloud_thunder)

        }

    }


}
