//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.

package com.openbravo.pos.config;

import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class JPanelConfigSpecial extends JPanel implements PanelConfig {

    private final DirtyManager dirty = new DirtyManager();

    private JTextField jtxtEmail;
    private JTextField jtxtSMTPServer;
    private JTextField jtxtSMTPPort;
    private JTextField jtxtSMTPUser;
    private JPasswordField jtxtSMTPPassword;
    private JCheckBox jchkSMTPSSL;

    private JCheckBox jchkAlertLowStock;
    private JCheckBox jchkAlertDailyClose;
    private JCheckBox jchkAlertPriceChange;
    private JCheckBox jchkAlertSpecialSales;

    public JPanelConfigSpecial() {
        initComponents();
        registerListeners();
    }

    private void initComponents() {
        setLayout(new java.awt.BorderLayout());
        setOpaque(true);
        setBackground(new Color(248, 250, 252)); // Slate 50

        // Create transparent ScrollPane
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Centering Wrapper Panel
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        // Main Rounded Card
        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(1024, 520));
        card.setMinimumSize(new Dimension(800, 520));
        card.setMaximumSize(new Dimension(1400, 520));

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.weightx = 1.0;
        cardGbc.insets = new Insets(0, 0, 16, 0);

        // --- Card Header Section ---
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);

        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.gridx = 0;
        hgbc.gridy = 0;
        hgbc.gridheight = 2;
        hgbc.anchor = GridBagConstraints.WEST;
        hgbc.insets = new Insets(0, 0, 0, 20);
        headerPanel.add(new BellPanel(), hgbc);

        hgbc.gridx = 1;
        hgbc.gridheight = 1;
        hgbc.weightx = 1.0;
        hgbc.fill = GridBagConstraints.HORIZONTAL;
        hgbc.anchor = GridBagConstraints.SOUTHWEST;
        hgbc.insets = new Insets(0, 0, 4, 0);

        JLabel titleLabel = new JLabel(AppLocal.getIntString("label.special.title"));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 41, 59)); // Slate 800
        headerPanel.add(titleLabel, hgbc);

        hgbc.gridy = 1;
        hgbc.anchor = GridBagConstraints.NORTHWEST;
        hgbc.insets = new Insets(4, 0, 0, 0);

        JLabel subtitleLabel = new JLabel(AppLocal.getIntString("label.special.subtitle"));
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 116, 139)); // Slate 500
        headerPanel.add(subtitleLabel, hgbc);

        card.add(headerPanel, cardGbc);

        // --- Separator Line ---
        cardGbc.gridy = 1;
        cardGbc.insets = new Insets(0, 0, 20, 0);
        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(1, 1));
        separator.setBackground(new Color(226, 232, 240)); // Slate 200
        card.add(separator, cardGbc);

        // --- Form Fields Layout ---
        JPanel formFieldsPanel = new JPanel(new GridBagLayout());
        formFieldsPanel.setOpaque(false);

        jtxtEmail = createStyledTextField();
        jtxtSMTPServer = createStyledTextField();
        jtxtSMTPPort = createStyledTextField();
        jtxtSMTPUser = createStyledTextField();
        jtxtSMTPPassword = createStyledPasswordField();
        
        jchkSMTPSSL = createStyledCheckBox(AppLocal.getIntString("label.special.smtp.ssl"));

        jchkAlertLowStock = createStyledCheckBox(AppLocal.getIntString("label.special.alert.low_stock"));
        jchkAlertDailyClose = createStyledCheckBox(AppLocal.getIntString("label.special.alert.daily_close"));
        jchkAlertPriceChange = createStyledCheckBox(AppLocal.getIntString("label.special.alert.price_change"));
        jchkAlertSpecialSales = createStyledCheckBox(AppLocal.getIntString("label.special.alert.special_sales"));

        // Left Column Panel (SMTP Configuration)
        JPanel leftColPanel = new JPanel(new GridBagLayout());
        leftColPanel.setOpaque(false);
        GridBagConstraints lcGbc = new GridBagConstraints();
        lcGbc.fill = GridBagConstraints.HORIZONTAL;
        lcGbc.weightx = 1.0;
        lcGbc.gridx = 0;

        // Destination Email
        lcGbc.gridy = 0;
        lcGbc.insets = new Insets(0, 0, 16, 0);
        leftColPanel.add(createFieldGroup(AppLocal.getIntString("label.special.email"), jtxtEmail), lcGbc);

        // SMTP Section Title
        lcGbc.gridy = 1;
        lcGbc.insets = new Insets(8, 0, 8, 0);
        JLabel smtpTitle = new JLabel(AppLocal.getIntString("label.special.smtp.section"));
        smtpTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        smtpTitle.setForeground(new Color(79, 70, 229)); // Indigo 600
        leftColPanel.add(smtpTitle, lcGbc);

        // Server and Port Row
        JPanel serverPortRow = new JPanel(new GridBagLayout());
        serverPortRow.setOpaque(false);
        GridBagConstraints spGbc = new GridBagConstraints();
        spGbc.fill = GridBagConstraints.HORIZONTAL;
        spGbc.gridy = 0;

        spGbc.gridx = 0;
        spGbc.weightx = 0.7;
        spGbc.insets = new Insets(0, 0, 0, 8);
        serverPortRow.add(createFieldGroup(AppLocal.getIntString("label.special.smtp.server"), jtxtSMTPServer), spGbc);

        spGbc.gridx = 1;
        spGbc.weightx = 0.3;
        spGbc.insets = new Insets(0, 0, 0, 0);
        serverPortRow.add(createFieldGroup(AppLocal.getIntString("label.special.smtp.port"), jtxtSMTPPort), spGbc);

        lcGbc.gridy = 2;
        lcGbc.insets = new Insets(0, 0, 12, 0);
        leftColPanel.add(serverPortRow, lcGbc);

        // User and Password Row
        JPanel userPassRow = new JPanel(new GridBagLayout());
        userPassRow.setOpaque(false);
        GridBagConstraints upGbc = new GridBagConstraints();
        upGbc.fill = GridBagConstraints.HORIZONTAL;
        upGbc.gridy = 0;

        upGbc.gridx = 0;
        upGbc.weightx = 0.5;
        upGbc.insets = new Insets(0, 0, 0, 8);
        userPassRow.add(createFieldGroup(AppLocal.getIntString("label.special.smtp.user"), jtxtSMTPUser), upGbc);

        upGbc.gridx = 1;
        upGbc.weightx = 0.5;
        upGbc.insets = new Insets(0, 0, 0, 0);
        userPassRow.add(createFieldGroup(AppLocal.getIntString("label.special.smtp.password"), jtxtSMTPPassword), upGbc);

        lcGbc.gridy = 3;
        lcGbc.insets = new Insets(0, 0, 16, 0);
        leftColPanel.add(userPassRow, lcGbc);

        // SSL / TLS Checkbox
        lcGbc.gridy = 4;
        lcGbc.insets = new Insets(0, 0, 0, 0);
        leftColPanel.add(jchkSMTPSSL, lcGbc);


        // Right Column Panel (Alerts Configuration)
        JPanel rightColPanel = new JPanel(new GridBagLayout());
        rightColPanel.setOpaque(false);
        GridBagConstraints rcGbc = new GridBagConstraints();
        rcGbc.fill = GridBagConstraints.HORIZONTAL;
        rcGbc.weightx = 1.0;
        rcGbc.gridx = 0;

        // Alerts Section Title
        rcGbc.gridy = 0;
        rcGbc.insets = new Insets(0, 0, 12, 0);
        JLabel alertsTitle = new JLabel(AppLocal.getIntString("label.special.alerts.section"));
        alertsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        alertsTitle.setForeground(new Color(79, 70, 229)); // Indigo 600
        rightColPanel.add(alertsTitle, rcGbc);

        // Checkboxes vertical stack
        rcGbc.gridy = 1;
        rcGbc.insets = new Insets(0, 0, 12, 0);
        rightColPanel.add(jchkAlertLowStock, rcGbc);

        rcGbc.gridy = 2;
        rcGbc.insets = new Insets(0, 0, 12, 0);
        rightColPanel.add(jchkAlertDailyClose, rcGbc);

        rcGbc.gridy = 3;
        rcGbc.insets = new Insets(0, 0, 12, 0);
        rightColPanel.add(jchkAlertPriceChange, rcGbc);

        rcGbc.gridy = 4;
        rcGbc.insets = new Insets(0, 0, 0, 0);
        rightColPanel.add(jchkAlertSpecialSales, rcGbc);


        // Add columns to formFieldsPanel
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.BOTH;
        fgbc.weighty = 1.0;

        fgbc.gridx = 0;
        fgbc.gridy = 0;
        fgbc.weightx = 0.55;
        fgbc.insets = new Insets(0, 0, 0, 24);
        formFieldsPanel.add(leftColPanel, fgbc);

        fgbc.gridx = 1;
        fgbc.gridy = 0;
        fgbc.weightx = 0.45;
        fgbc.insets = new Insets(0, 24, 0, 0);
        formFieldsPanel.add(rightColPanel, fgbc);

        cardGbc.gridy = 2;
        cardGbc.fill = GridBagConstraints.BOTH;
        cardGbc.weighty = 1.0;
        cardGbc.insets = new Insets(0, 0, 0, 0);
        card.add(formFieldsPanel, cardGbc);

        // Add Card to wrapper
        GridBagConstraints wgbc = new GridBagConstraints();
        wgbc.gridx = 0;
        wgbc.gridy = 0;
        wgbc.fill = GridBagConstraints.HORIZONTAL;
        wgbc.weightx = 1.0;
        wgbc.insets = new Insets(20, 20, 20, 20);
        centerWrapper.add(card, wgbc);

        scrollPane.setViewportView(centerWrapper);
        add(scrollPane, java.awt.BorderLayout.CENTER);
    }

    private JTextField createStyledTextField() {
        final JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(180, 36));
        tf.setBackground(Color.WHITE);
        tf.setForeground(new Color(30, 41, 59)); // Slate 800
        tf.setCaretColor(new Color(59, 130, 246)); // Focus Blue
        
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(226, 232, 240), 8, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(new Color(59, 130, 246), 8, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(new Color(226, 232, 240), 8, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                ));
            }
        });

        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        final JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setPreferredSize(new Dimension(180, 36));
        pf.setBackground(Color.WHITE);
        pf.setForeground(new Color(30, 41, 59)); // Slate 800
        pf.setCaretColor(new Color(59, 130, 246)); // Focus Blue
        
        pf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(226, 232, 240), 8, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        pf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(new Color(59, 130, 246), 8, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(new Color(226, 232, 240), 8, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                ));
            }
        });

        return pf;
    }

    private JCheckBox createStyledCheckBox(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setForeground(new Color(30, 41, 59)); // Slate 800
        cb.setOpaque(false);
        return cb;
    }

    private JPanel createFieldGroup(String labelText, JComponent input) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 6, 0);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(71, 85, 105)); // Slate 600
        panel.add(label, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(input, gbc);

        return panel;
    }

    private void registerListeners() {
        jtxtEmail.getDocument().addDocumentListener(dirty);
        jtxtSMTPServer.getDocument().addDocumentListener(dirty);
        jtxtSMTPPort.getDocument().addDocumentListener(dirty);
        jtxtSMTPUser.getDocument().addDocumentListener(dirty);
        jtxtSMTPPassword.getDocument().addDocumentListener(dirty);
        jchkSMTPSSL.addActionListener(dirty);

        jchkAlertLowStock.addActionListener(dirty);
        jchkAlertDailyClose.addActionListener(dirty);
        jchkAlertPriceChange.addActionListener(dirty);
        jchkAlertSpecialSales.addActionListener(dirty);
    }

    private String getProp(AppConfig config, String key) {
        String val = config.getProperty(key);
        return val == null ? "" : val;
    }

    private boolean getBoolProp(AppConfig config, String key, boolean defaultVal) {
        String val = config.getProperty(key);
        if (val == null) {
            return defaultVal;
        }
        return Boolean.parseBoolean(val);
    }

    @Override
    public void loadProperties(AppConfig config) {
        jtxtEmail.setText(getProp(config, "notifications.email"));
        jtxtSMTPServer.setText(getProp(config, "notifications.smtp.server"));
        jtxtSMTPPort.setText(getProp(config, "notifications.smtp.port"));
        jtxtSMTPUser.setText(getProp(config, "notifications.smtp.user"));
        jtxtSMTPPassword.setText(getProp(config, "notifications.smtp.password"));
        
        jchkSMTPSSL.setSelected(getBoolProp(config, "notifications.smtp.ssl", false));

        jchkAlertLowStock.setSelected(getBoolProp(config, "notifications.alert.low_stock", false));
        jchkAlertDailyClose.setSelected(getBoolProp(config, "notifications.alert.daily_close", false));
        jchkAlertPriceChange.setSelected(getBoolProp(config, "notifications.alert.price_change", false));
        jchkAlertSpecialSales.setSelected(getBoolProp(config, "notifications.alert.special_sales", false));

        dirty.setDirty(false);
    }

    @Override
    public void saveProperties(AppConfig config) {
        config.setProperty("notifications.email", jtxtEmail.getText());
        config.setProperty("notifications.smtp.server", jtxtSMTPServer.getText());
        config.setProperty("notifications.smtp.port", jtxtSMTPPort.getText());
        config.setProperty("notifications.smtp.user", jtxtSMTPUser.getText());
        config.setProperty("notifications.smtp.password", new String(jtxtSMTPPassword.getPassword()));
        
        config.setProperty("notifications.smtp.ssl", Boolean.toString(jchkSMTPSSL.isSelected()));

        config.setProperty("notifications.alert.low_stock", Boolean.toString(jchkAlertLowStock.isSelected()));
        config.setProperty("notifications.alert.daily_close", Boolean.toString(jchkAlertDailyClose.isSelected()));
        config.setProperty("notifications.alert.price_change", Boolean.toString(jchkAlertPriceChange.isSelected()));
        config.setProperty("notifications.alert.special_sales", Boolean.toString(jchkAlertSpecialSales.isSelected()));

        dirty.setDirty(false);
    }

    @Override
    public boolean hasChanged() {
        return dirty.isDirty();
    }

    @Override
    public Component getConfigComponent() {
        return this;
    }

    // Custom Rounded Border Class
    private static class RoundedBorder implements Border {
        private final Color color;
        private final int radius;
        private final int thickness;

        public RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            for (int i = 0; i < thickness; i++) {
                g2.drawRoundRect(x + i, y + i, width - 1 - i * 2, height - 1 - i * 2, radius, radius);
            }
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    // Custom Rounded Card Class
    private static class RoundedCard extends JPanel {
        private final int cornerRadius = 16;

        public RoundedCard() {
            setOpaque(false);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw card background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            
            // Draw card border
            g2.setColor(new Color(226, 232, 240)); // Slate 200
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            
            g2.dispose();
        }
    }

    // Dynamic Vector Bell Badge
    private static class BellPanel extends JPanel {
        public BellPanel() {
            setPreferredSize(new Dimension(72, 72));
            setMinimumSize(new Dimension(72, 72));
            setMaximumSize(new Dimension(72, 72));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Gradient Paint for background circle (Indigo theme)
            java.awt.GradientPaint gradientIndigo = new java.awt.GradientPaint(
                0, 0, new Color(238, 242, 255), // Indigo 50
                0, h, new Color(224, 231, 255)  // Indigo 100
            );
            g2.setPaint(gradientIndigo);
            g2.fillOval(2, 2, w - 5, h - 5);

            // Draw Border
            g2.setColor(new Color(99, 102, 241)); // Indigo 500
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawOval(2, 2, w - 5, h - 5);

            // Draw Bell Icon in center
            g2.setColor(new Color(79, 70, 229)); // Indigo 600
            g2.setStroke(new java.awt.BasicStroke(2.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            
            int cx = w / 2;
            int cy = h / 2 - 2;
            
            // Bell dome & handle
            g2.drawArc(cx - 3, cy - 13, 6, 6, 0, 180);
            
            // Main bell body path
            java.awt.geom.Path2D bellPath = new java.awt.geom.Path2D.Double();
            bellPath.moveTo(cx - 3, cy - 10);
            bellPath.lineTo(cx - 6, cy - 6);
            bellPath.quadTo(cx - 8, cy, cx - 10, cy + 5);
            bellPath.lineTo(cx + 10, cy + 5);
            bellPath.quadTo(cx + 8, cy, cx + 6, cy - 6);
            bellPath.lineTo(cx + 3, cy - 10);
            bellPath.closePath();
            g2.fill(bellPath);
            
            // Bell bottom clapper
            g2.fillOval(cx - 3, cy + 8, 6, 6);
            
            // Bell bottom rim line
            g2.drawLine(cx - 12, cy + 5, cx + 12, cy + 5);

            g2.dispose();
        }
    }
}
