package com.ex2i.fisherman

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        list = arrayListOf("벵에돔","벤자리","보구치","볼락","보리멸","붕장어","부시리","참돔","청어","대구","대구횟대","띠볼락","독가시치","도다리",
            "돌돔","개볼락","각시가자미","갈치","감성돔","강도다리","갑오징어","쥐노래미","고등어","광어","학꽁치","한치","홍감펭","호래기","황어","황점볼락","임연수어"
        ,"자바리","전어","전갱이","쭈꾸미","조피볼락","긴꼬리벵에돔","꼬치고기","망상어","무늬오징어","눈볼대","노랑볼락","넙치농어","삼치","붉은쏨뱅이","숭어","열기","연어병치")

        autoCompleteTextView.setAdapter(
            ArrayAdapter(
                this, android.R.layout.simple_dropdown_item_1line, list
            )
        )

        autoCompleteTextView.threshold = 1

        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()

            val result: RealmResults<Model> = realm.where<Model>()
                .equalTo("title", selectedItem)
                .findAll()

            val image = result[0]?.image

            val intent = Intent(this, DetailActivity::class.java)

            intent.putExtra("name",selectedItem)
            intent.putExtra("image",image)
            startActivity(intent)
            finish()
        }

        // Set a focus change listener for auto complete text view
        autoCompleteTextView.onFocusChangeListener = View.OnFocusChangeListener{
                view, b ->
            if(b){
                // Display the suggestion dropdown on focus
                autoCompleteTextView.showDropDown()
            }
        }

    }


}
