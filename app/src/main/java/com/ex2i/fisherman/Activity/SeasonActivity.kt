package com.ex2i.fisherman.Activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ex2i.fisherman.*
import com.ex2i.fisherman.Constant.APP_KEY
import com.ex2i.fisherman.Constant.PM_APP_KEY
import com.ex2i.fisherman.Constant.SPECIAL_APP_KEY
import com.ex2i.fisherman.Util.PreferenceUtil
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

    private var t1: String? = null
    private var t2: String? = null
    private var t3: String? = null
    private var t4: String? = null
    private var t5: String? = null
    private var t6: String? = null
    private var t7: String? = null
    private var other: String? = null

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
            val objectAnimator = ObjectAnimator.ofFloat(
                iv_refresh,
                View.ROTATION,
                currentDegree,
                currentDegree + -360f
            )
            objectAnimator.apply {
                duration = 300
                objectAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        iv_refresh.isEnabled = false
                    }

                    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                        super.onAnimationEnd(animation, isReverse)
                        iv_refresh.isEnabled = true
                    }
                })
                start()
            }
            Toast.makeText(this, "날씨정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
        }

        spring_layout.setOnClickListener {
            val intent = Intent(this, SpringActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        summer_layout.setOnClickListener {
            val intent = Intent(this, SummerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        autumn_lyaout.setOnClickListener {
            val intent = Intent(this, AutumnActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        winter_layout.setOnClickListener {
            val intent = Intent(this, WinterActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        iv_search.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        tv_severe_result.setOnClickListener {
            if (!tv_severe_result.text.equals("없음") && !tv_severe_result.text.equals("없다")) {
                AlertDialog.Builder(this)
                    .setTitle("기상특보")
                    .setMessage(
                        "$t1\n" + "$t2\n" + "$t3\n" + "$t4\n" + "$t5\n" + "$t6\n" + "$t7\n" + "$other"
                    )
                    .setCancelable(false)
                    .setNegativeButton(
                        "확인"
                    ) { dialog, id -> dialog.cancel() }
                    .create().show()
            }
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
        distributionArea: String,
        image: Int,
        description: String
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
        newItem.image = image
        newItem.description = description

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
                                        } else if (pm10Grade.equals("")) {
                                            tv_pm_result.text = "수신불량"
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<Model_Item?>, t: Throwable) {}

                            })

                        service?.getModel_special(SPECIAL_APP_KEY, "1", latitude, longitude)
                            ?.enqueue(object : Callback<Model_weather?> {
                                override fun onResponse(
                                    call: Call<Model_weather?>,
                                    response: Response<Model_weather?>
                                ) {
                                    val modelItem = response.body()!!

                                    if (modelItem.weather?.alert?.size!! > 0) {
                                        val varName =
                                            modelItem.weather?.alert?.get(0)?.alert51?.varName
                                        t1 = modelItem.weather?.alert?.get(0)?.alert60?.t1
                                        t2 = modelItem.weather?.alert?.get(0)?.alert60?.t2
                                        t3 = modelItem.weather?.alert?.get(0)?.alert60?.t3
                                        t4 = modelItem.weather?.alert?.get(0)?.alert60?.t4
                                        t5 = modelItem.weather?.alert?.get(0)?.alert60?.t5
                                        t6 = modelItem.weather?.alert?.get(0)?.alert60?.t6
                                        t7 = modelItem.weather?.alert?.get(0)?.alert60?.t7
                                        other = modelItem.weather?.alert?.get(0)?.alert60?.other

                                        tv_severe_result.text = varName
                                        tv_severe_result.setTextColor(resources.getColor(
                                            R.color.red
                                        ))

                                    } else {
                                        tv_severe_result.text = "없음"
                                        tv_severe_result.setTextColor(resources.getColor(
                                            R.color.purple
                                        ))
                                    }
                                }

                                override fun onFailure(call: Call<Model_weather?>, t: Throwable) {}

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

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
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
            , "우리나라의 동해와 남해, 제주도 연안 해역, 일본 중부 이남, 동중국해, 타이완 근해",
            R.drawable.item_bangedom,
            "몸이 검다고 하여 전남에서는 깜정이, 깜정고기, 경남에서는 흑돔 제주도에서는 구릿이라고 불린다. 오팔 같은 푸른 눈을 가지고 있다 하여 미국에서는 'opaleye'라고 불리며, 일본에서는 눈이 머리 앞쪽에 위치한다는 뜻, 또는 어릴 때 떼지어 노는 모습을 상징하는 뜻의 메지나(メジナ)라고 불리며 몸이 검은색이기 때문에 '구로다이(クロダイ, 검은색의 돔)'이라고 불리기도 하는데 일본어로 감성돔을 '구로다이'라고 부르기 때문에 혼돈되기도 한다.\n" +
                    "\n" +
                    "몸길이는 50~60cm까지 자란다. 몸은 타원형으로 납작하며, 주둥이는 짧고 그 앞 끝은 둔하다. 이빨 끝이 세 갈래로 갈라져 있어서 갯바위에 붙어 있는 해조류를 긁어 먹기에 적합하다. 몸은 전체적으로 매우 어두운 흑갈색을 띠며 배 부분은 약간 밝다. 몸은 빗모양의 비교적 큰 비늘로 덮여 있으며, 각 비늘에는 검은 점이 있다. 지느러미는 검은색이고, 꼬리지느러미의 뒷윤곽은 어릴 때는 거의 직선이지만, 자라면서 안쪽으로 약간 오목하게 들어간다.\n" +
                    "\n" +
                    "연안성 어종으로서 암초 또는 자갈이 많은 지역의 해초가 무성한 곳에 떼를 지어 서식하며, 수온 18~25℃ 정도의 따뜻한 바다를 좋아한다. 수면 근처에서 생활하는 아주 어릴 때에는 동물성 플랑크톤을 먹다가 1cm 가량으로 자라면 부드러운 해조류를 먹기 시작한다. 1년생 이상은 해조류뿐만 아니라 갯지렁이, 게, 새우 등 소형 동물도 먹는 것으로 추측되지만, 초식성인지 육식성인지에 대해서는 아직 논란이 많다. 산란기는 2∼6월로 추정되며, 산란기 중 1회만 산란한다.\n" +
                    "\n" +
                    "여름철에 많이 잡히지만, 큰 것은 늦가을부터 이른 봄에 잡힌다. 주로 낚시로 잡으며, 맛은 겨울에 가장 좋다."
        )

        insertModel(
            "벤자리", "최대40cm", "", "겨울에는 등쪽 희흑색이며 배족은 연한 빛, 봄과 여름에는 녹색을 띤 연한 갈색 바탕에 3줄의 폭이 넓은 황갈색 세로줄"
            , "6월~8월", "연안의 깊은 곳이나 해조류가 많은 곳"
            , "타이완, 동중국해, 일본 남부, 한국 연근해",
            R.drawable.item_benjari,
            "영명인 'Grunt'는 무리를 짓거나, 잡으면 민어과 물고기처럼 부레로 구-구-소리를 낸다 하여 붙여진 이름이다. 부산을 중심으로 한 남해동부지방에서는 일본명을 따라 '이사끼'라고 부르기도 한다. 크기에 따라 45㎝급이면 '돗벤자리', 30㎝ 이하는 '아롱이'라 따로 부르기도 한다.\n" +
                    "\n" +
                    "몸은 가늘고 긴 타원형으로 옆으로 납작하다. 주둥이는 짧은 편이며 입은 작고 입술은 얇다. 아래턱은 머리의 윗부분에 위치하고 경사져 있으며, 양 턱에는 비교적 작은 이빨이 3∼4줄 나 있다. 주둥이를 제외한 몸과 머리는 작은 사각형의 빗비늘로 덮여 있다. 몸빛깔은 크기와 계절에 따라서 그 빛깔과 반문의 형태가 달라 겨울에는 아무런 띠도 없으며, 봄과 여름에는 3줄의 폭이 넓은 황갈색 세로줄이 있으나 성장함에 따라 없어진다.\n" +
                    "\n" +
                    "온대성의 연안성 어종으로 쿠로시오해류의 영향을 받는 연안의 깊은 곳이나 해조류가 많은 곳에 서식한다. 낮에는 연안의 깊은 곳에 있다가 밤이 되면 수면 가까이로 올라오는 수직이동을 하며, 어릴 때에는 얕은 바다의 해조류가 무성한 암초 지대와 모래바닥의 경계부분에서 무리를 지어 서식하지만 성어는 무리를 이루지 않는다. 먹이는 작은 어류나 갑각류 등이며 만 3년 정도 지나면 어미가 된다.\n" +
                    "\n" +
                    "산란기는 6∼8월이며, 몸길이 22cm(3년생) 이상이면 산란을 시작한다. 산란기 동안에 여러 번 산란한다. 먹이로는 요각류·새우 등의 갑각류와 소형 어류를 주로 먹는다. 저층 트롤어업과 낚시어업으로 어획되나 그 양은 많지 않다. 여름철에 가장 맛이 좋다. 담백하며 기름기가 적어 회나 물회가 특히 인기가 좋다. 이 외에 구이, 튀김 및 국, 매운탕 등 여러 가지로 요리가 가능하다. 조리 시 지느러미에 있는 침이 매우 단단하므로 미리 제거해내는 것이 좋다."
        )

        insertModel(
            "보구치", "약 30cm", "", "등쪽 연한 갈색, 측선을 경계로 밝아져 배쪽 은백색"
            , "5월~8월", "수심 40~100m의 바닥이 모래나 뻘로 된 근해"
            , "일본 남부, 동중국해, 타이완, 한국 연안",
            R.drawable.item_boguchi,
            "부산에서는 백조기, 전남에서는 흰조기, 법성포에서는 보거치라 불린다.\n" +
                    "\n" +
                    "몸길이 약 30cm이다. 몸은 옆으로 납작하고 짧으며 몸높이가 비교적 높다. 등쪽은 연한 갈색이고 측선을 경계로 밝아져 배쪽으로는 은백색을 띤다. 아가미뚜껑 위쪽에는 눈만한 크기의 검은 색 반점이 있다. 등지느러미에는 가시가 있다. 옆줄은 몸의 등쪽으로 치우쳐 있으며 꼬리지느러미까지 완만한 곡선을 그리며 뻗는다.\n" +
                    "\n" +
                    "수심 40∼100m의 바닥이 모래나 뻘로 된 근해에서 주로 서식하며, 연안이나 만에서 정착생활을 하는 무리도 있다. 따뜻한 바다의 중층이나 하층에서 무리를 이루어 헤엄쳐 다니며, 큰 부레를 움직여 높고 큰 소리를 낸다. 우리나라 서해안의 경우, 가을철에 남쪽으로 이동하여 1~3월에 제주도 서남방 해역에서 겨울을 나고, 봄에는 다시 북쪽으로 이동하여 서해안으로 돌아간다. 산란기는 5∼8월이며 중국 연안과 우리나라 서해안에서 알을 낳는다. 새우류, 게류, 갯가재류, 오징어류, 작은 어류 등을 먹는다. 몸길이 15cm가 되면 30% 정도가 성숙되어 산란을 시작한다. 수명은 10년 정도이다.\n" +
                    "\n" +
                    "그물의 아랫깃이 바다 밑바닥에 닿도록 하여 어선으로 그물을 끌어서 잡아 올린다. 낚시 어종으로서도 인기 있으며, 여름이 제철이다.\n" +
                    "\n" +
                    "살이 희고 연하기 때문에 회로 먹으며, 소금구이, 간장조림, 튀김 등으로 먹기도 한다."

        )

        insertModel(
            "볼락", "최대35cm", "0.8kg", "회갈색, 몸 옆구리 불분명한 검은색 가로무늬가 5~6줄(한국)"
            , "1월~2월", "암초로 된 연안"
            , "한국, 일본 등 북서태평양의 아열대 해역",
            R.drawable.item_bolack,
            "《자산어보》에는 발락어(發落魚)로 기재되어 있다. 경남과 전남에서는 뽈라구, 경북에서는 꺽저구, 강원도에서는 열갱이, 함경남도에서는 구럭으로 불린다.\n" +
                    "\n" +
                    "최대 몸길이 35cm, 몸무게 0.8kg까지 성장한다. 몸은 방추형이고 옆으로 납작하다. 눈은 크고, 눈 앞쪽 아래에는 날카로운 가시가 2개 있다. 주둥이는 원뿔형으로 끝이 뾰족하며, 입은 크지만 이빨은 작고 여러 개가 촘촘히 나 있다. 몸빛깔은 서식 장소와 깊이에 따라 다양하여, 얕은 곳에 사는 것은 회갈색을 띠며, 깊은 곳에 사는 것은 회적색, 암초지대의 그늘에 숨어 사는 큰 볼락은 검은빛을 띠어 ‘돌볼락’이라고 불리기도 한다. 우리나라 주변 해역에는 회갈색인 것이 가장 많다. 몸 옆구리에는 불분명한 검은색 가로무늬가 5∼6줄로 희미하게 나 있으며, 죽으면 없어진다. 동해에서 주로 발견되는 탁자볼락과 매우 비슷하게 생겼으며, 아래턱에 비늘이 있으면 볼락, 없으면 탁자볼락이다.\n" +
                    "\n" +
                    "수온 15∼25℃의 따뜻한 바다의 암초로 된 연안에서 서식한다. 어릴 때에는 떠다니는 해조류들과 함께 20∼30마리 정도 무리를 지어 다니며, 낮에도 활동한다. 다 자라면 어릴 때보다는 작은 무리를 지으며, 밤에 활동한다. 암컷과 수컷이 교미하여 암컷의 배 속에서 알을 부화시킨 후 새끼를 낳는다. 교미는 11~12월에 이루어지며, 새끼를 낳는 시기는 1∼2월이다. 육식성으로서, 새우류, 게류, 갯지렁이류, 오징어류, 어류 등을 먹는다.\n" +
                    "\n" +
                    "일년 내내 잡히며, 4~5월에 특히 많이 잡힌다. 주로 낚시로 잡으며, 자연산은 산지에서 거의 소비되고 대도시에 유통되는 것은 양식산이다. 깊은 바다보다 연안의 얕은 바다에서 잡히는 것이 맛이 더 좋으며, 크기가 작은 것이 맛이 좋다. 10~12㎝ 정도의 작은 것은 통째로 회로 먹으며, 손바닥 크기의 중간 크기는 뼈째로 썰어서 회로 먹거나, 소금구이로 먹는다. 큰 것은 구이나 매운탕으로 먹는다."
        )

        insertModel(
            "보리멸", "최대 30cm", "", "등쪽 푸른빛의 연한 갈색, 배쪽 은백색"
            , "6∼8월", "연안 가까이의 모래 바닥이나 강 하구의 간석지"
            , "서부태평양, 인도양, 지중해 등 열대 해역",
            R.drawable.item_borimyul,
            "마산에서는 모래바닥에 사는 망둑어라는 뜻의 모래문저리라 불리며, 전남에서는 모래무찌, 경남에서는 모래무치, 밀찡이, 밀징이, 경북에서는 보리메레치, 울산에서는 갈송어, 제주에서는 모살치, 고졸맹이라고 불린다.\n" +
                    "\n" +
                    "최대 몸길이 30cm까지 성장한다. 몸높이는 낮고, 몸은 원통형으로 가늘고 길며 옆으로 납작하다. 입은 튀어나와 있으며 주둥이가 매우 길다. 모래를 더듬어 먹이를 빨아먹는 습성에 맞게 위턱이 아래턱보다 약간 길다. 옆줄은 몸 옆면 가운데보다 약간 위를 지나며, 옆줄을 경계로 몸 등쪽은 푸른빛의 연한 갈색을 띠고, 배쪽은 은백색을 띤다. 몸은 가시가 있는 비교적 큰 빗모양의 비늘로 덮여 있으며, 눈의 아래쪽에 매우 큰 비늘이 6개 있다. 뺨에는 가시가 없는 작은 둥근 비늘이 있다.  \n" +
                    "\n" +
                    "연안 가까이의 모래 바닥이나 강 하구의 간석지에 주로 서식한다. 난류성 어류로서, 겨울에 연안의 수온이 낮아지면 깊은 바다로 나가 겨울을 나고, 수온이 올라가면 다시 연안 가까이로 이동한다. 무리를 이루어 다니며, 불안할 때는 모래 속에 숨는다. 바다 밑바닥에 사는 작은 어류, 새우류, 게류, 갯지렁이류, 오징어류 등을 먹으며, 수온이 8℃ 이하로 내려가면 먹이를 전혀 먹지 않는다. 산란기는 6∼8월이며, 수온이 21℃ 정도 되면 알을 낳는다.\n" +
                    "\n" +
                    "바다 밑바닥에 살기 때문에, 그물의 아랫깃이 해저에 닿도록 하여 어선으로 그물을 끌어서 잡거나, 대략 수심 50m 이하의 연안에 일정 기간 동안 그물을 설치해 놓고, 고기가 되돌아 나올 수 없도록 고기떼의 통로를 막아 자연히 그물 속으로 들어가도록 하여 잡는다. 산란기에는 맛이 떨어지기 때문에, 봄부터 초여름까지가 제철이다.\n" +
                    "\n" +
                    "생선회, 초밥으로 주로 먹으며, 반건조시켜서 냉동보관하였다가 구이나 튀김으로 먹기도 한다."
        )

        insertModel(
            "붕장어", "암컷 90㎝, 수컷 40㎝", "", "옆구리와 등쪽 - 암갈색, 배쪽 - 흰색"
            , "봄~여름(추정)", "깊고 따뜻한 바다"
            , "대서양·인도양·태평양", R.drawable.item_bungjanga,
            "뱀장어목 붕장어과에 속하는 바닷물고기로 생김새는 뱀장어와 비슷하나 뱀장어와 달리 바다에서만 서식한다. 우리나라에서는 뱀을 닮은 모습 때문에 잘 먹지 않다가 일제강점기 때 붕장어를 즐겨 먹는 일본인들의 영향을 받아 식용하기 시작하였다. 낚시보다는 저층트롤어업과 통발어업 등으로 어획되며, 어획량의 90%가 10월~4월에 잡힌다. 껍질은 피혁제품을 만드는데 쓰이며, 주로 회나 구이로 식용된다.\n" +
                    "\n" +
                    "몸은 원통형으로 가늘고 길며, 체색은 서식 장소에 따라 조금씩 다르다. 수심이 얕은 연안에 서식하는 개체는 다갈색으로 지느러미 가장자리가 검은색이며, 깊은 바다에 서식하는 개체는 회갈색을 띤다. 산란기가 되면 수컷은 혼인색을 띤다. 배지느러미가 없고, 측선에서 흰색 점이 같은 간격으로 줄지어 있으며, 등쪽과 머리 부분에도 흰색 점이 산재한다.\n" +
                    "\n" +
                    "산란장은 확실히 밝혀져 있지 않으며 뱀장어와 마찬가지로 아열대 해역 가까운 곳까지 남하한 후 봄·여름에 걸쳐 산란한다고 추정한다. 다른 장어류와 마찬가지로 어린 시기에 투명한 버들잎처럼 생긴 렙토세파루스(leptocephalus) 유생기를 거치며 완전히 자라기까지 8년이 걸린다. 성장함에 따라 서식장소도 바뀌는데 어릴수록 얕은 내만에 서식하다가 4년생 이상은 먼 바다로 나간다."
        )
        insertModel(
            "부시리", "100cm 이상", "", "등쪽 선명한 푸른색, 배쪽 은백색, 몸 중앙 옆구리 짙은 노란색의 세로띠"
            , "5∼8월", "연안의 수면 가까이"
            , "한국·일본·타이완을 포함한 남태평양", R.drawable.item_busiri,
            "몸은 긴 방추형이고 약간 납작하며 등이 푸른 바다 물고기이다. 우리나라 남해안 먼바다 따뜻한 해역에 분포한다. 조류흐름이 거의 없거나 수온이 갑자기 떨어지는 등의 특수한 경우가 아니면 바닥층에 머무는 일이 거의 없으며, 연안의 수면가까이를 유영하다가 먹이를 발견하면 무리지어 몰려든다. 육식성으로 작은 물고기나 오징어, 새우 등을 잡아먹는다. 5∼8월에 부유성 알을 낳는다. 정치망, 낚시 등으로 어획한다. 비슷한 생김새의 바다 물고기로 방어가 있는데 겉모양이 비슷하나 방어보다 좀 더 납작하며 주둥이에서 꼬리지느러미까지 이어지는 노란색의 세로띠가 더 진하고 위턱의 뒷가장자리의 각이 둥글다. 몸길이도 방어보다 길어 약 2m까지 자란다. 방어에 비해 배지느러미가 가슴지느러미보다 길다.\n" +
                    "\n" +
                    "부시리를 부르는 명칭은 지방마다 다양하다. 전북지역에선 평방어, 포항에선 납작방어라 부르고 강원도에서는 나분대, 북한 함경도 지방에서는 나분치라고 부른다. 일본에서는 '히라마사(ヒラマサ)' 또는 '히라스(ヒラス)'라고 부른다.\n" +
                    "\n" +
                    "낚시를 하는 사람들 사이에서는 힘이 좋고 순식간에 수십 미터씩 나간다 하여 ‘미사일’이라고 불린다. 여름에서 가을 사이에 가장 맛이 좋으며 특히 기름기가 적고 살이 단단한 배부분은 회로 인기가 많다. 부시리는 피가 살보다 빨리 상하기 때문에 미리 피를 빼주어야 회로 먹을 때 비리지 않고 맛있게 먹을 수 있다. 회로 사용한 부분을 제외한 나머지는 조리거나 구워먹으며, 뼈나 내장은 매운탕으로 활용한다."
        )
        insertModel(
            "참돔", "최대 몸길이 100cm 이상", "", "몸 등쪽은 붉은색, 배쪽은 노란색 또는 흰색"
            , "4∼6월", "수심 10∼200m의 바닥 기복이 심한 암초 지역"
            , "동남아시아, 타이완, 남중국해, 일본, 한국 연근해 등 북서태평양의 아열대 해역",
            R.drawable.item_chamdom,
            "색채가 아름답고, 모양새가 잘 짜여져 있다고 하여 '참(眞)' 자를 붙여 예로부터 참돔, 참도미, 진도미어(眞道味魚)로 불렸다.\n" +
                    "\n" +
                    "《자산어보》에는 강항어(强項魚)로 기록되어 형태, 특성, 잡는 방법 등에 관해 서술되어 있고, 《전어지》에는 독미어(禿尾魚), 조선시대 《경상도지리지》에는 도음어(都音魚)로 기록되어 있다. 지방과 성장 단계에 따라서도 다른 이름을 가지고 있어서, 강원도에서는 도미(道尾, 道味),돔, 돗도미라 하고, 어린 참돔을 전남에서는 상사리, 제주도에서는 배들래기, 경남에서는 고다이라고 부른다. 또한 경남에서는 일본어로 붉은 돔이라는 뜻의 아까다이라고 부르기도 한다.\n" +
                    "\n" +
                    "최대 몸길이 100cm 내외로 암컷보다 수컷의 성장이 빠르다. 몸은 타원형이고 옆으로 납작하며, 몸은 빗 모양의 직사각형 비늘로 덮여 있다. 몸 등쪽은 붉은색을 띠며 배쪽은 노란색 또는 흰색을 띤다. 측선 주위로 푸른빛의 작은 반점이 흩어져 있다. 어릴 때에는 선홍색 바탕에 5줄의 짙은 붉은색의 띠를 갖고 있으나 성장함에 따라 없어지며, 나이를 먹으면 검은빛이 짙어진다.\n" +
                    "\n" +
                    "수심 10∼200m의 바닥 기복이 심한 암초 지역에 주로 서식하며, 제주도 남방 해역에서 겨울잠을 자고 봄이 되면 서해안과 중국 연안으로 이동한다. 잡식성으로서 새우나 갯지렁이, 어류 등을 주로 먹는다. 산란기는 4∼6월이며, 산란에 적합한 수온은 15∼17℃이다. 산란기 동안에는 살이 오르고 무리를 지어 다니며, 밤에는 20~40m 이상의 수심에서 헤엄쳐 다닌다.\n" +
                    "\n" +
                    "주로 낚시어업에 의해 많이 어획되며, 커다란 수건 모양의 그물로 둘러싸서 우리에 가둔 후에 그물을 차차 좁혀 떠올려서 잡거나, 그물의 아랫깃이 해저에 닿도록 하여 어선으로 그물을 끌어서 잡기도 한다. 겨울부터 봄까지가 제철이며, 도미찜으로 유명하다."
        )
        insertModel(
            "청어",
            "최대 몸길이 46cm",
            "",
            "등쪽 암청색, 배쪽 은백색"
            ,
            "겨울~봄",
            "수온이 2∼10℃, 수심 0∼150m의 연안, 민물, 강 어귀"
            ,
            "백해 등의 북극해, 일본 북부, 한국 연근해 등의 서부태평양",
            R.drawable.item_chunga,
            "청어목 청어과에 속하는 한류성 어종으로 등쪽은 암청색, 배쪽은 은백색인 등푸른 생선의 한 종류이다. 예로부터 우리나라 근해에서 잡혀온 친숙한 어종으로 값이 싸고 맛이 있어 다양한 방법으로 즐겨 먹었다. 동해안 지역의 겨울철 별미인 과메기의 원래 재료이기도 하다. 옆으로 길다란 사각형의 그물을 고기떼가 이동하는 길목에 수직으로 펼쳐서 잡거나, 일정 기간 동안 그물을 설치해 두었다가 거두어 올려 대량으로 잡는다.\n" +
                    "\n" +
                    "몸은 다소 옆으로 납작하며 몸의 높이가 높다. 몸의 등쪽은 암청색을 띠며, 중앙부터 배쪽은 은백색을 띤다. 눈 주위에 지방질로 된 기름눈꺼풀이 있다. 위턱에는 이빨이 없지만, 아래턱 앞에는 흔적뿐인 이빨이 있다. 비늘은 떨어지기 쉬운 둥근비늘이며, 옆줄은 잘 보이지 않는다. 몸의 배쪽 정중선을 따라 날카로운 모비늘이 1줄로 나 있다.\n" +
                    "\n" +
                    "수온 2∼10℃, 수심 0∼150m의 한류가 흐르는 연안에서 무리를 이루어 서식하며, 민물에 사는 종류도 있다. 성숙한 성어는 해안에 가까운 곳으로 이동하고 산란기가 되면 강 어귀로 올라간다. 주로 큰 새우, 게와 작은 어류 등을 먹는다. 평소에는 바다 밑 부분에 흩어져 서식하다가 산란기인 3~4월이 되면 큰 무리를 이루어 북쪽으로 이동한다."
        )
        insertModel(
            "대구",
            "40~110cm, 최대 119cm",
            "",
            "배쪽은 흰색이며 등쪽으로 갈수록 갈색으로 변함, 진한 갈색 점이 있음"
            ,
            "1~3월",
            "연안 또는 대륙사면"
            ,
            "한국, 일본, 알래스카 등의 북태평양 연안",
            R.drawable.item_daegu,
            "생김새는 명태와 비슷하지만 몸 앞쪽이 보다 두툼하고 뒤쪽은 점점 납작해진다. 눈과 입이 크고 위턱이 아래턱에 비해 앞으로 튀어나와 있다. 뒷지느러미는 두 개로 검고, 등지느러미는 세 개로 넓게 퍼져있으며 가슴지느러미와 함께 노란색을 띤다. 크기는 태어난 지 2~3살경에 50cm 정도가 되고, 더 자라면 1m 정도로 자라기도 한다. 몸무게도 많이 나가는 편이어서 지금까지 가장 무거운 경우 22.7kg로 보고되었다.\n" +
                    "\n" +
                    "무리를 지어 이동하며 주로 수심이 30~250m 되는 지역에서 산다. 산란기인 12월에서 1월경에는 수심이 얕은 연안으로 찾아들어 200여 만개의 알을 낳는데 그 산란지가 바로 경남 진해만과 경북 영일만이다. 체외수정에 의해 알이 수정되며, 짝짓기를 마친 암컷과 수컷은 수정된 알을 바닥이나 돌 표면 등에 부착된 채로 남기고 이동한다. 1mm 정도 크기의 알은 29일 정도 후에 부화하여 어린 치어가 된다. 치어는 요각류 등을 먹고 자라다가, 성체가 되면 작은 물고기나 연체동물, 갑각류, 수생곤충 등 별로 가리지 않고 잡아먹으며 아주 식성이 좋다.\n" +
                    "\n" +
                    "산란기가 되는 겨울에 맛이 가장 좋으나 최근에는 어획량이 많이 줄어서 값이 많이 비싸졌다. 주로 트롤어업과 걸그물을 이용하는 방식으로 포획하며, 신선도를 유지한 생생한 채로 판매되기도 하고 얼리거나 말려서, 또는 소금에 절이거나 훈제를 하기도 한다. 요리할 수 있는 방법도 다양해서 대구찜이나 대구튀김, 대구매운탕 등으로 조리하여 밥상에 오르고 알, 아가미, 창자는 젓갈을 만들 때 이용된다. 예로부터 한약재로도 이용되었으며 마른 대구포는 잔치나 제사 때에 사용되기도 하였다. 특히 대구의 간에서 추출한 간유(肝油)는 의약품을 만들 때 요긴하게 사용되는 것으로 알려져 있다.\n" +
                    "\n" +
                    "신선한 대구를 고르기 위해서는 빛깔이 푸르스름하고 배 부분이 단단한가를 살펴보고, 아가미를 들춰보았을 때 싱싱한 선홍색을 띠고 있는가를 유심히 따져봐야 한다. 몸집이 클수록 살이 부드럽지만, 다른 생선에 비해 살이 물러서 쉽게 상하기 때문에 싱싱한 대구를 사려면 상당한 주의를 기울여야 한다."
        )
        insertModel(
            "대구횟대",
            "30cm",
            "",
            "노란색, 등쪽 갈색, 배쪽 흰색"
            ,
            "6∼7월",
            "수심 50∼150m의 대륙붕"
            ,
            "한국 동해, 일본 홋카이도, 사할린섬, 오호츠크해 등의 북서태평양",
            R.drawable.item_daeguhoedae,
            "일본명은 Tsumagurokazika이다. 몸길이 30cm이다. 체고(body depth:몸의 가장 높은 곳으로 지느러미는 포함시키지 않음)는 낮고 몸의 횡단면은 둥글다. 머리는 크며 위아래로 납작하고, 꼬리자루(caudal peduncle:뒷지느러미 마지막 연조 기저에서 꼬리지느러미 기저까지의 부분)는 가늘고 길다. 두 눈 사이는 평탄하며, 두 눈의 중앙 부위와 머리 뒷부분은 조잡한 골질판(거북복류의 몸 표면을 덮고 있는 것과 같이 골화된 판 모양의 비늘)으로 덮여 있다.\n" +
                    "\n" +
                    "주둥이의 윗쪽 변두리는 급한 경사를 이루고 있다. 입은 크고 아래턱은 윗턱에 덮여 있으며, 양 턱에는 원뿔니 모양의 이빨띠가 있다. 위턱의 뒤끝은 동공의 뒷가장자리 아래에 달한다. 전새개골(preopercle:아가미뚜껑부를 지지하는 4개의 뼈 중 하나)에는 4개의 강한 가시가 있으며, 가장 위쪽의 가시는 끝이 갈라져 있다.\n" +
                    "\n" +
                    "등지느러미는 2개이며, 가슴지느러미는 매우 길어서 뒤끝이 뒷지느러미 기부(origin:기관 또는 부속기관이 몸통과 연결되는 부위 중 가장 앞쪽 끝 지점)를 지난다. 꼬리지느러미는 가운데가 약간 오목하지만 수직형에 가깝다. 몸은 전체적으로 노란색을 띠며 등쪽은 갈색을 띠지만 배쪽은 희다. 모든 지느러미는 노란색 바탕에 2∼3줄의 검은색 띠를 가지지만, 배지느러미와 뒷지느러미에는 띠가 없다.\n" +
                    "\n" +
                    "수심 50∼150m의 대륙붕에 주로 서식한다. 새우류나 작은 어류를 먹는다. 산란기는 6∼7월이다. 한국 동해, 일본 홋카이도, 사할린섬, 오호츠크해 등의 북서태평양에 분포한다."
        )
        insertModel(
            "띠볼락",
            "40cm",
            "",
            "연한 갈색 또는 연한 회색에 3개의 넓은 흑갈색 가로띠가 있는 것과 연한 갈색에 많은 어두운 갈색 얼룩무늬가 교차"
            ,
            "봄",
            "천해의 암초지역"
            ,
            "한국(중부·남부)·일본",
            R.drawable.item_ddibolak,
            "몸길이 40cm 정도이다. 겉모습은 볼락과 비슷한데, 특히 누루시볼락과 가장 비슷하다. 눈의 앞쪽에는 날카로운 가시가 한 쌍 있고 눈 사이에는 두 쌍, 그 뒤로는 한 쌍의 가시가 튀어나와 있다. 눈 앞가장자리를 둘러싸고 있는 골격에는 아래로 향하는 3개의 가시가 있다. 입은 크며 위턱은 눈의 뒷가장자리에 달한다. 양 턱에는 매우 작은 이빨이 띠를 형성한다.\n" +
                    "\n" +
                    "전새개골(preopercle:아가미뚜껑부를 지지하는 4개의 뼈 중 하나)에는 5개, 주새개골(opercular:아가미뚜껑부를 지지하는 4개의 뼈 중 하나)에는 2개의 가시가 있다. 몸은 작고 미약한 사각형의 빗비늘로 덮여 있다. 양 턱과 아가미뚜껑에는 비늘이 없다. 측선의 비늘수가 38개 이상이다.  \n" +
                    "\n" +
                    "등지느러미는 1개로 극조(spinous ray:지느러미 중 연조가 경골화한 것으로 마디가 없음)부와 연조(soft ray:마디가 있고 끝이 갈라져 있는 지느러미 줄기)부는 깊게 파여 있다. 등지느러미 가시는 잘 발달되어 있으며 가시 사이의 막은 깊게 파여 있다. 등지느러미 연조부는 바깥쪽 가장자리가 둥글다. 가슴지느러미는 배쪽에 치우쳐 있으며 뒷가장자리가 둥글다. 배지느러미는 가슴지느러미보다 조금 뒤에서 시작하며 항문에 겨우 달한다. 뒷지느러미는 두 번째 가시가 비교적 강하며 가장자리는 둥글다.\n" +
                    "\n" +
                    "몸은 전반적으로 흑갈색을 띠며 배쪽은 회색을 띤다. 지느러미는 흑갈색을 띤다. 머리에는 눈을 가로지르는 2개의 검은색 띠가 나타난다. 꼬리지느러미의 위쪽과 아래쪽 끝에 흰색 점이 있다.\n" +
                    "\n" +
                    "수심 10∼100m인 연안의 암초지대에서 주로 서식한다. 가을과 겨울에 남쪽으로 이동하여 월동하는 계절회유를 한다. 유어는 떠다니는 해초와 함께 이동한다. 밤에는 표층이나 중층에서 별로 움직임이 없으나 낮에는 무리를 이루어 활발히 움직인다. 먹이로는 작은 어류, 오징어류 등을 먹는 포식성 어류이다.\n" +
                    "\n" +
                    "난태생 어류로 산란은 4∼6월에 주로 이루어지는데 암컷 35cm(3세어), 수컷 28cm(2세어)가 되어야 산란을 시작할 수 있다. 정치망, 땅주낙, 걸그물로 주로 어획한다. 한국, 일본 홋카이도와 규슈지방, 중국 등의 온대 해역에 분포한다."
        )
        insertModel(
            "독가시치",
            "40cm",
            "",
            "등쪽 황갈색 바탕에 흰색 점, 배쪽 연한 노란색 바탕에 흰색 무늬"
            ,
            "7~8월",
            "연안의 얕은 암초 지역"
            ,
            "서부태평양의 열대 및 아열대 해역",
            R.drawable.item_dockgasichi,
            "일독이 있는 가시를 가지고 있다 하여 ‘독가시치’라 불린다. 제주도에서는 따치라고 불린다.\n" +
                    "\n" +
                    "최대 몸길이 40cm까지 성장한다. 몸은 달걀모양으로 몸높이는 높은 편이고 몸은 심하게 옆으로 납작하며, 꼬리자루가 매우 가늘다. 피부는 매끄러워 마치 비늘이 없는 것 같지만 실제는 작은 둥근 비늘이 묻혀 있다. 주둥이는 매우 작고 두 입술은 매우 두껍다. 배지느러미의 안쪽 가시의 길이가 바깥쪽 가시의 길이보다 짧다. 가시는 가늘고 뾰족하며 독이 있다. 등 쪽은 황갈색 바탕에 동공보다 작은 원형 또는 타원형의 흰색 점이 흩어져 있으며, 배 쪽은 연한 노란색 또는 회색 바탕에 등 쪽과 마찬가지로 흰색 무늬가 여러 개 있다. 어릴 때에는 흰색 무늬가 많이 보이지만 자라면서 점차 희미해진다. 머리의 등 쪽은 노란색을 띠며 뺨 부위를 포함한 배 쪽은 은회색을 띤다.\n" +
                    "\n" +
                    "해조류가 무성한 연안의 얕은 암초지역 주위에 무리를 이루어 생활한다. 산란기는 7-8월로서 연안의 암초 또는 해조류가 많은 곳에서 알을 낳는다. 주로 낮에 활동하며, 물 위에 떠다니며 생활하는 어릴 때에는 동물성 플랑크톤을 먹다가, 자라서는 잎이 많이 달린 조류 등을 먹으며, 해조류에 붙어 있는 동물성 먹이도 잘 먹는 잡식성이다.\n" +
                    "\n" +
                    "그물의 아랫깃이 바다 밑바닥에 닿도록 하여 어선으로 그물을 끌어서 잡거나, 일정 기간 그물을 설치해 두었다가 한 번에 여러 마리를 잡는다. 따뜻한 물을 좋아하기 때문에 여름에 주로 잡힌다.\n" +
                    "\n" +
                    "맛이 좋아 유용한 상업 어종이며, 회로 먹어도 좋다. 지느러미 가시에 독선을 가지고 있어 찔리면 통증이 심하므로 주의해야 한다."
        )
        insertModel(
            "도다리",
            "30cm",
            "",
            "눈 있는 쪽은 불규칙한 형태의 짙은 갈색 무늬, 눈이 없는 쪽은 흰색"
            ,
            "가을∼겨울",
            "바닥이 모래와 진흙으로 된 연안지역",
            "북서태평양의 온대 해역"
            ,
            R.drawable.item_dodari,
            "작은 점이 많다고 하여 영어로는 'finespotted flounder'라고 불린다. 여수에서는 담배도다리로 불린다.\n" +
                    "\n" +
                    "최대 몸길이 30cm이다. 몸은 심하게 옆으로 납작하고, 몸높이가 매우 높아 넙치나 가자미류에 비하여 몸이 마름모꼴이다. 눈이 몸의 왼쪽에 몰려있으며 입이 크고 이빨이 있는 넙치와는 달리, 눈이 몸의 오른쪽에 치우쳐 있으며, 입이 작고 이빨이 없다. 눈이 있는 쪽은 몸과 지느러미에 걸쳐 불규칙한 형태의 짙은 갈색 무늬가 빽빽하게 덮여 있는 반면에, 눈이 없는 쪽은 희다. 몸은 작은 둥근 비늘로 덮여 있다.\n" +
                    "\n" +
                    "바닥이 모래와 진흙으로 된 연안지역에 서식하며, 우리나라 서해안의 경우 가을에서 겨울철에 남쪽으로 이동하여 제주도 근처에서 겨울을 나고, 봄이 되면 북쪽으로 떼를 지어 이동하는 것으로 생각된다. 봄에서 여름에는 바다 밑바닥에 사는 갯지렁이류, 조개류, 새우류 등을 주로 먹으며, 겨울에는 동물성 플랑크톤을 먹는다. 산란기는 가을에서 겨울이며, 산란기 동안 여러 번에 걸쳐 알을 낳는다. 부화된 어린새끼는 몸길이 12 mm 정도 되면 왼쪽 눈이 이동하여 눈이 한쪽으로 쏠리기 시작하며, 25 mm 정도 되면 오른쪽으로 완전히 이동한다. 이때부터 바다 밑바닥에서 생활하기 시작하며, 모래바닥과 뻘밭에서 납작한 상태로 느리게 헤엄을 치면서 주로 생활한다.\n" +
                    "\n" +
                    "바다 밑바닥에 살기 때문에, 그물의 아랫깃이 바닥에 닿도록 한 후 어선으로 그물을 끌어서 주로 잡는다. 4~6월에 주로 잡히며, 키우는데 3~4년이 걸리기 때문에 양식은 하지 않는다. 겨울철은 산란기로서 육질이 무르고 양이 많이 줄어들며 맛이 떨어지기 때문에 회로는 먹지 않는 것이 좋다. 배의 부분이 붉은빛이 돌지 않고 붉게 멍든 부분이 없어야 상태가 좋은 것이다. 산란기가 끝나고 살이 차오르기 시작하는 4월에 가장 맛이 좋으며, 가격도 가장 비싸다. 주로 회로 먹으며, 도다리 쑥국도 유명하다.\n" +
                    "\n" +
                    "살이 매우 하얀 토종 도다리는 가슴에 호랑이 무늬가 있어 범가자미로 불리며, 1년에 100~200여 마리 밖에 잡히지 않아 횟감용 최고급 어종에 속한다. 토종 도다리는 남해안 거제, 고흥 앞바다에서 잡히며, 물 밖으로 나오면 호랑이 무늬가 사라진다."
        )
        insertModel(
            "돌돔",
            "40cm 이상",
            "",
            "청흑색, 옆구리에 7개의 뚜렷한 검은색 세로띠"
            ,
            "5∼8월",
            "연안 암초지역"
            ,
            "한국(다도해·제주도), 일본 연해, 동중국해, 남중국해",
            R.drawable.item_doldom,
            "돌밭, 즉 바다 밑 해초가 무성한 암초지대에서 살기 때문에 돌돔이라고 불린다. 충남에서는 청돔, 부산에서는 줄돔, 제주도에서는 물톳, 갓돔, 울릉도에서는 벤찌라고 부르며, 어린 돌돔은 경남에서 아홉동가리라고 불린다.\n" +
                    "\n" +
                    "몸길이는 40cm 이상으로, 몸은 옆으로 납작하며 긴 타원형으로 몸높이가 높다. 몸빛깔은 청흑색으로 옆구리에 7개의 뚜렷한 검은색 세로띠가 있으며, 암컷의 경우에는 줄무늬가 계속 유지되지만 수컷은 자라면서 줄무늬가 사라져 전체적인 몸색깔이 은회색을 띤 청흑색이 되고, 주둥이 부분만 검은색을 유지한다. 그러나 주위의 환경과 먹이에 따라 몸색깔이 달라질 수 있기 때문에 몸색깔만 가지고 정확히 암수의 구별을 하기는 힘들다. 양 턱의 이빨은 새의 부리모양이며, 몸에는 빗모양의 작은 비늘이 있다.\n" +
                    "\n" +
                    "대표적인 연안성 어류로서 어릴 때에는 떠다니는 해조류 그늘 아래에서 주로 동물성 플랑크톤을 먹고 자라며, 어느 정도 성장하면 연안의 암초지대의 물 밑바닥으로 내려가 생활한다. 잡식성으로서 해조류 등도 먹으며 갑각류, 성게류 등을 이빨로 깨물어 속살을 빨아 먹는다. 산란기는 5∼8월경이며, 해질 무렵 연안에서 알을 낳는다.\n" +
                    "\n" +
                    "낚시로 주로 잡으며, 온대성 어류로서 연안 수온이 20℃ 이상으로 상승하는 6~7월부터 장마철 전후에 바람이 불고 비나 안개가 많을 때 많이 잡힌다. 이빨이 강하며, 시력이 좋고 경계심이 강하여 낚시하기 까다로운 어종이다. 양식을 하기도 한다.\n" +
                    "\n" +
                    "살이 단단하고 맛이 독특하여 생선회, 소금구이, 매운탕으로도 최고급에 속하며, 창자 또한 진미로 알려져 있다. 여름에 가장 맛이 좋다."

        )
        insertModel(
            "개볼락",
            "25cm(검은 점을 가진 종류 35cm)",
            "",
            "어두운 갈색 바탕에 배는 연한 빛"
            ,
            "1∼2월경(부산 근해) 또는 4∼5월경(일본 도쿄 근해)",
            "근해 암초지역"
            ,
            "한국 중부·남부, 일본 홋카이도 남쪽",
            R.drawable.item_gaebolack,
            "몸길이 약 25cm이나 검은 점을 가진 종류는 35cm에 이른다. 몸은 좌우로 납작하나 볼락에 비해서 배가 불룩하고, 몸높이가 다소 높은 달걀 모양 타원형이다. 머리와 눈은 크고, 머리에 있는 눈 위쪽 가시가 크고 강하며 양 눈 사이가 오목하게 패여 있다. 머리 뒷부분은 둥글게 솟아올라 있다. 아래턱은 위턱보다 짧다. 양턱에는 융털 모양의 이빨띠가 있다.\n" +
                    "\n" +
                    "몸빛깔은 서식장소나 깊이에 따라 변이가 심하다. 어두운 갈색 바탕에 배는 연한 빛이고 배와 가슴과 머리 아래에 둥글고 작은 검은 점이 흩어져 있는 것과, 검은 점이 없고 등지느러미 기저(base:기관 또는 부속기관과 몸통과 연결되는 부위)와 옆구리에 일정하지 않은 모양의 노란색 또는 붉은빛을 띤 노란색의 얼룩무늬가 있는 것의 두 가지가 있다.\n" +
                    "\n" +
                    "정착성 어류로 근해 암초지역에 서식한다. 주로 새우류, 게류, 갑각류와 작은 어류, 두족류 등을 잡아먹는다. 난태생어로, 산란기는 1∼2월경(부산 근해) 또는 4∼5월경(일본 도쿄 근해)이다. 한국 중부·남부, 일본 홋카이도 남쪽 등지에 분포한다."
        )
        insertModel(
            "각시가자미",
            "48cm",
            "",
            "황갈색으로 암갈색의 반점이 있음. 눈이 없는 쪽은 흰색 또는 연한 노란색"
            ,
            "",
            "수심 400m 이하의 바다 밑바닥"
            ,
            "한국·일본·오호츠크해·사할린·베링해·알래스카",
            R.drawable.item_gaksigajami,
            "몸길이 약 48cm이고, 몸은 달걀처럼 한쪽이 갸름하게 둥글다. 두 개의 눈은 오른쪽에 쏠려 있으며, 입은 작고 아래턱은 튀어나와 있다. 이빨은 뭉툭한 원뿔 모양으로 불규칙하게 한 줄로 되어 있다. 몸빛깔은 황갈색으로 암갈색의 반점이 있으며 눈이 없는 쪽은 흰색 또는 연한 노란색을 띤다. 등지느러미와 뒷지느러미의 가장자리가 오렌지색이다. 옆줄은 가슴지느러미 위에서 반달 모양으로 구부러져 있다. 눈이 있는 쪽의 옆구리에 거칠고 촘촘한 비늘이 있으며, 비늘에는 작은 가시가 1∼3개 있다.\n" +
                    "\n" +
                    "수온이 낮은 곳에서 서식하는 한대성 어류로서 수심 400m 이하에서 서식한다. 바다 밑바닥에 몸을 붙인 채 생활하며, 위쪽으로 떠오를 때에도 그 자세를 그대로 유지한 채 몸을 앞뒤로 휘면서 헤엄친다. 바닥에서 이동을 하지 않을 때에는 몸을 모래나 진흙에 묻고 두 눈만을 깜박이며 먹이를 기다린다. 육식성이 강한 잡식성 어류로서 작은 조개류 등을 먹는다. 알을 낳을 시기가 되면 연안으로 접근하는 경향이 있어 봄부터 여름에 수심 100m 이하의 바다로 이동하여 알을 낳는다.\n" +
                    "\n" +
                    "눈이 한쪽으로 몰리는 변태가 일어나기 전의 새끼들은 플랑크톤처럼 바다 표층에 떠서 생활하다가, 두 눈이 한쪽으로 몰리게 되면 바다 밑바닥에서 생활한다.\n" +
                    "\n" +
                    "바다 밑바닥에 살기 때문에, 그물의 아랫깃이 바닥에 닿도록 한 후 어선으로 그물을 끌어서 주로 잡는다.\n" +
                    "\n" +
                    "회, 물회, 튀김, 구이, 조림, 국 등으로 먹으며, 겨울에 가장 맛이 좋다. 가자미식해는 가자미와 무를 채 썰어 양념하여 삭힌 것으로서, 함경도 등지에서 전해져 내려온 음식이다."
        )
        insertModel(
            "갈치",
            "50~100cm, 최대 150cm",
            "",
            "은백색"
            ,
            "4~9월",
            "연안의 물 속"
            ,
            "우리나라의 서해와 남해. 일본, 중국을 비롯한 세계의 온대 또는 아열대 해역",
            R.drawable.item_galchi,
            "칼치·도어(刀魚)라고도 한다. 《자산어보》에서는 군대어(裙帶魚)라 하고 속명을 갈치어(葛峙魚)라 하였으며 《난호어목지》에서는 갈치(葛侈)라 하였다.\n" +
                    "\n" +
                    "빛깔은 광택이 나는 은백색을 띠며 등지느러미는 연한 황록색을 띤다. 꼬리는 실 모양이고 배와 꼬리에는 지느러미가 없다. 눈이 머리에 비해 큰 편이며 입 또한 커서 위턱과 아래턱에 날카로운 이빨들이 줄지어 있다. 갈고리모양의 이빨도 있으며 아래턱은 위턱에 비해 앞으로 튀어나와 있다. 배설을 하기 위한 항문은 몸 중앙보다 앞쪽에 있고, 항문의 바로 뒤쪽에는 뒷지느러미가 숨겨져 있다.\n" +
                    "\n" +
                    "주로 50~300m 정도의 깊은 바다 속에서 살지만, 육지와 가까운 연안에서 발견되기도 한다. 마치 바다 속에 서있는 것처럼 머리를 위로 곧바로 세우고 있기도 하지만 헤엄을 칠 때에는 W자 모양으로 꼬리를 움직여 이동한다. 우리나라 근처에서는 2~3월경에 제주도 서쪽 바다에서 겨울을 보내다가, 4월경에 북쪽으로 무리를 지어 이동하여, 여름에는 남해와 서해, 중국 근처의 연안에 머무르며 알을 낳기 시작한다. 암컷 한 마리는 산란기간 동안 10만여 개의 알을 낳는 것으로 알려져 있다.\n" +
                    "\n" +
                    "연령에 따라 먹이와 식성이 달라서, 태어난 지 1~2년 된 어린 갈치는 동물성 플랑크톤을 주로 먹지만, 좀더 자라면 작은 물고기나 오징어나 새우, 게 등을 먹고 산다. 또한 어린 갈치는 주로 낮에 바다 속에 머물다가 밤에 수면으로 올라와 떠다니는 플랑크톤을 잡아먹지만, 그와는 반대로 다 자란 성체는 낮에 수면 근처에서 먹이를 잡다가 밤이 되면 바다 밑으로 내려간다. 계절에 따라 집단이 커진 경우에는 종종 서로를 잡아먹기도 한다.\n" +
                    "\n" +
                    "7~11월 사이에 많이 잡히며 주로 저층 트롤어업이나 낚시를 이용하여 어획한다. 단백질이 풍부하고 맛이 있어 인기가 좋으며 시장에서도 어렵지 않게 구할 수 있다. 여름, 가을에 먹는 갈치 맛이 가장 좋다고 알려져 있으며 그 요리방법도 다양해서 살아있는 싱싱한 갈치는 회로 먹고, 갈치조림이나 갈치찌개, 갈치국, 갈치구이 등으로 조리하기도 한다. 신선한 갈치를 고르기 위해서는 몸을 덮고 있는 은분이 밝으며 상하지 않았는가를 확인해야 한다. 이 은분의 성분은 구아닌(guanine)이라는 색소로 진주에 광택을 내는 원료 및 립스틱의 성분으로 사용된다."
        )
        insertModel(
            "감성돔",
            "약 30~50cm",
            "",
            "등쪽은 금속 광택을 띤 회흑색, 배쪽 부분은 연함. 옆구리에 세로로 그어진 가늘고 불분명한 선이 있다"
            ,
            "4~6월",
            "바닥에 해조류가 있고 모래질이나 암초로 된 수심 50m 이내의 연안에 주로 서식"
            ,
            "우리나라의 서해와 남해, 일본의 홋카이도 이남, 동중국해 등에 분포",
            R.drawable.item_gamsungdom,
            "도미류 중에서 가장 검은 빛을 띠고 있기 때문에, 《자산어보》에는 흑조, 일본에서는 '검은돔' 이란 뜻의 구로다이로 불린다. 다 자란 감성돔을 전남에서는 감상어, 경북에서는 감성도미, 부산에서는 감셍이, 제주에서는 구릿이라고 부르며, 어린 감성돔의 경우, 강원도에서는 남정바리, 경북에서는 뺑철이, 전남에서는 비돔, 비드락, 서해안 지방에서는 비디미, 배디미, 남해안 지방에서는 살감싱이, 똥감생이, 제주에서는 뱃돔이라고 불린다.\n" +
                    "\n" +
                    "몸길이 약 30~50cm로, 몸은 타원형이며 등쪽이 약간 높다. 빗모양의 비늘로 덮여 있으며, 등쪽은 금속 광택을 띤 회흑색이고, 배쪽 부분은 연하다. 옆구리에는 세로로 그어진 가늘고 불분명한 선이 있다.\n" +
                    "\n" +
                    "바닥에 해조류가 있고 모래질이나 암초로 된 수심 50m 이내의 연안에 주로 서식한다. 바닷물과 민물이 섞여 있는 강어귀 등에서 발견되기도 하며, 어린 고기는 갯벌의 물이 괴어 있는 곳에도 들어간다. 정착하여 생활하기도 하고, 계절에 따라 이동하는 무리도 있지만 큰 이동은 없으며, 겨울철에 깊은 곳으로 이동한다. 잡식성으로서 조개류, 갯지렁이 등을 먹는다. 산란기는 4∼6월이며 바닥이 자갈, 펄, 모래 등으로 해저지형이 비교적 복잡한 곳에 알을 낳는다. 성전환을 하는 물고기로서, 1년생의 경우 대부분 수컷이지만, 이후 암수한몸이었다가 4∼5년생부터는 암수로 완전히 분리되며 대부분이 암컷으로 성전환한다. 우리나라의 서해와 남해, 일본의 홋카이도 이남, 동중국해 등에 분포한다.\n" +
                    "\n" +
                    "갯바위낚시로 잡거나, 낚싯줄에 여러 개의 낚시를 달아 얼레에 감아 물살을 따라서 감았다 풀었다 하여 잡으며, 옆으로 기다란 사각형의 그물을 고기떼의 통로에 수직으로 펼쳐서 고기가 그물코에 꽂히게 하여 잡기도 한다. 주로 회로 먹는다."
        )
        insertModel(
            "강도다리",
            "30∼40cm(최대 몸길이 91cm)",
            "9kg",
            "눈이 있는 쪽-짙은 갈색, 눈이 없는 쪽-연한 노란색"
            ,
            "2∼3월(북쪽)",
            "연안 근처의 150m 내의 수심"
            ,
            "북태평양의 전 해역",
            R.drawable.item_gangdodari,
            "일본명은 Numagarei이다. 최대 몸길이 91cm, 몸무게 9kg까지 성장하나 일반적인 크기는 30∼40cm이다. 대형종으로 체고(body depth:몸의 가장 높은 곳으로 지느러미는 포함시키지 않음)는 높고, 등지느러미와 뒷지느러미의 연조(soft ray:마디가 있고 끝이 갈라져 있는 지느러미 줄기)는 비교적 높다.\n" +
                    "\n" +
                    "눈은 몸의 왼쪽에 있고 위쪽 눈이 아래쪽 눈보다 크다. 입은 작고 심하게 경사져 있으며 위턱의 뒤끝은 눈의 앞가장자리에 달한다. 양턱의 끝부분에 일직선인 이빨이 1줄로 줄지어 있다. 아래턱 배쪽에는 눈이 있는 쪽의 경우 4개의 감각공(sensory pore:측선의 유공과 같이 감각을 느낄 수 있는 구멍)이 있지만, 눈이 없는 쪽에는 6개의 감각공이 있다.\n" +
                    "\n" +
                    "측선은 주새개골(opercular:아가미 뚜껑부를 지지하는 4개의 뼈 중 하나) 위에서 시작하여 몸의 중앙을 일직선으로 달리는데 가슴지느러미 부위에서만 등쪽으로 활처럼 휘어져 있다. 머리와 몸은 수십 개의 가시를 가진 특화된 비늘로 덮여 있으며, 등지느러미와 뒷지느러미의 기저(base:기관 또는 부속기관과 몸통과 연결되는 부위)에는 가시를 가진 특화된 비늘이 있다. 꼬리지느러미는 완만한 둥근형이다.  \n" +
                    "\n" +
                    "눈이 있는 쪽은 짙은 갈색이며, 눈이 없는 쪽은 연한 노란색을 띤다. 등지느러미는 노란색 바탕에 7개의 짙은 갈색 띠가 있고, 뒷지느러미에는 3개의 짙은 갈색 띠가 있다. 꼬리지느러미에는 3줄의 세로띠가 있다.  \n" +
                    "\n" +
                    "수심이 400m 정도인 뻘, 자갈, 모래 등의 바닥에서도 보이나 대개 연안 근처의 150m 내의 수심에서 서식한다. 담수 해역에도 종종 출현하며, 치어 때는 조간대 지역에 서식한다. 먹이는 소형의 갑각류, 조개류, 다모류 등이다.\n" +
                    "\n" +
                    "산란기는 북쪽에서 2∼3월이다. 체표면이 거칠지만 육질이 좋고 자원량은 적당한 편이다. 북미 연안국의 주요 상업 어종에 속한다. 한국, 일본 북부, 오호츠크해, 베링해, 알래스카만에서 캘리포니아만에 이르는 북태평양의 전 해역에 광범위하게 분포한다."
        )
        insertModel(
            "갑오징어",
            "몸길이 17cm, 나비 9cm",
            "",
            "수컷의 등면은 암갈색 가로줄무늬가 있으나 암컷은 무늬가 없음. 배면은 연갈색임"
            ,
            "4~6월",
            "바다"
            ,
            "한국, 일본, 중국, 오스트레일리아 북부",
            R.drawable.item_gapojinga,
            "갑오징어라고도 한다. 제주지역에선 맹마구리, 서산, 태안, 당진 부근에서는 찰배기나 찰박, 영덕에선 오작어, 강릉, 동해, 삼척 부근에서는 먹통, 여수, 장흥, 보성, 고흥 부근에선 배오징어나 깍세기라고도 부른다. 오징어뼈, 이걸치, 이고치뼈당구라 부르기도 한다.\n" +
                    "\n" +
                    "몸통은 원통형이며 몸길이 17cm, 나비 9cm 정도이다. 몸통 양쪽에 전체 가장자리에 걸쳐 지느러미가 있다. 10개의 다리 중 8개는 약 10cm정도이고 나머지 두개는 먹이를 잡을 때 사용하며 이를 촉완(觸腕)이라 한다. 촉완의 길이는 약 20cm이며 네 줄의 빨판이 있다. 등면에는 외투막에 싸여 있는 석회질의 뼈(갑,甲)가 있으며 그 뒤끝이 예리하게 튀어나와 있다. 뼈의 내부는 얇고 납작한 공기방으로 이루어져 있어 부력을 조절하는데 쓰인다. 살아 있을 때 수컷은 등면에 물결 모양의 암갈색 가로무늬가 뚜렷하게 있으나 암컷은 이렇다 할 무늬가 없다. 배면은 암수가 모두 연한 갈색이다.\n" +
                    "\n" +
                    "산란기는 4~6월 경이다. 산란기가 되면 오징어떼가 육지 사이의 좁은 해역으로 이동하여 수심 2~10m 전후의 모래질 바닥에 서식하는 해초나 해조류에 길이 1cm 정도의 알을 부착시킨다. 한국, 일본, 중국, 오스트레일리아 북부 등지에 분포한다. 주간에 사냥하며, 출수구로 모래를 불어 치워내는 방식으로 야행성 새우류를 사냥하거나 작은 물고기, 연체동물을 먹는다.\n" +
                    "\n" +
                    "전통적으로는 '대통발'을 사용하여 산란기인 4~6월에 어획한다. 통발 안에는 짚이나 잔디뿌리, 싸리나무 가지 등을 이용한 알받이를 넣어 놓거나 아무 것도 넣지 않는다. 이는 해조류 등에 알을 부착시켜 산란하는 습성을 이용한 것이다. 부안, 군산, 대천 등 서해에서 주로 잡힌다. 낚시로 잡을 경우는 9~10월 중이 적기이다.\n" +
                    "\n" +
                    "지방은 적고 단백질은 많은 건강식품으로 쫄깃하고 담백한 맛이 일품이다. 각종 혈관계 질환을 예방하는 고밀도 콜레스테롤이 많이 들어있다. 피로해소에 좋다고 알려진 타우린과 여러가지 무기질이 많이 들어있다. 오징어의 살은 기력을 증진시키며, 정신력을 강하게 한다. 뼈는 위산 중화 기능이 있으며, 해표초라 하여 가루를 내어 지혈제로도 이용한다. 알은 소화력을 향상시키며 산모가 삶아먹으면 부기가 가라 앉는다. 회나 포(鮑)로 많이 쓰이며 이외에도 무침, 볶음, 튀김, 전, 구이 등 거의 모든 요리의 재료로 활용 가능하다."
        )
        insertModel(
            "쥐노래미",
            "40cm",
            "",
            "장소에 따라 노란색, 적갈색, 자갈색, 흑갈색 등"
            ,
            "10∼1월",
            "바닥이 모래나 진흙으로 된 곳이거나 암초 또는 인공암초가 있는 곳"
            ,
            "북서태평양의 온대 해역",
            R.drawable.item_geenoraemi,
            "《자산어보(玆山魚譜)》에는 노래미로 기록되어 있다. 서해안에서는 노래미, 부산에서는 게르치, 동해안 강릉 지역에서는 돌삼치, 평안남도에서는 석반어로 불린다.\n" +
                    "\n" +
                    "몸길이 40cm이다. 몸높이는 낮고 옆으로 납작하며, 생김새는 노래미와 비슷하다. 서식장소에 따라 몸빛깔이 다르게 나타나며, 노란색, 적갈색, 자갈색, 흑갈색 등으로 다양하다. 배 부분은 회색이다. 1줄의 옆줄을 가지고 있는 대부분의 물고기와는 달리 등쪽에 3줄, 배쪽에 2줄, 모두 5줄의 옆줄을 가지고 있다. 몸과 머리는 가시가 있는 빗모양의 작은 비늘로 덮여 있다. 산란기가 되면 수컷은 오렌지색으로 몸빛깔이 짙어지며, 산란기가 지나면 없어진다.  \n" +
                    "\n" +
                    "바닥이 모래나 진흙으로 된 곳이나 암초가 있는 연안 등에 서식한다. 활동이 활발하지 않으며, 배 부분을 바위나 돌에 닿은 채 생활하여 부레가 없다. 산란기는 10∼1월이며, 알을 낳아 돌에 붙이고 수컷이 부화할 때까지 옆에서 지킨다. 포식성으로 먹이는 주로 작은 어류, 게류, 새우류, 다모류 등 여러 가지이다.\n" +
                    "\n" +
                    "산란기는 10∼1월이며, 알을 낳아 돌에 붙이고 수컷이 부화할 때까지 옆에서 지킨다. 어릴 때에는 표층을 헤엄쳐 다니며, 플랑크톤을 먹는다. 자라면서 바다 밑바닥으로 내려가며, 바다 밑바닥에 사는 게, 새우, 갯지렁이, 어류 등을 잡아먹는다.\n" +
                    "\n" +
                    "바다 밑바닥에 서식하기 때문에, 그물의 아랫깃이 바다 밑바닥에 닿도록 하여 어선으로 그물을 끌어서 주로 잡는다. 요즘에는 양식을 하기도 한다. 봄이 제철이다.\n" +
                    "\n" +
                    "회, 구이 등으로 먹는다."
        )
        insertModel(
            "고등어",
            "30cm",
            "",
            "등쪽 암청색, 중앙에서부터 배쪽 은백색"
            ,
            "",
            "바다"
            ,
            "태평양, 대서양, 인도양의 온대 및 아열대 해역",
            R.drawable.item_godoenga,
            "《자산어보》에 벽문어(碧紋魚)·고등어(皐登魚), 《재물보》에 고도어(古道魚), 《경상도 속한지리지》에 고도어(古都魚)라고 한 기록이 보인다. 일본명은 Hirasaba, Masaba이다. 오스트레일리아에서는 Common mackerel이라고 부른다. 몸은 길고 방추형으로 약간 측편(compressed:어류의 체형 가운데 좌·우로 납작한 형)되어 있다.\n" +
                    "\n" +
                    "눈은 크며 기름눈까풀이 잘 발달되어 있으며 동공 부위는 노출되어 있다. 두 눈 사이는 평편하다. 위턱의 뒤끝은 동공의 중앙 아래에 달한다. 등지느러미는 2개로 멀리 떨어져 있고, 기저(base:기관 또는 부속기관과 몸통과 연결되는 부위)길이는 비슷하지만 높이는 1번째 등지느러미가 더 높다. 가슴지느러미는 체측의 중앙에 있으며 비교적 작다. 뒷지느러미는 2번째 등지느러미와 대칭을 이룬다. 등지느러미와 뒷지느러미 후방으로 5개씩의 토막지느러미가 있고, 꼬리자루(caudal peduncle:뒷지느러미 마지막 연조 기저에서 꼬리지느러미 기저까지의 부분)는 매우 잘록하다. 꼬리지느러미는 잘 발달된 가랑이형이다. 1번째 등지느러미는 제2가시가 가장 길다.\n" +
                    "\n" +
                    "몸 등쪽은 암청색을 띠며 중앙에서부터 배쪽으로는 은백색을 띤다. 몸 등쪽에는 청흑색의 물결무늬가 측선에까지 분포한다. 등지느러미는 투명하지만 흑색 소포(chromatophore:색을 띠는 세포로 흑색소포, 황색소포 및 적색소포가 있음)가 산재하여 어둡게 보이며, 가슴지느러미 기저부는 희지만 기저부의 상반부 윗가장자리 및 후반부는 검다. 배지느러미와 뒷지느러미는 무색 투명하며, 꼬리지느러미는 회색을 띠지만 바깥쪽 가장자리가 검다.\n" +
                    "\n" +
                    "태평양, 대서양, 인도양의 온대 및 아열대 해역에 널리 분포한다. 부어성 어종으로 표층 또는 표층으로부터 300m 이내의 중층에 서식한다. 계절회유(seasonal migration:어류가 계절에 따라 알맞은 수온의 해역을 찾아 떼를 지어 이동해 가는 것)를 하며, 북반구에 서식하는 종은 수온이 상승하는 여름철에 북쪽으로 이동을 하고 겨울철에는 남쪽으로 이동하여 산란한다.\n" +
                    "\n" +
                    "3cm 이상 성장하면 크기별로 군집을 이루어 생활한다. 북동태평양에서는 다른 어종들과 함께 군집을 이루어 이동하기도 한다. 한국에는 2∼3월경에 제주 성산포 근해에 몰려와 차차 북으로 올라가는데 그 중 한 무리는 동해로, 다른 한 무리는 서해로 올라간다. 9월∼다음해 1월경부터 남으로 내려가기 시작한다. 한국에서 고등어가 서해로 올라가는 무리들이 성하면 동으로 올라가는 무리들이 쇠해지고, 동해로 올라가는 무리들이 성하면 서해로 올라가는 무리들이 쇠해진다. 그 주기는 약 40년인 듯하다.\n" +
                    "\n" +
                    "산란은 수온 15∼20℃에서 이루어지며, 지역에 따라 약간의 차이를 보인다. 먹이는 요각류, 갑각류, 어류, 오징어류 등을 먹으며, 군집을 이루어 사는 다른 어종과 먹이 경쟁을 한다. 최대 몸길이 50cm까지 성장하나, 일반적으로 30cm 범위이다.  \n" +
                    "\n" +
                    "한국에는 고등어속 어류에 2종이 알려져 있는데 고등어와 유사종으로 망치고등어가 있다. 망치고등어는 체측의 중앙을 따라 둥근 암청색의 무늬가 산재하여 잘 구별되지만 고등어와 망치고등어의 중간 형태를 띤 개체변이가 관찰되어 자세한 연구가 요망된다. 한편 망치고등어는 1번째 등지느러미의 3∼4번째 가시가 가장 길어 고등어와 잘 구별된다."
        )
        insertModel(
            "광어",
            "60~80cm",
            "",
            "황갈색 바탕에 짙은 갈색과 흰색 점, 반대쪽은 흰색"
            ,
            "2~6월",
            "바다 속 모래 바닥"
            ,
            "우리나라, 중국, 일본의 인근 해역",
            R.drawable.item_gwanga,
            "몸은 바다 밑 환경에 적응하여 납작하며, 몸색깔은 모래바닥과 잘 구분이 되지 않는 황갈색의 보호색을 띤다. 몸집이 큰 편이어서 1m 정도가 되는 것도 있으며 보통 암컷이 수컷에 비해 10cm 정도 더 크다. 몸의 가장자리에는 다소 단단한 지느러미가 있으며, 등쪽에는 77~81개, 배쪽에는 59~61개 정도의 뼈로 지느러미가 나와있다. 비늘이 매우 작은 편이며 가슴지느러미 부근에서 시작되는 옆줄은 107~120여 개의 비늘로 이루어져있다. 입이 크고 날카로운 이빨이 발달해 있으며, 아래턱이 위턱에 비해 앞으로 튀어나와 있다.\n" +
                    "\n" +
                    "보통 깊이가 200m를 넘지 않는 바다 밑 모래바닥에서 생활하며, 계절에 따라 장소를 옮겨가며 먹이를 찾거나 알을 낳는다. 우리나라 서해안에 서식하는 넙치는 가을과 겨울 사이에 남쪽으로 무리를 지어 이동하여 겨울을 보내고, 다시 봄이 되면 북쪽으로 이동하여 짝짓기와 산란이 이루어진다. 짝짓기를 마친 암컷은 알을 낳기에 알맞은 장소로 옮겨가서 약 40~50만 개의 알을 낳는다. 산란 후 2~3일 뒤에 깨어난 어린 넙치는 수심이 얕은 바닷물에 떠다니면서 생활을 하다가, 시간이 지나 좀더 자란 치어기가 되면 바다 밑바닥 생활을 시작한다. 어린 시기에는 작은 새우류나 다른 물고기의 치어를 먹으며 살다가 몸집이 불어나면 갑각류나 연체동물류 또는 작은 어류를 잡아먹고 산다. 넙치의 특징인 한쪽으로 몰려있는 눈은 어린 시기에는 나타나지 않다가, 자라면서 점차 오른쪽 눈이 왼쪽으로 이동한다.\n" +
                    "\n" +
                    "12~3월 사이에 주로 주낙이나 저층 트롤어업을 통해 많이 잡히지만, 최근에는 양식기술이 발달하여 시중에서 어렵지 않게 접할 수 있다. 양식기술 덕분에 연중 내내 그 맛을 볼 수 있지만, 특히 가을과 겨울 사이에 가장 맛이 좋다고 알려져 있다. 단백질 함량이 높고 지방은 적어서 맛이 담백하고 개운해서 신선한 횟감으로 많이 이용된다. 쫄깃쫄깃한 배쪽 살과 더불어 지느러미 밑부분에 위치한 근육 또한 회로 많이 먹는다. 신선한 회로 가장 인기가 많지만 이 밖에도 튀김이나 찜, 탕을 만들어 먹기에도 좋다."
        )
        insertModel(
            "학꽁치",
            "40cm",
            "",
            "등쪽 청록색, 배쪽 은백색"
            ,
            "4∼7월",
            "수심 50m 이내의 내만이나 강, 호수"
            ,
            "중국 남부, 일본, 한국 연근해",
            R.drawable.item_hakggongchi,
            "입이 학의 주둥이처럼 길게 튀어나와 있어서 학공치, 학꽁치라 불린다. 《자산어보》에는 공치로 기록되어 있다. 경북에서는 사이루, 경남에서는 꽁치, 강화도에서는 청갈치, 원산에서는 공매리, 강원도에서는 굉메리, 충남, 전남에서는 공치, 평안북도에서는 곰능이, 평안남도에서는 청망어라 불린다.\n" +
                    "\n" +
                    "몸길이 40cm이다. 몸은 가늘고 길며 옆으로 납작하다. 몸높이는 낮으며 횡단면은 타원형에 가깝다. 아래턱이 앞쪽으로 길게 뻗어 있으며 위턱 길이의 두 배 이상이다. 등쪽은 청록색을 띠며, 배쪽은 은백색을 띤다. 몸은 작은 둥근 비늘로 덮여 있으며 떨어지기 쉽다.\n" +
                    "\n" +
                    "수심 50m 이내의 내만이나 강, 호수 등에서 떼를 지어 서식한다. 봄과 여름에는 북쪽으로 이동했다가, 가을과 겨울에는 남쪽으로 내려간다. 헤엄치는 속도는 빠르지 않지만 주위의 변화에 민감하게 반응해서 날치와 같이 뛰어오르는 습성이 있다. 물 위에 떠다니는 작은 동물성 플랑크톤이나 새우, 게 등을 주로 먹는다. 산란기는 4∼7월이며 떠다니는 해조류 등에 알을 낳는다.\n" +
                    "\n" +
                    "옆으로 길다란 사각형의 그물을 고기떼가 지나가는 길목에 수직으로 펼쳐서 고기가 그물코에 꽂히게 하여 잡거나, 눈으로 고기떼의 위치를 확인한 후, 커다란 수건모양의 그물로 둘러싸서 우리에 가두고, 그 범위를 차차 좁혀 떠올려서 잡는다. 일정 기간 동안 그물을 설치해 두었다가 거두어 올려 대량으로 잡기도 한다. 낚시 어종으로서도 인기 있으며, 겨울에 많이 잡힌다.\n" +
                    "\n" +
                    "예전에는 생산량의 대부분이 일본으로 수출되었으나, 최근에는 국내 소비량도 늘어서 국내에도 많이 공급되고 있다. 주로 회로 먹으며, 국을 끓이거나 소금구이로 먹기도 한다. 요리할 때에는 뱃속의 검은 막을 잘 벗겨내야 쓴맛이 없어진다."
        )
        insertModel(
            "한치",
            "외투장 183mm",
            "",
            ""
            ,
            "",
            "15∼70m의 연안"
            ,
            "열대 서인도 태평양, 남동중국해, 한국, 일본남부, 오스트레일리아 북부",
            R.drawable.item_hanchi,
            "다리 길이가 한 치밖에 안 될 정도로 짧다고 하여 이름에 '한치'라는 표현이 붙었다. 몸통을 둘러싸고 있는 외투막은 원통형이며, 위쪽 끝부분은 가늘어져서 원추형을 이룬다. 수컷은 외투막의 길이가 40㎝에 달하는 것도 있다. 외투막의 앞쪽 끝에는 외투막 길이의 60%가 넘고 폭도 50%에 달하는 커다란 마름모꼴의 지느러미가 있으며, 창오징어의 지느러미보다 넓다. 8개의 다리와 먹이를 잡을 때 쓰는 2개의 촉완이 있으며, 촉완은 다리보다 가늘고 길어서 잡는 과정에서 잘려나가기도 한다. 촉완에는 먹이 등에 흡착할 수 있는 빨판인 흡반이 나 있으며, 촉완의 끝은 창 모양이다.\n" +
                    "\n" +
                    "15∼70m의 연안에 서식한다. 동해 남부, 남해안, 제주도에서 6월말부터 9월까지 여름에 잡는다. 불빛을 보고 모여드는 특성을 이용하여, 밤에 어선에 불을 밝힌 채 잡는다. 긴 낚시줄에 낚시 바늘을 일정한 간격으로 여러 개를 달아 물 속에 늘어뜨린 후, 빛에 반사되어 먹이처럼 보이게 하기 위해 손으로 들었다 놓았다를 반복하여 걸려들게 한다. 요즘은 물레처럼 생긴 기계를 이용하여 거두어 올리기도 한다.\n" +
                    "\n" +
                    "살이 부드럽고 담백하여 오징어보다 맛이 좋으며, 값도 두 배 이상 비싸다. 회나 물회, 물에 살짝 데친 숙회, 구이 등으로 먹으며, 일본에서는 고급 초밥 재료로 사용된다. 어획량이 많을 때에는 내장을 빼고 껍질을 벗겨 냉동해두었다 겨울철 비수기에 출하하기도 한다. 냉동 한치는 그대로 썰어 회로 먹어도 활어 맛을 느낄 수 있다. 오징어처럼 말려서 유통되기도 하는데, 마른 한치는 백지장처럼 하얀 것이 특징이다. 육질이 부드러워 굽지 않아도 먹기가 좋다."
        )
        insertModel(
            "홍감펭",
            "최대 몸길이 30cm",
            "",
            "전체 붉은 오렌지색, 3줄의 폭이 넓고 짙은 붉은색 가로띠"
            ,
            "겨울에서 봄",
            "수심 200∼500m의 대륙붕 가장자리로서 바닥이 조개껍데기가 섞인 모래질인 곳"
            ,
            "타이완, 동중국해, 일본 남부해, 한국 남부",
            R.drawable.item_hongampeng,
            "일본명은 Yumekasago이다. 최대 몸길이 30cm까지 성장한다. 몸은 소형으로 몸과 머리는 옆으로 납작하며 몸높이는 머리 뒷부분이 가장 높다. 눈은 크고 머리 앞부분 위쪽에 치우쳐 있다. 눈의 등쪽 가장자리를 따라 3∼4개의 가시가 나 있고 후두부에는 2개의 뒤로 향하는 가시가 있다. 눈 밑에는 어떠한 가시도 없다. 주둥이 끝은 날카롭고 입은 크며 경사져 있다. 위턱은 아래턱보다 약간 길고 눈의 중앙 아래에 달한다.\n" +
                    "\n" +
                    "전새개골(preopercle:아가미뚜껑부를 지지하는 4개의 뼈 중 하나)의 가장자리에는 5개의 가시가, 주새개골(opercular:아가미뚜껑부를 지지하는 4개의 뼈 중 하나)의 상단부에는 2개의 딱딱하고 뾰족한 가시가 있다. 양턱과 서골(vomer:경골어류의 두개골에 있는 가장 앞쪽 배면에 위치하는 골편으로 주로 1개이지만 1쌍인 경우도 있음) 및 구개골(palatine: 입천장부를 지지하는 7개의 뼈 중 하나로 가장 앞쪽에 위치)에는 작은 이빨이 무리지어 있다.\n" +
                    "\n" +
                    "머리나 몸은 빗비늘로 덮여 있고, 윗턱 뒷부분, 아래턱, 눈앞 부분에는 비늘이 없다. 몸은 전체적으로 붉은 오렌지색을 띠며, 희미하게 체측을 가로지르는 3줄의 폭이 넓고 짙은 붉은색 가로띠가 나타난다. 아가미뚜껑 안쪽은 검다. 모든 지느러미는 노란색 또는 오렌지색을 띤다.\n" +
                    "\n" +
                    "수심 200∼500m의 대륙붕 가장자리로서 바닥이 조개껍데기가 섞인 모래질인 곳에 주로 서식한다. 산란기는 겨울에서 봄까지이며 태자어를 낳는 태생어이다. 저층 트롤어업에 의하여 어획되며 상업적 가치가 있는 종이다. 타이완, 동중국해, 일본 남부해, 한국 남부의 연근해에 분포한다. 이 종은 점감펭과 형태 및 몸빛깔에서 비슷하지만, 점감펭은 눈 밑에 3개의 가시가 있으므로 잘 구별된다."
        )
        insertModel(
            "호래기",
            "60mm",
            "",
            "연한 자주빛"
            ,
            "3월",
            "연안"
            ,
            "우리나라 전 연안, 동남아시아, 유럽",
            R.drawable.item_horagi,
            "화살오징어과에 속하는 연체동물이다. 정약전이 쓴 어류학서 자산어보(玆山魚譜) 에는 오징어와 비슷하나 몸이 좀더 길고 좁으며 등판에 껍질이 없고 종이장처럼 얇은 뼈를 가지고 있는 것으로, 선비들이 바다에서 나는 귀중한 고기라 하여 '고록어(高祿魚)'라고 불렸다고 써있다. 화살꼴두기과에는 꼴뚜기 외에 창꼴뚜기, 화살꼴뚜기, 흰꼴뚜기 등을 포함한 7종이 널리 알려져 있다.  \n" +
                    " \n" +
                    "몸이 부드럽고 좌우 대칭이며, 빛깔은 흰색 바탕에 자주빛 반점이 있다. 몸통은 길쭉하게 생겼는데 길이가 폭의 3배정도 된다. 뼈는 얇고 투명하며 각질(角質)로 되어있다. 다리의 길이는 몸통의 반정도 이다.\n" +
                    " \n" +
                    "짝짓기 시 수컷은 좌측 네번째 팔을 사용하여 정자가 들어있는 정포를 암컷의 몸 안으로 전달한다. 짝짓기가 끝난 암컷은 수심 약 100m이내인 얕은 곳에서 주로 봄철에 산란한다. 알은 덩어리로 응고된 상태로 낳는데 하나의 덩어리에 20-40개의 알이 들어 있다.\n" +
                    "수명 1년이며, 연안에 많이 서식하고 이동을 많이 하지 않아 유영능력이 떨어진다. 그래서 근육이 덜 발달되어 있고 오징어보다 훨씬 연하고 부드럽다.\n" +
                    " \n" +
                    "물살이 빠른 곳에서 그물을 물살에 흘러가지 않게 고정해놓고 그 물살에 의해 그물로 들어가게 하는 안강망(stow net)을 비롯하여 여러가지 방법으로 잡으며, 잡힌 꼴뚜기는 주로 젓갈로 만들어 먹는다."
        )
        insertModel(
            "황어",
            "약 45cm",
            "",
            "등쪽 노란 갈색이나 푸른빛을 띤 검은색, 옆구리와 배쪽 은백색"
            ,
            "3∼4월",
            "수심 10∼150cm의 물이 비교적 맑은 강 하류"
            ,
            "한국, 일본, 사할린섬, 중국 동북부, 연해지방, 시베리아",
            R.drawable.item_hwanga,
            "강원도에서는 황사리, 경북에서는 밀하라 불린다.\n" +
                    "\n" +
                    "몸길이 약 45cm로 몸은 길고 옆으로 납작하다. 몸빛깔은 등쪽이 노란 갈색이나 푸른빛을 띤 검은색이고, 옆구리와 배쪽은 은백색이다. 봄철 산란기에는 옆구리의 아래로 넓은 붉은빛 띠가 나타나고, 등쪽에는 분명하지는 않으나 붉은빛의 세로띠가 나타난다. 수컷은 이 붉은빛이 암컷보다 선명하며, 산란기가 되면 몸 전체에 원뿔 모양의 돌기가 나타난다.\n" +
                    "\n" +
                    "수심 10∼150cm의 물이 비교적 맑은 강 하류에 서식한다. 강에서 태어난 뒤 바다로 내려가 대부분의 일생을 보내고, 3월 중순에 알을 낳기 위해 강으로 돌아온다. 산란기는 3∼4월이며, 산란은 암컷 한 마리와 여러 마리의 수컷이 어울려 낮에 주로 이루어진다. 알은 모래자갈 바닥의 돌 표면에 붙인다. 잡식성으로서 물에 사는 곤충, 곤충의 알, 동물성플랑크톤, 식물성플랑크톤, 작은 물고기 등을 먹는다. 일본에서는 강산성의 호수에서 서식하는 것이 발견되기도 한다.\n" +
                    "\n" +
                    "옆으로 길다란 사각형의 그물을 고기떼가 다니는 길목에 수직으로 펼쳐서 고기가 그물코에 꽂히게 하여 잡거나, 낚시로 잡는다. 민물에서는 알을 낳으러 돌아오는 3월 한 달 동안 잠깐 잡히며, 해안에서는 가을부터 봄까지 잡힌다. 바다 낚시의 경우 한 겨울이 제철이며, 이 때 잡힌 것이 맛이 가장 좋다.\n" +
                    "\n" +
                    "회, 매운탕 등으로 먹는다."
        )
        insertModel(
            "황점볼락",
            "35cm 이상",
            "",
            "암황갈색, 옆구리에 4∼5줄의 불규칙하고 희미한 가로띠"
            ,
            "11∼1월",
            "근해의 암초"
            ,
            "한국(남해), 일본(홋카이도)",
            R.drawable.item_hwangjumbolack,
            "몸체에 붙은 작은 점이 많아 ‘황점볼락’이라 부른다. 여수, 통영 지역에서는 ‘진강구’, ‘꺽더구’, ‘검서구’, ‘깍다구’라고 부르기도 한다. 볼락과 우럭을 합쳐 놓은 듯한 모습으로 볼락보다 머리가 크고 가시가 많다.\n" +
                    "\n" +
                    "몸은 방추형으로 몸과 머리는 옆으로 납작하며 약간 길다. 머리의 가시는 강하나 위로 뻗지 않는다. 머리는 크고 눈은 작아 주둥이의 길이보다 약간 짧다. 측선은 뚜렷하고 아가미구멍의 위쪽에서 시작하여 꼬리지느러미 기부까지 뻗는다. 몸빛깔은 암황갈색으로 옆구리에 4∼5줄의 불규칙하고 희미한 가로띠가 있으나 어려서는 더욱 분명하지 않다. 눈을 중심으로 방사상의 검은색 띠가 있다.\n" +
                    "\n" +
                    "우리나라 남해 근방의 연안 암초지대에 서식하며 거의 이동하지 않는 정착성 어종으로 11~1월에 새끼를 출산하는 난태생이다. 성어 1마리가 약 30,000마리의 자어를 출산한다. 가두리에서 월동이 가능하고 내만에서 거의 이동하지 않고 사는 습성 때문에 종묘 방류를 통한 연안자원을 조성하고 어업생산을 높이는데 효과가 좋다. 그러나 최근에는 남획으로 인한 자원 고갈이 심각하여 수산진흥원에서는 1994년부터 매년 10만 마리씩 종묘를 연안에 방류사업을 실시하고 있다. 맛이 좋아 회, 구이, 찜, 어죽으로 인기가 많다."
        )
        insertModel(
            "임연수어",
            "27~50cm",
            "",
            "등쪽 암갈색, 배쪽 황백색"
            ,
            "9월∼이듬해 2월",
            "수심 100∼200m 사이의 바위나 자갈로 된 암초 지대"
            ,
            "북태평양의 오호츠크해, 동해 등",
            R.drawable.item_imyeonsua,
            "원래 한자는 임연수어(林延壽魚)이지만,《신증동국여지승람》에는 한자어로 음이 같은 임연수어(臨淵水魚)라고 하였으며,《전호지》에는 이면수어(利面水魚)라 하였다. 《난호어목지》에는 임연수(林延壽)라는 사람이 이 고기를 잘 낚았다고 하여 그의 이름을 따서 임연수어(林延壽魚)라 적고, 한글로 '임연슈어'라고 하였다. 영어명인 아트카 매커럴(atka mackerel)은 유명한 임연수어 어장인 알래스카 남부의 아토카섬의 이름을 딴 것이다. 경남을 비롯한 전국에서 이면수라고도 불리며, 함경북도에서는 이민수, 함경남도에서는 찻치, 강원도에서는 새치, 다롱치, 가지랭이라고 한다. 어릴 때에는 청색을 띠기 때문에 청새치로 불리기도 한다.\n" +
                    "\n" +
                    "몸길이는 27~50cm 정도이다. 몸은 방추형에 가깝고 길며 옆으로 납작하다. 몸 옆구리에는 불분명한 검은색 세로띠가 있다. 하나의 옆줄 가지고 있는 일반 경골어류와는 달리 5줄의 옆줄을 가지고 있다. 몸은 빗모양의 작은 비늘로 덮여 있다. 몸의 등쪽은 암갈색을 띠며 배쪽은 황백색을 띤다. 번식기가 되면 수컷은 코발트색으로 변하며 몸쪽에 진한 노란색 무늬가 나타나고, 암컷의 경우 흙색에 노란색 무늬가 보이지만 수컷보다 선명하지 않다. 쥐노래미와 모습이 비슷하지만 꼬리지느러미가 깊이 두 갈래로 갈라져 있는 점이 다르며, 쥐노래미는 몸통이 황갈색 또는 적갈색을 띤다.\n" +
                    "\n" +
                    "수심 100∼200m 정도의 수온이 낮은 바다의 바위나 자갈로 된 암초지대에 주로 서식한다. 산란기는 9월부터 이듬해 2월이며, 조류의 흐름이 좋은 연안의 암초 지역에서 알을 낳는다. 잡식성 어류로서 바다 밑바닥에 사는 생물을 주로 잡아먹는다.\n" +
                    "\n" +
                    "바다 밑바닥에 살기 때문에, 그물의 아랫깃이 바다 밑바닥에 닿도록 하여 어선으로 그물을 끌어서 잡으며, 커다란 수건 모양의 그물로 둘러싸서 우리에 가둔 후에 그물을 차차 좁혀 떠올려서 잡기도 한다. 산란기를 맞아 육지 가까이 이동하는 9월에서 이듬해 2월까지 많이 잡힌다.\n" +
                    "\n" +
                    "회, 튀김, 조림, 구이, 매운탕 등으로 먹으며, 두꺼운 껍질이 맛있기 때문에 껍질을 벗겨 밥을 싸먹기도 한다. 11월부터 이듬해 2월까지가 제철이다."
        )
        insertModel(
            "자바리",
            "60cm 이상",
            "",
            "다갈색 바탕, 옆구리에 6줄의 흑갈색 가로띠"
            ,
            "",
            "수심 50m 이내의 암초 지역"
            ,
            "한국(남부·제주도), 일본, 타이완, 중국, 말레이시아, 인도",
            R.drawable.item_jabari,
            "몸길이 60cm 이상이다. 몸과 머리는 방추형이고 옆으로 납작하다. 등지느러미 연조(soft ray:마디가 있고 끝이 갈라져 있는 지느러미 줄기)의 기저(base:기관 또는 부속기관과 몸통과 연결되는 부위)에서 측선까지의 비늘은 13∼15개이다. 꼬리지느러미 끝은 둥글다. 몸빛깔은 다갈색 바탕에 옆구리에 6줄의 흑갈색 가로띠가 뒤쪽까지 있다. 각 띠는 모양이 일정하지 않다. 이 줄무늬는 자라면서 차차 희미해져 노성어가 되면 없어진다.\n" +
                    "\n" +
                    "연안성 물고기로서 수심 50m 이내의 암초 지역에 서식하며 한 곳에 정착하여 산다. 야행성으로 저녁 때부터 먹이를 찾아 움직인다. 먹이로는 오징어류나 작은 어류 등이 있다. 산란기는 8∼10월이다. 맛이 좋아 회나 매운탕으로 이용된다. 한국(남부·제주도), 일본, 타이완, 중국, 말레이시아, 인도에 분포한다."
        )
        insertModel(
            "전어",
            "15∼31cm",
            "",
            "등쪽 암청색, 배쪽 은백색"
            ,
            "3∼8월(산란 성기 4∼5월)",
            "서식 수심은 보통 30m 이내의 바다(연안)"
            ,
            "동중국해, 일본 중부 이남, 한국 남해",
            R.drawable.item_jeona,
            "옛 문헌에는 '화살 전'자를 사용해서 전어(箭魚)로도 표기하였다. 정약전의《자산어보》에는 ‘기름이 많고 달다.’라고 기록하고 있다. 강릉에서는 새갈치, 전라도에서는 되미, 뒤애미, 엽삭, 경상도에서는 전애라고 불린다. 크기에 따라 큰 것은 대전어, 중간 크기의 것은 엿사리라고 하며, 강원도에서는 작은 것을 전어사리라 부른다. 전어는 그 맛이 좋아 다양한 방법으로 먹는데 특히 전어를 구울때 나는 냄새는 '집나간 며느리도 돌아온다.'는 말이 있고 전어의 고소한 맛 때문에 '전어 머리에는 참깨가 서말'이라는 말도 있다.\n" +
                    "\n" +
                    "몸길이는 15∼31cm이다. 몸의 등쪽은 암청색, 배쪽은 은백색을 띠며, 등쪽의 비늘에는 가운데에 각각 1개의 검은색 점이 있어 마치 세로줄이 있는 것처럼 보인다. 눈은 지방질로 되어 있는 기름눈꺼풀이 덮고 있지만, 동공 부분에는 홈이 있어 밖으로 드러나 있다. 몸은 비교적 큰 둥근비늘로 덮여 있으며, 배쪽 정중선을 따라 수십 개의 날카롭고 강한 모비늘이 나 있다.\n" +
                    "\n" +
                    "수심 30m 이내의 연안에 주로 서식한다. 6∼9월에는 만 밖으로 나갔다가 가을이면 다시 만 안으로 들어온다. 남쪽에서 겨울을 나고, 4∼6월에 난류를 타고 북상하여 강 하구에서 알을 낳는다. 산란기는 3∼8월로 긴 편이며, 4∼5월에 가장 성하다. 작은 동물성, 식물성 플랑크톤과 바닥의 유기물을 개흙과 함께 먹는다.\n" +
                    "\n" +
                    "전어는 큰 무리가 함께 이동하는데 그물로 고기떼를 둘러싼 후 배를 방망이로 두들기거나 돌이나 장대로 위협하여 놀란 고기들이 그물코에 꽂히게 하여 잡았으며 또는 함정그물로 고기떼가 지나가는 통로를 막아 고기떼를 가둘 수 있는 그물 쪽으로 유도하여 살아 있는 채로 잡기도 한다. 즉 지역에 따라 전어를 잡는 방법이 달랐다. 요즘에는 충남 보령 대천항을 중심으로 전어를 많이 잡는데 어군탐지기로 전어 무리의 위치가 발견되면 빠른 배로 전어무리 둘레에 그물을 던져 전어를 잡는다. 경남에는 보령 앞바다 만큼 전어를 많이 잡지는 않지만 그물로 전어가 이동하는 길을 막아 잡는다. 전어를 잡는 계절은 가을로 들어서는 10월이 성어기로 주로 이때 잡은 전어가 뼈도 연하고 맛도 가장 좋다. 그 이후에 잡은 전어는 뼈가 억세져 뼈가 많은 전어의 맛도 떨어진다.\n" +
                    "\n" +
                    "전어는 뼈째로 썰어서 회로 먹거나, 소금구이, 무침 등으로 먹는다. 젓갈을 담그기도 하는데, 전어 새끼로 담근 것은 엽삭젓, 혹은 뒈미젓, 내장만을 모아 담근 것은 전어 속젓이라 한다. 내장 중에서도 위만을 모아 담은 것은 전어 밤젓 또는 돔배젓이라 하며, 양이 많지 않아 귀한 젓갈에 속한다. 호남지방에서는 전어 깍두기를 담가 먹기도 한다."
        )
        insertModel(
            "전갱이",
            "약 40cm",
            "",
            "등쪽 암청색, 배쪽 은백색"
            ,
            "4∼7월",
            "수심 10∼100m의 연안이나 외양"
            ,
            "타이완, 동중국해, 일본 남부, 한국 등 북서태평양의 열대 해역",
            R.drawable.item_jeongangi,
            "경남에서는 ‘전광어’, 부산에서는 ‘메가리’또는 ‘전겡이’, 완도에서는 ‘가라지’, 함남에서는 ‘빈쟁이’, 제주에서는 ‘각재기’, 전남에서는 ‘매생이’라 부른다. 포항·마산등지에서는 일본명 그대로 ‘아지’라고 부르기도 한다.\n" +
                    "\n" +
                    "몸은 방추형으로 머리길이가 몸높이보다 길다. 아래턱이 약간 튀어나와 있고, 양 턱에는 한 줄의 작은 이빨이 흔적만 남아 있다. 등지느러미는 두 개이고 뒷지느러미의 앞쪽에는 두 개의 가시가 분리되어 있다. 꼬리지느러미는 크게 갈라져 있으며 꼬리자루는 매우 잘록하다. 남쪽에 사는 것일수록 몸빛깔이 짙고 북쪽에 사는 것일수록 옅다. 몸 중앙부터 등쪽은 암청색을 띠며, 배쪽은 은백색을 띤다.\n" +
                    "\n" +
                    "수심 10∼100m의 연안이나 외양에서 서식하며 사는 곳에 따라 습성과 몸빛깔에 차이가 있다. 회유성 어종으로 어군을 이루어 봄에서 여름에는 북쪽으로 이동하고 가을에서 겨울에는 남쪽으로 이동한다. 산란기는 북쪽으로 갈수록 늦어지는데, 한국의 경우 4∼7월이며 산란수온은 15∼26℃이다. 산란기 동안 약 2~18만 개의 알을 낳는다. 부화된 새끼는 연안의 표층에서 부유성 해조류와 함께 이동하다가 성장하면서 차츰 깊은 곳으로 이동한다. 몸길이가 14cm 전후가 되면 중, 하층의 수층에서 서식한다.\n" +
                    "\n" +
                    "어려서는 부유성 동물플랑크톤을 먹고 자라서는 작은 새우나 젓새우, 요각류 등을 좋아하며, 그 외 작은 어류, 오징어 등을 먹는다. 일단 먹이를 먹으면 한 번 토하는 습성이 있어 주로 낮에 먹이를 잡아먹는다. 주로 두릿그물, 저층 트롤어업, 정치망 등에 의하여 어획된다. 7∼8월이 제철로 소금구이, 조림, 튀김, 초밥 등으로 요리되며 일년 내내 낚시로 낚을 수 있는 어종으로 인기가 좋다. 타이완, 동중국해, 일본 남부, 한국 등 북서태평양의 열대 해역에 분포한다.\n" +
                    "\n" +
                    "수심 10∼100m의 연안이나 외양에서 서식하며 사는 곳에 따라 습성과 몸빛깔에 차이가 있다. 회유성 어종으로 어군을 이루어 봄에서 여름에는 북쪽으로 이동하고 가을에서 겨울에는 남쪽으로 이동한다. 어려서는 부유성 동물플랑크톤을 먹고 자라서는 갑각류(작은 새우류·젓새우류·요각류 등)를 좋아하며, 그 외 작은 어류, 오징어류 등을 먹는다. 일단 먹이를 먹으면 한 번 토하는 습성이 있어 주로 낮에 먹이를 잡아먹는다.\n" +
                    "\n" +
                    "산란기는 북쪽으로 갈수록 늦어지는데, 한국의 경우 4∼7월이며 산란수온은 15∼26℃이다. 부화된 새끼는 연안의 표층에서 부유성 해조류와 함께 이동하다가 성장하면서 차츰 깊은 곳으로 이동한다. 주로 두릿그물, 저층 트롤어업, 정치망 등에 의하여 어획된다. 타이완, 동중국해, 일본 남부, 한국 등 북서태평양의 열대 해역에 분포한다."
        )
        insertModel(
            "쭈꾸미",
            "약 20cm",
            "",
            "변화가 많으나 대체로 자회색"
            ,
            "5∼6월",
            "수심 10m 정도 연안의 바위틈"
            ,
            "타이완, 동중국해, 일본 남부, 한국 등 북서태평양의 열대 해역",
            R.drawable.item_jjuggumi,
            "전라남도와 충청남도에서는 쭈깨미, 경상남도에서는 쭈게미라고도 불린다. 흔히 '쭈꾸미'로 부르기도 하지만 '주꾸미'가 정확한 이름이다.\n" +
                    "\n" +
                    "몸통에 8개의 팔이 달려 있는 것은 낙지와 비슷하나, 크기가 70cm 정도 되는 낙지에 비해 몸길이 약 20cm로 작은 편에 속한다. 한 팔이 긴 낙지와 달리, 8개의 팔은 거의 같은 길이이며 몸통부의 약 두 배 정도에 달한다. 몸통을 둘러싸고 있는 외투막은 달걀처럼 한쪽이 갸름하다. 눈과 눈 사이에 긴 사각형의 무늬가 있고 눈의 아래 양쪽에 바퀴 모양의 동그란 무늬가 있으며 모두 금색이다. 몸빛깔은 변화가 많으나 대체로 자회색이다.\n" +
                    "\n" +
                    "수심 10m 정도 연안의 바위틈에 서식하며, 주로 밤에 활동한다. 산란기는 5∼6월이며, 바다 밑의 오목한 틈이 있는 곳에 포도모양의 알을 낳는다. 알은 긴지름이 1cm 정도로 큰 편이다. 봄이 되어 수온이 올라가면 먹이가 되는 새우가 많아지기 때문에 서해 연안으로 몰려든다.\n" +
                    "\n" +
                    "그물로 잡거나 소라와 고둥의 빈 껍데기를 이용한 전통적인 방식으로 잡기도 한다. 고둥, 전복 등의 껍데기를 몇 개씩 줄에 묶어서 바다 밑에 가라앉혀 놓으면 밤에 활동하던 주꾸미가 이 속에 들어간다.\n" +
                    "\n" +
                    "산란 직전 주꾸미 어미와 어린 개체 어획이 성행하면서 개체 수가 크게 감소함에 따라 해양수산부는 2018년부터 매년 5월 11일부터 8월 31일까지 주꾸미 포획을 금지해 생태계를 보호하도록 하고 있다. 이를 어길 시 2년 이하의 징역 또는 2천만 원 이하의 벌금이 부과된다."
        )
        insertModel(
            "조피볼락",
            "약 40cm",
            "",
            "전체 흑갈색, 배쪽 회색"
            ,
            "4∼6월",
            "수심 10∼100m인 연안의 암초지대"
            ,
            "타이완, 동중국해, 일본 남부, 한국 등 북서태평양의 열대 해역",
            R.drawable.item_jopibolak,
            "몸길이 40cm 정도이다. 겉모습은 볼락과 비슷한데, 특히 누루시볼락과 가장 비슷하다. 눈의 앞쪽에는 날카로운 가시가 한 쌍 있고 눈 사이에는 두 쌍, 그 뒤로는 한 쌍의 가시가 튀어나와 있다. 눈 앞가장자리를 둘러싸고 있는 골격에는 아래로 향하는 3개의 가시가 있다. 입은 크며 위턱은 눈의 뒷가장자리에 달한다. 양 턱에는 매우 작은 이빨이 띠를 형성한다.\n" +
                    "\n" +
                    "전새개골(preopercle:아가미뚜껑부를 지지하는 4개의 뼈 중 하나)에는 5개, 주새개골(opercular:아가미뚜껑부를 지지하는 4개의 뼈 중 하나)에는 2개의 가시가 있다. 몸은 작고 미약한 사각형의 빗비늘로 덮여 있다. 양 턱과 아가미뚜껑에는 비늘이 없다. 측선의 비늘수가 38개 이상이다.  \n" +
                    "\n" +
                    "등지느러미는 1개로 극조(spinous ray:지느러미 중 연조가 경골화한 것으로 마디가 없음)부와 연조(soft ray:마디가 있고 끝이 갈라져 있는 지느러미 줄기)부는 깊게 파여 있다. 등지느러미 가시는 잘 발달되어 있으며 가시 사이의 막은 깊게 파여 있다. 등지느러미 연조부는 바깥쪽 가장자리가 둥글다. 가슴지느러미는 배쪽에 치우쳐 있으며 뒷가장자리가 둥글다. 배지느러미는 가슴지느러미보다 조금 뒤에서 시작하며 항문에 겨우 달한다. 뒷지느러미는 두 번째 가시가 비교적 강하며 가장자리는 둥글다.\n" +
                    "\n" +
                    "몸은 전반적으로 흑갈색을 띠며 배쪽은 회색을 띤다. 지느러미는 흑갈색을 띤다. 머리에는 눈을 가로지르는 2개의 검은색 띠가 나타난다. 꼬리지느러미의 위쪽과 아래쪽 끝에 흰색 점이 있다.\n" +
                    "\n" +
                    "수심 10∼100m인 연안의 암초지대에서 주로 서식한다. 가을과 겨울에 남쪽으로 이동하여 월동하는 계절회유를 한다. 유어는 떠다니는 해초와 함께 이동한다. 밤에는 표층이나 중층에서 별로 움직임이 없으나 낮에는 무리를 이루어 활발히 움직인다. 먹이로는 작은 어류, 오징어류 등을 먹는 포식성 어류이다.\n" +
                    "\n" +
                    "난태생 어류로 산란은 4∼6월에 주로 이루어지는데 암컷 35cm(3세어), 수컷 28cm(2세어)가 되어야 산란을 시작할 수 있다. 정치망, 땅주낙, 걸그물로 주로 어획한다. 한국, 일본 홋카이도와 규슈지방, 중국 등의 온대 해역에 분포한다."
        )
        insertModel(
            "긴꼬리벵에돔",
            "60~70㎝",
            "",
            "몸은 전체적으로 회흑색"
            ,
            "",
            ""
            ,
            "",
            R.drawable.item_kinggoribangedom,
            "국내에 서식하고 있는 벵에돔은 두 종류다. 제주도와 남해안 · 동해안에 모두 흔한 벵에돔, 그리고 제주도에 많은 긴꼬리벵에돔이다. 이 두 벵에돔은 생김새와 색상은 비슷하지만 습성은 전혀 딴판인 물고기다.\n" +
                    "\n" +
                    "두 고기를 구별하는 가장 큰 특징은 아가미 테와 꼬리지느러미다. 벵에돔과는 달리 긴꼬리벵에돔은 아가미 뚜껑 가장자리에 검은 테두리가 있고, 꼬리자루가 벵에돔보다 가늘고 길며, 꼬리지느러미 상하엽 끝이 뾰족한 데 이어 가장자리의 윤곽이 반달 모양으로 패여 있다.\n" +
                    "\n" +
                    "습성의 차이는 크다. 벵에돔은 일정 권역에 서식하면서 계절과 수온에 따라 근거리 이동을 하는 반면, 긴꼬리벵에돔은 계절에 따라 회유 지역이 달라진다. 벵에돔에 비해 회유성이 강한 셈이다.\n" +
                    "\n" +
                    "체구에 비해 입이 작은 벵에돔 모두는 작은 이빨이 밀생하여 해조류를 갉아먹기에 적합한 구조이며 등각류와 단각류, 새우와 갯지렁이 등을 먹고 겨울에는 해조류를 즐겨 취하는 초식성이기도 하다. 50㎝가 넘는 크기의 벵에돔이 드문 데 비해 긴꼬리벵에돔은 60㎝ 이상까지도 자란다."
        )
        insertModel(
            "꼬치고기",
            "30cm",
            "",
            "등쪽은 붉은색을 띤 황갈색, 배쪽은 백색"
            ,
            "6∼7월",
            "수심 60m 부근"
            ,
            "우리나라, 일본 남부해, 동중국해, 인도양, 호주 등지",
            R.drawable.item_kkochigogi,
            "속명인 스피라에나(Sphyraena)는 그리스어로 망치(hammer)를 의미하여 이는 지질학자들이 사용하는 뾰족한 망치와 비슷하게 생겼기 때문에 붙여진 이름이다. 봉암도에서는 꼬치라고 부른다.\n" +
                    "\n" +
                    "최대 몸길이 길이 50㎝까지 자란다. 눈이 큰 점에서는 애꼬치와 닮았으나 꼬치고기의 경우는 제1등지느러미가 배지느러미보다 뒤쪽에 있는 점에서 구별된다. 몸은 원통형으로 가늘고 길며, 주둥이가 길고 눈이 크다. 입은 크고 위턱보다 아래턱이 돌출되어 있으며, 양 턱에는 날카로운 이빨이 있다. 꼬리지느러미는 암회색 바탕에 뒤끝 가장자리가 검은 색으로 끝부분이 깊게 갈라져 있다. 측선은 아가미구멍 바로 뒤에서 시작하여 직선으로 뻗으며, 측선의 비늘수는 95개이다.\n" +
                    "\n" +
                    "열대에서 온대에 걸친 바다의 표층에서 작은 고기나 오징어, 작은 새우, 게 등을 먹고 산다. 겨울에는 제주도 남부 해역에서 월동하다가, 수온이 상승하는 시기가 되면 산란을 하고 먹이를 얻기 위해 북쪽으로 올라온다. 여름과 가을에 우리나라 남해 및 동해안 일대에서 많이 출현하여 이 시기에 정치망을 이용하여 어획한다. 초여름인 6~7월경에 산란하는데, 산란기 동안 여러 번에 나누어 알을 낳는다.\n" +
                    "\n" +
                    "일본인들이 특히 즐겨먹는 물고기로 살은 희고 지방이 적당하여 맛이 좋다. 주로 소금구이로 먹으며 말려서도 먹는다. 성질이 매우 사납고 공격적이어서 맨손으로 건드리면 물릴 수 있으므로 주의한다. 음식점에서 '세꼬시'라 부르는 것은 보통 어린 물고기나 뼈가 연한 물고기를 얇게 썰어 뼈째 먹는 회를 의미하는 것으로, 말 자체는 일본어인 'せごし(세고시)' 가 어원인 것으로 보이나 가늘게(細) 썰어 꼬치고기 같은 모양으로 만들어 먹는다 하여 '세꼬치' 가 세꼬시로 변한 것으로 보기도 한다. 물의 pH가 5.0보다 낮아지면 생존하지 못하므로 수질의 오염 정도를 짐작하게 한다."
        )
        insertModel(
            "망상어",
            "15~25cm",
            "",
            "등쪽은 짙은 푸른색 또는 적갈색, 배쪽은 은백색"
            ,
            "4~6월",
            "수심 30m 정도의 얕은 바다"
            ,
            "우리나라, 중국, 일본 등의 북서태평양",
            R.drawable.item_mangsang,
            "《자산어보》에서는 망치어(望峙魚)로 소개하였다. 남해에서는 바다망성어, 충무에서는 망싱이, 흑산도에서는 망치어라고도 부른다.\n" +
                    "\n" +
                    "같은 과에 속한 인상어와 생김새가 아주 비슷해서 자주 혼동이 되지만 체형과 이빨이 난 방식, 무늬에서 다소 차이가 난다. 몸은 타원형이며 좌우로 아주 납작한 편이다. 입이 작으며 길이가 서로 비슷한 위, 아래턱에는 한 줄로 작은 이빨이 나 있다. 눈에서 위턱 방향으로 두 줄의 어두운 갈색선이 지나가며 아가미 뚜껑 위에 검은색 점이 있다. 등지느러미에는 9~11개의 가시가 있다. 몸색깔은 살아가는 장소에 따라서 다소 차이가 나서, 크게 등쪽이 거무스름한 푸른색을 띠는 것과 적갈색인 두 종류로 나눌 수 있다.\n" +
                    "\n" +
                    "우리나라와 일본, 중국 등 북서태평양의 일부지역에서만 서식하고 있으며, 주로 바닥이 모래나 진흙으로 이루어진 얕은 바다에서 산다. 무리를 지어 생활하는 습성이 있고, 동물성플랑크톤이나 갯지렁이, 작은 새우류, 조개 등을 먹고 산다. 일반 물고기와 달리 체내수정을 통해 번식을 하는데, 10~12월 사이에 짝짓기를 통해 암컷 몸 안에 있는 알이 수정되면 이듬해 1~2월 경에 알이 부화한다. 부화한 알은 5~10mm 정도 크기의 새끼가 되며, 이 이후에도 약 5~6개월 동안 암컷의 몸 안에서 자라다가, 4~6월이 되어서야 몸 밖으로 나오게 된다. 암컷이 한 배에서 낳는 새끼는 10~30마리이며, 이때 새끼의 몸길이는 5~6cm 정도로 1년 만에 12~16cm, 3~4년이 지나면 25cm 가까이 자라게 된다.\n" +
                    "\n" +
                    "연안에서 그물로 물고기를 잡다가 많이 따라올라오며, 해안가에서는 밑밥을 이용한 손낚시로 잡을 수 있다. 구이, 탕, 조림 등으로 다양하게 조리해 먹을 수 있다."
        )
        insertModel(
            "무늬오징어",
            "몸길이 약 21cm, 나비 약 10cm",
            "",
            "등쪽에 여러 개의 암회갈색 가로무늬, 타원형의 반문(수컷)"
            ,
            "",
            "수심 15~100m정도의 연안"
            ,
            "남서태평양·남동중국해·한국(다도해 이남)·일본",
            R.drawable.item_muniojinga,
            "두족류에 속한다. 무늬오징어라고도 한다. 최대 몸길이 38cm, 최대 무게 5kg에 달하는 대형 오징어이다. 위쪽으로 보았을 때 전체적으로 타원형의 몸통을 갖는다. 배쪽에 입술자국의 무늬가 있으며, 등쪽 표면에도 입술자국 같은 무늬가 있어 산재하여 입술무늬오징어라는 이름이 붙었다. 몸통의 등쪽과 지느러미의 경계부분에 은백색의 세로줄이 나 있다.\n" +
                    "\n" +
                    "4번째 팔이 가장 길며 3번째, 1번째 팔 순으로 길이가 짧아진다. 일반적으로 크기가 거의 같은 흡반이 8줄로 나 있다. 3~5에 산란을 위해  15~30m 수심의 연안으로 접근하며, 이 시기에 어업이 성행한다. 살이 두껍고 맛있으며 일본과 홍콩 등지에서 갑오징어 중 두번째로 중요한 상업종이다. 남서태평양 및 남동중국해·한국(다도해 이남)·일본 등지에 분포한다."
        )
        insertModel(
            "눈볼대",
            "10~15cm, 암컷 최대 40cm",
            "",
            "전체적으로 붉은색, 배쪽으로 갈수록 연해짐"
            ,
            "7~10월",
            "깊이 80~150m의 연안"
            ,
            "우리나라의 남해, 일본, 인도네시아, 오스트레일리아 북서부를 지나는 서태평양",
            R.drawable.item_nonboldae,
            "몸에 비해 눈이 상당히 커서 사람들은 이 물고기에 ‘눈볼대’라는 이름을 붙였다. 또한 입 속이 검은 빛을 띠고 있기 때문에 ‘목이 검은 농어’라는 뜻의 ‘blackthroat seaperch’라는 영어 이름을 사용한다. 몸 빛이 붉어서 부산과 경상남도에서는 ‘빨간고기’ 또는 ‘붉은고기’로 부르기도 한다.\n" +
                    " \n" +
                    "몸이 타원형이며 옆으로 납작한 편이다. 아래턱이 위턱보다 튀어나와 위쪽을 향해있고, 눈이 상당히 커서 그 지름을 따지면 주둥이 길이보다 길다. 입과 아가미 속은 검은색이며 양 턱에는 송곳니가 1쌍씩 솟아있다. 등지느러미는 1개이며, 꼬리지느러미 끝부분이 약간 오목하고 검은 빛을 띤다. 배 부분을 제외한 몸은 전체적으로 붉은빛을 띠지만 지느러미는 노란색에 가깝다.\n" +
                    " \n" +
                    "수온이 10~20℃에 이르는 깊이 80~150m 연안에서 살며, 가을과 겨울 사이에 깊은 바다에서 지내다가 봄이 되어 얕은 연안으로 이동하는 회유성을 보인다. 육식성으로 작은 물고기, 새우나 게와 같은 갑각류, 오징어 같은 연체동물을 잡아먹고 산다. 7~10월 사이에 산란이 이루어지며, 이 때 암컷은 약 25만 개의 알을 낳는다. 알은 0.9mm정도의 크기로, 부화한 후 몸길이 1cm로 자라면 각 지느러미가 모두 갖춰진다. 이후 자라는 속도나 수명은 암컷과 수컷에 따라서 차이가 생기는데, 수컷이 3~4년 정도를 살면서 20cm 이하의 크기로 자라는 반면, 암컷은 약 10년 가량을 살며 30~40cm까지 자란다. 수컷은 알에서 부화한 후 3년, 암컷의 경우 4년이 지나면 짝짓기가 가능한 시기가 된다.\n" +
                    " \n" +
                    "주로 트롤어업에 의해서 잡힌다. 다른 물고기에 비해 몸집이 작지만, 맛이 좋은 고급 어종으로 취급되며 구이나 찌개 등으로 조리해서 먹는다."
        )
        insertModel(
            "농어",
            "1m",
            "",
            "등쪽-푸른색, 배쪽-은백색"
            ,
            "11월∼이듬해 4월",
            "연안이나 만입구의 수심 50∼80m 되는 약간 깊은 곳"
            ,
            "동중국해, 타이완, 일본, 한국 연근해",
            R.drawable.item_nonga,
            "《난호어목지》에는 '깍정'이라 하였고, 정약용의《아언각비》에서는 농어(農魚), 《자산어보》에서는 걸덕어(乞德魚)라 하였다. 경남 통영에서는 농에, 부산에서는 깡다구, 전남에서는 깔대기, 껄떡, 울릉도에서는 연어병치, 독도돔으로 불린다. 30cm 안팎의 작은 것은 부산에서는 까지매기, 완도에서는 절떡이라고 불리며, 특히 몸통에 검은 점이 많고 작은 것은 전남 순천과 장흥에서 깔따구, 껄떡이로 불린다.\n" +
                    "\n" +
                    "몸길이 약 1m이다. 몸은 긴 타원형으로 8등신이라 할 만큼 가늘고 길며, 옆으로 납작하다. 옆줄은 몸 중앙보다 약간 등쪽에 있으며 꼬리지느러미까지 거의 일직선으로 뻗어 있다. 몸의 등 쪽은 푸른색을 띠며 옆줄을 경계로 밝아져서 배 쪽은 은백색을 띤다. 어릴 때에는 옆구리와 등지느러미에 작고 검은 점이 많이 흩어져 있으나, 자라면서 검은 점의 수가 적어진다. 우리나라 서해에서 서식하는 농어의 경우에는 성장한 후에도 비교적 큰 검은 점을 가지고 있다. 등지느러미와 뒷지느러미에 강한 가시가 있으며, 등지느러미에는 2∼3개의 작고 어두운 갈색의 둥근 무늬가 나타난다. 몸과 머리는 뒷가장자리에 가시가 있는 빗 모양의 작은 비늘로 덮여 있다.\n" +
                    "\n" +
                    "봄부터 여름까지는 먹이를 먹기 위하여 육지에 가까운 얕은 바다로 이동하고, 겨울철에는 알을 낳고 겨울을 나기 위하여 수심이 깊은 곳으로 이동한다. 어릴 때에는 담수를 좋아하여 봄에는 육지에 가까운 바다로 들어오며, 여름에는 강 하구까지 거슬러 왔다가 가을이 되면 깊은 바다로 이동한다. 육식성으로서 소형 어류, 새우류를 먹는다. 특히 멸치를 잘 먹어서, 멸치가 연안으로 몰려오는 봄, 여름이면 멸치떼를 쫓아 연안을 돌아다닌다. 산란기는 11월에서 이듬해 4월이며, 연 1회, 연안이나 만 입구의 수심 50∼80m 되는 약간 깊은 곳의 암초 지대에 알을 낳는다. 10～20℃에서 산란이 가능하며 최적 수온은 15℃이다. 보통은 수온 7～25℃인 곳에서 서식하고 최적 서식 수온은 15～19℃이다.\n" +
                    "\n" +
                    "깊은 바다에 서식하기 때문에, 낚시에 미끼를 달아 바닥 가까이 내려서 잡거나, 그물의 아랫깃이 바다 밑바닥에 닿도록 한 후 어선으로 그물을 끌어서 잡기도 한다. 여름에 많이 잡히며, 6~8월이 제철이다. 살이 희며, 어린 고기보다는 성장할수록 맛이 좋다. 지리, 찜, 회 등으로 먹는다."
        )
        insertModel(
            "노랑볼락", "최대 몸길이 40cm", "", "전체 노란 체색"
            , "", "수심 200~350m 이내의 암초나 모래지대"
            , "", R.drawable.item_norangbolak,
            "체형은 영락없는 우럭(조피볼락)인데, 이름처럼 노란 체색을 띈다. 그래서 시장에선 일반적으로 황우럭,황볼락,황열기,노랑우럭 등으로 많이 불리고 있다\n" +
                    "수심 200~350m 이내의 암초나 모래지대에서 서식하는데, 볼락이나 열기(불볼락)처럼 무리를 이루어 생활한다.\n" +
                    "몸길이는 40cm 정도까지 자란다고 알려져 있는데 그보다 큰 놈들도 자주 잡힌다.\n" +
                    "가장 큰 특징은 역시 밝은 황색을 띄는 체색이다.\n" +
                    "노란 바탕의 몸에는 거뭇거뭇하고 불규칙적인 얼룩무늬가 나 있고, 측선을 따라서 아주 선명한 황색 줄이 나타난다.\n" +
                    "찬 물에서 서식하는 한류성 어종으로, 국내에서는 동해 북부 강원 지역에서만 볼 수 있는 특산어종이다. 어획량이 많지 않아 쉽게 보긴 어려운 생선이다. 산지인 강원 지역에서는 20년 가까지 자취를 감췄다는데, 얼마전부터 다시 모습을 드러내며 낚시대상으로 인기가 급상승하고 있다.\n" +
                    "특히 많은 개체수가 나오는 시기가 2~4월인데, 이 무렵은 어한기(다른 낚시 대상어종이 드문 때)이기도 해서 낚시꾼들에겐 노랑볼락이 더욱 반갑다.\n" +
                    "국내 주 산지는 역시 동해안 북부지역이고 강원도 고성에서 삼척, 경북 울진까지 또한 낚시 시즌이 지나면 다시 모습을 감추기 때문에 희소성까지 더해져, 한번쯤 낚아보고 싶어하는 사람들이 많다.\n" +
                    "게다가, 위에 말했듯이 무리를 이뤄 생활하는 군집성이 있기 때문에 운만 따르면 쿨러를 가득 채워올 수도 있다.\n" +
                    "특히 외줄낚시 채비에 몽땅걸이로 끌려 나올 때면 손맛도 장난이 아니라고 한다. 한마리 한마리 씨알도 볼락이나 열기보다 더욱 굵직하고," +
                    "노랑우럭은 손맛 뿐만 아니라 입맛도 좋은 생선이다. 회나 매운탕으로 주로 즐겨먹는데, 신선할 때 바로 회를 떠 먹어 보면 살에 단맛/감칠맛이 제법 진한편이며 식감 또한 제법 탄탄하다. 또한 '구이의 최강자'볼락의 사촌답게 구워 먹어도 맛이 좋다. 제한된 시기에 제한된 곳에서만 잡히는 데다가, 손맛은 물론 입맛까지 좋으니 기특한 놈이다."
        )
        insertModel(
            "넙치농어",
            "70cm",
            "",
            "등쪽-회청록색, 배쪽-은백색"
            ,
            "",
            "연해와 내만"
            ,
            "한국·일본·타이완·동중국해",
            R.drawable.item_nupchinoga,
            "몸길이 약 70cm이다. 몸은 가늘고 길며 옆으로 납작하다. 등지느러미의 가시부와 연조부는 분리되어 있고 성어의 연조수는 15∼16개이다. 아래턱 배쪽에 1줄의 비늘이 있고 유공측선(有孔側線)의 비늘이 71∼76개인 점이 특징이다.\n" +
                    "몸빛깔은 등쪽이 회청록색이고 배쪽은 은백색이다. 유어에는 옆구리와 등지느러미에 작고 검은 점이 흩어져 있다. 각 지느러미는 암회색이다.\n" +
                    "\n" +
                    "몸길이 20∼30cm인 것은 강 하류에서, 50∼70cm인 것은 내만에서 낚시의 대상이 된다. 봄철에는 야위어 별종인 것처럼 변하였다가 여름이 되면 회복되어 맛이 좋다. 한국·일본·타이완·동중국해의 연해와 내만 등지에 분포한다."
        )
        insertModel(
            "삼치",
            "최대 몸길이 100cm",
            "7.1kg",
            "등쪽 회색을 띤 푸른색, 배쪽 은백색"
            ,
            "",
            "연근해의 아표층"
            ,
            "북서태평양의 온대 해역",
            R.drawable.item_samchi,
            "몸은 가늘고 길며 옆으로 납작하다. 등쪽은 회색을 띤 푸른색이며, 배쪽은 은백색으로 금속성 광택을 띤다. 몸 옆구리에는 회색의 반점이 7∼8줄 세로로 점이 흩어져 있고, 등, 가슴, 꼬리지느러미는 검은색이다. 매우 작은 비늘로 덮여 있다. 옆줄은 한 개로 물결무늬 모양이고, 옆줄의 아래 위에 직각방향으로 가느다란 가지가 많이 나와 있다. 최대 몸길이 100cm, 몸무게 7.1kg까지 성장한다.\n" +
                    "\n" +
                    "삼치 본문 이미지 1\n" +
                    "\n" +
                    "우리나라의 서해와 남해, 동중국해, 일본의 홋카이도 이남, 러시아의 블라디보스토크 등 북서태평양의 온대 해역에 분포하며, 연근해의 수심 100~300m에 해당하는 아표층에 서식한다. 봄(3∼6월)에는 산란을 위해 연안 또는 북쪽으로 이동하는 산란회유(spawning migration)를 하며, 가을(9∼11월)에는 남쪽인 일본 근해로 먹이가 풍부한 곳을 찾아 이동하는 색이회유(feeding migration)를 한다. 거문도 주변 해역에서는 일 년 내내 분포한다. 산란기는 4∼6월 경으로서 서해와 남해의 연안에 몰려와 새벽녘에 산란하며, 성장속도가 매우 빨라 부화 후 6개월이면 몸길이 33~46cm까지 자란다. 어릴 때에는 갑각류, 어류 등을 먹지만 어른이 되면 멸치, 까나리 등 어류를 주로 먹는다.\n" +
                    "\n" +
                    "수심 50m 정도의 연안의 낮은 바다에서 걸그물을 이용하여 그물코에 걸리게 하여 잡거나, 난바다(원양)로 향하는 울타리 그물을 이용하여 고기 떼를 유도한 뒤, 통로를 차단하여 하루에 1~2회 그물에 걸린 고기를 잡아 올리는 정치망 어업으로 어획한다. 남해안과 서해안에서 주로 잡히나, 수입산도 유통된다.\n" +
                    "\n" +
                    "삼치는 살이 약해 숙련된 사람이 아니면 회로 뜨기가 어렵기 때문에, 대개는 살짝 얼려서 회를 뜬다. 소금구이, 찜, 튀김 등으로 조리하며, 지방 함량이 높은 편이나 불포화지방산이기 때문에 동맥경화, 뇌졸중, 심장병 예방에 도움이 된다. 살이 연하고 지방질이 많아 다른 생선에 비해 부패 속도가 빠르므로 식중독에 주의해야 한다.\n" +
                    "\n" +
                    "보통 삼치라는 이름으로 여러 종의 물고기들을 뭉뚱그려서 부르기도 하지만 공식적으로 삼치라는 국명은 'Japanese Spanish mackerel(Scomberomorus niphonius)' 단일종에 한한다."

        )
        insertModel(
            "붉은쏨뱅이",
            "최대 몸길이 30cm",
            "",
            "전체 갈색, 배쪽 흰색"
            ,
            "11∼3월",
            "수심 10∼100m의 암초 지역"
            ,
            "중국, 타이완, 일본, 한국, 필리핀 등 서부태평양의 열대 해역",
            R.drawable.item_ssombangi,
            "일본명은 Utkari-kasago이다. 최대 몸길이 37cm까지 성장한다. 몸은 크고 몸높이는 높으며 옆으로 납작하다. 눈은 크며 두 눈 사이는 부드럽게 안으로 들어가 있다. 눈의 등쪽 가장자리를 따라 4개의 가시가 있고, 머리 뒷부분에는 3개의 가시가 있지만 눈의 아랫가장자리 골격에는 가시가 없다.\n" +
                    "\n" +
                    "입은 머리의 앞 끝에 위치하며 위턱은 동공의 끝에 달한다. 양 턱에는 작은 이빨이 무리지어 있고 구개골(palatine:입천장부를 지지하는 7개의 뼈 중 하나로 가장 앞쪽에 위치)과 서골(vomer:경골어류의 두개골에 있는 가장 앞쪽 배면에 위치하는 골편으로 주로 1개이지만 1쌍인 경우도 있음)에도 이빨이 있다.\n" +
                    "\n" +
                    "가슴지느러미는 11번째 연조(soft ray:마디가 있고 끝이 갈라져 있는 지느러미 줄기)가 가장 길다. 등지느러미 가시와 가시 사이를 연결하는 막은 깊게 패어 있다. 몸의 등쪽은 붉은색 바탕에 크기가 다양한 둥근 모양의 연한 색 반문이 있으며 중앙에서부터 배쪽으로는 희다. 모든 지느러미는 붉은색을 띠며 가슴지느러미 기저(base:기관 또는 부속기관과 몸통과 연결되는 부위)의 중앙에는 작은 점이 흔적만 남아 있다.\n" +
                    "\n" +
                    "저서성 어류로 깊은 바다의 암초 지역에서 주로 서식한다. 먹이는 주로 새우류, 게류 등의 갑각류와 어류를 먹는다. 태생어이다. 동중국해, 일본 남부, 한국 남부 등 서부태평양의 열대 해역에 분포한다.\n" +
                    "\n" +
                    "쏨뱅이와 비슷하지만 몸빛깔에서 쏨뱅이는 짙은 갈색을 띠므로 이 점에 유의하면 구별하기 쉽다. 또 가슴지느러미의 기저에 있는 담황색의 반문이 쏨뱅이는 분명하게 나타난다."
        )
        insertModel(
            "숭어",
            "최대 몸길이 120cm",
            "8kg",
            "등쪽 암청색, 배쪽 은백색"
            ,
            "한국 10∼2월(산란성기 10∼11월)",
            "연안"
            ,
            "태평양, 대서양, 인도양의 온대·열대 해역",
            R.drawable.item_sunga,
            "《자산어보》에는 치어라 기재하고, 숭어의 형태·생태·어획·이명 등에 관하여 설명하고 있다. “몸은 둥글고 검으며 눈이 작고 노란빛을 띤다. 성질이 의심이 많아 화를 피할 때 민첩하다. 작은 것을 속칭 등기리(登其里)라 하고 어린 것을 모치(毛峙) 또는 모쟁이라고 한다. 맛이 좋아 물고기 중에서 제1이다.”라고 하였다.\n" +
                    "\n" +
                    "숭어는 예로부터 음식으로서만 아니라 약재로도 귀하게 여겼다. 또 고급 술안주로도 이용하였는데 난소를 염장하여 말린 것을 치자(子)라 하여 귀한 손님이 왔을 때만 대접하였다고 한다. 《난호어목지》에 “숭어를 먹으면 비장(脾臟)에 좋고, 알을 말린 것을 건란(乾卵)이라 하여 진미로 삼는다.”고 하였다.\n" +
                    "\n" +
                    "《향약집성방》 《동의보감》에는 수어(水魚)라 하였고, “숭어를 먹으면 위를 편하게 하고 오장을 다스리며, 오래 먹으면 몸에 살이 붙고 튼튼해진다. 이 물고기는 진흙을 먹으므로 백약(百藥)에 어울린다.”고 하였다. 《세종실록 지리지》에는 건제품(乾製品)을 건수어(乾水魚)라 하며 자주 보이는 것으로 보아 소비가 많았던 것으로 추정된다. 한국산 숭어 중에는 영산강 하류 수역에서 잡히는 것이 숭어회로서 일품이다.\n" +
                    "\n" +
                    "최대 몸길이 120cm, 몸무게 8kg이다. 머리는 다소 납작하지만 몸 뒤쪽으로 가면 옆으로 납작하다. 눈은 크며 잘 발달된 기름눈까풀로 덮여 있다. 눈 앞에는 2쌍의 콧구멍이 있다. 입은 비스듬히 경사져 있고 입술은 얇으며 위턱의 뒤끝은 눈의 앞가장자리에 달한다. 위턱은 아래턱보다 약간 길며, 양 턱에는 가느다란 솜털 모양의 이빨이 1줄로 나 있다.\n" +
                    "\n" +
                    "새개골의 뒷가장자리는 부드럽다. 아가미는 아랫조각과 윗조각의 경계가 마치 활처럼 휘어져 있으며 짧고 가느다란 새파(gill raker:원구류를 제외한 어류에서 새궁의 안쪽에 2줄로 줄지어 있는 돌기물을 가리키며, 새파의 중심부는 골질로서 표면은 편평상피로 덮여 있고 점액세포나 맛봉오리도 산재함)를 가진다.\n" +
                    "\n" +
                    "등지느러미는 2개로 분리되어 있으며, 제1등지느러미는 주둥이 끝과 꼬리지느러미 기저(base:기관 또는 부속기관과 몸통과 연결되는 부위)와의 중간에 위치한다. 가슴지느러미는 비교적 작고, 몸의 중앙에 위치한다. 몸은 비교적 큰 둥근비늘(원린)로 덮여 있으며 머리는 주둥이 끝에만 비늘이 없다. 측선은 없다.\n" +
                    "\n" +
                    "몸의 등쪽은 암청색을 띠며 배쪽으로 밝아져 은백색을 띤다. 지느러미는 연한 갈색을 띠며 배지느러미만 투명하다. 가슴지느러미 기저에 푸른색의 반점이 있다. 비늘 가운데에 흑백 반점이 있어 여러 줄의 작은 세로줄이 있는 것처럼 보인다.\n" +
                    "\n" +
                    "산란은 한국의 경우 10∼2월(산란성기는 10∼11월)에 연안에서 이루어지며, 산란기에는 쿠로시오난류의 영향을 받는 따뜻한 해역으로 회유한다. 산란을 위한 최소 몸길이는 30cm 이상이다. 알은 한 배에 290∼720만 개 정도이고 2∼5일 후 부화한다. 겨울 동안 바다에서 태어난 유어들은 무리를 지어 연안으로 몰려와 부유생물을 먹는다. 여름에는 성장이 빨라서 초가을이 되면 몸길이가 20cm가 넘는다. 수온이 내려가는 가을에는 민물을 떠나 바다로 내려간다. 성어의 경우 잡식성으로 작은 어류를 비롯한 저서생물, 단각류, 유기성 잔류물 등을 먹는다.\n" +
                    "\n" +
                    "주로 연안에 서식하나 강 하구나 민물에도 들어간다. 도약력이 뛰어나 수면 위로 약 1m~1.5m까지 뛰어오른다. 뛰어오를 때에는 꼬리로 수면을 치면 거의 수직으로 뛰어오르며 내려올 때는 몸을 한 번 돌려 머리를 아래로 하고 떨어진다. 대개 수명은 4∼5년이다. 태평양, 대서양, 인도양의 온대·열대 해역에 광범위하게 분포한다."
        )
        insertModel(
            "열기",
            "최대 35cm",
            "0.8kg",
            "회갈색, 몸 옆구리 불분명한 검은색 가로무늬가 5∼6줄(한국)"
            ,
            "1~2월",
            "암초로 된 연안"
            ,
            "한국·일본 등 북서태평양의 아열대 해역",
            R.drawable.item_yeolgi,
            "《현산어보》에는 적박순어, 맹춘어로 기록되어 있다. 남해안에서는 열기, 함경북도에서는 동감펭이라고 불린다.\n" +
                    "\n" +
                    "최대 몸길이 30cm 정도로, 볼락보다 크다. 몸은 긴 달걀모양이며, 몸과 머리는 옆으로 납작하다. 머리는 크고 머리에 있는 가시는 약한 편이다. 양 턱에는 가느다란 솜털 모양의 이빨이 띠를 형성한다. 옆줄은 뚜렷하며 등의 바깥가장자리를 따라 둥글게 구부러져 있다. 몸은 비교적 작은 사각형의 빗모양의 비늘로 덮여 있다. 몸은 전체적으로 붉은색이며, 등쪽으로 4∼5개의 짙은 갈색 무늬가 나타난다. 아가미뚜껑 위쪽에 1개의 검은 반점이 있고, 눈은 황금색이다. 등지느러미는 녹갈색을 띠, 가슴지느러미, 배지느러미, 뒷지느러미는 오렌지색, 꼬리지느러미는 짙은 갈색을 띤다.\n" +
                    "\n" +
                    "수심 80∼150m의 암초지역의 바다 밑바닥에 주로 서식한다. 어릴 때에는 떠다니는 해조류의 그늘 아래에서 함께 떠다니다가, 자라면서 해조류에서 벗어나기 시작하여, 6cm 정도 자라면 바다 밑바닥에서 생활한다. 암컷과 수컷이 교미하여 암컷의 배 속에서 알을 부화시켜, 2∼6월에 몸길이 6mm 정도의 새끼를 낳는다. 바다 밑바닥에 사는 새우류, 게류, 작은 어류, 갯지렁이류 등을 먹는다.\n" +
                    "\n" +
                    "바다 밑바닥에 서식하기 때문에, 그물의 아랫깃이 바다 밑바닥에 닿도록 하여 어선으로 그물을 끌어서 잡거나, 한 가닥의 기다란 줄에 일정한 간격으로 줄을 달고, 그 끝에 낚시와 미끼를 달아 바다 밑바닥에 닿도록 드리웠다가 차례로 거두어 올리면서 낚인 고기를 떼어낸다.\n" +
                    "\n" +
                    "볼락보다는 육질이 무르지만 맛이 좋다. 주로 회로 먹으며, 소금구이나 찌개로 먹기도 한다."
        )
        insertModel(
            "연어병치",
            "최대 몸길이 90cm",
            "",
            "검은색 또는 푸른빛"
            ,
            "",
            "수심 100∼500m의 바다"
            ,
            "서부태평양, 동중국해, 일본 남부, 한국 남해",
            R.drawable.item_yeonabyuongchi,
            "일본명은 Medai이다. 최대 몸길이 90cm까지 성장한다. 몸은 높고 횡단면은 타원형에 가까우며 옆으로 납작하다. 머리도 옆으로 납작하고 머리의 앞끝은 둥글다. 눈은 커서 지름이 주둥이의 길이보다 길다. 눈 앞쪽에는 2쌍의 콧구멍이 있고 앞콧구멍은 타원형이지만 뒤콧구멍은 칼로 찢은 듯하다.\n" +
                    "\n" +
                    "주둥이는 끝이 둥글고 입은 머리의 배쪽으로 약간 치우쳐 있다. 위턱의 뒤끝은 눈의 앞가장자리를 조금 지난다. 양 턱에는 매우 작은 이빨이 1줄로 뻗어 있다. 전새개골(preopercle:아가미뚜껑부를 지지하는 4개의 뼈 중 하나)의 끝은 톱니가 덜 발달해 있다.\n" +
                    "\n" +
                    "등지느러미 가시는 매우 미약하게 나타나고 몸의 중앙부터는 연조(soft ray:마디가 있고 끝이 갈라져 있는 지느러미 줄기)부가 잘 발달되어 있다. 가슴지느러미는 잘 발달되어 항문에 달하고, 배지느러미는 가슴지느러미의 절반에 달한다. 꼬리지느러미는 가랑이형이다.\n" +
                    "\n" +
                    "측선은 약간 구불구불한 형태로 아가미구멍 뒤에서 시작하여 머리의 등쪽에 치우쳐서 나타나며 뒤로 갈수록 몸쪽의 중앙으로 경사진다. 몸은 매우 작은 둥근비늘(원린)로 덮여 있고 떨어지기 쉬우며 머리에는 비늘이 없다. 몸은 전체적으로 검거나 푸른빛을 띤다. 각 지느러미는 검거나 회갈색을 띤다.\n" +
                    "\n" +
                    "치어 때는 떠 다니는 해조류에서 서식하다 성어가 되면 수심 100∼500m에서 주로 서식한다. 일본쪽에서는 주로 겨울∼봄 동안에 산란이 일어난다. 저층 트롤어업에 의하여 부수어획되나 어획량은 많지가 않다. 서부태평양, 동중국해, 일본 남부, 한국 남해 등에 분포한다."
        )

    }

}
