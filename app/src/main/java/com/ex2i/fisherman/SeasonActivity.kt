package com.ex2i.fisherman

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ex2i.fisherman.Constant.APP_KEY
import com.ex2i.fisherman.Constant.PM_APP_KEY
import com.ex2i.fisherman.Util.PreferenceUtil
import com.ex2i.fisherman.retrofit.*
import io.realm.Realm
import io.realm.RealmResults
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


        checkIsFirstRun()

    }

    private fun checkIsFirstRun() {
        val isFirstRun = PreferenceUtil.getInstance(this).getBooleanExtra("isFirstRun")

        if (!isFirstRun) {
            realmInsert()
            PreferenceUtil.getInstance(this).putBooleanExtra("isFirstRun", true)
        }
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
                            Toast.makeText(
                                this@SeasonActivity,
                                "현재 위치를 받아올수 없습니다",
                                Toast.LENGTH_LONG
                            )
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

    private fun realmInsert() {
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

        insertModel(
            "보리멸", "최대 30cm", "", "등쪽 푸른빛의 연한 갈색, 배쪽 은백색"
            , "6∼8월", "연안 가까이의 모래 바닥이나 강 하구의 간석지"
            , "서부태평양, 인도양, 지중해 등 열대 해역"
        )

        insertModel(
            "붕장어", "암컷 90㎝, 수컷 40㎝", "", "옆구리와 등쪽 - 암갈색, 배쪽 - 흰색"
            , "봄~여름(추정)", "깊고 따뜻한 바다"
            , "대서양·인도양·태평양"
        )
        insertModel(
            "부시리", "100cm 이상", "", "등쪽 선명한 푸른색, 배쪽 은백색, 몸 중앙 옆구리 짙은 노란색의 세로띠"
            , "5∼8월", "연안의 수면 가까이"
            , "한국·일본·타이완을 포함한 남태평양"
        )
        insertModel(
            "참돔", "최대 몸길이 100cm 이상", "", "몸 등쪽은 붉은색, 배쪽은 노란색 또는 흰색"
            , "4∼6월", "수심 10∼200m의 바닥 기복이 심한 암초 지역"
            , "동남아시아, 타이완, 남중국해, 일본, 한국 연근해 등 북서태평양의 아열대 해역"
        )
        insertModel(
            "청어", "최대 몸길이 46cm", "", "등쪽 암청색, 배쪽 은백색"
            , "겨울~봄", "수온이 2∼10℃, 수심 0∼150m의 연안, 민물, 강 어귀"
            , "백해 등의 북극해, 일본 북부, 한국 연근해 등의 서부태평양"
        )
        insertModel(
            "대구", "40~110cm, 최대 119cm", "", "배쪽은 흰색이며 등쪽으로 갈수록 갈색으로 변함, 진한 갈색 점이 있음"
            , "1~3월", "연안 또는 대륙사면"
            , "한국, 일본, 알래스카 등의 북태평양 연안"
        )
        insertModel(
            "대구횟대", "30cm", "", "노란색, 등쪽 갈색, 배쪽 흰색"
            , "6∼7월", "수심 50∼150m의 대륙붕"
            , "한국 동해, 일본 홋카이도, 사할린섬, 오호츠크해 등의 북서태평양"
        )
        insertModel(
            "띠볼락",
            "40cm",
            "",
            "연한 갈색 또는 연한 회색에 3개의 넓은 흑갈색 가로띠가 있는 것과 연한 갈색에 많은 어두운 갈색 얼룩무늬가 교차되어 있음"
            ,
            "봄",
            "천해의 암초지역"
            ,
            "한국(중부·남부)·일본"
        )
        insertModel(
            "독가시치", "40cm", "", "등쪽 황갈색 바탕에 흰색 점, 배쪽 연한 노란색 바탕에 흰색 무늬"
            , "7~8월", "연안의 얕은 암초 지역"
            , "서부태평양의 열대 및 아열대 해역"
        )
        insertModel(
            "도다리", "30cm", "", "눈 있는 쪽은 불규칙한 형태의 짙은 갈색 무늬, 눈이 없는 쪽은 흰색"
            , "가을∼겨울", "바닥이 모래와 진흙으로 된 연안지역"
            , "북서태평양의 온대 해역"
        )
        insertModel(
            "돌돔", "40cm 이상", "", "청흑색, 옆구리에 7개의 뚜렷한 검은색 세로띠"
            , "5∼8월", "연안 암초지역"
            , "한국(다도해·제주도), 일본 연해, 동중국해, 남중국해"
        )
        insertModel(
            "개볼락", "25cm(검은 점을 가진 종류 35cm)", "", "어두운 갈색 바탕에 배는 연한 빛"
            , "1∼2월경(부산 근해) 또는 4∼5월경(일본 도쿄 근해)", "근해 암초지역"
            , "한국 중부·남부, 일본 홋카이도 남쪽"
        )
        insertModel(
            "각시가자미", "48cm", "", "황갈색으로 암갈색의 반점이 있음. 눈이 없는 쪽은 흰색 또는 연한 노란색"
            , "", "수심 400m 이하의 바다 밑바닥"
            , "한국·일본·오호츠크해·사할린·베링해·알래스카"
        )
        insertModel(
            "갈치", "50~100cm, 최대 150cm", "", "은백색"
            , "4~9월", "연안의 물 속"
            , "우리나라의 서해와 남해. 일본, 중국을 비롯한 세계의 온대 또는 아열대 해역"
        )
        insertModel(
            "감성돔", "약 30~50cm", "", "등쪽은 금속 광택을 띤 회흑색, 배쪽 부분은 연함. 옆구리에 세로로 그어진 가늘고 불분명한 선이 있다"
            , "4~6월", "바닥에 해조류가 있고 모래질이나 암초로 된 수심 50m 이내의 연안에 주로 서식"
            , "우리나라의 서해와 남해, 일본의 홋카이도 이남, 동중국해 등에 분포"
        )
        insertModel(
            "강도다리", "30∼40cm(최대 몸길이 91cm)", "9kg", "눈이 있는 쪽-짙은 갈색, 눈이 없는 쪽-연한 노란색"
            , "2∼3월(북쪽)", "연안 근처의 150m 내의 수심"
            , "북태평양의 전 해역"
        )
        insertModel(
            "갑오징어", "몸길이 17cm, 나비 9cm", "", "수컷의 등면은 암갈색 가로줄무늬가 있으나 암컷은 무늬가 없음. 배면은 연갈색임"
            , "4~6월", "바다"
            , "한국, 일본, 중국, 오스트레일리아 북부"
        )
        insertModel(
            "쥐노래미", "40cm", "", "장소에 따라 노란색, 적갈색, 자갈색, 흑갈색 등"
            , "10∼1월", "바닥이 모래나 진흙으로 된 곳이거나 암초 또는 인공암초가 있는 곳"
            , "북서태평양의 온대 해역"
        )
        insertModel(
            "고등어", "30cm", "", "등쪽 암청색, 중앙에서부터 배쪽 은백색"
            , "", "바다"
            , "태평양, 대서양, 인도양의 온대 및 아열대 해역"
        )
        insertModel(
            "광어", "60~80cm", "", "황갈색 바탕에 짙은 갈색과 흰색 점, 반대쪽은 흰색"
            , "2~6월", "바다 속 모래 바닥"
            , "우리나라, 중국, 일본의 인근 해역"
        )
        insertModel(
            "학꽁치", "40cm", "", "등쪽 청록색, 배쪽 은백색"
            , "4∼7월", "수심 50m 이내의 내만이나 강, 호수"
            , "중국 남부, 일본, 한국 연근해"
        )
        insertModel(
            "한치", "외투장 183mm", "", ""
            , "", "15∼70m의 연안"
            , "열대 서인도 태평양, 남동중국해, 한국, 일본남부, 오스트레일리아 북부"
        )
        insertModel(
            "홍감펭", "최대 몸길이 30cm", "", "전체 붉은 오렌지색, 3줄의 폭이 넓고 짙은 붉은색 가로띠"
            , "겨울에서 봄", "수심 200∼500m의 대륙붕 가장자리로서 바닥이 조개껍데기가 섞인 모래질인 곳"
            , "타이완, 동중국해, 일본 남부해, 한국 남부"
        )
        insertModel(
            "호래기", "60mm", "", "연한 자주빛"
            , "3월", "연안"
            , "우리나라 전 연안, 동남아시아, 유럽"
        )
        insertModel(
            "황어", "약 45cm", "", "등쪽 노란 갈색이나 푸른빛을 띤 검은색, 옆구리와 배쪽 은백색"
            , "3∼4월", "수심 10∼150cm의 물이 비교적 맑은 강 하류"
            , "한국, 일본, 사할린섬, 중국 동북부, 연해지방, 시베리아"
        )
        insertModel(
            "황점볼락", "35cm 이상", "", "암황갈색, 옆구리에 4∼5줄의 불규칙하고 희미한 가로띠"
            , "11∼1월", "근해의 암초"
            , "한국(남해), 일본(홋카이도)"
        )
        insertModel(
            "임연수어", "27~50cm", "", "등쪽 암갈색, 배쪽 황백색"
            , "9월∼이듬해 2월", "수심 100∼200m 사이의 바위나 자갈로 된 암초 지대"
            , "북태평양의 오호츠크해, 동해 등"
        )
        insertModel(
            "자바리", "60cm 이상", "", "다갈색 바탕, 옆구리에 6줄의 흑갈색 가로띠"
            , "", "수심 50m 이내의 암초 지역"
            , "한국(남부·제주도), 일본, 타이완, 중국, 말레이시아, 인도"
        )
        insertModel(
            "전어", "15∼31cm", "", "등쪽 암청색, 배쪽 은백색"
            , "3∼8월(산란 성기 4∼5월)", "서식 수심은 보통 30m 이내의 바다(연안)"
            , "동중국해, 일본 중부 이남, 한국 남해"
        )
        insertModel(
            "전갱이", "약 40cm", "", "등쪽 암청색, 배쪽 은백색"
            , "4∼7월", "수심 10∼100m의 연안이나 외양"
            , "타이완, 동중국해, 일본 남부, 한국 등 북서태평양의 열대 해역"
        )
        insertModel(
            "쭈꾸미", "약 20cm", "", "변화가 많으나 대체로 자회색"
            , "5∼6월", "수심 10m 정도 연안의 바위틈"
            , "타이완, 동중국해, 일본 남부, 한국 등 북서태평양의 열대 해역"
        )
        insertModel(
            "조피볼락", "약 40cm", "", "전체 흑갈색, 배쪽 회색"
            , "4∼6월", "수심 10∼100m인 연안의 암초지대"
            , "타이완, 동중국해, 일본 남부, 한국 등 북서태평양의 열대 해역"
        )
        insertModel(
            "긴꼬리뱅에돔", "60~70㎝", "", ""
            , "", ""
            , ""
        )
        insertModel(
            "꼬치고기", "30cm", "", "등쪽은 붉은색을 띤 황갈색, 배쪽은 백색"
            , "6∼7월", "수심 60m 부근"
            , "우리나라, 일본 남부해, 동중국해, 인도양, 호주 등지"
        )
        insertModel(
            "망상어", "15~25cm", "", "등쪽은 짙은 푸른색 또는 적갈색, 배쪽은 은백색"
            , "4~6월", "수심 30m 정도의 얕은 바다"
            , "우리나라, 중국, 일본 등의 북서태평양"
        )
        insertModel(
            "무늬오징어", "몸길이 약 21cm, 나비 약 10cm", "", "등쪽에 여러 개의 암회갈색 가로무늬, 타원형의 반문(수컷)"
            , "", "수심 15~100m정도의 연안"
            , "남서태평양·남동중국해·한국(다도해 이남)·일본"
        )
        insertModel(
            "눈볼대", "10~15cm, 암컷 최대 40cm", "", "전체적으로 붉은색, 배쪽으로 갈수록 연해짐"
            , "7~10월", "깊이 80~150m의 연안"
            , "우리나라의 남해, 일본, 인도네시아, 오스트레일리아 북서부를 지나는 서태평양"
        )
        insertModel(
            "농어", "1m", "", "등쪽-푸른색, 배쪽-은백색"
            , "11월∼이듬해 4월", "연안이나 만입구의 수심 50∼80m 되는 약간 깊은 곳"
            , "동중국해, 타이완, 일본, 한국 연근해"
        )
        insertModel(
            "노랑볼락", "최대 몸길이 25cm", "", "전체 황갈색(드물게 푸른빛을 띠기도 함)"
            , "11월", "연안, 만의 입구, 기수역"
            , "한국, 일본 홋카이도, 연해지방 등의 북서태평양 온대 해역"
        )
        insertModel(
            "넙치농어", "70cm", "", "등쪽-회청록색, 배쪽-은백색"
            , "", "연해와 내만"
            , "한국·일본·타이완·동중국해"
        )
        insertModel(
            "삼치", "최대 몸길이 100cm", "7.1kg", "등쪽 회색을 띤 푸른색, 배쪽 은백색"
            , "", "연근해의 아표층"
            , "북서태평양의 온대 해역"
        )
        insertModel(
            "붉은쏨뱅이", "최대 몸길이 30cm", "", "전체 갈색, 배쪽 흰색"
            , "11∼3월", "수심 10∼100m의 암초 지역"
            , "중국, 타이완, 일본, 한국, 필리핀 등 서부태평양의 열대 해역"
        )
        insertModel(
            "숭어", "최대 몸길이 120cm", "8kg", "등쪽 암청색, 배쪽 은백색"
            , "한국 10∼2월(산란성기 10∼11월)", "연안"
            , "태평양, 대서양, 인도양의 온대·열대 해역"
        )
        insertModel(
            "열기", "최대 35cm", "0.8kg", "회갈색, 몸 옆구리 불분명한 검은색 가로무늬가 5∼6줄(한국)"
            , "1~2월", "암초로 된 연안"
            , "한국·일본 등 북서태평양의 아열대 해역"
        )
        insertModel(
            "연어병치", "최대 몸길이 90cm", "", "검은색 또는 푸른빛"
            , "", "수심 100∼500m의 바다"
            , "서부태평양, 동중국해, 일본 남부, 한국 남해"
        )

    }

}
