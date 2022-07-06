package com.millerk97.ais.coingecko.ess;

/**
 * ESS = Exchange Support Score
 */
public class ESSCalculator {

    /*
        String input = dogecoin
        fetch all exchanges that support dogecoin

        int threshold = 20; // might be changed
        int ess = 0;

        List<Exchange> exchanges = new ArrayList<Exchange>();
        exchanges.addAll( fetchTopCentralizedExchanges( threshold ));
        exchanges.addAll( fetchTopDecentralizedExchanges( threshold ));

        for (Exchange e : exchanges) {
            if (e.supports(crypto) {
                ess += (threshold - e.rank());
            }
        }
     */

    public static int calculateExchangeSupportScore(String cryptocurrency) {
        DataFetcher.getSupportedExchanges(cryptocurrency);
        return 0;
    }


}
