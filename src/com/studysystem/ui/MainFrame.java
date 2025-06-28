package com.studysystem.ui;

import com.studysystem.dao.StudyDAO;
import com.studysystem.model.StudySession;
import com.studysystem.model.User;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

public class MainFrame extends JFrame {
    private final User currentUser;
    private final StudyDAO studyDAO;
    private JTabbedPane tabbedPane;
    private JTable todaysRevisionsTable;
    private JTable upcomingRevisionsTable;
    private JTable historyTable;
    private DefaultTableModel todaysModel;
    private DefaultTableModel upcomingModel;
    private DefaultTableModel historyModel;
    
    private JTextField subjectField;
    private JTextField topicField;
    private JSpinner difficultySpinner;
    private JTextArea notesArea;

    public MainFrame(User user) {
        this.currentUser = user;
        this.studyDAO = new StudyDAO();
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setTitle("Smart Study System - Welcome " + currentUser.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        fileMenu.add(logoutItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Main Tabbed Pane
        tabbedPane = new JTabbedPane();
        
        // Add Tabs
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Add Session", createAddSessionPanel());
        tabbedPane.addTab("Today's Revisions", createTodaysRevisionsPanel());
        tabbedPane.addTab("Upcoming", createUpcomingRevisionsPanel());
        tabbedPane.addTab("History", createHistoryPanel());

        add(tabbedPane);
    }

    // ======================== DASHBOARD PANEL ========================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(240, 245, 250));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        JLabel headerLabel = new JLabel(currentUser.getUsername() + "'s Study Dashboard");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Stats Cards
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        int todaysCount = studyDAO.getTodaysRevisions(currentUser.getId()).size();
        int upcomingCount = studyDAO.getUpcomingRevisions(currentUser.getId()).size();
        int totalSessions = studyDAO.getStudyHistory(currentUser.getId()).size();
        
        statsPanel.add(createStatCard("Today's Revisions", todaysCount, new Color(70, 130, 180)));
        statsPanel.add(createStatCard("Upcoming Revisions", upcomingCount, new Color(60, 179, 113)));
        statsPanel.add(createStatCard("Total Sessions", totalSessions, new Color(255, 165, 0)));
        statsPanel.add(createStatCard("Active Subjects", getSubjectCount(), new Color(147, 112, 219)));
        
        panel.add(statsPanel, BorderLayout.CENTER);

        // Recent Activity
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBorder(BorderFactory.createTitledBorder("Recent Activity"));
        
        String[] columns = {"Date", "Subject", "Topic"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable activityTable = new JTable(model);
        
        // Load recent activity
        List<String[]> history = studyDAO.getStudyHistory(currentUser.getId());
        int displayCount = Math.min(5, history.size());
        for (int i = 0; i < displayCount; i++) {
            String[] session = history.get(i);
            model.addRow(new Object[]{session[2], session[0], session[1]});
        }
        
        activityPanel.add(new JScrollPane(activityTable), BorderLayout.CENTER);
        panel.add(activityPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatCard(String title, int value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.DARK_GRAY);

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private int getSubjectCount() {
        // Simple implementation - you might want to query the database for actual count
        return Math.min(5, studyDAO.getStudyHistory(currentUser.getId()).size());
    }

    // ======================== TODAY'S REVISIONS PANEL ========================
    private JPanel createTodaysRevisionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table Model with hidden Revision ID column
        String[] columns = {"Subject", "Topic", "Study Date", "Revision Date", "Interval", "Action", "Revision ID"};
        todaysModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        todaysRevisionsTable = new JTable(todaysModel);
        todaysRevisionsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        todaysRevisionsTable.setRowHeight(30);
        todaysRevisionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        // Hide Revision ID column
        todaysRevisionsTable.removeColumn(todaysRevisionsTable.getColumnModel().getColumn(6));

        // Configure action column
        TableColumn actionColumn = todaysRevisionsTable.getColumnModel().getColumn(5);
        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(todaysRevisionsTable);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadTodaysRevisions());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void loadTodaysRevisions() {
        todaysModel.setRowCount(0);
        List<String[]> revisions = studyDAO.getTodaysRevisions(currentUser.getId());
        
        for (String[] revision : revisions) {
            Object[] row = {
                revision[0], // subject
                revision[1], // topic
                revision[2], // study date
                revision[3], // revision date
                revision[4] + " days", // interval
                "Mark Complete", // button text
                revision[5]  // revision ID (hidden)
            };
            todaysModel.addRow(row);
        }
    }

    // ======================== BUTTON COMPONENTS ========================
    class ButtonRenderer extends DefaultTableCellRenderer {
        private final JButton button = new JButton();

        public ButtonRenderer() {
            button.setOpaque(true);
            button.setBackground(new Color(76, 175, 80));
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            button.setText(value == null ? "" : value.toString());
            return button;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = todaysRevisionsTable.getSelectedRow();
                if (row >= 0) {
                    int revisionId = Integer.parseInt(todaysModel.getValueAt(row, 6).toString());
                    showCompletionDialog(revisionId);
                }
            }
            isPushed = false;
            return label;
        }

        private void showCompletionDialog(int revisionId) {
            JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
            panel.add(new JLabel("How well did you recall this material? (1-5)"));
            
            JComboBox<Integer> ratingCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
            panel.add(ratingCombo);
            
            int result = JOptionPane.showConfirmDialog(
                MainFrame.this,
                panel,
                "Rate Your Recall",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.OK_OPTION) {
                int quality = (int) ratingCombo.getSelectedItem();
                if (studyDAO.markRevisionCompleted(revisionId, quality)) {
                    JOptionPane.showMessageDialog(
                        MainFrame.this,
                        "Revision marked as completed!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    loadTodaysRevisions();
                } else {
                    JOptionPane.showMessageDialog(
                        MainFrame.this,
                        "Failed to update revision status",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }

    // ======================== OTHER PANELS ========================
    private JPanel createAddSessionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Subject
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Subject:"), gbc);
        subjectField = new JTextField(25);
        subjectField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(subjectField, gbc);

        // Topic
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Topic:"), gbc);
        topicField = new JTextField(25);
        topicField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(topicField, gbc);

        // Difficulty
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Difficulty (1-5):"), gbc);
        difficultySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        difficultySpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(difficultySpinner, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Notes:"), gbc);
        notesArea = new JTextArea(5, 25);
        notesArea.setFont(new Font("Arial", Font.PLAIN, 14));
        notesArea.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        gbc.gridx = 1;
        formPanel.add(notesScroll, gbc);

        // Add Button
        JButton addButton = new JButton("Add Study Session");
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setBackground(new Color(70, 130, 180));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addStudySession());
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(addButton, gbc);

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private void addStudySession() {
        String subject = subjectField.getText().trim();
        String topic = topicField.getText().trim();
        int difficulty = (int) difficultySpinner.getValue();
        String notes = notesArea.getText().trim();

        if (subject.isEmpty() || topic.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Subject and Topic are required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StudySession session = new StudySession(
            currentUser.getId(),
            subject,
            topic,
            LocalDate.now(),
            difficulty,
            notes.isEmpty() ? null : notes
        );

        if (studyDAO.addStudySession(session)) {
            JOptionPane.showMessageDialog(this,
                "Study session added successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to add study session", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        subjectField.setText("");
        topicField.setText("");
        difficultySpinner.setValue(3);
        notesArea.setText("");
    }

    private JPanel createUpcomingRevisionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Subject", "Topic", "Study Date", "Revision Date", "Interval"};
        upcomingModel = new DefaultTableModel(columns, 0);

        upcomingRevisionsTable = new JTable(upcomingModel);
        upcomingRevisionsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        upcomingRevisionsTable.setRowHeight(25);
        upcomingRevisionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(upcomingRevisionsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Subject", "Topic", "Study Date", "Difficulty", "Notes"};
        historyModel = new DefaultTableModel(columns, 0);

        historyTable = new JTable(historyModel);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 14));
        historyTable.setRowHeight(25);
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    // ======================== UTILITY METHODS ========================
    private void loadData() {
        loadTodaysRevisions();
        loadUpcomingRevisions();
        loadStudyHistory();
    }

    private void loadUpcomingRevisions() {
        upcomingModel.setRowCount(0);
        List<String[]> revisions = studyDAO.getUpcomingRevisions(currentUser.getId());
        
        for (String[] revision : revisions) {
            Object[] row = {
                revision[0], // subject
                revision[1], // topic
                revision[2], // study date
                revision[3], // revision date
                revision[4] + " days" // interval
            };
            upcomingModel.addRow(row);
        }
    }

    private void loadStudyHistory() {
        historyModel.setRowCount(0);
        List<String[]> history = studyDAO.getStudyHistory(currentUser.getId());
        
        for (String[] session : history) {
            Object[] row = {
                session[0], // subject
                session[1], // topic
                session[2], // study date
                session[3], // difficulty
                session[4]  // notes
            };
            historyModel.addRow(row);
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}