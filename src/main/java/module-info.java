module cookbook {
    requires java.desktop;

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;	// TODO transitive?

    requires transitive javafx.graphics;

    requires slf4j.api;
    requires org.apache.logging.log4j;

    opens org.sergeys.cookbook.ui to javafx.fxml;
    exports org.sergeys.cookbook.ui;
    exports org.sergeys.cookbook.logic;
}
