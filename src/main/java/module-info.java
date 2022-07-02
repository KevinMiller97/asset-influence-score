module com.millerk97.ais {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.twitter4j.core;
    requires com.fasterxml.jackson.annotation;
    requires retrofit2;
    requires okhttp3;
    requires retrofit2.converter.jackson;
    requires static lombok;

    opens com.millerk97.ais to javafx.fxml;
    exports com.millerk97.ais;
    exports com.millerk97.ais.coingecko;
    exports com.millerk97.ais.coingecko.domain;
    exports com.millerk97.ais.coingecko.domain.Exchanges;
    exports com.millerk97.ais.coingecko.domain.Shared;
}