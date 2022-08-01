package com.millerk97.ais.twitter.data.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NonPublicMetrics {
    @JsonProperty("impression_count")
    int impressionCount;
    @JsonProperty("url_link_clicks")
    int urlLinkClicks;
    @JsonProperty("user_profile_clicks")
    int userProfileClicks;
}
