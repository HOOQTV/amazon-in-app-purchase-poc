package com.dimpossitorus.sample.amazoniap.plan


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amazon.device.iap.PurchasingService
import com.dimpossitorus.sample.amazoniap.R
import kotlinx.android.synthetic.main.fragment_plan_detail.*

class PlanDetailFragment : Fragment() {

    lateinit var sku: Sku;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plan_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sku = arguments?.get("sku") as Sku
        name.text = sku.name
        detail.text = sku.description
        price.text = "%.0f".format(sku.price)

        subscribe.setOnClickListener {
            PurchasingService.purchase(sku.skuId);
        }

        cancel.setOnClickListener {
            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.remove(this)
            fragmentTransaction?.commit()
        }
    }

    override fun onResume() {
        super.onResume()
    }
}
