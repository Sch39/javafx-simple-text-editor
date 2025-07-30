module dev.sch.simpletexteditor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires static lombok;

    opens dev.sch.simpletexteditor to javafx.fxml;
    exports dev.sch.simpletexteditor;
}