package com.dingdongdeng.autotrading.infra.common.type

import java.util.EnumMap


enum class CandleUnit(
    val desc: String,
    val unitType: UnitType,
    val size: Int,
) {
    UNIT_1M("1분 봉", UnitType.MIN, 1),
    UNIT_3M("3분 봉", UnitType.MIN, 3),
    UNIT_5M("5분 봉", UnitType.MIN, 5),
    UNIT_10M("10분 봉", UnitType.MIN, 10),
    UNIT_15M("15분 봉", UnitType.MIN, 15),
    UNIT_30M("30분 봉", UnitType.MIN, 30),
    UNIT_60M("60분 봉", UnitType.MIN, 60),
    UNIT_240M("240분 봉", UnitType.MIN, 240),
    UNIT_1D("1일 봉", UnitType.DAY, 1),
    UNIT_1W("1주 봉", UnitType.WEEK, 1),
    ;

    enum class UnitType {
        MIN,
        DAY,
        WEEK
    }

    companion object {
        fun toMap(): EnumMap<CandleUnit, String> {
            val map: EnumMap<CandleUnit, String> = EnumMap<CandleUnit, String>(
                CandleUnit::class.java
            )
            for (value in values()) {
                map[value] = value.desc
            }
            return map
        }
    }
}