module cookbook {
    requires java.sql;
    requires java.desktop;	// java.beans
    requires java.xml;
    requires java.net.http;

    //requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;	// TODO transitive?
    requires transitive javafx.graphics;

    requires org.jsoup;
    requires org.flywaydb.core;
    requires org.kordamp.bootstrapfx.core;
//    requires com.h2database;

    requires slf4j.api;
    requires org.apache.logging.log4j; // 2.12
    //requires log4j.api; // 2.9.1

    opens org.sergeys.cookbook.ui to javafx.fxml;
    opens org.sergeys.cookbook.logic to javafx.fxml;

    opens db.migration to org.flywaydb.core;

    exports org.sergeys.cookbook.ui;
    exports org.sergeys.cookbook.logic;
}
