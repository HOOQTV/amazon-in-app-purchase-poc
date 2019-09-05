package com.dimpossitorus.sample.amazoniap.plan

import java.io.Serializable

data class Sku(
    val name: String, val description: String, val skuId: String, val parentSku: String, val price: Float
) : Serializable

data class SkuResponse(val skus: List<Sku>) : Serializable