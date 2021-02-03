module cookbook {
// open module allows flyway to work  https://www.baeldung.com/java-9-modularity

    requires java.sql;
    requires java.desktop;	// java.beans.XMLDecoder
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

    requires org.slf4j;
    requires org.apache.logging.log4j;

    opens org.sergeys.cookbook.ui to javafx.fxml;

    opens db.migration; // for flyway

    exports org.sergeys.cookbook.ui;
    exports org.sergeys.cookbook.logic;
}
