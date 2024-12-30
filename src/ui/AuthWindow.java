package ui;

import core.models.enums.UserRole;
import core.services.AuthService;
import ui.users.ClientWindow;
import ui.users.ManagerWindow;
import ui.users.TrainerWindow;

import javax.swing.*;
import java.awt.*;

public class AuthWindow extends JFrame {
    private final AuthService authService;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    private JTextField nameField;
    private JPasswordField passwordField;

    public AuthWindow(AuthService authService) {
        this.authService = authService;

        setTitle("Dance Club Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create and add both client and employee login panels
        mainPanel.add(createClientPanel(), "CLIENT");
        mainPanel.add(createEmployeePanel(), "EMPLOYEE");

        // Start with client login
        cardLayout.show(mainPanel, "CLIENT");

        add(mainPanel);
    }

    private JPanel createClientPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Client Login");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Name field
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        nameField = new JTextField(20);
        namePanel.add(new JLabel("Name: "));
        namePanel.add(nameField);

        // Login button
        JButton loginButton = new JButton("Login as Client");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleClientLogin());

        // Switch to employee login button
        JButton switchToEmployeeButton = new JButton("Employee Login");
        switchToEmployeeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        switchToEmployeeButton.addActionListener(e -> cardLayout.show(mainPanel, "EMPLOYEE"));

        // Add components
        panel.add(Box.createVerticalStrut(30));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(namePanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(20));
        panel.add(switchToEmployeeButton);

        return panel;
    }

    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Employee Login");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Name field
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JTextField empNameField = new JTextField(20);
        namePanel.add(new JLabel("Name: "));
        namePanel.add(empNameField);

        // Password field
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        passwordField = new JPasswordField(20);
        passwordPanel.add(new JLabel("Password: "));
        passwordPanel.add(passwordField);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleEmployeeLogin(empNameField.getText(), new String(passwordField.getPassword())));

        // Back to client login button
        JButton backToClientButton = new JButton("Back to Client Login");
        backToClientButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToClientButton.addActionListener(e -> cardLayout.show(mainPanel, "CLIENT"));

        // Add components
        panel.add(Box.createVerticalStrut(30));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(namePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(passwordPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(20));
        panel.add(backToClientButton);

        return panel;
    }

    private void handleClientLogin() {
        String name = nameField.getText();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name");
            return;
        }

        AuthService.AuthResult result = authService.authenticate(name, "");
        if (result.success() && result.role() == UserRole.CLIENT) {
            openClientWindow(result.userId());
        } else {
            JOptionPane.showMessageDialog(this, "Client not found");
        }
    }

    private void handleEmployeeLogin(String name, String password) {
        if (name.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both name and password");
            return;
        }

        AuthService.AuthResult result = authService.authenticate(name, password);
        if (result.success()) {
            switch (result.role()) {
                case MANAGER -> openManagerWindow(result.userId());
                case TRAINER -> openTrainerWindow(result.userId());
                default -> JOptionPane.showMessageDialog(this, "Invalid role");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials");
        }
    }

    private void openClientWindow(String clientId) {
        dispose(); // Close auth window
        SwingUtilities.invokeLater(() -> new ClientWindow(clientId).setVisible(true));
    }

    private void openManagerWindow(String managerId) {
        dispose(); // Close auth window
        SwingUtilities.invokeLater(() -> new ManagerWindow(managerId).setVisible(true));
    }

    private void openTrainerWindow(String trainerId) {
        dispose(); // Close auth window
        SwingUtilities.invokeLater(() -> new TrainerWindow(trainerId).setVisible(true));
    }
}

