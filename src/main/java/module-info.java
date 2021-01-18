module eu.hansolo.fx.customcontrols {
    // Java
    requires java.base;

    // Java-FX
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;

    // 3rd Party
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.antdesignicons;

    // Exports
    exports eu.hansolo.fx.customcontrols.restyled;
    exports eu.hansolo.fx.customcontrols.tools;
}