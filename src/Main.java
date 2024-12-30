import core.services.AuthService;
import core.services.storage.ClientStorageService;
import core.services.storage.ManagerStorageService;
import core.services.storage.TrainerStorageService;
import ui.AuthWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Настройка UI с использованием Nimbus Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Если Nimbus недоступен, используем системную тему оформления
            System.err.println("Failed to set Nimbus Look and Feel: " + e.getMessage());
        }

        // Запуск Swing-приложения в Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AuthService authService = new AuthService(
                    new ClientStorageService(),
                    new ManagerStorageService(),
                    new TrainerStorageService()
            );
            AuthWindow authWindow = new AuthWindow(authService);
            authWindow.setVisible(true);
        });
    }
}
