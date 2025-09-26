module com.example.sqllearingapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires com.fasterxml.jackson.annotation;
    requires java.sql;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires atlantafx.base;
    requires org.fxmisc.richtext;
    requires javafx.web;
    requires java.desktop;


    opens com.sqllearningapp to javafx.fxml;
    exports com.sqllearningapp;
}