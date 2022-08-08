module com.millerk97.ais {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires org.twitter4j.core;
    requires com.fasterxml.jackson.annotation;
    requires retrofit2;
    requires okhttp3;
    requires retrofit2.converter.jackson;
    requires static lombok;
    requires org.apache.commons.io;
    requires com.fasterxml.jackson.databind;
    requires com.opencsv;

    opens com.millerk97.ais to javafx.fxml;
    exports com.millerk97.ais;
    exports com.millerk97.ais.coingecko;
    exports com.millerk97.ais.coingecko.domain;
    exports com.millerk97.ais.coingecko.domain.Exchanges;
    exports com.millerk97.ais.coingecko.domain.Shared;
    exports com.millerk97.ais.coingecko.coins;
    exports com.millerk97.ais.coingecko.global;
    exports com.millerk97.ais.cryptocompare;
    exports com.millerk97.ais.cryptocompare.domain.Exchange;
    exports com.millerk97.ais.cryptocompare.domain.market;
    exports com.millerk97.ais.cryptocompare.domain.ohlc;
    exports com.millerk97.ais.twitter;
    exports com.millerk97.ais.twitter.api;
    exports com.millerk97.ais.twitter.data;
    exports com.millerk97.ais.twitter.data.context;
    exports com.millerk97.ais.twitter.data.entities;
    exports com.millerk97.ais.twitter.data.geo;
    exports com.millerk97.ais.twitter.data.metrics;
    exports com.millerk97.ais.twitter.data.user;
    exports com.millerk97.ais.twitter.data.dataframe;
}