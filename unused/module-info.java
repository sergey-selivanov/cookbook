module cookbook {
    requires java.sql;
    //requires java.desktop;	// awt Dimension
    requires java.xml;
    requires java.net.http;

    //requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;	// TODO transitive?
    requires transitive javafx.graphics;

    requires slf4j.api;
    requires org.apache.logging.log4j; // 2.12
    //requires log4j.api; // 2.9.1

    requires org.jsoup;
    requires org.kordamp.bootstrapfx.core;
    requires com.h2database;

    opens org.sergeys.cookbook.ui to javafx.fxml;
    opens org.sergeys.cookbook.logic to javafx.fxml;

    exports org.sergeys.cookbook.ui;
    exports org.sergeys.cookbook.logic;
}
