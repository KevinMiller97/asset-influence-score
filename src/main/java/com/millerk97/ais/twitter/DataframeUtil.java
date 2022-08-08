package com.millerk97.ais.twitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.twitter.data.Tweet;
import com.millerk97.ais.twitter.data.dataframe.Dataframe;
import com.millerk97.ais.util.TimeFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DataframeUtil {

    private static final String PREFIX = "src/main/resources/com/millerk97/dataframes/";
    private static final String SUBDIR = "%s/";
    private static final String FILE_TEMPLATE = "DF_%s_%s.json";

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Dataframe storeDataframe(String cryptocurrency, OHLC ohlc, List<Tweet> tweets) {
        Dataframe df = new Dataframe();
        df.setOhlc(ohlc);
        df.setTweets(tweets.toArray(new Tweet[0]));

        // makes sure the directory exists, doesn't do anything if it already does
        new File(PREFIX + String.format(SUBDIR, cryptocurrency)).mkdirs();

        String fileName = String.format(PREFIX, cryptocurrency) + String.format(FILE_TEMPLATE, TimeFormatter.formatISO8601(ohlc.getTime() * 1000).substring(0, 13), TimeFormatter.formatISO8601((ohlc.getTime() + 3600) * 1000).substring(0, 13));

        try {
            if (!new File(fileName).exists() || Files.readString(Path.of(fileName)).isBlank()) {
                FileWriter fWriter = new FileWriter(fileName);
                fWriter.write(mapper.writeValueAsString(df));
                fWriter.flush();
                fWriter.close();
            }
            return df;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return df;
    }

    public static Dataframe getDataframe(Long startTimestamp) {
        String fileName = PREFIX + String.format(FILE_TEMPLATE, TimeFormatter.formatISO8601(startTimestamp * 1000).substring(0, 13), TimeFormatter.formatISO8601((startTimestamp + 3600) * 1000).substring(0, 13));

        try {
            if (new File(fileName).exists() && !Files.readString(Path.of(fileName)).isBlank()) {
                return mapper.readValue(Files.readString(Path.of(fileName)), Dataframe.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
