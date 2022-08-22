package com.millerk97.ais.coingecko.impl;

import com.millerk97.ais.coingecko.domain.Exchanges.Exchanges;

import java.util.Comparator;

/**
 * IS = Influencability Score
 */
public class ISCalculator {

    private static final int THRESHOLD = 20; // amount of exchanges incorporated

    public static double calculateInfluencabilityScore(String cryptocurrency, double pcc, boolean reload) {
        final double ess = calculateExchangeSupportScore(cryptocurrency.toLowerCase(), reload);
        final double mcr = calculateMCR(cryptocurrency.toLowerCase(), reload);
        return ((ess / mcr) / pcc) / 1000;
    }

    public static String getInfluenceabilityScoreCalculationString(String cryptocurrency, double pcc) {
        final double ess = calculateExchangeSupportScore(cryptocurrency.toLowerCase(), false);
        final double mcr = calculateMCR(cryptocurrency.toLowerCase(), false);
        final double is = ((ess / mcr) / pcc) / 1000;
        return String.format("([%s{ESS}/%.4f{MCR}]/%s{PCC})/1000 = %.4f", ess, mcr, pcc, is);
    }

    public static int calculateExchangeSupportScore(String cryptocurrency, boolean reload) {
        return DataFetcher.getSupportedExchanges(cryptocurrency, reload).stream().sorted(Comparator.comparingInt(Exchanges::getTrustScoreRank)).limit(THRESHOLD).mapToInt(e -> (THRESHOLD - e.getTrustScoreRank())).sum();
    }

    public static double calculateMCR(String cryptocurrency, boolean reload) {
        return DataFetcher.getMarketCap(cryptocurrency, reload) / DataFetcher.getGlobalMarketCap(reload);
    }

}
