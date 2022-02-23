package com.dingdongdeng.coinautotrading.auth.component;

import com.dingdongdeng.coinautotrading.auth.model.KeyRegisterRequest;
import com.dingdongdeng.coinautotrading.auth.model.KeyRegisterRequest.KeyPair;
import com.dingdongdeng.coinautotrading.domain.entity.ExchangeKey;
import com.dingdongdeng.coinautotrading.domain.service.ExchangeKeyService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KeyService {

    private final ExchangeKeyService exchangeKeyService;

    public void register(KeyRegisterRequest request, String userId) {
        List<KeyPair> keyPairList = request.getKeyPairList();
        String pairId = UUID.randomUUID().toString();
        exchangeKeyService.saveAll(
            keyPairList.stream()
                .map(
                    k -> ExchangeKey.builder()
                        .pairId(pairId)
                        .coinExchangeType(request.getCoinExchangeType())
                        .name(k.getKeyName())
                        .value(k.getValue())
                        .userId(userId)
                        .build()
                )
                .collect(Collectors.toList())
        );
    }
}
