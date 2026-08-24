package com.ict.lending;

/**
 * Non-Application entry point. The JVM refuses to launch a class that
 * directly extends javafx.application.Application from a plain classpath
 * jar (java -jar / jpackage), reporting "JavaFX runtime components are
 * missing" even when the JavaFX jars are present. Delegating through this
 * class avoids that check while Main's own launch(args) call still resolves
 * Main as the Application subclass.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
