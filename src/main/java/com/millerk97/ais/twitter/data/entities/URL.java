package com.millerk97.ais.twitter.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class URL {
    @JsonProperty("start")
    int start;
    @JsonProperty("end")
    int end;
    @JsonProperty("url")
    String url;
    @JsonProperty("expanded_url")
    String expandedUrl;
    @JsonProperty("display_url")
    String displayUrl;
    @JsonProperty("status")
    String status;
    @JsonProperty("title")
    String title;
    @JsonProperty("description")
    String description;
    @JsonProperty("unwound_url")
    String unwoundUrl;
}
