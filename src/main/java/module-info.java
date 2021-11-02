module pdfmergev1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires pdfbox;
    requires xmpbox;
    requires java.xml;
    requires commons.logging;

    opens xpdfmergeV1 to javafx.fxml;
    exports xpdfmergeV1;
}