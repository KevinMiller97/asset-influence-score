package com.millerk97.ais.twitter.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Meta {
    @JsonProperty("newest_id")
    String newestId;
    @JsonProperty("oldest_id")
    String oldestId;
    @JsonProperty("result_count")
    int resultCount;
    @JsonProperty("next_token")
    String nextToken;
}
