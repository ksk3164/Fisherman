package com.ex2i.fisherman.Activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.ex2i.fisherman.Model
import com.ex2i.fisherman.R
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private var list: ArrayList<String> = arrayListOf()
    private val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        list = arrayListOf(
            "각시가자미",
            "갈치",
            "감성돔",
            "갑오징어",
            "강도다리",
            "개볼락",
            "고등어",
            "광어",
            "긴꼬리벵에돔",
            "꼬치고기",
            "넙치농어",
            "노랑볼락",
            "눈볼대",
            "대구",
            "대구횟대",
            "도다리",
            "독가시치",
            "돌돔",
            "띠볼락",
            "망상어",
            "무늬오징어",
            "벤자리",
            "벵에돔",
            "보구치",
            "보리멸",
            "볼락",
            "부시리",
            "붉은쏨뱅이",
            "붕장어",
            "삼치",
            "숭어",
            "연어병치",
            "열기",
            "임연수어",
            "자바리",
            "전갱이",
            "전어",
            "조피볼락",
            "쥐노래미",
            "쭈꾸미",
            "참돔",
            "청어",
            "학꽁치",
            "한치",
            "호래기",
            "홍감펭",
            "황어",
            "황점볼락"
        )

        autoCompleteTextView.setAdapter(
            ArrayAdapter(
                this, android.R.layout.simple_dropdown_item_1line, list
            )
        )

        autoCompleteTextView.threshold = 1

        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()

                val result: RealmResults<Model> = realm.where<Model>()
                    .equalTo("title", selectedItem)
                    .findAll()

                val image = result[0]?.image

                val intent = Intent(this, DetailActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                intent.putExtra("name", selectedItem)
                intent.putExtra("image", image)
                startActivity(intent)
            }

        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }

        // Set a focus change listener for auto complete text view
        autoCompleteTextView.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            if (b) {
                // Display the suggestion dropdown on focus
                autoCompleteTextView.showDropDown()
            }
        }

        autoCompleteTextView.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(p0: View?, p1: Int, p2: KeyEvent?): Boolean {
                if(p1 == KEYCODE_ENTER){
                    return true
                }
                return false
            }
        })
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }


}
