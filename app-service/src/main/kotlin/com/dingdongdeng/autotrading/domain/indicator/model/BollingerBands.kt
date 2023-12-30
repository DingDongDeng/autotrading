package com.dingdongdeng.autotrading.domain.indicator.model

data class BollingerBands(
    val upper: Double,
    val middle: Double,
    val lower: Double,
    val height: Double,
    val heightHist: Double,
)