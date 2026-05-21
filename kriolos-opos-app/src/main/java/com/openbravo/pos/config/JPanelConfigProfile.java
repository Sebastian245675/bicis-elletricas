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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.DefaultListCellRenderer;
import javax.swing.border.Border;

public class JPanelConfigProfile extends JPanel implements PanelConfig {

    private final DirtyManager dirty = new DirtyManager();
    private final String[] genderKeys = {"unspecified", "male", "female", "other"};

    private JTextField jtxtName;
    private JTextField jtxtEmail;
    private JTextField jtxtPhone;
    private JTextField jtxtBirthday;
    private JTextField jtxtAddress;
    private JComboBox<String> jcomboGender;

    public JPanelConfigProfile() {
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
        card.setPreferredSize(new Dimension(640, 520));
        card.setMinimumSize(new Dimension(600, 520));
        card.setMaximumSize(new Dimension(640, 520));

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.weightx = 1.0;
        cardGbc.insets = new Insets(0, 0, 20, 0);

        // --- Card Header Section ---
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);

        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.gridx = 0;
        hgbc.gridy = 0;
        hgbc.gridheight = 2;
        hgbc.anchor = GridBagConstraints.WEST;
        hgbc.insets = new Insets(0, 0, 0, 20);
        headerPanel.add(new AvatarPanel(), hgbc);

        hgbc.gridx = 1;
        hgbc.gridheight = 1;
        hgbc.weightx = 1.0;
        hgbc.fill = GridBagConstraints.HORIZONTAL;
        hgbc.anchor = GridBagConstraints.SOUTHWEST;
        hgbc.insets = new Insets(0, 0, 4, 0);

        JLabel titleLabel = new JLabel(AppLocal.getIntString("label.profile.title"));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 41, 59)); // Slate 800
        headerPanel.add(titleLabel, hgbc);

        hgbc.gridy = 1;
        hgbc.anchor = GridBagConstraints.NORTHWEST;
        hgbc.insets = new Insets(4, 0, 0, 0);

        JLabel subtitleLabel = new JLabel("Gestiona tu información de perfil para el sistema local.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 116, 139)); // Slate 500
        headerPanel.add(subtitleLabel, hgbc);

        card.add(headerPanel, cardGbc);

        // --- Separator Line ---
        cardGbc.gridy = 1;
        cardGbc.insets = new Insets(0, 0, 24, 0);
        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(1, 1));
        separator.setBackground(new Color(226, 232, 240)); // Slate 200
        card.add(separator, cardGbc);

        // --- Form Fields Layout ---
        JPanel formFieldsPanel = new JPanel(new GridBagLayout());
        formFieldsPanel.setOpaque(false);

        jtxtName = createStyledTextField();
        jtxtEmail = createStyledTextField();
        jtxtPhone = createStyledTextField();
        jtxtBirthday = createStyledTextField();
        jtxtAddress = createStyledTextField();
        
        jcomboGender = new JComboBox<>();
        jcomboGender.addItem(AppLocal.getIntString("label.profile.gender.unspecified"));
        jcomboGender.addItem(AppLocal.getIntString("label.profile.gender.male"));
        jcomboGender.addItem(AppLocal.getIntString("label.profile.gender.female"));
        jcomboGender.addItem(AppLocal.getIntString("label.profile.gender.other"));
        styleComboBox(jcomboGender);

        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.weightx = 0.5;

        // Row 0: Nombre & Correo
        fgbc.gridy = 0;
        fgbc.gridx = 0;
        fgbc.insets = new Insets(0, 0, 16, 12);
        formFieldsPanel.add(createFieldGroup("Nombre Completo", jtxtName), fgbc);

        fgbc.gridx = 1;
        fgbc.insets = new Insets(0, 12, 16, 0);
        formFieldsPanel.add(createFieldGroup("Correo Electrónico", jtxtEmail), fgbc);

        // Row 1: Teléfono & Fecha Nacimiento
        fgbc.gridy = 1;
        fgbc.gridx = 0;
        fgbc.insets = new Insets(0, 0, 16, 12);
        formFieldsPanel.add(createFieldGroup("Teléfono", jtxtPhone), fgbc);

        fgbc.gridx = 1;
        fgbc.insets = new Insets(0, 12, 16, 0);
        formFieldsPanel.add(createFieldGroup("Fecha de Nacimiento", jtxtBirthday), fgbc);

        // Row 2: Género
        fgbc.gridy = 2;
        fgbc.gridx = 0;
        fgbc.insets = new Insets(0, 0, 16, 12);
        formFieldsPanel.add(createFieldGroup("Género", jcomboGender), fgbc);

        fgbc.gridx = 1;
        fgbc.insets = new Insets(0, 12, 16, 0);
        formFieldsPanel.add(new JPanel() {{ setOpaque(false); }}, fgbc); // Spacer

        // Row 3: Dirección Completa (Spans 2 columns)
        fgbc.gridy = 3;
        fgbc.gridx = 0;
        fgbc.gridwidth = 2;
        fgbc.weightx = 1.0;
        fgbc.insets = new Insets(0, 0, 0, 0);
        formFieldsPanel.add(createFieldGroup("Dirección Completa", jtxtAddress), fgbc);

        cardGbc.gridy = 2;
        cardGbc.fill = GridBagConstraints.BOTH;
        cardGbc.weighty = 1.0;
        cardGbc.insets = new Insets(0, 0, 0, 0);
        card.add(formFieldsPanel, cardGbc);

        // Add Card to wrapper
        GridBagConstraints wgbc = new GridBagConstraints();
        wgbc.gridx = 0;
        wgbc.gridy = 0;
        wgbc.insets = new Insets(30, 30, 30, 30);
        centerWrapper.add(card, wgbc);

        scrollPane.setViewportView(centerWrapper);
        add(scrollPane, java.awt.BorderLayout.CENTER);
    }

    private JTextField createStyledTextField() {
        final JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(240, 36));
        tf.setBackground(Color.WHITE);
        tf.setForeground(new Color(30, 41, 59)); // Slate 800
        tf.setCaretColor(new Color(59, 130, 246)); // Focus Blue
        
        // Base Rounded Border
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(226, 232, 240), 8, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        // Interaction Listener
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

    private void styleComboBox(JComboBox<String> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setPreferredSize(new Dimension(240, 36));
        cb.setBackground(Color.WHITE);
        cb.setForeground(new Color(30, 41, 59));
        
        cb.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(226, 232, 240), 8, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                if (isSelected) {
                    lbl.setBackground(new Color(59, 130, 246));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(30, 41, 59));
                }
                return lbl;
            }
        });
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
        jtxtName.getDocument().addDocumentListener(dirty);
        jtxtEmail.getDocument().addDocumentListener(dirty);
        jtxtPhone.getDocument().addDocumentListener(dirty);
        jtxtBirthday.getDocument().addDocumentListener(dirty);
        jtxtAddress.getDocument().addDocumentListener(dirty);
        jcomboGender.addActionListener(dirty);
    }

    private String getProp(AppConfig config, String key) {
        String val = config.getProperty(key);
        return val == null ? "" : val;
    }

    private String getSelectedGenderKey() {
        int idx = jcomboGender.getSelectedIndex();
        if (idx >= 0 && idx < genderKeys.length) {
            return genderKeys[idx];
        }
        return "unspecified";
    }

    private void setSelectedGenderKey(String key) {
        if (key == null) {
            jcomboGender.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < genderKeys.length; i++) {
            if (genderKeys[i].equals(key)) {
                jcomboGender.setSelectedIndex(i);
                return;
            }
        }
        jcomboGender.setSelectedIndex(0);
    }

    @Override
    public void loadProperties(AppConfig config) {
        jtxtName.setText(getProp(config, "profile.name"));
        jtxtEmail.setText(getProp(config, "profile.email"));
        jtxtPhone.setText(getProp(config, "profile.phone"));
        jtxtBirthday.setText(getProp(config, "profile.birthday"));
        jtxtAddress.setText(getProp(config, "profile.address"));
        setSelectedGenderKey(config.getProperty("profile.gender"));

        dirty.setDirty(false);
    }

    @Override
    public void saveProperties(AppConfig config) {
        config.setProperty("profile.name", jtxtName.getText());
        config.setProperty("profile.email", jtxtEmail.getText());
        config.setProperty("profile.phone", jtxtPhone.getText());
        config.setProperty("profile.birthday", jtxtBirthday.getText());
        config.setProperty("profile.address", jtxtAddress.getText());
        config.setProperty("profile.gender", getSelectedGenderKey());

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

    // Dynamic Vector Avatar
    private static class AvatarPanel extends JPanel {
        public AvatarPanel() {
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

            // Gradient Paint for background circle
            java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                0, 0, new Color(224, 242, 254), // Sky 100
                0, h, new Color(186, 230, 253)  // Sky 200
            );
            g2.setPaint(gradient);
            g2.fillOval(2, 2, w - 5, h - 5);

            // Draw Avatar Border
            g2.setColor(new Color(14, 165, 233)); // Sky 500
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawOval(2, 2, w - 5, h - 5);

            // Draw head
            g2.setColor(new Color(2, 132, 199)); // Sky 600
            int headSize = w / 3;
            int headX = (w - headSize) / 2;
            int headY = (int) (h * 0.22);
            g2.fillOval(headX, headY, headSize, headSize);

            // Draw body/shoulders
            int bodyW = (int) (w * 0.58);
            int bodyH = h / 3;
            int bodyX = (w - bodyW) / 2;
            int bodyY = (int) (h * 0.54);
            g2.fillArc(bodyX, bodyY, bodyW, bodyH * 2, 0, 180);

            g2.dispose();
        }
    }
}
