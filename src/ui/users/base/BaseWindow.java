package ui.users.base;

import core.services.AuthService;
import core.services.storage.ClientStorageService;
import core.services.storage.ManagerStorageService;
import core.services.storage.TrainerStorageService;
import ui.AuthWindow;

import javax.swing.*;
import java.awt.*;

public abstract class BaseWindow extends JFrame {
    protected final String userId;

    public BaseWindow(String title, String userId) {
        this.userId = userId;
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Add logout button to all windows
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        // Add the logout button to the frame
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(logoutButton);
        add(topPanel, BorderLayout.NORTH);
    }

    protected void logout() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            AuthWindow authWindow = new AuthWindow(new AuthService(
                    new ClientStorageService(),
                    new ManagerStorageService(),
                    new TrainerStorageService()
            ));
            authWindow.setVisible(true);
        });
    }
}

