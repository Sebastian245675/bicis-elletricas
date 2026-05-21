//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

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
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

/**
 *
 * @author JG uniCenta & Google DeepMind Team
 */
public class JPanelConfigCompany extends JPanel implements PanelConfig {
    
    private final DirtyManager dirty = new DirtyManager();

    // Visual Controls
    private JLabel jLbllogoPath;
    private JLabel jLogo;
    private JPanel jPanel1;
    private JTextField jtxtTktFooter1;
    private JTextField jtxtTktFooter2;
    private JTextField jtxtTktFooter3;
    private JTextField jtxtTktFooter4;
    private JTextField jtxtTktFooter5;
    private JTextField jtxtTktFooter6;
    private JTextField jtxtTktHeader1;
    private JTextField jtxtTktHeader2;
    private JTextField jtxtTktHeader3;
    private JTextField jtxtTktHeader4;
    private JTextField jtxtTktHeader5;
    private JTextField jtxtTktHeader6;
    private JLabel lblLogo;
    private JLabel lblTktFooter1;
    private JLabel lblTktHeader1;
    private JCheckBox webSwtch_Logo;

    /**
     * Creates new form JPanelConfigCompany
     */
    public JPanelConfigCompany() {
        initComponents();
        registerListeners();
    }

    private void registerListeners() {
        jtxtTktHeader1.getDocument().addDocumentListener(dirty);
        jtxtTktHeader2.getDocument().addDocumentListener(dirty);
        jtxtTktHeader3.getDocument().addDocumentListener(dirty);
        jtxtTktHeader4.getDocument().addDocumentListener(dirty);
        jtxtTktHeader5.getDocument().addDocumentListener(dirty);
        jtxtTktHeader6.getDocument().addDocumentListener(dirty);

        jtxtTktFooter1.getDocument().addDocumentListener(dirty);
        jtxtTktFooter2.getDocument().addDocumentListener(dirty);
        jtxtTktFooter3.getDocument().addDocumentListener(dirty);
        jtxtTktFooter4.getDocument().addDocumentListener(dirty);
        jtxtTktFooter5.getDocument().addDocumentListener(dirty);
        jtxtTktFooter6.getDocument().addDocumentListener(dirty);
        
        webSwtch_Logo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webSwtch_LogoActionPerformed(evt);
            }
        });
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasChanged() {
        return dirty.isDirty();
    }
    
    /**
     *
     * @return
     */
    @Override
    public Component getConfigComponent() {
        return this;
    }
   
    /**
     *
     * @param config
     */
    @Override
    public void loadProperties(AppConfig config) {
        jtxtTktHeader1.setText(config.getProperty("tkt.header1"));
        jtxtTktHeader2.setText(config.getProperty("tkt.header2"));
        jtxtTktHeader3.setText(config.getProperty("tkt.header3"));  
        jtxtTktHeader4.setText(config.getProperty("tkt.header4"));  
        jtxtTktHeader5.setText(config.getProperty("tkt.header5"));  
        jtxtTktHeader6.setText(config.getProperty("tkt.header6"));  
        
        jtxtTktFooter1.setText(config.getProperty("tkt.footer1"));
        jtxtTktFooter2.setText(config.getProperty("tkt.footer2"));
        jtxtTktFooter3.setText(config.getProperty("tkt.footer3"));  
        jtxtTktFooter4.setText(config.getProperty("tkt.footer4"));  
        jtxtTktFooter5.setText(config.getProperty("tkt.footer5"));  
        jtxtTktFooter6.setText(config.getProperty("tkt.footer6"));  
        
        dirty.setDirty(false);        
    }
   
    /**
     *
     * @param config
     */
    @Override
    public void saveProperties(AppConfig config) {
        config.setProperty("tkt.header1", jtxtTktHeader1.getText());
        config.setProperty("tkt.header2", jtxtTktHeader2.getText()); 
        config.setProperty("tkt.header3", jtxtTktHeader3.getText()); 
        config.setProperty("tkt.header4", jtxtTktHeader4.getText()); 
        config.setProperty("tkt.header5", jtxtTktHeader5.getText()); 
        config.setProperty("tkt.header6", jtxtTktHeader6.getText()); 
        
        config.setProperty("tkt.footer1", jtxtTktFooter1.getText());
        config.setProperty("tkt.footer2", jtxtTktFooter2.getText()); 
        config.setProperty("tkt.footer3", jtxtTktFooter3.getText()); 
        config.setProperty("tkt.footer4", jtxtTktFooter4.getText()); 
        config.setProperty("tkt.footer5", jtxtTktFooter5.getText()); 
        config.setProperty("tkt.footer6", jtxtTktFooter6.getText());          

        if (jLbllogoPath != null) {
            config.setProperty("tkt.logopath", jLbllogoPath.getText());
        }
        
        dirty.setDirty(false);
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

        // Stretching Wrapper Panel
        ScrollablePanel centerWrapper = new ScrollablePanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        // Main Rounded Card
        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        card.setMinimumSize(new Dimension(640, 500));

        // GridBagConstraints to make the card stretch to fill the available space
        GridBagConstraints wgbc = new GridBagConstraints();
        wgbc.gridx = 0;
        wgbc.gridy = 0;
        wgbc.fill = GridBagConstraints.BOTH;
        wgbc.weightx = 1.0;
        wgbc.weighty = 1.0;
        wgbc.insets = new Insets(24, 24, 24, 24); // 24px padding around the card
        centerWrapper.add(card, wgbc);

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
        
        PrinterBadgePanel printerBadge = new PrinterBadgePanel();
        headerPanel.add(printerBadge, hgbc);

        hgbc.gridx = 1;
        hgbc.gridheight = 1;
        hgbc.weightx = 1.0;
        hgbc.fill = GridBagConstraints.HORIZONTAL;
        hgbc.anchor = GridBagConstraints.SOUTHWEST;
        hgbc.insets = new Insets(0, 0, 4, 0);

        JLabel titleLabel = new JLabel(AppLocal.getIntString("jpanelconfiguration.tab.company.title")); // "Empresa"
        if (titleLabel.getText() == null || titleLabel.getText().trim().isEmpty() || titleLabel.getText().startsWith("jpanel")) {
            titleLabel.setText("Empresa - Impresión de Ticket");
        } else {
            titleLabel.setText(titleLabel.getText() + " - Impresión de Ticket");
        }
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 41, 59)); // Slate 800
        headerPanel.add(titleLabel, hgbc);

        hgbc.gridy = 1;
        hgbc.anchor = GridBagConstraints.NORTHWEST;
        hgbc.insets = new Insets(4, 0, 0, 0);

        JLabel subtitleLabel = new JLabel("Establece el encabezado y pie de página de los comprobantes impresos.");
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

        // Initialize Text Fields
        jtxtTktHeader1 = createStyledTextField();
        jtxtTktHeader2 = createStyledTextField();
        jtxtTktHeader3 = createStyledTextField();
        jtxtTktHeader4 = createStyledTextField();
        jtxtTktHeader5 = createStyledTextField();
        jtxtTktHeader6 = createStyledTextField();

        jtxtTktFooter1 = createStyledTextField();
        jtxtTktFooter2 = createStyledTextField();
        jtxtTktFooter3 = createStyledTextField();
        jtxtTktFooter4 = createStyledTextField();
        jtxtTktFooter5 = createStyledTextField();
        jtxtTktFooter6 = createStyledTextField();

        // Initialize unused compat components
        jLbllogoPath = new JLabel("");
        lblLogo = new JLabel("");
        webSwtch_Logo = new JCheckBox();
        jLogo = new JLabel();
        jPanel1 = new JPanel();

        // Column layout constraints
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.BOTH;
        fgbc.weightx = 0.5;
        fgbc.weighty = 1.0;

        // Left Column Panel: Encabezado
        JTextField[] headerFields = {jtxtTktHeader1, jtxtTktHeader2, jtxtTktHeader3, jtxtTktHeader4, jtxtTktHeader5, jtxtTktHeader6};
        lblTktHeader1 = new JLabel(AppLocal.getIntString("label.tktheader1")); // "Encabezado"
        if (lblTktHeader1.getText() == null || lblTktHeader1.getText().trim().isEmpty() || lblTktHeader1.getText().startsWith("label.")) {
            lblTktHeader1.setText("Encabezado");
        }
        JPanel headerCol = createSectionPanel(lblTktHeader1.getText(), headerFields);
        
        fgbc.gridx = 0;
        fgbc.gridy = 0;
        fgbc.insets = new Insets(0, 0, 0, 16);
        formFieldsPanel.add(headerCol, fgbc);

        // Right Column Panel: Pie de página
        JTextField[] footerFields = {jtxtTktFooter1, jtxtTktFooter2, jtxtTktFooter3, jtxtTktFooter4, jtxtTktFooter5, jtxtTktFooter6};
        lblTktFooter1 = new JLabel(AppLocal.getIntString("label.tktfooter1")); // "Pie de página"
        if (lblTktFooter1.getText() == null || lblTktFooter1.getText().trim().isEmpty() || lblTktFooter1.getText().startsWith("label.")) {
            lblTktFooter1.setText("Pie de página");
        }
        JPanel footerCol = createSectionPanel(lblTktFooter1.getText(), footerFields);

        fgbc.gridx = 1;
        fgbc.insets = new Insets(0, 16, 0, 0);
        formFieldsPanel.add(footerCol, fgbc);

        // Add formFieldsPanel to card
        cardGbc.gridy = 2;
        cardGbc.fill = GridBagConstraints.BOTH;
        cardGbc.weighty = 1.0;
        cardGbc.insets = new Insets(0, 0, 0, 0);
        card.add(formFieldsPanel, cardGbc);

        scrollPane.setViewportView(centerWrapper);
        add(scrollPane, java.awt.BorderLayout.CENTER);
    }

    private JPanel createSectionPanel(String sectionTitle, JTextField[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 12, 0);

        JLabel titleLabel = new JLabel(sectionTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(30, 41, 59)); // Slate 800
        panel.add(titleLabel, gbc);

        for (int i = 0; i < fields.length; i++) {
            gbc.gridy = i + 1;

            JPanel fieldRow = new JPanel(new GridBagLayout());
            fieldRow.setOpaque(false);

            GridBagConstraints rgbc = new GridBagConstraints();
            rgbc.gridx = 0;
            rgbc.gridy = 0;
            rgbc.anchor = GridBagConstraints.WEST;
            rgbc.insets = new Insets(0, 0, 0, 8);

            JLabel lineLabel = new JLabel("Línea " + (i + 1));
            lineLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lineLabel.setForeground(new Color(100, 116, 139)); // Slate 500
            lineLabel.setPreferredSize(new Dimension(70, 36));
            fieldRow.add(lineLabel, rgbc);

            rgbc.gridx = 1;
            rgbc.fill = GridBagConstraints.HORIZONTAL;
            rgbc.weightx = 1.0;
            rgbc.insets = new Insets(0, 0, 0, 0);
            fieldRow.add(fields[i], rgbc);

            gbc.insets = new Insets(0, 0, 8, 0);
            panel.add(fieldRow, gbc);
        }

        // Add a vertical spacer at the bottom to push all fields to the top
        gbc.gridy = fields.length + 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel() {{ setOpaque(false); }}, gbc);

        return panel;
    }

    private JTextField createStyledTextField() {
        final JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(180, 36));
        tf.setBackground(Color.WHITE);
        tf.setForeground(new Color(30, 41, 59)); // Slate 800
        tf.setCaretColor(new Color(59, 130, 246)); // Focus Blue
        tf.setHorizontalAlignment(JTextField.CENTER); // receipts center alignment
        
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

    private void webSwtch_LogoActionPerformed(java.awt.event.ActionEvent evt) {
        // JG - For future
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
            setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
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

    // Dynamic Printer Badge Panel
    private static class PrinterBadgePanel extends JPanel {
        private final ImageIcon printerIcon;

        public PrinterBadgePanel() {
            setPreferredSize(new Dimension(72, 72));
            setMinimumSize(new Dimension(72, 72));
            setMaximumSize(new Dimension(72, 72));
            setOpaque(false);
            
            java.net.URL imgUrl = getClass().getResource("/com/openbravo/images/printer.png");
            if (imgUrl != null) {
                printerIcon = new ImageIcon(imgUrl);
            } else {
                printerIcon = null;
            }
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
                0, 0, new Color(239, 246, 255), // Blue 50
                0, h, new Color(219, 234, 254)  // Blue 100
            );
            g2.setPaint(gradient);
            g2.fillOval(2, 2, w - 5, h - 5);

            // Draw Border
            g2.setColor(new Color(59, 130, 246)); // Blue 500
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawOval(2, 2, w - 5, h - 5);

            // Draw Printer Icon in the center
            if (printerIcon != null) {
                int iconW = printerIcon.getIconWidth();
                int iconH = printerIcon.getIconHeight();
                int x = (w - iconW) / 2;
                int y = (h - iconH) / 2;
                g2.drawImage(printerIcon.getImage(), x, y, null);
            }

            g2.dispose();
        }
    }

    // Scrollable Wrapper Panel class for viewport tracking and responsiveness
    private static class ScrollablePanel extends JPanel implements javax.swing.Scrollable {
        public ScrollablePanel(java.awt.LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            if (getParent() instanceof javax.swing.JViewport) {
                return getParent().getWidth() > getPreferredSize().width;
            }
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            if (getParent() instanceof javax.swing.JViewport) {
                return getParent().getHeight() > getPreferredSize().height;
            }
            return false;
        }
    }
}
