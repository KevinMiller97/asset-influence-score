package com.millerk97.ais.twitter.data.dataframe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.twitter.data.Tweet;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataframe {
    @JsonProperty("ohlc")
    OHLC ohlc;
    @JsonProperty("tweets")
    Tweet[] tweets;
}