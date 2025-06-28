package com.studysystem.main;

import com.studysystem.database.DatabaseConnection;
import com.studysystem.ui.LoginFrame;
import javax.swing.*;
import javax.swing.UIManager;

public class SmartStudySystem {
    public static void main(String[] args) {
        // Set system look and feel
try {
    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
} catch (Exception e) {
    // Fallback to cross-platform
    try {
        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
    } catch (Exception e2) {
        System.out.println("Could not set look and feel: " + e2.getMessage());
    }
}  
        // Create database and tables
        System.out.println("Initializing Smart Study System...");
        DatabaseConnection.createDatabase();
        
        // Start the application
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}