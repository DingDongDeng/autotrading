package com.dingdongdeng.autotrading.domain.chart.repository

import com.dingdongdeng.autotrading.domain.chart.entity.CoinCandle
import com.dingdongdeng.autotrading.infra.common.exception.CriticalException
import com.dingdongdeng.autotrading.infra.common.type.CandleUnit
import com.dingdongdeng.autotrading.infra.common.type.CoinType
import com.dingdongdeng.autotrading.infra.common.type.ExchangeType
import com.dingdongdeng.autotrading.infra.common.utils.CandleDateTimeUtils
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class CachedCoinCandleRepository(
    private val coinCandleRepository: CoinCandleRepository,
) {
    private val cachedData = mutableMapOf<String, CachedCandles>()

    fun findAllCoinCandle(
        exchangeType: ExchangeType,
        coinType: CoinType,
        unit: CandleUnit,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<CoinCandle> {
        if (from > to) {
            throw CriticalException.of("조회 범위가 적절하지 않음, from=$from, to=$to")
        }
        val key = CachedCandles.makeCacheKey(exchangeType, coinType, unit)
        synchronized(this) {
            val cachedCandles = cachedData[key] ?: saveCachedData(exchangeType, coinType, unit, from, to)
            if (cachedCandles.hasEnough(from, to)) {
                return cachedCandles.get(from, to)
            }
            // 과거 캔들을 조회하는 경우는 캐싱이 의미가 없음 (따라서 실제 DB에서 데이터를 조회)
            // 시간 흐름에 따라 연속적으로 데이터를 조회하는 상황이 아닐것이기 때문
            // 예를 들어,60분봉 2주전 누락된 캔들을 생성을 하려할때 등등
            if (from < cachedCandles.firstDateTime) { //FIXME 스레드 컨텍스트로 만들어야겠다... 재시도하면 히트를 못하게되네
                return findData(exchangeType, coinType, unit, from, to)
            }
            return saveCachedData(exchangeType, coinType, unit, from, to).get(from, to)
        }
    }

    private fun saveCachedData(
        exchangeType: ExchangeType,
        coinType: CoinType,
        unit: CandleUnit,
        from: LocalDateTime,
        to: LocalDateTime,
    ): CachedCandles {
        val candles = findData(
            exchangeType = exchangeType,
            coinType = coinType,
            unit = unit,
            from = from.minusSeconds(unit.getSecondSize() * (CACHED_CANDLE_COUNT * 10 / 100)), // 10%
            to = to.plusSeconds(unit.getSecondSize() * (CACHED_CANDLE_COUNT * 90 / 100)), // 90%
        )
        val cachedCandles = CachedCandles.of(exchangeType, coinType, unit, candles)

        cachedData[cachedCandles.key] = cachedCandles
        return cachedCandles
    }

    private fun findData(
        exchangeType: ExchangeType,
        coinType: CoinType,
        unit: CandleUnit,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<CoinCandle> {
        return coinCandleRepository.findAllCoinCandle(
            exchangeType = exchangeType,
            coinType = coinType,
            unit = unit,
            from = from,
            to = to,
        )
    }

    companion object {
        const val CACHED_CANDLE_COUNT = 5000
    }
}

data class CachedCandles(
    val exchangeType: ExchangeType,
    val coinType: CoinType,
    val unit: CandleUnit,
    val indexMap: Map<LocalDateTime, Int>,
    val candles: List<CoinCandle>,
) {
    val key = makeCacheKey(exchangeType, coinType, unit)
    val firstDateTime = candles.first().candleDateTimeKst
    val lastDateTime = candles.last().candleDateTimeKst

    fun get(from: LocalDateTime, to: LocalDateTime): List<CoinCandle> {
        val startIndex = indexMap[CandleDateTimeUtils.makeUnitDateTime(from, unit, true)]
            ?: candles.indexOfFirst { from <= it.candleDateTimeKst }
        val endIndex = indexMap[CandleDateTimeUtils.makeUnitDateTime(to, unit, false)]
            ?: candles.indexOfLast { to >= it.candleDateTimeKst }
        if (startIndex <= -1 || endIndex <= -1) {
            throw CriticalException.of("캐시 데이터에서 조회하려는 인덱스가 올바르지 않음, from=$from, to=$to, startIndex=$startIndex, endIndex=$endIndex, exchangeType=$exchangeType, coinType=$coinType, unit=$unit")
        }
        if (startIndex > endIndex) {
            throw CriticalException.of("캐시 데이터에서 조회하려는 인덱스가 올바르지 않음, from=$from, to=$to, startIndex=$startIndex, endIndex=$endIndex, exchangeType=$exchangeType, coinType=$coinType, unit=$unit")
        }
        return candles.subList(startIndex, endIndex + 1)
    }

    fun hasEnough(from: LocalDateTime, to: LocalDateTime): Boolean {
        if (candles.isEmpty()) {
            return false
        }
        if (from < firstDateTime) {
            return false
        }
        if (to > lastDateTime) {
            return false
        }
        return true
    }

    companion object {
        fun of(
            exchangeType: ExchangeType,
            coinType: CoinType,
            unit: CandleUnit,
            candles: List<CoinCandle>,
        ): CachedCandles {
            val indexMap = candles.mapIndexed { index, coinCandle -> coinCandle.candleDateTimeKst to index }.toMap()
            return CachedCandles(
                exchangeType = exchangeType,
                coinType = coinType,
                unit = unit,
                indexMap = indexMap,
                candles = candles,
            )
        }

        fun makeCacheKey(
            exchangeType: ExchangeType,
            coinType: CoinType,
            unit: CandleUnit,
        ): String {
            val sb = StringBuilder()
            sb.append(exchangeType)
                .append(":")
                .append(coinType)
                .append(":")
                .append(unit)
            return sb.toString()
        }
    }
}