package com.irvan.smartlaundry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.modalmap_fragment.view.*

class ModalMapFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modalmap_fragment, container, false)

        val nama = arguments?.getString("nama")
        val opsi1 = arguments?.getString("opsi1")
        val opsi2 = arguments?.getString("opsi2")
        val opsi3 = arguments?.getString("opsi3")
        val durasi1 = arguments?.getString("durasi1")
        val durasi2 = arguments?.getString("durasi2")
        val durasi3 = arguments?.getString("durasi3")
        val cover = arguments?.getInt("cover")

        view.name.setText(nama)




        return view
    }

}