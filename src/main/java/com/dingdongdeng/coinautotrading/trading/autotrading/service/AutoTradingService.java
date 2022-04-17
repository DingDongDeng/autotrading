package com.dingdongdeng.coinautotrading.trading.autotrading.service;

import com.dingdongdeng.coinautotrading.common.slack.SlackSender;
import com.dingdongdeng.coinautotrading.trading.autotrading.model.AutoTradingProcessor;
import com.dingdongdeng.coinautotrading.trading.autotrading.model.AutoTradingRegisterRequest;
import com.dingdongdeng.coinautotrading.trading.autotrading.model.type.AutoTradingProcessStatus;
import com.dingdongdeng.coinautotrading.trading.exchange.service.ExchangeService;
import com.dingdongdeng.coinautotrading.trading.exchange.service.selector.ExchangeServiceSelector;
import com.dingdongdeng.coinautotrading.trading.strategy.Strategy;
import com.dingdongdeng.coinautotrading.trading.strategy.StrategyFactory;
import com.dingdongdeng.coinautotrading.trading.strategy.model.StrategyCoreParam;
import com.dingdongdeng.coinautotrading.trading.strategy.model.StrategyServiceParam;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AutoTradingService {

    private final AutoTradingManager autoTradingManager;
    private final ExchangeServiceSelector processorSelector;
    private final StrategyFactory strategyFactory;
    private final SlackSender slackSender;

    public List<AutoTradingProcessor> getUserProcessorList(String userId) {
        return autoTradingManager.getList(userId);
    }

    public AutoTradingProcessor register(AutoTradingRegisterRequest request, String userId) {
        ExchangeService exchangeService = processorSelector.getTargetService(request.getCoinExchangeType());
        StrategyServiceParam serviceParam = StrategyServiceParam.builder()
            .strategyCode(request.getStrategyCode())
            .exchangeService(exchangeService)
            .coinType(request.getCoinType())
            .tradingTerm(request.getTradingTerm())
            .keyPairId(request.getKeyPairId())
            .build();

        //fixme 클라이언트 input과 실제 StrategyCore클래스의 관계를 동적으로 표현할 수 있는 구조를 고민해보자
        StrategyCoreParam coreParam = StrategyCoreParam.builder()
            .buyRsi(request.getBuyRsi())
            .profitRsi(request.getProfitRsi())
            .lossRsi(request.getLossRsi())
            .profitLimitPriceRate(request.getProfitLimitPriceRate())
            .lossLimitPriceRate(request.getLossLimitPriceRate())
            .tooOldOrderTimeSeconds(request.getTooOldOrderTimeSeconds())
            .orderPrice(request.getOrderPrice())
            .accountBalanceLimit(request.getAccountBalanceLimit())
            .build();

        Strategy strategy = strategyFactory.create(serviceParam, coreParam);
        AutoTradingProcessor processor = autoTradingManager.register(
            AutoTradingProcessor.builder()
                .id(UUID.randomUUID().toString())
                .title(request.getTitle())
                .userId(userId)
                .coinType(request.getCoinType())
                .coinExchangeType(request.getCoinExchangeType())
                .status(AutoTradingProcessStatus.INIT)
                .tradingTerm(request.getTradingTerm())
                .strategy(strategy)
                .duration(4000)
                .slackSender(slackSender)
                .build()
        );
        return processor;
    }

    public AutoTradingProcessor start(String processorId, String userId) {
        return autoTradingManager.start(processorId, userId);
    }

    public AutoTradingProcessor stop(String processorId, String userId) {
        return autoTradingManager.stop(processorId, userId);
    }

    public AutoTradingProcessor terminate(String processorId, String userId) {
        return autoTradingManager.terminate(processorId, userId);
    }

}
