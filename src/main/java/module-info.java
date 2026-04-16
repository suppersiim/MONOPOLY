module monopoly {
    requires javafx.controls;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;

    opens client.gui to javafx.graphics, javafx.fxml;
    opens game_logic to com.fasterxml.jackson.databind;

    exports game_logic;
}
