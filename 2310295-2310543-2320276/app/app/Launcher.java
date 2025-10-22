package app;

import controller.GameFacade;
import ui.MainWindow;

import javax.swing.SwingUtilities;
import java.util.Arrays;

public class Launcher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFacade facade = new GameFacade(Arrays.asList("A", "B"));
            MainWindow win = new MainWindow(facade);
            win.setVisible(true);
        });
    }
}
