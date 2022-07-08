package com.millerk97.ais.coingecko.impl;

import com.millerk97.ais.coingecko.domain.Exchanges.Exchanges;

import java.util.Comparator;

/**
 * IS = Influencability Score
 */
public class ISCalculator {

    private static final int THRESHOLD = 20; // amount of exchanges incorporated
    private static final double PCC = 0.67; // TODO find a way to fetch this dynamically
    private static final boolean FORCE_RELOAD = false;

    public static double calculateInfluencabilityScore(String cryptocurrency) {
        final double ess = calculateExchangeSupportScore(cryptocurrency);
        final double mcr = calculateMCR(cryptocurrency);
        final double is = ((ess / mcr) / PCC) / 1000;
        System.out.println(String.format("IS Calculation: ( [ %s {ESS} / %s {MCR} ] / %s {PCC} ) / 1000 = %s", ess, mcr, PCC, is));
        return is;
    }

    private static int calculateExchangeSupportScore(String cryptocurrency) {
        return DataFetcher.getSupportedExchanges(cryptocurrency, FORCE_RELOAD).stream().sorted(Comparator.comparingInt(Exchanges::getTrustScoreRank)).limit(THRESHOLD).mapToInt(e -> (THRESHOLD - e.getTrustScoreRank())).sum();
    }

    private static double calculateMCR(String cryptocurrency) {
        return DataFetcher.getMarketCap(cryptocurrency, FORCE_RELOAD) / DataFetcher.getGlobalMarketCap(FORCE_RELOAD);
    }

}
