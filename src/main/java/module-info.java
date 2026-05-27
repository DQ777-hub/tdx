module com.tdx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.tdx to javafx.fxml;

    exports com.tdx;
}
