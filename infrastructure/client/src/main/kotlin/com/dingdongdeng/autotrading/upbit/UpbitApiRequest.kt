package com.dingdongdeng.autotrading.upbit

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderChanceRequest(
    @JsonProperty("market")
    val market: String, // 마켓 ID
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderInfoRequest(
    @JsonProperty("uuid")
    val uuid: String, // 주문 UUID
    @JsonProperty("identifier")
    val identifier: String, // 조회용 사용자 지정 값    
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderInfoListRequest(
    @JsonProperty("market")
    val market: String, // 마켓 아이디
    @JsonProperty("uuids[]")
    val uuidList: List<String>, // 주문 UUID의 목록
    @JsonProperty("identifiers")
    val identifierList: List<String>, // 주문 identifier의 목록
    @JsonProperty("state")
    val state: State, // 주문 상태
    @JsonProperty("states")
    val stateList: List<State>, // 주문 상태의 목록 //* 미체결 주문(wait, watch)과 완료 주문(done, cancel)은 혼합하여 조회하실 수 없습니다.
    @JsonProperty("page")
    val page: Int, // 페이지 수, default: 1
    @JsonProperty("limit")
    val limit: Int, // 요청 개수, default: 100
    @JsonProperty("order_by")
    val orderBy: String, // 정렬 방식 - asc : 오름차순 - desc : 내림차순 (default)
)


@JsonInclude(JsonInclude.Include.NON_NULL)
class MarketCodeRequest {
    @JsonProperty("isDetail")
    val isDetail = false // 유의종목 필드과 같은 상세 정보 노출 여부
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderRequest(
    @JsonProperty("market")
    val market: String, // 마켓 ID (필수)
    @JsonProperty("side")
    val side: Side, // 주문 종류 (필수) - bid : 매수 - ask : 매도
    @JsonProperty("volume")
    val volume: Double, // 주문량 (지정가, 시장가 매도 시 필수)
    @JsonProperty("price")
    val price: Double, // 주문 가격. (지정가, 시장가 매수 시 필수)  ex) KRW-BTC 마켓에서 1BTC당 1,000 KRW로 거래할 경우, 값은 1000 이 된다. ex) KRW-BTC 마켓에서 1BTC당 매도 1호가가 500 KRW 인 경우, 시장가 매수 시 값을 1000으로 세팅하면 2BTC가 매수된다. (수수료가 존재하거나 매도 1호가의 수량에 따라 상이할 수 있음)
    @JsonProperty("ord_type")
    val ordType: OrdType, // 주문 타입 (필수) - limit : 지정가 주문 - price : 시장가 주문(매수) - market : 시장가 주문(매도)
    @JsonProperty("identifier")
    val identifier: String? = null // 조회용 사용자 지정값 (선택) (Uniq 값 사용)
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderCancelRequest(
    @JsonProperty("uuid")
    val uuid: String, // 취소할 주문의 UUID
    @JsonProperty("identifier")
    val identifier: String? = null // 조회용 사용자 지정값
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CandleRequest(
    val unit: Int, // 분 단위. 가능한 값 : 1, 3, 5, 15, 10, 30, 60, 240
    val market: String, // 마켓 ID (필수)
    @JsonIgnore
    val toKst: LocalDateTime,
    val count: Int, // 캔들 개수(최대 200개)
) {
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val to: LocalDateTime?
        get() =// 마지막 캔들 시각 (비우면 가장 최근 시각), UTC 기준
            if (Objects.isNull(toKst)) {
                null
            } else toKst!!.atZone(ZoneId.of("Asia/Seoul"))
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime()
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderBookRequest(
    @JsonProperty("markets")
    val marketList: List<String>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class TickerRequest(
    @JsonProperty("markets")
    val marketList: List<String>
)