package com.ex2i.fisherman.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.ex2i.fisherman.R


class ForthFragment : Fragment() {
    // Store instance variables
    private var title: String? = null
    private var page = 0

    // Store instance variables based on arguments passed
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        page = arguments!!.getInt("someInt", 0)
        title = arguments!!.getString("someTitle")
    }

    // Inflate the view for the fragment based on layout XML
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_tutorial_forth, container, false)
        return view
    }

    companion object {
        // newInstance constructor for creating fragment with arguments
        fun newInstance(page: Int, title: String?): ForthFragment {
            val fragment = ForthFragment()
            val args = Bundle()
            args.putInt("someInt", page)
            args.putString("someTitle", title)
            fragment.arguments = args
            return fragment
        }
    }
}