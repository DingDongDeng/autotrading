package com.dingdongdeng.autotrading.infra.common.utils

import com.dingdongdeng.autotrading.infra.common.type.CandleUnit
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs


val yyyyMMddHHmmss: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun LocalDateTime.convertToString(): String {
    return this.format(yyyyMMddHHmmss)
}

fun LocalDateTime.toUtc(): LocalDateTime {
    return this // 마지막 캔들 시각 (비우면 가장 최근 시각), UTC 기준
        .atZone(ZoneId.of("Asia/Seoul"))
        .withZoneSameInstant(ZoneId.of("UTC"))
        .toLocalDateTime()
}

object CandleDateTimeUtils {

    private val STANDARD_TIME = LocalTime.of(9, 0, 0, 0)

    /**
     * 캔들 단위와 일치하는 시간을 반환
     * ex) 15:17, UNIT_15M, true = 15:15
     * ex) 15:14, UNIT_15M, true = 15:00
     * ex) 15:17, UNIT_15M, false = 15:30
     * ex) 15:14, UNIT_15M, false = 15:15
     */
    fun makeUnitDateTime(
        dateTime: LocalDateTime,
        candleUnit: CandleUnit,
        next: Boolean = false,
    ): LocalDateTime {
        val simpleDateTime = dateTime.withSecond(0).withNano(0)
        val standardDate = if (candleUnit == CandleUnit.UNIT_1W) { // 기준으로 사용할 과거 단위 날짜
            simpleDateTime.toLocalDate().with(DayOfWeek.MONDAY).minusWeeks(1)
        } else {
            simpleDateTime.toLocalDate().minusDays(1)
        }
        // dateTime 기준 다음 단위 시간
        if (next) {
            val diffSeconds = abs(
                LocalDateTime.of(standardDate, STANDARD_TIME)
                    .until(simpleDateTime, ChronoUnit.SECONDS)
            )
            if (diffSeconds % candleUnit.getSecondSize() == 0L) {
                return simpleDateTime
            }
            return simpleDateTime.plusSeconds(candleUnit.getSecondSize() - (diffSeconds % candleUnit.getSecondSize()))
        }

        // dateTime 기준 이전 단위 시간
        val diffSeconds = abs(
            LocalDateTime.of(standardDate, STANDARD_TIME).until(simpleDateTime, ChronoUnit.SECONDS)
        )
        return simpleDateTime.minusSeconds(diffSeconds % candleUnit.getSecondSize())
    }

    /**
     *  val candleDateTimes = [15:30, 15:45, 16:00 ....]
     *  candleDateTime의 시간 간격이 candleUnit만큼 일정한지를 검사하고
     *  만약 누락된 시간이 있다면 그 시간들을 리턴
     */
    fun findMissingDateTimes(
        candleUnit: CandleUnit,
        from: LocalDateTime,
        to: LocalDateTime,
        candleDateTimes: List<LocalDateTime>
    ): List<LocalDateTime> {
        val fromUnitDateTime = makeUnitDateTime(from, candleUnit)
        val toUnitDateTime = makeUnitDateTime(to, candleUnit, next = true)
        val missingCandles = mutableListOf<LocalDateTime>()

        // from이 단위 시간과 일치할때는 따로 확인
        if (from == fromUnitDateTime && candleDateTimes.contains(from).not()) {
            missingCandles.add(from)
        }

        // to이 단위 시간과 일치할때는 따로 확인
        if (to == toUnitDateTime && candleDateTimes.contains(to).not()) {
            missingCandles.add(to)
        }

        val list = listOf(fromUnitDateTime) + candleDateTimes + toUnitDateTime
        list.zipWithNext { currentKst, nextKst ->
            val expectedDifference = candleUnit.getSecondSize()
            val actualDifference = ChronoUnit.SECONDS.between(currentKst, nextKst)
            if (actualDifference != expectedDifference) {
                val missingCandleCount = (actualDifference / expectedDifference).toInt() - 1
                val missingCandlesInRange = (1..missingCandleCount).map {
                    currentKst.plusSeconds(expectedDifference * it)
                }
                missingCandles.addAll(missingCandlesInRange)
            }
        }
        return missingCandles
    }
}