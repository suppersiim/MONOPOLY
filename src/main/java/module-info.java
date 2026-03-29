module monopoly {
    requires javafx.controls;
    requires javafx.graphics;

    opens client.gui to javafx.graphics, javafx.fxml;
}
