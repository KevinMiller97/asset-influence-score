package com.millerk97.ais.cryptowatch.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.cryptowatch.domain.ohlc.CryptoCompareResult;
import com.millerk97.ais.cryptowatch.domain.ohlc.OHLC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * hourly data was only available through a separate API
 * https://min-api.cryptocompare.com/
 */
public class CryptoCompareReader {

    private static final String DIR = "src/main/resources/com/millerk97/ohlc/dogecoin_hourly/";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<OHLC> getHourlyOHLCForDays(List<OHLC> days) {
        List<OHLC> hourly = new ArrayList<>();
        File[] hourlyJsons = new File(DIR).listFiles();

        for (OHLC day : days) {
            long start = day.getCloseTime() - 86400;
            // find corresponding hourly file with timeframe;
            for (File hourlyJson : hourlyJsons) {
                // [0] .. "from"; [1] .. {from}; [2] .. "to"; [3] .. {to}
                String[] timestamps = hourlyJson.getName().split("[_.]");
                if (start > Long.parseLong(timestamps[1]) && day.getCloseTime() < Long.parseLong(timestamps[3])) {
                    // correct file
                    try {
                        CryptoCompareResult result = mapper.readValue(Files.readString(Path.of(DIR + hourlyJson.getName())), CryptoCompareResult.class);
                        hourly.addAll(Arrays.stream(result.getData().getData()).map(data -> OHLC.of(data.getTime(), data.getOpen(), data.getHigh(), data.getLow(), data.getClose(), (long) data.getVolumeTo(), 0L)).collect(Collectors.toList()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return hourly;
    }


}
