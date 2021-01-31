module cookbook {
// open module allows flyway to work  https://www.baeldung.com/java-9-modularity
//module cookbook {
    // TODO make separate open module for flyway sql files?

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
//    requires com.h2database;

    requires slf4j.api;
    requires org.apache.logging.log4j; // 2.12
    //requires log4j.api; // 2.9.1

//    opens org.sergeys.cookbook.ui to javafx.fxml, org.flywaydb.core;
//    opens org.sergeys.cookbook.logic to javafx.fxml, org.flywaydb.core;
    opens org.sergeys.cookbook.ui to javafx.fxml;
//    opens org.sergeys.cookbook.logic to javafx.fxml; ??

    //opens db.migration to org.flywaydb.core;	// fails
    opens db.migration;	// works

    exports org.sergeys.cookbook.ui;
    exports org.sergeys.cookbook.logic;

//    exports db.migration;
}
