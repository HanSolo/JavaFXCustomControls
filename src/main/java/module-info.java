module eu.hansolo.fx.customcontrols {
    // Java
    requires java.base;

    // Java-FX
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;

    // Exports
    exports eu.hansolo.fx.customcontrols.restyled;
    exports eu.hansolo.fx.customcontrols.combined;
    exports eu.hansolo.fx.customcontrols.extended;
    exports eu.hansolo.fx.customcontrols.tools;
}