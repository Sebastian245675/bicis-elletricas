package com.openbravo.pos.config;

import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.util.EmailService;
import com.openbravo.pos.util.EmailScheduler;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class JPanelConfigEmailReports extends JPanel implements PanelConfig {

    private final DirtyManager dirty = new DirtyManager();

    private JTextField jtxtEmail;
    private JTextField jtxtSMTPServer;
    private JTextField jtxtSMTPPort;
    private JTextField jtxtSMTPUser;
    private JPasswordField jtxtSMTPPassword;
    private JCheckBox jchkSMTPSSL;
    private JButton jbtnPresetGmail;
    private JButton jbtnTestEmail;

    private JCheckBox jchkAlertLowStock;
    private JCheckBox jchkAlertOverdueDebts;

    private JCheckBox jchkReportsEnabled;
    private JComboBox<String> jcmbFrequency;
    private JComboBox<String> jcmbDayOfWeek;
    private JCheckBox jchkReportClosedPos;
    private JCheckBox jchkReportInventory;
    private JCheckBox jchkReportDebtors;
    private JCheckBox jchkReportCategorySales;
    private JCheckBox jchkReportCustomers;
    private JCheckBox jchkReportProductProfit;
    private JCheckBox jchkReportCashFlow;
    private JCheckBox jchkReportSaleTaxes;
    private JCheckBox jchkReportUserSales;
    private JCheckBox jchkReportUserVoids;
    private JCheckBox jchkReportInventoryDiff;
    private JCheckBox jchkReportSuppliers;
    private JCheckBox jchkReportConsolidated;
    private JButton jbtnSendNow;

    private AppConfig currentConfig;
    private AppView appView;

    public JPanelConfigEmailReports(AppView appView) {
        this.appView = appView;
        initComponents();
        registerListeners();
    }

    private void initComponents() {
        setLayout(new java.awt.BorderLayout());
        setOpaque(true);
        setBackground(new Color(248, 250, 252)); // Slate 50

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        // Rounded Card container
        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(1024, 650));
        card.setMinimumSize(new Dimension(850, 650));

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.weightx = 1.0;
        cardGbc.insets = new Insets(0, 0, 16, 0);

        // Header Section
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);

        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.gridx = 0;
        hgbc.gridy = 0;
        hgbc.gridheight = 2;
        hgbc.anchor = GridBagConstraints.WEST;
        hgbc.insets = new Insets(0, 0, 0, 20);
        headerPanel.add(new MailIconPanel(), hgbc);

        hgbc.gridx = 1;
        hgbc.gridheight = 1;
        hgbc.weightx = 1.0;
        hgbc.fill = GridBagConstraints.HORIZONTAL;
        hgbc.anchor = GridBagConstraints.SOUTHWEST;
        hgbc.insets = new Insets(0, 0, 4, 0);

        JLabel titleLabel = new JLabel("Configuración de Correo y Reportes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 41, 59)); // Slate 800
        headerPanel.add(titleLabel, hgbc);

        hgbc.gridy = 1;
        hgbc.anchor = GridBagConstraints.NORTHWEST;
        hgbc.insets = new Insets(4, 0, 0, 0);

        JLabel subtitleLabel = new JLabel("Configura tus credenciales de Gmail, activa alertas críticas y programa reportes automatizados.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 116, 139)); // Slate 500
        headerPanel.add(subtitleLabel, hgbc);

        card.add(headerPanel, cardGbc);

        // Separator
        cardGbc.gridy = 1;
        cardGbc.insets = new Insets(0, 0, 16, 0);
        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(1, 1));
        separator.setBackground(new Color(226, 232, 240)); // Slate 200
        card.add(separator, cardGbc);

        // Form Fields Layout
        JPanel formFieldsPanel = new JPanel(new GridBagLayout());
        formFieldsPanel.setOpaque(false);

        jtxtEmail = createStyledTextField();
        jtxtSMTPServer = createStyledTextField();
        jtxtSMTPPort = createStyledTextField();
        jtxtSMTPUser = createStyledTextField();
        jtxtSMTPPassword = createStyledPasswordField();
        jchkSMTPSSL = createStyledCheckBox("Conexión Segura SSL/TLS");

        jchkAlertLowStock = createStyledCheckBox("Notificar Stock Bajo");
        jchkAlertOverdueDebts = createStyledCheckBox("Notificar Cuotas/Apartados Vencidos");

        jchkReportsEnabled = createStyledCheckBox("Activar Reportes Automáticos");
        jcmbFrequency = new JComboBox<>(new String[] {"Diario", "Semanal", "Mensual"});
        jcmbFrequency.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jcmbFrequency.setPreferredSize(new Dimension(140, 36));

        jcmbDayOfWeek = new JComboBox<>(new String[] {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"});
        jcmbDayOfWeek.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jcmbDayOfWeek.setPreferredSize(new Dimension(140, 36));

        jchkReportClosedPos = createStyledCheckBox("Cierre de Caja (PDF)");
        jchkReportInventory = createStyledCheckBox("Inventario (PDF)");
        jchkReportDebtors = createStyledCheckBox("Clientes Deudores (PDF)");
        jchkReportCategorySales = createStyledCheckBox("Ventas por Departamento (PDF)");
        jchkReportCustomers = createStyledCheckBox("Catálogo de Clientes (PDF)");
        jchkReportProductProfit = createStyledCheckBox("Rentabilidad por Producto (PDF)");
        jchkReportCashFlow = createStyledCheckBox("Flujo de Caja (PDF)");
        jchkReportSaleTaxes = createStyledCheckBox("Impuestos sobre Ventas (PDF)");
        jchkReportUserSales = createStyledCheckBox("Ventas por Cajero (PDF)");
        jchkReportUserVoids = createStyledCheckBox("Ventas Anuladas (PDF)");
        jchkReportInventoryDiff = createStyledCheckBox("Diferencias de Inventario (PDF)");
        jchkReportSuppliers = createStyledCheckBox("Proveedores Acreedores (PDF)");
        jchkReportConsolidated = createStyledCheckBox("Consolidado Resumido (HTML)");

        // --- Column 1: SMTP Config ---
        JPanel leftColPanel = new JPanel(new GridBagLayout());
        leftColPanel.setOpaque(false);
        GridBagConstraints lcGbc = new GridBagConstraints();
        lcGbc.fill = GridBagConstraints.HORIZONTAL;
        lcGbc.weightx = 1.0;
        lcGbc.gridx = 0;

        lcGbc.gridy = 0;
        lcGbc.insets = new Insets(0, 0, 10, 0);
        leftColPanel.add(createFieldGroup("Correo Destinatario (Admin)", jtxtEmail), lcGbc);

        lcGbc.gridy = 1;
        lcGbc.insets = new Insets(6, 0, 6, 0);
        JLabel smtpTitle = new JLabel("Configuración de Servidor de Correo");
        smtpTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        smtpTitle.setForeground(new Color(79, 70, 229)); // Indigo 600
        leftColPanel.add(smtpTitle, lcGbc);

        // Preset Gmail button
        lcGbc.gridy = 2;
        lcGbc.insets = new Insets(0, 0, 10, 0);
        jbtnPresetGmail = new JButton("Configurar Gmail");
        styleButtonSolid(jbtnPresetGmail, new Color(219, 68, 85)); // Gmail redish
        jbtnPresetGmail.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jtxtSMTPServer.setText("smtp.gmail.com");
                jtxtSMTPPort.setText("587");
                jchkSMTPSSL.setSelected(true);
                dirty.setDirty(true);
            }
        });
        leftColPanel.add(jbtnPresetGmail, lcGbc);

        JPanel serverPortRow = new JPanel(new GridBagLayout());
        serverPortRow.setOpaque(false);
        GridBagConstraints spGbc = new GridBagConstraints();
        spGbc.fill = GridBagConstraints.HORIZONTAL;
        spGbc.gridy = 0;
        spGbc.gridx = 0;
        spGbc.weightx = 0.7;
        spGbc.insets = new Insets(0, 0, 0, 8);
        serverPortRow.add(createFieldGroup("Servidor SMTP", jtxtSMTPServer), spGbc);

        spGbc.gridx = 1;
        spGbc.weightx = 0.3;
        spGbc.insets = new Insets(0, 0, 0, 0);
        serverPortRow.add(createFieldGroup("Puerto", jtxtSMTPPort), spGbc);

        lcGbc.gridy = 3;
        lcGbc.insets = new Insets(0, 0, 10, 0);
        leftColPanel.add(serverPortRow, lcGbc);

        JPanel userPassRow = new JPanel(new GridBagLayout());
        userPassRow.setOpaque(false);
        GridBagConstraints upGbc = new GridBagConstraints();
        upGbc.fill = GridBagConstraints.HORIZONTAL;
        upGbc.gridy = 0;
        upGbc.gridx = 0;
        upGbc.weightx = 0.5;
        upGbc.insets = new Insets(0, 0, 0, 8);
        userPassRow.add(createFieldGroup("Usuario / Correo", jtxtSMTPUser), upGbc);

        upGbc.gridx = 1;
        upGbc.weightx = 0.5;
        upGbc.insets = new Insets(0, 0, 0, 0);
        userPassRow.add(createFieldGroup("Contraseña de Aplicación", jtxtSMTPPassword), upGbc);

        lcGbc.gridy = 4;
        lcGbc.insets = new Insets(0, 0, 10, 0);
        leftColPanel.add(userPassRow, lcGbc);

        lcGbc.gridy = 5;
        lcGbc.insets = new Insets(0, 0, 12, 0);
        leftColPanel.add(jchkSMTPSSL, lcGbc);

        lcGbc.gridy = 6;
        lcGbc.insets = new Insets(0, 0, 0, 0);
        jbtnTestEmail = new JButton("Enviar Correo de Prueba");
        styleButtonOutline(jbtnTestEmail);
        jbtnTestEmail.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendTestEmailAction();
            }
        });
        leftColPanel.add(jbtnTestEmail, lcGbc);

        // Push everything to the top
        lcGbc.gridy = 7;
        lcGbc.weighty = 1.0;
        leftColPanel.add(Box.createVerticalGlue(), lcGbc);

        // --- Column 2: Alerts and Reports ---
        JPanel rightColPanel = new JPanel(new GridBagLayout());
        rightColPanel.setOpaque(false);
        GridBagConstraints rcGbc = new GridBagConstraints();
        rcGbc.fill = GridBagConstraints.HORIZONTAL;
        rcGbc.weightx = 1.0;
        rcGbc.gridx = 0;

        rcGbc.gridy = 0;
        rcGbc.insets = new Insets(0, 0, 4, 0);
        JLabel alertsTitle = new JLabel("Alertas Automatizadas");
        alertsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        alertsTitle.setForeground(new Color(79, 70, 229)); // Indigo 600
        rightColPanel.add(alertsTitle, rcGbc);

        rcGbc.gridy = 1;
        rcGbc.insets = new Insets(0, 0, 4, 0);
        rightColPanel.add(jchkAlertLowStock, rcGbc);

        rcGbc.gridy = 2;
        rcGbc.insets = new Insets(0, 0, 10, 0);
        rightColPanel.add(jchkAlertOverdueDebts, rcGbc);

        rcGbc.gridy = 3;
        rcGbc.insets = new Insets(0, 0, 4, 0);
        JLabel reportsTitle = new JLabel("Reportes Programados");
        reportsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        reportsTitle.setForeground(new Color(79, 70, 229)); // Indigo 600
        rightColPanel.add(reportsTitle, rcGbc);

        rcGbc.gridy = 4;
        rcGbc.insets = new Insets(0, 0, 8, 0);
        rightColPanel.add(jchkReportsEnabled, rcGbc);

        JPanel schedRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        schedRow.setOpaque(false);
        schedRow.add(new JLabel("Frecuencia: "));
        schedRow.add(jcmbFrequency);
        schedRow.add(new JLabel("  Día de la semana: "));
        schedRow.add(jcmbDayOfWeek);

        rcGbc.gridy = 5;
        rcGbc.insets = new Insets(0, 0, 10, 0);
        rightColPanel.add(schedRow, rcGbc);

        // Reports Grid Panel (2 Columns)
        JPanel reportsGridPanel = new JPanel(new GridLayout(0, 2, 8, 4));
        reportsGridPanel.setOpaque(false);
        reportsGridPanel.add(jchkReportClosedPos);
        reportsGridPanel.add(jchkReportCashFlow);
        
        reportsGridPanel.add(jchkReportInventory);
        reportsGridPanel.add(jchkReportSaleTaxes);
        
        reportsGridPanel.add(jchkReportDebtors);
        reportsGridPanel.add(jchkReportUserSales);
        
        reportsGridPanel.add(jchkReportCategorySales);
        reportsGridPanel.add(jchkReportUserVoids);
        
        reportsGridPanel.add(jchkReportCustomers);
        reportsGridPanel.add(jchkReportInventoryDiff);
        
        reportsGridPanel.add(jchkReportProductProfit);
        reportsGridPanel.add(jchkReportSuppliers);
        
        reportsGridPanel.add(jchkReportConsolidated);

        rcGbc.gridy = 6;
        rcGbc.insets = new Insets(0, 0, 12, 0);
        rightColPanel.add(reportsGridPanel, rcGbc);

        rcGbc.gridy = 7;
        rcGbc.insets = new Insets(0, 0, 0, 0);
        rcGbc.fill = GridBagConstraints.NONE;
        rcGbc.anchor = GridBagConstraints.CENTER;
        jbtnSendNow = new JButton("Enviar Reportes Ahora");
        styleButtonSolid(jbtnSendNow, new Color(79, 70, 229));
        jbtnSendNow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendReportsNowAction();
            }
        });
        rightColPanel.add(jbtnSendNow, rcGbc);

        // Push everything to the top
        rcGbc.gridy = 8;
        rcGbc.weighty = 1.0;
        rcGbc.fill = GridBagConstraints.BOTH;
        rightColPanel.add(Box.createVerticalGlue(), rcGbc);

        // Add columns to formFieldsPanel
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.BOTH;
        fgbc.weighty = 1.0;

        fgbc.gridx = 0;
        fgbc.gridy = 0;
        fgbc.weightx = 0.45;
        fgbc.insets = new Insets(0, 0, 0, 16);
        formFieldsPanel.add(leftColPanel, fgbc);

        fgbc.gridx = 1;
        fgbc.gridy = 0;
        fgbc.weightx = 0.55;
        fgbc.insets = new Insets(0, 16, 0, 0);
        formFieldsPanel.add(rightColPanel, fgbc);

        cardGbc.gridy = 2;
        cardGbc.fill = GridBagConstraints.BOTH;
        cardGbc.weighty = 1.0;
        cardGbc.insets = new Insets(0, 0, 0, 0);
        card.add(formFieldsPanel, cardGbc);

        GridBagConstraints wgbc = new GridBagConstraints();
        wgbc.gridx = 0;
        wgbc.gridy = 0;
        wgbc.fill = GridBagConstraints.HORIZONTAL;
        wgbc.weightx = 1.0;
        wgbc.insets = new Insets(20, 20, 20, 20);
        centerWrapper.add(card, wgbc);

        scrollPane.setViewportView(centerWrapper);
        add(scrollPane, java.awt.BorderLayout.CENTER);

        // Configure dynamic enablement based on scheduled reports check
        jchkReportsEnabled.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateScheduledReportsComponents();
            }
        });

        if (appView == null) {
            jbtnSendNow.setEnabled(false);
            jbtnSendNow.setToolTipText("Esta función solo está disponible dentro de la aplicación.");
        }
    }

    private void updateScheduledReportsComponents() {
        boolean enabled = jchkReportsEnabled.isSelected();
        jcmbFrequency.setEnabled(enabled);
        jcmbDayOfWeek.setEnabled(enabled && "Semanal".equals(jcmbFrequency.getSelectedItem()));
        jchkReportClosedPos.setEnabled(enabled);
        jchkReportInventory.setEnabled(enabled);
        jchkReportDebtors.setEnabled(enabled);
        jchkReportCategorySales.setEnabled(enabled);
        jchkReportCustomers.setEnabled(enabled);
        jchkReportProductProfit.setEnabled(enabled);
        jchkReportCashFlow.setEnabled(enabled);
        jchkReportSaleTaxes.setEnabled(enabled);
        jchkReportUserSales.setEnabled(enabled);
        jchkReportUserVoids.setEnabled(enabled);
        jchkReportInventoryDiff.setEnabled(enabled);
        jchkReportSuppliers.setEnabled(enabled);
        jchkReportConsolidated.setEnabled(enabled);
    }

    private void sendTestEmailAction() {
        jbtnTestEmail.setEnabled(false);
        saveProperties(currentConfig); // save values temporarily in memory

        new SwingWorker<Void, Void>() {
            private String errorMsg = null;
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    EmailService.sendEmail(
                        currentConfig, 
                        "Correo de Prueba - Voltium Sanrey TPV", 
                        "<h3>¡Conexión Exitosa!</h3><p>Este es un correo de prueba para verificar tu configuración SMTP.</p>", 
                        null
                    );
                } catch (Exception ex) {
                    errorMsg = ex.getMessage();
                    throw ex;
                }
                return null;
            }

            @Override
            protected void done() {
                jbtnTestEmail.setEnabled(true);
                if (errorMsg == null) {
                    JOptionPane.showMessageDialog(JPanelConfigEmailReports.this, 
                        "¡Correo de prueba enviado con éxito! Revisa la bandeja de entrada del destinatario.", 
                        "Conexión SMTP Exitosa", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(JPanelConfigEmailReports.this, 
                        "Error al enviar el correo de prueba:\n" + errorMsg, 
                        "Error de SMTP", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void sendReportsNowAction() {
        jbtnSendNow.setEnabled(false);
        saveProperties(currentConfig); // temporarily save fields

        new SwingWorker<Void, Void>() {
            private String errorMsg = null;
            private int sentPdfCount = 0;
            private boolean sentConsolidated = false;
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    List<File> attachments = new ArrayList<>();
                    List<String> reportNames = new ArrayList<>();

                    if (jchkReportClosedPos.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/sales_closedpos.bs", "cierre_caja");
                        attachments.add(pdf);
                        reportNames.add("Cierre de Caja");
                    }
                    if (jchkReportInventory.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/products.bs", "inventario");
                        attachments.add(pdf);
                        reportNames.add("Inventario");
                    }
                    if (jchkReportDebtors.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/customers_debtors.bs", "deudores");
                        attachments.add(pdf);
                        reportNames.add("Clientes Deudores");
                    }
                    if (jchkReportCategorySales.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/sales_categorysales.bs", "ventas_por_departamento");
                        attachments.add(pdf);
                        reportNames.add("Ventas por Departamento");
                    }
                    if (jchkReportCustomers.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/customers.bs", "catalogo_clientes");
                        attachments.add(pdf);
                        reportNames.add("Catálogo de Clientes");
                    }
                    if (jchkReportProductProfit.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/sales_productsalesprofit.bs", "rentabilidad_por_producto");
                        attachments.add(pdf);
                        reportNames.add("Rentabilidad por Producto");
                    }
                    if (jchkReportCashFlow.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/sales_cashflow.bs", "flujo_de_caja");
                        attachments.add(pdf);
                        reportNames.add("Flujo de Caja");
                    }
                    if (jchkReportSaleTaxes.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/sales_saletaxes.bs", "impuestos_ventas");
                        attachments.add(pdf);
                        reportNames.add("Impuestos sobre Ventas");
                    }
                    if (jchkReportUserSales.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/usersales.bs", "ventas_por_cajero");
                        attachments.add(pdf);
                        reportNames.add("Ventas por Cajero");
                    }
                    if (jchkReportUserVoids.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/uservoids.bs", "ventas_anuladas");
                        attachments.add(pdf);
                        reportNames.add("Ventas Anuladas");
                    }
                    if (jchkReportInventoryDiff.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/inventorydiff.bs", "diferencias_inventario");
                        attachments.add(pdf);
                        reportNames.add("Diferencias de Inventario");
                    }
                    if (jchkReportSuppliers.isSelected()) {
                        File pdf = EmailService.generateReportPdf(appView, "/com/openbravo/reports/suppliers_creditors.bs", "proveedores_acreedores");
                        attachments.add(pdf);
                        reportNames.add("Proveedores Acreedores");
                    }

                    String bodyHtml = "";
                    if (jchkReportConsolidated.isSelected()) {
                        bodyHtml = EmailService.generateConsolidatedReportHtml(appView);
                        sentConsolidated = true;
                    } else {
                        if (attachments.isEmpty()) {
                            errorMsg = "Por favor selecciona al menos un reporte para enviar.";
                            return null;
                        }
                        StringBuilder body = new StringBuilder("<h3>Reportes de TPV bajo demanda:</h3><ul>");
                        for (String name : reportNames) {
                            body.append("<li>").append(name).append("</li>");
                        }
                        body.append("</ul>");
                        bodyHtml = body.toString();
                    }

                    EmailService.sendEmail(currentConfig, "Reportes Manuales de TPV", bodyHtml, attachments.isEmpty() ? null : attachments);
                    sentPdfCount = attachments.size();

                    for (File f : attachments) {
                        f.delete();
                    }
                } catch (Exception ex) {
                    errorMsg = ex.getMessage();
                    throw ex;
                }
                return null;
            }

            @Override
            protected void done() {
                jbtnSendNow.setEnabled(true);
                if (errorMsg == null) {
                    String detailMsg = "";
                    if (sentConsolidated) {
                        detailMsg += "¡Se envió el Reporte Consolidado (HTML)!";
                        if (sentPdfCount > 0) {
                            detailMsg += " Además se adjuntaron " + sentPdfCount + " reporte(s) en PDF.";
                        }
                    } else {
                        detailMsg += "¡Se enviaron correctamente " + sentPdfCount + " reporte(s) en PDF!";
                    }
                    JOptionPane.showMessageDialog(JPanelConfigEmailReports.this, 
                        detailMsg, 
                        "Reportes Enviados", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(JPanelConfigEmailReports.this, 
                        "Error al enviar reportes:\n" + errorMsg, 
                        "Error al Generar Reportes", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private JTextField createStyledTextField() {
        final JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(180, 36));
        tf.setBackground(Color.WHITE);
        tf.setForeground(new Color(30, 41, 59));
        tf.setCaretColor(new Color(59, 130, 246));
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
        pf.setForeground(new Color(30, 41, 59));
        pf.setCaretColor(new Color(59, 130, 246));
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
        cb.setForeground(new Color(30, 41, 59));
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
        label.setForeground(new Color(71, 85, 105));
        panel.add(label, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(input, gbc);
        return panel;
    }

    private void styleButtonSolid(JButton btn, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }

    private void styleButtonOutline(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(79, 70, 229));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(79, 70, 229), 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }

    private void registerListeners() {
        jtxtEmail.getDocument().addDocumentListener(dirty);
        jtxtSMTPServer.getDocument().addDocumentListener(dirty);
        jtxtSMTPPort.getDocument().addDocumentListener(dirty);
        jtxtSMTPUser.getDocument().addDocumentListener(dirty);
        jtxtSMTPPassword.getDocument().addDocumentListener(dirty);
        jchkSMTPSSL.addActionListener(dirty);

        jchkAlertLowStock.addActionListener(dirty);
        jchkAlertOverdueDebts.addActionListener(dirty);

        jchkReportsEnabled.addActionListener(dirty);
        jcmbFrequency.addActionListener(dirty);
        jcmbDayOfWeek.addActionListener(dirty);

        jchkReportClosedPos.addActionListener(dirty);
        jchkReportInventory.addActionListener(dirty);
        jchkReportDebtors.addActionListener(dirty);
        jchkReportCategorySales.addActionListener(dirty);
        jchkReportCustomers.addActionListener(dirty);
        jchkReportProductProfit.addActionListener(dirty);
        jchkReportCashFlow.addActionListener(dirty);
        jchkReportSaleTaxes.addActionListener(dirty);
        jchkReportUserSales.addActionListener(dirty);
        jchkReportUserVoids.addActionListener(dirty);
        jchkReportInventoryDiff.addActionListener(dirty);
        jchkReportSuppliers.addActionListener(dirty);
        jchkReportConsolidated.addActionListener(dirty);

        jcmbFrequency.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jcmbDayOfWeek.setEnabled("Semanal".equals(jcmbFrequency.getSelectedItem()));
            }
        });
    }

    @Override
    public void loadProperties(AppConfig config) {
        this.currentConfig = config;

        jtxtEmail.setText(config.getProperty("notifications.email"));
        
        String host = config.getProperty("notifications.smtp.server");
        jtxtSMTPServer.setText(host != null && !host.isEmpty() ? host : "smtp.gmail.com");

        String port = config.getProperty("notifications.smtp.port");
        jtxtSMTPPort.setText(port != null && !port.isEmpty() ? port : "587");

        String smtpUser = config.getProperty("notifications.smtp.user");
        jtxtSMTPUser.setText(smtpUser != null && !smtpUser.isEmpty() ? smtpUser : "juansalazat100@gmail.com");

        jtxtSMTPPassword.setText(config.getProperty("notifications.smtp.password"));

        String sslVal = config.getProperty("notifications.smtp.ssl");
        jchkSMTPSSL.setSelected(sslVal != null ? Boolean.parseBoolean(sslVal) : true);

        jchkAlertLowStock.setSelected(Boolean.parseBoolean(config.getProperty("notifications.alert.low_stock")));
        jchkAlertOverdueDebts.setSelected(Boolean.parseBoolean(config.getProperty("notifications.alert.overdue_debts")));

        jchkReportsEnabled.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.enabled")));
        
        String freq = config.getProperty("notifications.reports.frequency");
        if ("DAILY".equals(freq)) {
            jcmbFrequency.setSelectedItem("Diario");
        } else if ("WEEKLY".equals(freq)) {
            jcmbFrequency.setSelectedItem("Semanal");
        } else if ("MONTHLY".equals(freq)) {
            jcmbFrequency.setSelectedItem("Mensual");
        }

        String day = config.getProperty("notifications.reports.dayofweek");
        if (day != null && !day.isEmpty()) {
            jcmbDayOfWeek.setSelectedItem(day);
        }

        jchkReportClosedPos.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.closedpos")));
        jchkReportInventory.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.inventory")));
        jchkReportDebtors.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.debtors")));
        jchkReportCategorySales.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.categorysales")));
        jchkReportCustomers.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.customers")));
        jchkReportProductProfit.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.productprofit")));
        jchkReportCashFlow.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.cashflow")));
        jchkReportSaleTaxes.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.saletaxes")));
        jchkReportUserSales.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.usersales")));
        jchkReportUserVoids.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.uservoids")));
        jchkReportInventoryDiff.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.inventorydiff")));
        jchkReportSuppliers.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.suppliers")));
        jchkReportConsolidated.setSelected(Boolean.parseBoolean(config.getProperty("notifications.reports.include.consolidated")));

        updateScheduledReportsComponents();
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
        config.setProperty("notifications.alert.overdue_debts", Boolean.toString(jchkAlertOverdueDebts.isSelected()));

        config.setProperty("notifications.reports.enabled", Boolean.toString(jchkReportsEnabled.isSelected()));
        
        String freq = (String) jcmbFrequency.getSelectedItem();
        if ("Diario".equals(freq)) {
            config.setProperty("notifications.reports.frequency", "DAILY");
        } else if ("Semanal".equals(freq)) {
            config.setProperty("notifications.reports.frequency", "WEEKLY");
        } else if ("Mensual".equals(freq)) {
            config.setProperty("notifications.reports.frequency", "MONTHLY");
        }

        config.setProperty("notifications.reports.dayofweek", (String) jcmbDayOfWeek.getSelectedItem());

        config.setProperty("notifications.reports.include.closedpos", Boolean.toString(jchkReportClosedPos.isSelected()));
        config.setProperty("notifications.reports.include.inventory", Boolean.toString(jchkReportInventory.isSelected()));
        config.setProperty("notifications.reports.include.debtors", Boolean.toString(jchkReportDebtors.isSelected()));
        config.setProperty("notifications.reports.include.categorysales", Boolean.toString(jchkReportCategorySales.isSelected()));
        config.setProperty("notifications.reports.include.customers", Boolean.toString(jchkReportCustomers.isSelected()));
        config.setProperty("notifications.reports.include.productprofit", Boolean.toString(jchkReportProductProfit.isSelected()));
        config.setProperty("notifications.reports.include.cashflow", Boolean.toString(jchkReportCashFlow.isSelected()));
        config.setProperty("notifications.reports.include.saletaxes", Boolean.toString(jchkReportSaleTaxes.isSelected()));
        config.setProperty("notifications.reports.include.usersales", Boolean.toString(jchkReportUserSales.isSelected()));
        config.setProperty("notifications.reports.include.uservoids", Boolean.toString(jchkReportUserVoids.isSelected()));
        config.setProperty("notifications.reports.include.inventorydiff", Boolean.toString(jchkReportInventoryDiff.isSelected()));
        config.setProperty("notifications.reports.include.suppliers", Boolean.toString(jchkReportSuppliers.isSelected()));
        config.setProperty("notifications.reports.include.consolidated", Boolean.toString(jchkReportConsolidated.isSelected()));

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
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            g2.setColor(new Color(226, 232, 240));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            g2.dispose();
        }
    }

    private static class MailIconPanel extends JPanel {
        public MailIconPanel() {
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

            GradientPaint gp = new GradientPaint(0, 0, new Color(238, 242, 255), 0, h, new Color(224, 231, 255));
            g2.setPaint(gp);
            g2.fillOval(2, 2, w - 5, h - 5);

            g2.setColor(new Color(99, 102, 241));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(2, 2, w - 5, h - 5);

            g2.setColor(new Color(79, 70, 229));
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = w / 2;
            int cy = h / 2;

            g2.drawRect(cx - 14, cy - 10, 28, 20);
            g2.drawLine(cx - 14, cy - 10, cx, cy + 1);
            g2.drawLine(cx, cy + 1, cx + 14, cy - 10);

            g2.dispose();
        }
    }
}
