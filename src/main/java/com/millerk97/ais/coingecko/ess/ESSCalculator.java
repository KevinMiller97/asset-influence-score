package com.millerk97.ais.coingecko.ess;

import com.millerk97.ais.coingecko.domain.Exchanges.Exchanges;

import java.util.Comparator;

/**
 * ESS = Exchange Support Score
 */
public class ESSCalculator {

    private static final int THRESHOLD = 20; // amount of exchanges incorporated

    public static int calculateExchangeSupportScore(String cryptocurrency) {
        return DataFetcher.getSupportedExchanges(cryptocurrency).stream().sorted(Comparator.comparingInt(Exchanges::getTrustScoreRank)).limit(THRESHOLD).mapToInt(e -> (THRESHOLD - e.getTrustScoreRank())).sum();
    }

}
