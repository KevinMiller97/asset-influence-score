package com.millerk97.ais.twitter.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.millerk97.ais.twitter.data.user.User;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncludedUsers {
    @JsonProperty("users")
    User[] users;
}
