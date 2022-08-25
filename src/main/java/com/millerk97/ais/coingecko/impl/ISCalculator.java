package com.millerk97.ais.coingecko.impl;

import com.millerk97.ais.coingecko.domain.Exchanges.Exchanges;
import com.millerk97.ais.coingecko.domain.MarketChart.MarketChart;

import java.util.Comparator;
import java.util.List;

/**
 * IS = Influencability Score
 */
public class ISCalculator {

    private static final int THRESHOLD = 20; // amount of exchanges incorporated
    private static final int MAX_ESS = 210;
    private static MarketChart marketChart;
    private static List<List<String>> globalMarketCapData;
    private static double ESS;

    public static void initialize(String cryptocurrency, Long from, Long to) {
        DataFetcher.fetchMarketCap(cryptocurrency, from, to);
        marketChart = DataFetcher.getMarketChart(cryptocurrency.toLowerCase());
        globalMarketCapData = DataFetcher.fetchGlobalMarketCap(from, to);
        ESS = calculateExchangeSupportScore(cryptocurrency);
    }

    public static double calculateInfluenceabilityScore(String cryptocurrency, double pcc, Long timestamp) {
        return (ESS / calculateMCR(timestamp)) * pcc;
    }

    public static String getInfluenceabilityScoreCalculationString(String cryptocurrency, double pcc, Long timestamp) {
        final double mcr = calculateMCR(timestamp);
        final double is = ((ESS / mcr) / pcc) / 1000;
        return String.format("([%s{ESS}/%.4f{MCR}]/%s{PCC})/1000 = %.4f", ESS, mcr, pcc, is);
    }

    public static double calculateExchangeSupportScore(String cryptocurrency) {
        return DataFetcher.getSupportedExchanges(cryptocurrency).stream().sorted(Comparator.comparingInt(Exchanges::getTrustScoreRank)).limit(THRESHOLD).mapToDouble(e -> (THRESHOLD - e.getTrustScoreRank())).sum() / MAX_ESS;
    }

    public static double calculateMCR(Long timestamp) {
        return DataFetcher.getMarketCap(marketChart, timestamp) / DataFetcher.getGlobalMarketCap(globalMarketCapData, timestamp);
    }

}
