module com.sqllearningapp {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.web;
    requires javafx.graphics;

    // RichTextFX and ReactFX for the code editor
    requires org.fxmisc.richtext;
    requires reactfx;

    // AtlantaFX theme library
    requires atlantafx.base;

    // Database
    requires java.sql;
    requires com.h2database;

    // JSON processing
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    // Logging
    requires org.slf4j;
    requires ch.qos.logback.classic;

    // Lombok (if using)
    requires static lombok;

    // Desktop integration
    requires java.desktop;

    // Exports (if other modules need to access your classes)
    exports com.sqllearningapp;
    exports com.sqllearningapp.core.models;
    exports com.sqllearningapp.core.services;

    opens com.sqllearningapp.utils to com.fasterxml.jackson.databind;
}