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
import java.awt.BorderLayout;
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
import javax.swing.JPasswordField;
import javax.swing.JComboBox;
import javax.swing.border.Border;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.util.AltEncrypter;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSeparator;
import javax.swing.JButton;
import java.awt.Cursor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JG uniCenta & Google DeepMind Team
 */
public class JPanelConfigCompany extends JPanel implements PanelConfig {

    private static final Logger LOGGER = Logger.getLogger(JPanelConfigCompany.class.getName());
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

    private JSpinner jPickupSize;
    private JSpinner jReceiptSize;
    private JTextField jTextReceiptPrefix;
    private JTextField jTicketExample;
    private JButton jbtnReset;
    private JCheckBox m_jReceiptPrintOff;
    private JTextField txtFacturamaUser;
    private JPasswordField txtFacturamaPassword;
    private JTextField txtFacturamaPostalCode;
    private JComboBox<String> comboFacturamaEnvironment;
    private AppConfig config;

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
        txtFacturamaUser.getDocument().addDocumentListener(dirty);
        txtFacturamaPassword.getDocument().addDocumentListener(dirty);
        txtFacturamaPostalCode.getDocument().addDocumentListener(dirty);
        comboFacturamaEnvironment.addActionListener(event -> dirty.setDirty(true));
        
        webSwtch_Logo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webSwtch_LogoActionPerformed(evt);
            }
        });

        // Ticket Setup Listeners
        jReceiptSize.addChangeListener(e -> {
            receiptPrefixExample();
            dirty.setDirty(true);
        });
        jPickupSize.addChangeListener(e -> dirty.setDirty(true));
        
        jTextReceiptPrefix.getDocument().addDocumentListener(dirty);
        jTextReceiptPrefix.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { receiptPrefixExample(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { receiptPrefixExample(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { receiptPrefixExample(); }
        });
        
        m_jReceiptPrintOff.addActionListener(e -> dirty.setDirty(true));

        jbtnReset.addActionListener(evt -> {
            int response = JOptionPane.showOptionDialog(this,
                    AppLocal.getIntString("message.resetpickup"),
                    "Reset",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, null, null);
            if (response == JOptionPane.YES_OPTION) {
                try {
                    String db_user = (config.getProperty("db.user"));
                    String db_url = (config.getProperty("db.URL") + config.getProperty("db.schema") + config.getProperty("db.options"));
                    String db_password = (config.getProperty("db.password"));

                    if (db_user != null && db_password != null && db_password.startsWith("crypt:")) {
                        AltEncrypter cypher = new AltEncrypter("cypherkey" + db_user);
                        db_password = cypher.decrypt(db_password.substring(6));
                    }

                    Session session = new Session(db_url, db_user, db_password);
                    session.begin();
                    session.DB.getSequenceSentence(session, "pickup_number").find();
                    session.DB.resetSequenceSentence(session, "pickup_number");
                    session.commit();

                } catch (BasicException | SQLException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        });
    }

    private void receiptPrefixExample() {
        String receipt = "";
        int x = 1;
        while (x < (Integer) jReceiptSize.getValue()) {
            receipt += "0";
            x++;
        }
        receipt += "1";
        jTicketExample.setText(jTextReceiptPrefix.getText() + receipt);
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
        this.config = config;
        
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
        
        // Ticket Setup properties
        int recSize;
        String receiptSize = (config.getProperty("till.receiptsize"));
        try {
            recSize = Integer.parseInt(receiptSize);
        } catch (NumberFormatException ex) {
            recSize = 1;
        }
        jReceiptSize.setModel(new SpinnerNumberModel(recSize, 0, 20, 1));

        int picSize;
        String pickupSize = (config.getProperty("till.pickupsize"));
        try {
            picSize = Integer.parseInt(pickupSize);
        } catch (NumberFormatException ex) {
            picSize = 1;
        }
        jPickupSize.setModel(new SpinnerNumberModel(picSize, 0, 20, 1));

        jTextReceiptPrefix.setText(config.getProperty("till.receiptprefix"));
        m_jReceiptPrintOff.setSelected(Boolean.parseBoolean(config.getProperty("till.receiptprintoff")));
        txtFacturamaUser.setText(config.getProperty("facturama.user"));
        txtFacturamaPassword.setText(config.getProperty("facturama.password"));
        txtFacturamaPostalCode.setText(config.getProperty("facturama.expeditionPostalCode"));
        String facturamaUrl = config.getProperty("facturama.url");
        comboFacturamaEnvironment.setSelectedIndex(
                facturamaUrl != null && facturamaUrl.contains("api.facturama.mx") ? 1 : 0);
        
        receiptPrefixExample();
        
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

        // Ticket Setup properties
        config.setProperty("till.receiptprefix", jTextReceiptPrefix.getText());
        config.setProperty("till.receiptsize", jReceiptSize.getValue().toString());
        config.setProperty("till.pickupsize", jPickupSize.getValue().toString());
        config.setProperty("till.receiptprintoff", Boolean.toString(m_jReceiptPrintOff.isSelected()));
        config.setProperty("facturama.user", txtFacturamaUser.getText().trim());
        config.setProperty("facturama.password", new String(txtFacturamaPassword.getPassword()));
        config.setProperty("facturama.expeditionPostalCode", txtFacturamaPostalCode.getText().trim());
        config.setProperty("facturama.url", comboFacturamaEnvironment.getSelectedIndex() == 1
                ? "https://api.facturama.mx/3/cfdis"
                : "https://apisandbox.facturama.mx/3/cfdis");

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

        // Add formFieldsPanel to card (weighty = 0.0, fill = HORIZONTAL)
        cardGbc.gridy = 2;
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.weighty = 0.0;
        cardGbc.insets = new Insets(0, 0, 20, 0);
        card.add(formFieldsPanel, cardGbc);

        // --- Separator Line 2 ---
        cardGbc.gridy = 3;
        cardGbc.insets = new Insets(0, 0, 20, 0);
        JPanel separator2 = new JPanel();
        separator2.setPreferredSize(new Dimension(1, 1));
        separator2.setBackground(new Color(226, 232, 240)); // Slate 200
        card.add(separator2, cardGbc);

        // --- Ticket Setup Fields Layout ---
        JPanel ticketSetupPanel = new JPanel(new GridBagLayout());
        ticketSetupPanel.setOpaque(false);
        GridBagConstraints tgbc = new GridBagConstraints();
        tgbc.fill = GridBagConstraints.HORIZONTAL;
        tgbc.weightx = 1.0;
        tgbc.gridy = 0;

        // Title for ticket setup section
        JLabel lblSetupTitle = new JLabel("Formato y Ajustes del Ticket");
        lblSetupTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSetupTitle.setForeground(new Color(30, 41, 59));
        tgbc.gridx = 0;
        tgbc.gridwidth = 3;
        tgbc.insets = new Insets(0, 0, 15, 0);
        ticketSetupPanel.add(lblSetupTitle, tgbc);

        // Col 1: Prefijo y Ejemplo
        JPanel col1 = new JPanel(new GridBagLayout());
        col1.setOpaque(false);
        GridBagConstraints gbcC1 = new GridBagConstraints();
        gbcC1.fill = GridBagConstraints.HORIZONTAL;
        gbcC1.weightx = 1.0;
        gbcC1.gridx = 0;

        JLabel lblPrefix = new JLabel("Prefijo de Ticket");
        lblPrefix.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPrefix.setForeground(new Color(100, 116, 139));
        gbcC1.gridy = 0;
        gbcC1.insets = new Insets(0, 0, 4, 0);
        col1.add(lblPrefix, gbcC1);

        jTextReceiptPrefix = createStyledTextField();
        gbcC1.gridy = 1;
        gbcC1.insets = new Insets(0, 0, 10, 0);
        col1.add(jTextReceiptPrefix, gbcC1);

        JLabel lblExample = new JLabel("Ejemplo de Ticket");
        lblExample.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblExample.setForeground(new Color(100, 116, 139));
        gbcC1.gridy = 2;
        gbcC1.insets = new Insets(0, 0, 4, 0);
        col1.add(lblExample, gbcC1);

        jTicketExample = createStyledTextField();
        jTicketExample.setEditable(false);
        jTicketExample.setBackground(new Color(243, 244, 246));
        gbcC1.gridy = 3;
        gbcC1.insets = new Insets(0, 0, 0, 0);
        col1.add(jTicketExample, gbcC1);

        // Col 2: Dígitos de Ticket y Dígitos de Recogida
        JPanel col2 = new JPanel(new GridBagLayout());
        col2.setOpaque(false);
        GridBagConstraints gbcC2 = new GridBagConstraints();
        gbcC2.fill = GridBagConstraints.HORIZONTAL;
        gbcC2.weightx = 1.0;
        gbcC2.gridx = 0;

        JLabel lblDigits = new JLabel("Nº de Dígitos de Ticket");
        lblDigits.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDigits.setForeground(new Color(100, 116, 139));
        gbcC2.gridy = 0;
        gbcC2.insets = new Insets(0, 0, 4, 0);
        col2.add(lblDigits, gbcC2);

        jReceiptSize = new JSpinner();
        jReceiptSize.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jReceiptSize.setPreferredSize(new Dimension(80, 36));
        gbcC2.gridy = 1;
        gbcC2.insets = new Insets(0, 0, 10, 0);
        col2.add(jReceiptSize, gbcC2);

        JLabel lblPickupDigits = new JLabel("Nº de Dígitos de Recogida");
        lblPickupDigits.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPickupDigits.setForeground(new Color(100, 116, 139));
        gbcC2.gridy = 2;
        gbcC2.insets = new Insets(0, 0, 4, 0);
        col2.add(lblPickupDigits, gbcC2);

        jPickupSize = new JSpinner();
        jPickupSize.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jPickupSize.setPreferredSize(new Dimension(80, 36));
        gbcC2.gridy = 3;
        gbcC2.insets = new Insets(0, 0, 0, 0);
        col2.add(jPickupSize, gbcC2);

        // Col 3: Impresión y Reset
        JPanel col3 = new JPanel(new GridBagLayout());
        col3.setOpaque(false);
        GridBagConstraints gbcC3 = new GridBagConstraints();
        gbcC3.fill = GridBagConstraints.HORIZONTAL;
        gbcC3.weightx = 1.0;
        gbcC3.gridx = 0;

        m_jReceiptPrintOff = new JCheckBox("Desactivar Impresión de Ticket");
        m_jReceiptPrintOff.setFont(new Font("Segoe UI", Font.BOLD, 13));
        m_jReceiptPrintOff.setForeground(new Color(100, 116, 139));
        m_jReceiptPrintOff.setOpaque(false);
        gbcC3.gridy = 0;
        gbcC3.insets = new Insets(20, 0, 20, 0);
        col3.add(m_jReceiptPrintOff, gbcC3);

        jbtnReset = new JButton("Reiniciar Turnos");
        jbtnReset.setFont(new Font("Segoe UI", Font.BOLD, 13));
        jbtnReset.setBackground(new Color(239, 68, 68));
        jbtnReset.setForeground(Color.WHITE);
        jbtnReset.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(239, 68, 68).darker(), 1),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        jbtnReset.setFocusPainted(false);
        jbtnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbcC3.gridy = 1;
        gbcC3.insets = new Insets(0, 0, 0, 0);
        col3.add(jbtnReset, gbcC3);

        // Add 3 columns to ticketSetupPanel
        tgbc.gridy = 1;
        tgbc.gridwidth = 1;
        tgbc.weightx = 0.33;

        tgbc.gridx = 0;
        tgbc.insets = new Insets(0, 0, 0, 16);
        ticketSetupPanel.add(col1, tgbc);

        tgbc.gridx = 1;
        tgbc.insets = new Insets(0, 16, 0, 16);
        ticketSetupPanel.add(col2, tgbc);

        tgbc.gridx = 2;
        tgbc.insets = new Insets(0, 16, 0, 0);
        ticketSetupPanel.add(col3, tgbc);

        // Add ticketSetupPanel to card
        cardGbc.gridy = 4;
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.weighty = 0.0;
        cardGbc.insets = new Insets(0, 0, 20, 0);
        card.add(ticketSetupPanel, cardGbc);

        // --- Facturación electrónica ---
        JPanel fiscalPanel = new JPanel(new GridBagLayout());
        fiscalPanel.setOpaque(false);
        GridBagConstraints fiscalGbc = new GridBagConstraints();
        fiscalGbc.fill = GridBagConstraints.HORIZONTAL;
        fiscalGbc.weightx = 1.0;

        JLabel fiscalTitle = new JLabel("Facturación electrónica (Facturama)");
        fiscalTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        fiscalTitle.setForeground(new Color(30, 41, 59));
        fiscalGbc.gridx = 0;
        fiscalGbc.gridy = 0;
        fiscalGbc.gridwidth = 3;
        fiscalGbc.insets = new Insets(0, 0, 12, 0);
        fiscalPanel.add(fiscalTitle, fiscalGbc);

        txtFacturamaUser = createStyledTextField();
        txtFacturamaPassword = new JPasswordField();
        txtFacturamaPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtFacturamaPassword.setPreferredSize(new Dimension(180, 36));
        txtFacturamaPostalCode = createStyledTextField();
        comboFacturamaEnvironment = new JComboBox<>(new String[]{
            "Pruebas (no fiscal)", "Producción (facturas reales)"
        });
        comboFacturamaEnvironment.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboFacturamaEnvironment.setPreferredSize(new Dimension(190, 36));

        fiscalGbc.gridy = 1;
        fiscalGbc.gridwidth = 1;
        fiscalGbc.weightx = 0.25;
        fiscalGbc.gridx = 0;
        fiscalGbc.insets = new Insets(0, 0, 0, 16);
        fiscalPanel.add(createLabeledField("Usuario de API", txtFacturamaUser), fiscalGbc);
        fiscalGbc.gridx = 1;
        fiscalGbc.insets = new Insets(0, 16, 0, 16);
        fiscalPanel.add(createLabeledField("Contraseña de API", txtFacturamaPassword), fiscalGbc);
        fiscalGbc.gridx = 2;
        fiscalGbc.insets = new Insets(0, 16, 0, 16);
        fiscalPanel.add(createLabeledField("C.P. de expedición", txtFacturamaPostalCode), fiscalGbc);
        fiscalGbc.gridx = 3;
        fiscalGbc.insets = new Insets(0, 16, 0, 0);
        fiscalPanel.add(createLabeledField("Ambiente", comboFacturamaEnvironment), fiscalGbc);

        cardGbc.gridy = 5;
        cardGbc.weighty = 0.0;
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.insets = new Insets(0, 0, 20, 0);
        card.add(fiscalPanel, cardGbc);

        // --- Push everything up with a vertical spacer ---
        cardGbc.gridy = 6;
        cardGbc.weighty = 1.0;
        cardGbc.fill = GridBagConstraints.BOTH;
        card.add(new JPanel() {{ setOpaque(false); }}, cardGbc);

        scrollPane.setViewportView(centerWrapper);
        add(scrollPane, java.awt.BorderLayout.CENTER);
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(100, 116, 139));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
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
