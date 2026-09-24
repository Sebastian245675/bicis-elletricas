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
import com.openbravo.pos.core.spi.gui.LafInfo;
import com.openbravo.pos.core.spi.gui.DefaultLafProvider;
import com.openbravo.pos.core.spi.gui.FlatlafProvider;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.beans.JPasswordDialog;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import com.openbravo.pos.util.FileChooserEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author JG uniCenta
 */

public class JPanelConfigGeneral extends javax.swing.JPanel implements PanelConfig {

    private static final Logger LOGGER = Logger.getLogger(JPanelConfigGeneral.class.getName());

    private final DirtyManager dirty = new DirtyManager();
    private com.openbravo.pos.forms.AppView m_App; // Para acceder a AppView y cambiar contraseña

    private final static String LOCALE_DEFAULT_VALUE = "(Default)";
    private javax.swing.JComboBox jcboLocale;
    private javax.swing.JComboBox jcboInteger;
    private javax.swing.JComboBox jcboDouble;
    private javax.swing.JComboBox jcboCurrency;
    private javax.swing.JComboBox jcboPercent;
    private javax.swing.JComboBox jcboDate;
    private javax.swing.JComboBox jcboTime;
    private javax.swing.JComboBox jcboDatetime;

    /** Creates new form JPanelConfigGeneral */
    public JPanelConfigGeneral() {
        this(null);
    }

    /** Creates new form JPanelConfigGeneral with AppView */
    public JPanelConfigGeneral(com.openbravo.pos.forms.AppView app) {
        m_App = app;

        initComponents();

        InetAddress IP = null;
        try {
            IP = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.SEVERE, "Cannot get LocalHost from InetAddress", ex);
        }

        jtxtMachineHostname.getDocument().addDocumentListener(dirty);
        jtxtMachineDepartment.getDocument().addDocumentListener(dirty);
        jtxtMachineAddress = new javax.swing.JTextField();
        jtxtMachineAddress.getDocument().addDocumentListener(dirty);
        lblIP_Address.setText(IP.toString());
        jcboLAF.addActionListener(dirty);
        jcboMachineScreenmode.addActionListener(dirty);
        jcboTicketsBag.addActionListener(dirty);
        jchkHideInfo.addActionListener(dirty);
        jtxtStartupText.getDocument().addDocumentListener(dirty);
        jbtnText.addActionListener(new FileChooserEvent(jtxtStartupText));
        jtxtStartupLogo.getDocument().addDocumentListener(dirty);
        jbtnLogo.addActionListener(new FileChooserEvent(jtxtStartupLogo));
        jtxtStartupHTML.getDocument().addDocumentListener(dirty);
        jbtnHTML.addActionListener(new FileChooserEvent(jtxtStartupHTML));

        // jtxtStartupMedia.getDocument().addDocumentListener(dirty); // Coming later!
        // jbtnMedia.addActionListener(new FileChooserEvent(jtxtStartupHTML)); // Coming
        // later!

        // Installed skins
        new DefaultLafProvider()
                .getLafInfoList()
                .forEach(i -> jcboLAF.addItem(i));

        // FlatLaf - Flat Look and Feel
        new FlatlafProvider()
                .getLafInfoList()
                .forEach(i -> jcboLAF.addItem(i));

        jcboLAF.addActionListener((java.awt.event.ActionEvent evt) -> {
            LOGGER.info("Current LaF: " + UIManager.getLookAndFeel().getClass().getName());
        });

        jcboMachineScreenmode.addItem(new ComboItem("window", "Ventana"));
        jcboMachineScreenmode.addItem(new ComboItem("fullscreen", "Pantalla Completa"));

        jcboTicketsBag.addItem(new ComboItem("simple", "Simple"));
        jcboTicketsBag.addItem(new ComboItem("standard", "Estándar"));

        jtxtStartupLogo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateLogoPreview();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateLogoPreview();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateLogoPreview();
            }
        });

        initLocaleComponents();

        buildModernLayout();
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

        jtxtMachineHostname.setText(config.getProperty("machine.hostname"));
        jtxtMachineDepartment.setText(config.getProperty("machine.department"));
        jtxtMachineAddress.setText(config.getProperty("machine.address"));

        String lafclass = config.getProperty("swing.defaultlaf");
        jcboLAF.setSelectedItem(null);
        for (int i = 0; i < jcboLAF.getItemCount(); i++) {
            LafInfo lafinfo = (LafInfo) jcboLAF.getItemAt(i);
            if (lafinfo.getClassName().equals(lafclass)) {
                jcboLAF.setSelectedIndex(i);
                break;
            }
        }

        setSelectedComboValue(jcboMachineScreenmode, config.getProperty("machine.screenmode"));
        setSelectedComboValue(jcboTicketsBag, config.getProperty("machine.ticketsbag"));
        jchkHideInfo.setSelected(Boolean.parseBoolean(config.getProperty("till.hideinfo")));
        jtxtStartupLogo.setText(config.getProperty("start.logo"));
        jtxtStartupText.setText(config.getProperty("start.text"));
        jtxtStartupLogo.setText(config.getProperty("start.logo"));
        jtxtStartupHTML.setText(config.getProperty("start.html"));
        updateLogoPreview();

        // Cargar propiedades de Localización
        String slang = config.getProperty("user.language");
        String scountry = config.getProperty("user.country");
        String svariant = config.getProperty("user.variant");

        if (slang != null && !slang.equals("") && scountry != null && svariant != null) {
            java.util.Locale currentlocale = new java.util.Locale(slang, scountry, svariant);
            for (int i = 0; i < jcboLocale.getItemCount(); i++) {
                LocaleInfo l = (LocaleInfo) jcboLocale.getItemAt(i);
                if (currentlocale.equals(l.getLocale())) {
                    jcboLocale.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            jcboLocale.setSelectedIndex(0);
        }

        jcboInteger.setSelectedItem(writeWithDefault(config.getProperty("format.integer")));
        jcboDouble.setSelectedItem(writeWithDefault(config.getProperty("format.double")));
        jcboCurrency.setSelectedItem(writeWithDefault(config.getProperty("format.currency")));
        jcboPercent.setSelectedItem(writeWithDefault(config.getProperty("format.percent")));
        jcboDate.setSelectedItem(writeWithDefault(config.getProperty("format.date")));
        jcboTime.setSelectedItem(writeWithDefault(config.getProperty("format.time")));
        jcboDatetime.setSelectedItem(writeWithDefault(config.getProperty("format.datetime")));

        showLocaleHelp();

        dirty.setDirty(false);
    }

    /**
     *
     * @param config
     */
    @Override
    public void saveProperties(AppConfig config) {

        config.setProperty("machine.hostname", jtxtMachineHostname.getText());
        config.setProperty("machine.department", jtxtMachineDepartment.getText());
        config.setProperty("machine.address", jtxtMachineAddress.getText());

        LafInfo laf = (LafInfo) jcboLAF.getSelectedItem();
        config.setProperty("swing.defaultlaf", laf == null
                ? System.getProperty("swing.defaultlaf", "javax.swing.plaf.metal.MetalLookAndFeel")
                : laf.getClassName());

        config.setProperty("machine.screenmode", getSelectedComboValue(jcboMachineScreenmode));
        config.setProperty("machine.ticketsbag", getSelectedComboValue(jcboTicketsBag));
        config.setProperty("till.hideinfo", Boolean.toString(jchkHideInfo.isSelected()));
        config.setProperty("start.logo", jtxtStartupLogo.getText());
        config.setProperty("start.text", jtxtStartupText.getText());
        config.setProperty("start.html", jtxtStartupHTML.getText());

        // Guardar propiedades de Localización
        java.util.Locale l = ((LocaleInfo) jcboLocale.getSelectedItem()).getLocale();
        if (l == null) {
            config.setProperty("user.language", "");
            config.setProperty("user.country", "");
            config.setProperty("user.variant", "");
        } else {
            config.setProperty("user.language", l.getLanguage());
            config.setProperty("user.country", l.getCountry());
            config.setProperty("user.variant", l.getVariant());
        }

        config.setProperty("format.integer", readWithDefault(jcboInteger.getSelectedItem()));
        config.setProperty("format.double", readWithDefault(jcboDouble.getSelectedItem()));
        config.setProperty("format.currency", readWithDefault(jcboCurrency.getSelectedItem()));
        config.setProperty("format.percent", readWithDefault(jcboPercent.getSelectedItem()));
        config.setProperty("format.date", readWithDefault(jcboDate.getSelectedItem()));
        config.setProperty("format.time", readWithDefault(jcboTime.getSelectedItem()));
        config.setProperty("format.datetime", readWithDefault(jcboDatetime.getSelectedItem()));

        dirty.setDirty(false);
    }

    private String comboValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private void changeLAF() {
        LOGGER.info("Current LaF: " + UIManager.getLookAndFeel().getClass().getName());
        final LafInfo laf = (LafInfo) jcboLAF.getSelectedItem();
        if (laf != null && !laf.getClassName().equals(UIManager.getLookAndFeel().getClass().getName())) {
            // The selected look and feel is different from the current look and feel.
            SwingUtilities.invokeLater(() -> {
                try {
                    String lafname = laf.getClassName();
                    Object laf1 = Class.forName(lafname).getDeclaredConstructor().newInstance();
                    if (laf1 instanceof LookAndFeel) {
                        UIManager.setLookAndFeel((LookAndFeel) laf1);
                    }
                    // Re-apply style customizations after look and feel is dynamically changed
                    com.openbravo.pos.util.ModernLookAndFeel.aplicarEstiloModerno();

                    SwingUtilities.updateComponentTreeUI(JPanelConfigGeneral.this.getTopLevelAncestor());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                        | UnsupportedLookAndFeelException | NoSuchMethodException | SecurityException
                        | IllegalArgumentException | InvocationTargetException ex) {
                    LOGGER.log(Level.WARNING, "Cannot set Look and Feel", ex);
                }
            });
        }
        LOGGER.info("Change LaF: " + UIManager.getLookAndFeel().getClass().getName());
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel11 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jtxtMachineHostname = new javax.swing.JTextField();
        jcboLAF = new javax.swing.JComboBox();
        jcboMachineScreenmode = new javax.swing.JComboBox();
        jcboTicketsBag = new javax.swing.JComboBox();
        jchkHideInfo = new javax.swing.JCheckBox();
        jLabel18 = new javax.swing.JLabel();
        jtxtStartupLogo = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jtxtStartupText = new javax.swing.JTextField();
        jbtnLogo = new javax.swing.JButton();
        jbtnText = new javax.swing.JButton();
        jbtnTextClear = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jtxtMachineDepartment = new javax.swing.JTextField();
        lblIP_Address = new javax.swing.JLabel();
        webLabel1 = new javax.swing.JLabel();
        jLblURL = new javax.swing.JLabel();
        jtxtStartupHTML = new javax.swing.JTextField();
        jbtnHTML = new javax.swing.JButton();
        jbtnClearHTML = new javax.swing.JButton();
        previewButton = new javax.swing.JButton();
        jLabelPassword = new javax.swing.JLabel();
        jbtnChangePassword = new javax.swing.JButton();
        jbtnCheckUpdates = new javax.swing.JButton();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(800, 450));

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setOpaque(false);
        jPanel11.setPreferredSize(new java.awt.Dimension(750, 450));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.MachineName")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.looknfeel")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.MachineScreen")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("label.Ticketsbag")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtMachineHostname.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtMachineHostname.setToolTipText(AppLocal.getIntString("tooltip.config.general.terminal")); // NOI18N
        jtxtMachineHostname.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jtxtMachineHostname.setMinimumSize(new java.awt.Dimension(130, 25));
        jtxtMachineHostname.setPreferredSize(new java.awt.Dimension(200, 30));

        jcboLAF.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboLAF.setToolTipText(AppLocal.getIntString("tooltip.config.general.skin")); // NOI18N
        jcboLAF.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jcboLAF.setPreferredSize(new java.awt.Dimension(200, 30));
        jcboLAF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboLAFActionPerformed(evt);
            }
        });

        jcboMachineScreenmode.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboMachineScreenmode.setToolTipText(AppLocal.getIntString("tooltip.config.general.screen")); // NOI18N
        jcboMachineScreenmode.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jcboMachineScreenmode.setPreferredSize(new java.awt.Dimension(200, 30));

        jcboTicketsBag.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboTicketsBag.setToolTipText(AppLocal.getIntString("tooltip.config.general.tickets")); // NOI18N
        jcboTicketsBag.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jcboTicketsBag.setPreferredSize(new java.awt.Dimension(200, 30));

        jchkHideInfo.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkHideInfo.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jchkHideInfo.setText(bundle.getString("label.Infopanel")); // NOI18N
        jchkHideInfo.setToolTipText(AppLocal.getIntString("tooltip.config.general.footer")); // NOI18N
        jchkHideInfo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jchkHideInfo.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jchkHideInfo.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkHideInfo.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkHideInfo.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText(bundle.getString("label.startuplogo")); // NOI18N
        jLabel18.setMaximumSize(new java.awt.Dimension(0, 25));
        jLabel18.setMinimumSize(new java.awt.Dimension(0, 0));
        jLabel18.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtStartupLogo.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtStartupLogo.setToolTipText(AppLocal.getIntString("tooltip.config.general.logo")); // NOI18N
        jtxtStartupLogo.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtStartupLogo.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtStartupLogo.setPreferredSize(new java.awt.Dimension(400, 30));

        jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel19.setText(AppLocal.getIntString("label.startuptext")); // NOI18N
        jLabel19.setMaximumSize(new java.awt.Dimension(0, 25));
        jLabel19.setMinimumSize(new java.awt.Dimension(0, 0));
        jLabel19.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtStartupText.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtStartupText.setToolTipText(AppLocal.getIntString("tooltip.config.general.text")); // NOI18N
        jtxtStartupText.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtStartupText.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtStartupText.setPreferredSize(new java.awt.Dimension(400, 30));
        jtxtStartupText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jtxtStartupTextFocusGained(evt);
            }
        });
        jtxtStartupText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxtStartupTextActionPerformed(evt);
            }
        });

        jbtnLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileopen.png"))); // NOI18N
        jbtnLogo.setText("  ");
        jbtnLogo.setToolTipText(AppLocal.getIntString("tooltip.config.general.logo")); // NOI18N
        jbtnLogo.setMaximumSize(new java.awt.Dimension(64, 32));
        jbtnLogo.setMinimumSize(new java.awt.Dimension(64, 32));
        jbtnLogo.setPreferredSize(new java.awt.Dimension(80, 45));
        jbtnLogo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnLogoActionPerformed(evt);
            }
        });

        jbtnText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileopen.png"))); // NOI18N
        jbtnText.setText("  ");
        jbtnText.setToolTipText(AppLocal.getIntString("tooltip.config.general.text")); // NOI18N
        jbtnText.setMaximumSize(new java.awt.Dimension(64, 32));
        jbtnText.setMinimumSize(new java.awt.Dimension(64, 32));
        jbtnText.setPreferredSize(new java.awt.Dimension(80, 45));
        jbtnText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnTextActionPerformed(evt);
            }
        });

        jbtnTextClear.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jbtnTextClear.setForeground(new java.awt.Color(255, 0, 153));
        jbtnTextClear.setText("X");
        jbtnTextClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnTextClearActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.MachineDepartment")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtMachineDepartment.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtMachineDepartment.setToolTipText(AppLocal.getIntString("tooltip.config.general.dept")); // NOI18N
        jtxtMachineDepartment.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jtxtMachineDepartment.setMinimumSize(new java.awt.Dimension(130, 25));
        jtxtMachineDepartment.setPreferredSize(new java.awt.Dimension(200, 30));

        lblIP_Address.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblIP_Address.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIP_Address.setToolTipText(AppLocal.getIntString("tooltip.config.general.compip")); // NOI18N
        lblIP_Address.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lblIP_Address.setPreferredSize(new java.awt.Dimension(230, 30));

        webLabel1.setFont(new java.awt.Font("Arial", 1, 10)); // NOI18N
        webLabel1.setText(bundle.getString("label.nameIP")); // NOI18N
        webLabel1.setPreferredSize(new java.awt.Dimension(300, 30));

        jLblURL.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblURL.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/pay.png"))); // NOI18N
        jLblURL.setText(AppLocal.getIntString("label.URL")); // NOI18N
        jLblURL.setToolTipText(bundle.getString("tooltip.config.general.URL")); // NOI18N
        jLblURL.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jLblURL.setMaximumSize(new java.awt.Dimension(0, 25));
        jLblURL.setMinimumSize(new java.awt.Dimension(0, 0));
        jLblURL.setPreferredSize(new java.awt.Dimension(150, 30));
        jLblURL.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLblURLMouseClicked(evt);
            }
        });

        jtxtStartupHTML.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtStartupHTML.setToolTipText(AppLocal.getIntString("tooltip.config.general.text")); // NOI18N
        jtxtStartupHTML.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtStartupHTML.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtStartupHTML.setPreferredSize(new java.awt.Dimension(400, 30));
        jtxtStartupHTML.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jtxtStartupHTMLFocusGained(evt);
            }
        });
        jtxtStartupHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxtStartupHTMLActionPerformed(evt);
            }
        });

        jbtnHTML.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileopen.png"))); // NOI18N
        jbtnHTML.setText("  ");
        jbtnHTML.setToolTipText(AppLocal.getIntString("tooltip.config.general.text")); // NOI18N
        jbtnHTML.setMaximumSize(new java.awt.Dimension(64, 32));
        jbtnHTML.setMinimumSize(new java.awt.Dimension(64, 32));
        jbtnHTML.setPreferredSize(new java.awt.Dimension(80, 45));
        jbtnHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnHTMLActionPerformed(evt);
            }
        });

        jbtnClearHTML.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jbtnClearHTML.setForeground(new java.awt.Color(255, 0, 153));
        jbtnClearHTML.setText("X");
        jbtnClearHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnClearHTMLActionPerformed(evt);
            }
        });

        previewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewButtonActionPerformed(evt);
            }
        });

        jLabelPassword.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelPassword.setText(AppLocal.getIntString("Menu.ChangePassword")); // NOI18N
        jLabelPassword.setPreferredSize(new java.awt.Dimension(150, 30));

        jbtnChangePassword.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jbtnChangePassword
                .setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/password.png"))); // NOI18N
        jbtnChangePassword.setText(AppLocal.getIntString("Menu.ChangePassword")); // NOI18N
        jbtnChangePassword.setPreferredSize(new java.awt.Dimension(200, 35));
        jbtnChangePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnChangePasswordActionPerformed(evt);
            }
        });

        jbtnCheckUpdates.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jbtnCheckUpdates
                .setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/utilities.png"))); // NOI18N
        jbtnCheckUpdates.setText("Verificar Actualizaciones"); // NOI18N
        jbtnCheckUpdates.setPreferredSize(new java.awt.Dimension(200, 35));
        jbtnCheckUpdates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnCheckUpdatesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
                jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel11Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel11Layout.createSequentialGroup()
                                                .addGroup(jPanel11Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                false)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                jPanel11Layout.createSequentialGroup()
                                                                        .addComponent(jLabel1,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addComponent(jtxtMachineHostname,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel11Layout.createSequentialGroup()
                                                                .addComponent(jLabel6,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(jtxtMachineDepartment,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(webLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 132,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lblIP_Address, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        271, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel11Layout.createSequentialGroup()
                                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jcboLAF, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(previewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel11Layout.createSequentialGroup()
                                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jcboMachineScreenmode,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel11Layout.createSequentialGroup()
                                                .addGroup(jPanel11Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                false)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                jPanel11Layout.createSequentialGroup()
                                                                        .addComponent(jLabel18,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addComponent(jtxtStartupLogo,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                jPanel11Layout.createSequentialGroup()
                                                                        .addGroup(jPanel11Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(jLabel19,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(jLblURL,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addGroup(jPanel11Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(jchkHideInfo,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        287,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(jtxtStartupText,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(jtxtStartupHTML,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel11Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addComponent(jbtnHTML, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                1, Short.MAX_VALUE)
                                                        .addComponent(jbtnText, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                1, Short.MAX_VALUE)
                                                        .addComponent(jbtnLogo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(jPanel11Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addComponent(jbtnTextClear,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jbtnClearHTML,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGroup(jPanel11Layout.createSequentialGroup()
                                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jcboTicketsBag, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel11Layout.createSequentialGroup()
                                                .addComponent(jLabelPassword, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jbtnChangePassword,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel11Layout.createSequentialGroup()
                                                .addGap(160, 160, 160)
                                                .addComponent(jbtnCheckUpdates, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel11Layout.setVerticalGroup(
                jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel11Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(jPanel11Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jtxtMachineHostname,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(webLabel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(lblIP_Address, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtMachineDepartment, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboLAF, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(previewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboMachineScreenmode, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboTicketsBag, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtStartupLogo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jbtnLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtStartupText, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jbtnText, javax.swing.GroupLayout.PREFERRED_SIZE, 32,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jbtnTextClear, javax.swing.GroupLayout.PREFERRED_SIZE, 32,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jbtnClearHTML, javax.swing.GroupLayout.Alignment.TRAILING,
                                                javax.swing.GroupLayout.PREFERRED_SIZE, 33,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel11Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLblURL, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jtxtStartupHTML, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jbtnHTML, javax.swing.GroupLayout.PREFERRED_SIZE, 33,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addComponent(jchkHideInfo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabelPassword, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jbtnChangePassword, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jbtnCheckUpdates, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 50, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE));

        getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents

    private void jbtnClearHTMLActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jbtnClearHTMLActionPerformed
        jtxtStartupHTML.setText("");
    }// GEN-LAST:event_jbtnClearHTMLActionPerformed

    private void jbtnHTMLActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jbtnHTMLActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_jbtnHTMLActionPerformed

    private void jtxtStartupHTMLActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jtxtStartupHTMLActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_jtxtStartupHTMLActionPerformed

    private void jtxtStartupHTMLFocusGained(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_jtxtStartupHTMLFocusGained
        // TODO add your handling code here:
    }// GEN-LAST:event_jtxtStartupHTMLFocusGained

    private void jLblURLMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLblURLMouseClicked
        JOptionPane.showMessageDialog(this,
                AppLocal.getIntString("message.URL"),
                "URL",
                JOptionPane.INFORMATION_MESSAGE);
    }// GEN-LAST:event_jLblURLMouseClicked

    private void jbtnTextClearActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jbtnTextClearActionPerformed
        jtxtStartupText.setText("");
    }// GEN-LAST:event_jbtnTextClearActionPerformed

    private void jbtnTextActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jbtnTextActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_jbtnTextActionPerformed

    private void jbtnLogoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jbtnLogoActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_jbtnLogoActionPerformed

    private void jtxtStartupTextActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jtxtStartupTextActionPerformed

    }// GEN-LAST:event_jtxtStartupTextActionPerformed

    private void jtxtStartupTextFocusGained(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_jtxtStartupTextFocusGained

    }// GEN-LAST:event_jtxtStartupTextFocusGained

    private void jcboLAFActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jcboLAFActionPerformed

    }// GEN-LAST:event_jcboLAFActionPerformed

    private void previewButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_previewButtonActionPerformed

        changeLAF();
    }// GEN-LAST:event_previewButtonActionPerformed

    private void jbtnChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jbtnChangePasswordActionPerformed
        if (m_App == null) {
            JOptionPane.showMessageDialog(this,
                    AppLocal.getIntString("message.cannotchangepassword"),
                    AppLocal.getIntString("message.title"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            AppUser m_appuser = m_App.getAppUserView().getUser();
            DataLogicSystem m_dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");

            String sNewPassword = JPasswordDialog.changePassword(this, m_appuser.getPassword());
            if (sNewPassword != null) {
                m_dlSystem.execChangePassword(new Object[] { sNewPassword, m_appuser.getId() });
                m_appuser.setPassword(sNewPassword);
                JOptionPane.showMessageDialog(this,
                        "Contraseña cambiada exitosamente",
                        AppLocal.getIntString("message.title"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cambiar contraseña", e);
            JOptionPane.showMessageDialog(this,
                    AppLocal.getIntString("message.cannotchangepassword"),
                    AppLocal.getIntString("message.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_jbtnChangePasswordActionPerformed

    private void jbtnCheckUpdatesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jbtnCheckUpdatesActionPerformed
        if (m_App != null && m_App instanceof com.openbravo.pos.forms.JRootApp) {
            ((com.openbravo.pos.forms.JRootApp) m_App).checkForUpdatesManually();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se puede verificar actualizaciones en este momento.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_jbtnCheckUpdatesActionPerformed

    private void updateLogoPreview() {
        if (lblLogoPreview == null)
            return;
        String logoPath = jtxtStartupLogo.getText();
        if (logoPath == null || logoPath.trim().isEmpty()) {
            lblLogoPreview.setIcon(null);
            lblLogoPreview.setText("+");
            lblLogoPreview.setFont(new Font("Segoe UI", Font.PLAIN, 36));
            lblLogoPreview.setForeground(new Color(156, 163, 175)); // grey
        } else {
            File file = new File(logoPath);
            if (file.exists() && file.isFile()) {
                try {
                    ImageIcon originalIcon = new ImageIcon(logoPath);
                    Image img = originalIcon.getImage();
                    if (img != null) {
                        int width = 150;
                        int height = 85;
                        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        lblLogoPreview.setIcon(new ImageIcon(scaledImg));
                        lblLogoPreview.setText("");
                    } else {
                        lblLogoPreview.setIcon(null);
                        lblLogoPreview.setText("Err");
                        lblLogoPreview.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    }
                } catch (Exception ex) {
                    lblLogoPreview.setIcon(null);
                    lblLogoPreview.setText("Error");
                }
            } else {
                lblLogoPreview.setIcon(null);
                lblLogoPreview.setText("?");
                lblLogoPreview.setFont(new Font("Segoe UI", Font.BOLD, 24));
            }
        }
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        Border lineBorder = BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true);
        Border padding = BorderFactory.createEmptyBorder(20, 24, 20, 24);
        panel.setBorder(BorderFactory.createCompoundBorder(lineBorder, padding));
        return panel;
    }

    private void styleInputField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(new Color(55, 65, 81));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    private void setSelectedComboValue(JComboBox combo, String value) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof ComboItem) {
                if (((ComboItem) item).getValue().equals(value)) {
                    combo.setSelectedIndex(i);
                    return;
                }
            } else if (item != null && item.toString().equals(value)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedItem(null);
    }

    private String getSelectedComboValue(JComboBox combo) {
        Object item = combo.getSelectedItem();
        if (item instanceof ComboItem) {
            return ((ComboItem) item).getValue();
        }
        return item == null ? "" : item.toString();
    }

    private static class ComboItem {
        private final String value;
        private final String label;

        public ComboItem(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private void styleComboBox(JComboBox combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setForeground(new Color(55, 65, 81));
        combo.setBackground(Color.WHITE);
    }

    private void buildModernLayout() {
        this.removeAll();
        this.setLayout(new BorderLayout());
        this.setOpaque(false);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new GridBagLayout());
        headerPanel.setOpaque(false);
        GridBagConstraints gbcHeader = new GridBagConstraints();
        gbcHeader.fill = GridBagConstraints.HORIZONTAL;
        gbcHeader.weightx = 1.0;
        gbcHeader.gridx = 0;

        lblMainTitle = new JLabel("Configuración del Perfil de la Empresa");
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblMainTitle.setForeground(new Color(17, 24, 39));

        lblSubtitle = new JLabel("Gestione la información y configuración de su negocio");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(107, 114, 128));

        gbcHeader.gridy = 0;
        gbcHeader.insets = new Insets(10, 20, 2, 20);
        headerPanel.add(lblMainTitle, gbcHeader);

        gbcHeader.gridy = 1;
        gbcHeader.insets = new Insets(0, 20, 20, 20);
        headerPanel.add(lblSubtitle, gbcHeader);

        // Cards Container Panel
        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new GridBagLayout());
        cardsContainer.setOpaque(false);

        GridBagConstraints gbcCards = new GridBagConstraints();
        gbcCards.fill = GridBagConstraints.BOTH;
        gbcCards.weighty = 1.0;
        gbcCards.weightx = 0.5;
        gbcCards.gridy = 0;

        // Left Card
        gbcCards.gridx = 0;
        gbcCards.insets = new Insets(0, 20, 20, 10);
        JPanel leftCard = createCardPanel();
        cardsContainer.add(leftCard, gbcCards);

        // Right Card (Idioma, Formato y Acciones)
        gbcCards.gridx = 1;
        gbcCards.insets = new Insets(0, 10, 20, 20);
        JPanel rightCard = createCardPanel();
        cardsContainer.add(rightCard, gbcCards);

        // --- POPULATE LEFT CARD ---
        leftCard.setLayout(new GridBagLayout());
        GridBagConstraints gbcL = new GridBagConstraints();
        gbcL.fill = GridBagConstraints.HORIZONTAL;
        gbcL.weightx = 1.0;
        gbcL.gridx = 0;
        int rowL = 0;

        // Card Title & IP Address
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setOpaque(false);

        lblCard1Title = new JLabel("Información General");
        lblCard1Title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCard1Title.setForeground(new Color(17, 24, 39));
        leftHeader.add(lblCard1Title, BorderLayout.WEST);

        JPanel ipPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        ipPanel.setOpaque(false);
        JLabel lblLocIcon = new JLabel("ID Ubicación / IP: ");
        lblLocIcon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLocIcon.setForeground(new Color(107, 114, 128));
        ipPanel.add(lblLocIcon);
        lblIP_Address.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblIP_Address.setForeground(new Color(75, 85, 99));
        ipPanel.add(lblIP_Address);
        leftHeader.add(ipPanel, BorderLayout.EAST);

        gbcL.gridy = rowL++;
        gbcL.gridwidth = 2;
        gbcL.insets = new Insets(0, 0, 15, 0);
        leftCard.add(leftHeader, gbcL);

        // Logo Section
        JPanel logoSection = new JPanel(new GridBagLayout());
        logoSection.setOpaque(false);
        GridBagConstraints gbcLogo = new GridBagConstraints();

        lblLogoPreview = new JLabel("+");
        lblLogoPreview.setHorizontalAlignment(JLabel.CENTER);
        lblLogoPreview.setVerticalAlignment(JLabel.CENTER);
        lblLogoPreview.setOpaque(true);
        lblLogoPreview.setBackground(new Color(243, 244, 246));
        lblLogoPreview.setPreferredSize(new Dimension(150, 90));
        lblLogoPreview.setMinimumSize(new Dimension(150, 90));
        lblLogoPreview.setMaximumSize(new Dimension(150, 90));
        lblLogoPreview.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        lblLogoPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLogoPreview.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jbtnLogo.doClick();
            }
        });

        gbcLogo.gridx = 0;
        gbcLogo.gridy = 0;
        gbcLogo.gridheight = 2;
        gbcLogo.fill = GridBagConstraints.NONE;
        gbcLogo.anchor = GridBagConstraints.CENTER;
        gbcLogo.insets = new Insets(0, 0, 0, 15);
        logoSection.add(lblLogoPreview, gbcLogo);

        JPanel logoDetails = new JPanel(new GridBagLayout());
        logoDetails.setOpaque(false);
        GridBagConstraints gbcDetails = new GridBagConstraints();
        gbcDetails.fill = GridBagConstraints.HORIZONTAL;
        gbcDetails.weightx = 1.0;
        gbcDetails.gridx = 0;

        JLabel lblLogoTitle = new JLabel("Logo de la Empresa");
        lblLogoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLogoTitle.setForeground(new Color(17, 24, 39));
        gbcDetails.gridy = 0;
        gbcDetails.insets = new Insets(0, 0, 2, 0);
        logoDetails.add(lblLogoTitle, gbcDetails);

        JLabel lblLogoDesc = new JLabel("Tamaño propuesto: 350px * 180px.");
        lblLogoDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLogoDesc.setForeground(new Color(156, 163, 175));
        gbcDetails.gridy = 1;
        gbcDetails.insets = new Insets(0, 0, 8, 0);
        logoDetails.add(lblLogoDesc, gbcDetails);

        JPanel logoButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoButtons.setOpaque(false);

        jbtnLogo.setText("Subir");
        jbtnLogo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jbtnLogo.setBackground(Color.WHITE);
        jbtnLogo.setForeground(new Color(59, 130, 246));
        jbtnLogo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        jbtnLogo.setFocusPainted(false);
        jbtnLogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoButtons.add(jbtnLogo);

        JButton jbtnRemoveLogo = new JButton("Quitar");
        jbtnRemoveLogo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jbtnRemoveLogo.setBackground(Color.WHITE);
        jbtnRemoveLogo.setForeground(new Color(107, 114, 128));
        jbtnRemoveLogo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        jbtnRemoveLogo.setFocusPainted(false);
        jbtnRemoveLogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jbtnRemoveLogo.addActionListener(e -> {
            jtxtStartupLogo.setText("");
            dirty.setDirty(true);
        });
        logoButtons.add(jbtnRemoveLogo);

        gbcDetails.gridy = 2;
        logoDetails.add(logoButtons, gbcDetails);

        gbcLogo.gridx = 1;
        gbcLogo.gridy = 0;
        gbcLogo.gridheight = 1;
        gbcLogo.fill = GridBagConstraints.HORIZONTAL;
        gbcLogo.weightx = 1.0;
        gbcLogo.anchor = GridBagConstraints.WEST;
        gbcLogo.insets = new Insets(0, 0, 0, 0);
        logoSection.add(logoDetails, gbcLogo);

        gbcL.gridy = rowL++;
        gbcL.gridwidth = 2;
        gbcL.insets = new Insets(0, 0, 10, 0);
        leftCard.add(logoSection, gbcL);

        // Logo Path read-only field
        jtxtStartupLogo.setEditable(false);
        jtxtStartupLogo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        jtxtStartupLogo.setForeground(new Color(107, 114, 128));
        jtxtStartupLogo.setBackground(new Color(243, 244, 246));
        jtxtStartupLogo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        gbcL.gridy = rowL++;
        gbcL.gridwidth = 2;
        gbcL.insets = new Insets(0, 0, 15, 0);
        leftCard.add(jtxtStartupLogo, gbcL);

        // Hostname (Nombre Comercial)
        styleInputField(jtxtMachineHostname);
        JLabel lblHostname = new JLabel("Nombre Comercial (Terminal)");
        lblHostname.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHostname.setForeground(new Color(75, 85, 99));
        gbcL.gridy = rowL++;
        gbcL.insets = new Insets(5, 0, 4, 0);
        leftCard.add(lblHostname, gbcL);
        gbcL.gridy = rowL++;
        gbcL.insets = new Insets(0, 0, 12, 0);
        leftCard.add(jtxtMachineHostname, gbcL);

        // Department (Razón Social)
        styleInputField(jtxtMachineDepartment);
        JLabel lblDepartment = new JLabel("Razón Social (Departamento)");
        lblDepartment.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDepartment.setForeground(new Color(75, 85, 99));
        gbcL.gridy = rowL++;
        gbcL.insets = new Insets(5, 0, 4, 0);
        leftCard.add(lblDepartment, gbcL);
        gbcL.gridy = rowL++;
        gbcL.insets = new Insets(0, 0, 12, 0);
        leftCard.add(jtxtMachineDepartment, gbcL);

        // Address (Dirección de la Empresa)
        styleInputField(jtxtMachineAddress);
        JLabel lblAddress = new JLabel("Dirección de la Empresa");
        lblAddress.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAddress.setForeground(new Color(75, 85, 99));
        gbcL.gridy = rowL++;
        gbcL.insets = new Insets(5, 0, 4, 0);
        leftCard.add(lblAddress, gbcL);
        gbcL.gridy = rowL++;
        gbcL.insets = new Insets(0, 0, 12, 0);
        leftCard.add(jtxtMachineAddress, gbcL);

        // Push everything up
        gbcL.gridy = rowL++;
        gbcL.gridwidth = 2;
        gbcL.weighty = 1.0;
        gbcL.fill = GridBagConstraints.BOTH;
        leftCard.add(Box.createGlue(), gbcL);

        // --- POPULATE RIGHT CARD (Idioma, Formato y Acciones) ---
        rightCard.setLayout(new GridBagLayout());
        GridBagConstraints gbcR = new GridBagConstraints();
        gbcR.fill = GridBagConstraints.HORIZONTAL;
        gbcR.weightx = 1.0;
        gbcR.gridx = 0;
        int rowR = 0;

        JLabel lblCardRTitle = new JLabel("Idioma y Formato");
        lblCardRTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCardRTitle.setForeground(new Color(17, 24, 39));
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(0, 0, 15, 0);
        rightCard.add(lblCardRTitle, gbcR);

        // 1. Idioma / Región (Locale)
        styleComboBox(jcboLocale);
        JLabel lblLocale = new JLabel("Localización (Idioma / Región)");
        lblLocale.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLocale.setForeground(new Color(75, 85, 99));
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(5, 0, 4, 0);
        rightCard.add(lblLocale, gbcR);
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(0, 0, 12, 0);
        rightCard.add(jcboLocale, gbcR);

        // 2. Currency
        styleComboBox(jcboCurrency);
        JLabel lblCurrency = new JLabel("Formato de Moneda");
        lblCurrency.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCurrency.setForeground(new Color(75, 85, 99));
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(5, 0, 4, 0);
        rightCard.add(lblCurrency, gbcR);
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(0, 0, 12, 0);
        rightCard.add(jcboCurrency, gbcR);

        // 3. Date
        styleComboBox(jcboDate);
        JLabel lblDate = new JLabel("Formato de Fecha");
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDate.setForeground(new Color(75, 85, 99));
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(5, 0, 4, 0);
        rightCard.add(lblDate, gbcR);
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(0, 0, 12, 0);
        rightCard.add(jcboDate, gbcR);

        // 4. DateTime
        styleComboBox(jcboDatetime);
        JLabel lblDateTime = new JLabel("Formato de Fecha y Hora");
        lblDateTime.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDateTime.setForeground(new Color(75, 85, 99));
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(5, 0, 4, 0);
        rightCard.add(lblDateTime, gbcR);
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(0, 0, 15, 0);
        rightCard.add(jcboDatetime, gbcR);

        // Divider
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(226, 232, 240));
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(10, 0, 15, 0);
        rightCard.add(separator, gbcR);

        // Look and feel selector
        styleComboBox(jcboLAF);
        previewButton.setText("Vista Previa");
        previewButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        previewButton.setBackground(Color.WHITE);
        previewButton.setForeground(new Color(75, 85, 99));
        previewButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        previewButton.setFocusPainted(false);
        previewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel lafPanel = new JPanel(new BorderLayout(8, 0));
        lafPanel.setOpaque(false);
        lafPanel.add(jcboLAF, BorderLayout.CENTER);
        lafPanel.add(previewButton, BorderLayout.EAST);

        JLabel lblLAF = new JLabel("Tema Visual (Apariencia)");
        lblLAF.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLAF.setForeground(new Color(75, 85, 99));
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(5, 0, 4, 0);
        rightCard.add(lblLAF, gbcR);
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(0, 0, 12, 0);
        rightCard.add(lafPanel, gbcR);

        // Screen mode & Tickets bag
        styleComboBox(jcboMachineScreenmode);
        styleComboBox(jcboTicketsBag);

        JLabel lblScreen = new JLabel("Modo de Pantalla");
        lblScreen.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblScreen.setForeground(new Color(75, 85, 99));

        JLabel lblBag = new JLabel("Venta de Tickets (Bolsa)");
        lblBag.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBag.setForeground(new Color(75, 85, 99));

        JPanel modeBagPanel = new JPanel(new GridBagLayout());
        modeBagPanel.setOpaque(false);
        GridBagConstraints gbcMB = new GridBagConstraints();
        gbcMB.fill = GridBagConstraints.HORIZONTAL;
        gbcMB.weighty = 1.0;
        gbcMB.weightx = 0.5;
        gbcMB.gridy = 0;

        gbcMB.gridx = 0;
        gbcMB.insets = new Insets(0, 0, 0, 6);
        modeBagPanel.add(jcboMachineScreenmode, gbcMB);

        gbcMB.gridx = 1;
        gbcMB.insets = new Insets(0, 6, 0, 0);
        modeBagPanel.add(jcboTicketsBag, gbcMB);

        JPanel modeBagLabels = new JPanel(new GridBagLayout());
        modeBagLabels.setOpaque(false);
        GridBagConstraints gbcMBL = new GridBagConstraints();
        gbcMBL.fill = GridBagConstraints.HORIZONTAL;
        gbcMBL.weighty = 1.0;
        gbcMBL.weightx = 0.5;
        gbcMBL.gridy = 0;

        gbcMBL.gridx = 0;
        gbcMBL.insets = new Insets(0, 0, 0, 6);
        modeBagLabels.add(lblScreen, gbcMBL);

        gbcMBL.gridx = 1;
        gbcMBL.insets = new Insets(0, 6, 0, 0);
        modeBagLabels.add(lblBag, gbcMBL);

        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(5, 0, 4, 0);
        rightCard.add(modeBagLabels, gbcR);
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(0, 0, 12, 0);
        rightCard.add(modeBagPanel, gbcR);

        // Hide Info checkbox
        jchkHideInfo.setText("Mostrar Panel de Información Inferior");
        jchkHideInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        jchkHideInfo.setForeground(new Color(75, 85, 99));
        jchkHideInfo.setOpaque(false);
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(5, 0, 15, 0);
        rightCard.add(jchkHideInfo, gbcR);

        // Divider 2
        JSeparator separator2 = new JSeparator();
        separator2.setForeground(new Color(226, 232, 240));
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(10, 0, 15, 0);
        rightCard.add(separator2, gbcR);

        // Security Title
        JLabel lblSecTitle = new JLabel("Acciones del Sistema");
        lblSecTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSecTitle.setForeground(new Color(17, 24, 39));
        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(0, 0, 12, 0);
        rightCard.add(lblSecTitle, gbcR);

        // Change password & check updates
        jbtnChangePassword.setText("Cambiar Contraseña");
        jbtnChangePassword.setFont(new Font("Segoe UI", Font.BOLD, 14));
        jbtnChangePassword.setBackground(new Color(51, 98, 140));
        jbtnChangePassword.setForeground(Color.WHITE);
        jbtnChangePassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 98, 140).darker(), 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        jbtnChangePassword.setFocusPainted(false);
        jbtnChangePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));

        jbtnCheckUpdates.setText("Verificar Actualizaciones");
        jbtnCheckUpdates.setFont(new Font("Segoe UI", Font.BOLD, 14));
        jbtnCheckUpdates.setBackground(new Color(75, 85, 99));
        jbtnCheckUpdates.setForeground(Color.WHITE);
        jbtnCheckUpdates.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(75, 85, 99).darker(), 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        jbtnCheckUpdates.setFocusPainted(false);
        jbtnCheckUpdates.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(0, 0, 12, 0);
        rightCard.add(jbtnChangePassword, gbcR);

        gbcR.gridy = rowR++;
        gbcR.insets = new Insets(0, 0, 12, 0);
        rightCard.add(jbtnCheckUpdates, gbcR);

        // Push everything up
        gbcR.gridy = rowR++;
        gbcR.weighty = 1.0;
        gbcR.fill = GridBagConstraints.BOTH;
        rightCard.add(Box.createGlue(), gbcR);

        // Add to main panel (wrapped in a JScrollPane)
        this.add(headerPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollPane, BorderLayout.CENTER);

        updateLogoPreview();
    }

    private void initLocaleComponents() {
        jcboLocale = new javax.swing.JComboBox();
        jcboInteger = new javax.swing.JComboBox();
        jcboDouble = new javax.swing.JComboBox();
        jcboCurrency = new javax.swing.JComboBox();
        jcboPercent = new javax.swing.JComboBox();
        jcboDate = new javax.swing.JComboBox();
        jcboTime = new javax.swing.JComboBox();
        jcboDatetime = new javax.swing.JComboBox();

        jcboLocale.addActionListener(dirty);
        jcboInteger.addActionListener(dirty);
        jcboDouble.addActionListener(dirty);
        jcboCurrency.addActionListener(dirty);
        jcboPercent.addActionListener(dirty);
        jcboDate.addActionListener(dirty);
        jcboTime.addActionListener(dirty);
        jcboDatetime.addActionListener(dirty);

        java.util.List<java.util.Locale> availablelocales = new java.util.ArrayList<>();
        availablelocales.addAll(java.util.Arrays.asList(java.util.Locale.getAvailableLocales()));

        java.util.Collections.sort(availablelocales, new java.util.Comparator<java.util.Locale>() {
            @Override
            public int compare(java.util.Locale o1, java.util.Locale o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });

        for (java.util.Locale l : availablelocales) {
            jcboLocale.addItem(new LocaleInfo(l));
        }

        jcboInteger.addItem(LOCALE_DEFAULT_VALUE);
        jcboInteger.addItem("#0");
        jcboInteger.addItem("#,##0");

        jcboDouble.addItem(LOCALE_DEFAULT_VALUE);
        jcboDouble.addItem("#0.0");
        jcboDouble.addItem("#,##0.#");

        jcboCurrency.addItem(LOCALE_DEFAULT_VALUE);
        jcboCurrency.addItem("\u00A4 #0.00");
        jcboCurrency.addItem("'$' #,##0.00");
        jcboCurrency.addItem("#00 '$'");
        jcboCurrency.addItem("#,##0'$'");

        jcboPercent.addItem(LOCALE_DEFAULT_VALUE);
        jcboPercent.addItem("#,##0.##%");

        jcboDate.addItem(LOCALE_DEFAULT_VALUE);

        jcboTime.addItem(LOCALE_DEFAULT_VALUE);

        jcboDatetime.addItem(LOCALE_DEFAULT_VALUE);

        // Action Listener setup for tooltips
        java.awt.event.ActionListener tooltipListener = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showLocaleHelp();
            }
        };
        jcboLocale.addActionListener(tooltipListener);
        jcboInteger.addActionListener(tooltipListener);
        jcboDouble.addActionListener(tooltipListener);
        jcboCurrency.addActionListener(tooltipListener);
        jcboPercent.addActionListener(tooltipListener);
        jcboDate.addActionListener(tooltipListener);
        jcboTime.addActionListener(tooltipListener);
        jcboDatetime.addActionListener(tooltipListener);
    }

    private void showLocaleHelp() {
        if (jcboLocale == null || jcboLocale.getSelectedItem() == null) {
            return;
        }
        // Set Current Locale to What is selected;
        java.util.Locale.setDefault(((LocaleInfo) jcboLocale.getSelectedItem()).getLocale());

        // Set Format/Pattern for: Number, Date, Currency
        com.openbravo.format.Formats.setIntegerPattern(readWithDefault(jcboInteger.getSelectedItem()));
        com.openbravo.format.Formats.setDoublePattern(readWithDefault(jcboDouble.getSelectedItem()));
        com.openbravo.format.Formats.setCurrencyPattern(readWithDefault(jcboCurrency.getSelectedItem()));
        com.openbravo.format.Formats.setPercentPattern(readWithDefault(jcboPercent.getSelectedItem()));
        com.openbravo.format.Formats.setDatePattern(readWithDefault(jcboDate.getSelectedItem()));
        com.openbravo.format.Formats.setTimePattern(readWithDefault(jcboTime.getSelectedItem()));
        com.openbravo.format.Formats.setDateTimePattern(readWithDefault(jcboDatetime.getSelectedItem()));

        jcboLocale.setToolTipText("<html>IETF BCP 47 Tag: " + java.util.Locale.getDefault().toLanguageTag());
        jcboInteger.setToolTipText("<html>123 formated: " + com.openbravo.format.Formats.INT.formatValue(123));
        jcboDouble.setToolTipText("<html>123.45 formated: " + com.openbravo.format.Formats.DOUBLE.formatValue(123.45));
        jcboCurrency
                .setToolTipText("<html>123.45 formated: " + com.openbravo.format.Formats.CURRENCY.formatValue(123.45));
        jcboPercent.setToolTipText("<html>0.23 formated: " + com.openbravo.format.Formats.PERCENT.formatValue(0.23));
        jcboDate.setToolTipText(
                "<html>Date formated: " + com.openbravo.format.Formats.DATE.formatValue(new java.util.Date()));
        jcboTime.setToolTipText(
                "<html>Time formated: " + com.openbravo.format.Formats.TIME.formatValue(new java.util.Date()));
        jcboDatetime.setToolTipText(
                "<html>DateTime formated: " + com.openbravo.format.Formats.TIMESTAMP.formatValue(new java.util.Date()));
    }

    private String readWithDefault(Object value) {
        if (LOCALE_DEFAULT_VALUE.equals(value)) {
            return "";
        } else {
            return value == null ? "" : value.toString();
        }
    }

    private Object writeWithDefault(String value) {
        if (value == null || value.equals("") || value.equals(LOCALE_DEFAULT_VALUE)) {
            return LOCALE_DEFAULT_VALUE;
        } else {
            return value;
        }
    }

    private static class LocaleInfo {
        private final java.util.Locale locale;

        public LocaleInfo(java.util.Locale locale) {
            this.locale = locale;
        }

        public java.util.Locale getLocale() {
            return (locale == null) ? java.util.Locale.ROOT : locale;
        }

        @Override
        public String toString() {
            return (locale == null || locale == java.util.Locale.ROOT)
                    ? "(System default)"
                    : locale.getDisplayName();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblLogoPreview;
    private javax.swing.JLabel lblCard1Title;
    private javax.swing.JLabel lblCard2Title;
    private javax.swing.JLabel lblMainTitle;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelPassword;
    private javax.swing.JLabel jLblURL;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JButton jbtnClearHTML;
    private javax.swing.JButton jbtnHTML;
    private javax.swing.JButton jbtnLogo;
    private javax.swing.JButton jbtnChangePassword;
    private javax.swing.JButton jbtnCheckUpdates;
    private javax.swing.JButton jbtnText;
    private javax.swing.JButton jbtnTextClear;
    private javax.swing.JComboBox jcboLAF;
    private javax.swing.JComboBox jcboMachineScreenmode;
    private javax.swing.JComboBox jcboTicketsBag;
    private javax.swing.JCheckBox jchkHideInfo;
    private javax.swing.JTextField jtxtMachineDepartment;
    private javax.swing.JTextField jtxtMachineHostname;
    private javax.swing.JTextField jtxtMachineAddress;
    private javax.swing.JTextField jtxtStartupHTML;
    private javax.swing.JTextField jtxtStartupLogo;
    private javax.swing.JTextField jtxtStartupText;
    private javax.swing.JLabel lblIP_Address;
    private javax.swing.JButton previewButton;
    private javax.swing.JLabel webLabel1;
    // End of variables declaration//GEN-END:variables
}
