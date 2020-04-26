package org.sergeys.cookbook.ui;

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

    public static void main(String[] args) {
        CookBook.main(args);
    }

}
