package com.dingdongdeng.autotrading.infra.common.type

enum class PriceType(
    val desc: String,
) {
    LIMIT("지정가"),
    MARKET("시장가"),
    ;
}