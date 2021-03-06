package org.sergeys.cookbook.ui;

import org.sergeys.cookbook.logic.SettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *	Launcher to use in "executable jar"
 * 	https://github.com/javafxports/openjdk-jfx/issues/236
 * 	"runtime will check if the main class extends javafx.application.Application, and if that is the case,
 * 	it strongly requires the javafx platform to be available as a module, and not as a jar for example.
 *
 * 	There are some easy workarounds though. For example, you can have a main class that is not extending
 * 	javafx.application.Application"
 *
 *
 * @author sergeys
 *
 */
public class CookBookLauncher {

    private static Logger log;

    public static void main(String[] args) {

        SettingsManager.getInstance(); // this initializes System.setProperty("log4j.log.file"

        log = LoggerFactory.getLogger(CookBookLauncher.class);
        log.debug("main");

        CookBook.main(args);
    }

}
