package com.studysystem.ui;

import com.studysystem.dao.UserDAO;
import com.studysystem.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.UUID;

public class ForgotPasswordFrame extends JFrame {
    private JTextField emailField;
    private UserDAO userDAO;
    
    public ForgotPasswordFrame() {
        userDAO = new UserDAO();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Password Recovery");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Reset Your Password");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        
        // Email Label
        JLabel emailLabel = new JLabel("Email Address:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(emailLabel, gbc);
        
        // Email Field
        emailField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(emailField, gbc);
        
        // Submit Button
        JButton submitButton = new JButton("Send Reset Link");
        submitButton.addActionListener(new SubmitActionListener());
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(submitButton, gbc);
        
        // Add focus request
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                emailField.requestFocusInWindow();
            }
        });
        
        add(mainPanel);
    }
    
    private class SubmitActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText().trim();
            
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(ForgotPasswordFrame.this, 
                    "Please enter your email address", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            User user = userDAO.getUserByEmail(email);
            
            if (user == null) {
                JOptionPane.showMessageDialog(ForgotPasswordFrame.this, 
                    "No account found with that email address", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String token = UUID.randomUUID().toString();
            
            if (userDAO.storeResetToken(user.getId(), token)) {
                JOptionPane.showMessageDialog(ForgotPasswordFrame.this, 
                    "Password reset link has been sent to your email", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(ForgotPasswordFrame.this, 
                    "Failed to generate reset token", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}