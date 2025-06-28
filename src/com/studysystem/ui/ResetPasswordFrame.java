package com.studysystem.ui;

import com.studysystem.dao.UserDAO;
import com.studysystem.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResetPasswordFrame extends JFrame {
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private final UserDAO userDAO;
    private final User user;
    
    public ResetPasswordFrame(String token) {
        userDAO = new UserDAO();
        this.user = userDAO.validateResetToken(token);
        
        if (user == null) {
            JOptionPane.showMessageDialog(null, 
                "Invalid or expired reset token", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Reset Password");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        
        JLabel titleLabel = new JLabel("Set New Password for " + user.getUsername());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel passLabel = new JLabel("New Password:");
        mainPanel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel confirmLabel = new JLabel("Confirm Password:");
        mainPanel.add(confirmLabel, gbc);
        
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        mainPanel.add(confirmPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JButton submitButton = new JButton("Update Password");
        submitButton.addActionListener(new SubmitActionListener());
        mainPanel.add(submitButton, gbc);
        
        add(mainPanel);
    }
    
    private class SubmitActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(ResetPasswordFrame.this, 
                    "Please enter and confirm your new password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(ResetPasswordFrame.this, 
                    "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(ResetPasswordFrame.this, 
                    "Password must be at least 6 characters", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (userDAO.updatePassword(user.getId(), password)) {
                JOptionPane.showMessageDialog(ResetPasswordFrame.this, 
                    "Password updated successfully! You can now login with your new password.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(ResetPasswordFrame.this, 
                    "Failed to update password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}