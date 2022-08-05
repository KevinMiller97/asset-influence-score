package com.millerk97.ais.twitter.data.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextAnnotations {
    Annotation[] contextAnnotations;
}
