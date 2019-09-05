package com.dimpossitorus.sample.amazoniap

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import com.dimpossitorus.sample.amazoniap.adapter.SkuListAdapter
import com.dimpossitorus.sample.amazoniap.plan.PlanDetailFragment
import com.dimpossitorus.sample.amazoniap.plan.Sku
import com.dimpossitorus.sample.amazoniap.plan.SkuResponse
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SkuListAdapter.OnItemClickListener {

    var currentUserId = ""
    var currentMarketplace = ""
    lateinit var handler: Handler
    lateinit var skuResponse: SkuResponse

    var hasActiveSku = false

    override fun onClick(sku: Sku) {
        val bundle = Bundle()
        bundle.putSerializable("sku", sku)
        val planDetailFragment = PlanDetailFragment()
        planDetailFragment.arguments = bundle;
        addFragment(planDetailFragment)
    }

    val skuJson = "{\n" +
            "  \"skus\": [\n" +
            "    {\n" +
            "      \"name\": \"30 Days Subscription\",\n" +
            "      \"description\": \"Sample SKU 30 days\",\n" +
            "      \"skuId\": \"sample-sku-30-days\",\n" +
            "      \"parentSku\": \"sample-sku\",\n" +
            "      \"price\": 69000\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"7 Days Subscription\",\n" +
            "      \"description\": \"Sample SKU 7 days\",\n" +
            "      \"skuId\": \"sample-sku-7-days\",\n" +
            "      \"parentSku\": \"sample-sku\",\n" +
            "      \"price\": 19000\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"30 Days Promo Subscription\",\n" +
            "      \"description\": \"Sample SKU 30 days Promo Price\",\n" +
            "      \"skuId\": \"sample-sku-30-days-promo\",\n" +
            "      \"parentSku\": \"sample-sku-promo\",\n" +
            "      \"price\": 49000\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"7 Days Subscription Promo\",\n" +
            "      \"description\": \"Sample SKU 7 days Promo\",\n" +
            "      \"skuId\": \"sample-sku-7-days-promo\",\n" +
            "      \"parentSku\": \"sample-sku-promo\",\n" +
            "      \"price\": 9000\n" +
            "    }\n" +
            "  ]\n" +
            "}"

    val purchasingListener = object : PurchasingListener {
        override fun onProductDataResponse(productDataResponse: ProductDataResponse?) {
            when (productDataResponse?.getRequestStatus()) {
                ProductDataResponse.RequestStatus.SUCCESSFUL -> {
                    //get informations for all IAP Items (parent SKUs)
                    val products = productDataResponse.productData;
                    for (key in products.keys) {
                        val product = products.get(key);

                    }
                }
                ProductDataResponse.RequestStatus.FAILED, ProductDataResponse.RequestStatus.NOT_SUPPORTED -> {
                    Log.v("IAP", "Status : ${productDataResponse.requestStatus}");
                }
            }
        }

        override fun onPurchaseResponse(response: PurchaseResponse?) {
            when (response?.requestStatus) {
                PurchaseResponse.RequestStatus.SUCCESSFUL -> {
                    PurchasingService.notifyFulfillment(response.receipt.receiptId, FulfillmentResult.FULFILLED)
                }
                PurchaseResponse.RequestStatus.FAILED, PurchaseResponse.RequestStatus.NOT_SUPPORTED -> {
                    Log.d("IAP", "Status : ${response.requestStatus}")
                }
                PurchaseResponse.RequestStatus.INVALID_SKU -> {
                    Log.d("IAP", "Status : ${response.requestStatus}")
                }
                PurchaseResponse.RequestStatus.ALREADY_PURCHASED -> {
                    Log.d("IAP", "Status : ${response.requestStatus}")
                }
                else -> {
                    Log.d("IAP", "Status : ${response?.requestStatus}")

                }
            }
        }

        override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse?) {
            when (response?.requestStatus) {
                PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
                    for (receipt in response.receipts) {
                        if (!receipt.isCanceled) {
                            handler.handleMessage(Message().apply {
                                obj = receipt.sku
                            })
                            hasActiveSku = true;
                            hideSkuList()
                        }
                        if (!hasActiveSku) {
                            showSkuList()
                        }
                        if (response.hasMore()) {
                            PurchasingService.getPurchaseUpdates(true)
                        }
                    }
                }
                PurchaseUpdatesResponse.RequestStatus.FAILED,
                PurchaseUpdatesResponse.RequestStatus.NOT_SUPPORTED -> {
                    Log.d("IAP", "Status : ${response.requestStatus}")
                }
            }
        }

        override fun onUserDataResponse(response: UserDataResponse?) {
            when (response?.requestStatus) {
                UserDataResponse.RequestStatus.SUCCESSFUL -> {
                    currentUserId = response.userData.userId
                    currentMarketplace = response.userData.marketplace
                    Log.d("IAP", "User ID : ${currentUserId}, marketplace : ${currentMarketplace}")
                }
                UserDataResponse.RequestStatus.FAILED, UserDataResponse.RequestStatus.NOT_SUPPORTED -> {
                    Log.d("IAP", "Status : ${response.requestStatus}")

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PurchasingService.registerListener(this, purchasingListener)
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(message: Message) {
                info.text = message.obj.toString()
            }
        }
        skuResponse = Gson().fromJson<SkuResponse>(skuJson, SkuResponse::class.java)
        val skuListAdapter = SkuListAdapter(skuResponse.skus, this)
        skuList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        skuList.adapter = skuListAdapter
    }

    override fun onResume() {
        super.onResume()
        PurchasingService.getUserData()
        PurchasingService.getPurchaseUpdates(true)

        val productSkus = HashSet<String>()
        for (sku in skuResponse.skus) {
            productSkus.add(sku.skuId)
            productSkus.add(sku.parentSku)
        }
        PurchasingService.getProductData(productSkus)
    }

    fun replaceFragment(fragment: Fragment) {
        try {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainer, fragment, fragment.javaClass.canonicalName)
            fragmentTransaction.commit()
        } catch (e: IllegalStateException) {

        }
    }

    fun addFragment(fragment: Fragment) {
        try {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            var tag = fragment.javaClass.canonicalName
            fragmentTransaction.add(R.id.fragmentContainer, fragment, tag)
            fragmentTransaction.commit()
        } catch (e: IllegalStateException) {
        }
    }

    fun showSkuList() {
        skuList.visibility = View.VISIBLE
    }

    fun hideSkuList() {
        skuList.visibility = View.GONE
        if (supportFragmentManager.fragments.size > 0) {
            val fragmentTransaction = supportFragmentManager?.beginTransaction()
            fragmentTransaction?.remove(supportFragmentManager.fragments.get(supportFragmentManager.fragments.size - 1))
            fragmentTransaction?.commit()
        }
    }
}
