package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.inventory.InventoryLine;
import com.openbravo.pos.inventory.JInventoryLines;
import com.openbravo.pos.panels.JProductFinder;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.BeanFactoryApp;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.openbravo.format.Formats;
import com.openbravo.beans.JCalendarDialog;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JPanelQuotations extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Logger LOGGER = Logger.getLogger(JPanelQuotations.class.getName());
    private AppView m_App;
    private DataLogicSales m_dlSales;
    private DataLogicSystem m_dlSystem;
    private JInventoryLines m_invlines;

    private JTextField m_jClientName;
    private JTextField m_jClientPhone;
    private JTextField m_jDate;
    private JTextField m_jQuoteNo;
    private JTextField m_jShipping;

    private JLabel m_jSubtotal;
    private JLabel m_jTotal;

    public JPanelQuotations() {
    }

    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public void activate() throws BasicException {
        reset();
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTitle() {
        return "Cotizaciones";
    }

    private void reset() {
        m_jClientName.setText("");
        m_jClientPhone.setText("");
        m_jDate.setText(Formats.DATE.formatValue(new Date()));
        m_jQuoteNo.setText("QC-" + (System.currentTimeMillis() / 1000));
        m_jShipping.setText("0.00");
        m_invlines.clear();
        updateTotals();
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        m_dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        m_dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- HEADER ---
        // Se elimina por solicitud del usuario para ganar espacio

        // --- MAIN CONTENT ---
        JPanel mainContent = new JPanel(new BorderLayout(25, 25));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(new EmptyBorder(15, 25, 20, 25));

        // Título de la vista
        JLabel titleView = new JLabel("GENERADOR DE COTIZACIONES PROFESIONALES");
        titleView.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        titleView.setForeground(new Color(46, 125, 50));
        titleView.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainContent.add(titleView, BorderLayout.NORTH);

        // Panel para el contenido debajo del título (WEST + CENTER)
        JPanel contentGrid = new JPanel(new BorderLayout(25, 25));
        contentGrid.setOpaque(false);
        mainContent.add(contentGrid, BorderLayout.CENTER);

        // Left Side: Client Info & Totals
        JPanel leftPanel = new JPanel(new BorderLayout(0, 20));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(350, 0));

        // Client Data Card
        JPanel clientCard = new JPanel(new java.awt.GridBagLayout());
        clientCard.setBackground(new Color(252, 252, 252));
        clientCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(15, 15, 15, 15)));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        int row = 0;
        gbc.gridy = row++;
        JLabel lblClient = createLabel("DATOS DEL CLIENTE", new Font("Segoe UI", Font.BOLD, 11), new Color(120, 120, 120));
        clientCard.add(lblClient, gbc);

        gbc.gridy = row++;
        m_jClientName = createTextField("Ingrese nombre del cliente o empresa...");
        clientCard.add(m_jClientName, gbc);

        gbc.gridy = row++;
        m_jClientPhone = createTextField("Ingrese teléfono de contacto...");
        clientCard.add(m_jClientPhone, gbc);

        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(15, 5, 5, 5);
        clientCard.add(createLabel("DETALLES DE COTIZACIÓN", new Font("Segoe UI", Font.BOLD, 11),
                new Color(120, 120, 120)), gbc);

        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        m_jQuoteNo = createTextField("QC-0000000");
        clientCard.add(m_jQuoteNo, gbc);

        gbc.gridy = row++;
        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.setOpaque(false);
        m_jDate = createTextField("Fecha");
        m_jDate.setEditable(false);
        JButton btnDate = new JButton("...");
        btnDate.addActionListener(e -> {
            Date d = JCalendarDialog.showCalendarTime(this, new Date());
            if (d != null)
                m_jDate.setText(Formats.DATE.formatValue(d));
        });
        datePanel.add(m_jDate, BorderLayout.CENTER);
        datePanel.add(btnDate, BorderLayout.EAST);
        clientCard.add(datePanel, gbc);

        leftPanel.add(clientCard, BorderLayout.NORTH);

        // Sumary & Actions
        JPanel summaryCard = new JPanel(new java.awt.GridBagLayout());
        summaryCard.setBackground(Color.WHITE); 
        summaryCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(46, 125, 50), 2), // Borde más grueso
                new EmptyBorder(20, 20, 20, 20)));

        row = 0;
        gbc.gridy = row++;
        summaryCard.add(createLabel("SUBTOTAL", new Font("Segoe UI Semibold", Font.PLAIN, 12), new Color(100, 100, 100)), gbc);
        gbc.gridy = row++;
        m_jSubtotal = new JLabel("$ 0.00", JLabel.RIGHT);
        m_jSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        summaryCard.add(m_jSubtotal, gbc);

        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(10, 5, 5, 5);
        summaryCard.add(createLabel("GASTOS DE ENVÍO", new Font("Segoe UI", Font.BOLD, 12), new Color(108, 117, 125)),
                gbc);
        gbc.gridy = row++;
        m_jShipping = createTextField("0.00");
        m_jShipping.setHorizontalAlignment(JTextField.RIGHT);
        m_jShipping.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateTotals();
            }
        });
        summaryCard.add(m_jShipping, gbc);

        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(15, 5, 0, 5);
        summaryCard.add(createLabel("PRECIO TOTAL A PAGAR", new Font("Segoe UI Black", Font.PLAIN, 12), new Color(46, 125, 50)), gbc);
        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(0, 5, 5, 5);
        m_jTotal = new JLabel("$ 0.00", JLabel.RIGHT);
        m_jTotal.setFont(new Font("Segoe UI", Font.BOLD, 36)); // ¡MUCHO MÁS GRANDE!
        m_jTotal.setForeground(new Color(46, 125, 50));
        summaryCard.add(m_jTotal, gbc);

        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(25, 5, 5, 5);
        JPanel actionButtons = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        actionButtons.setOpaque(false);

        JButton btnPreview = new JButton("PREVISUALIZAR");
        btnPreview.setBackground(new Color(46, 125, 50));
        btnPreview.setForeground(Color.WHITE);
        btnPreview.setFont(new Font("Arial", Font.BOLD, 14));
        btnPreview.setPreferredSize(new Dimension(0, 45));
        btnPreview.addActionListener(e -> generatePDF(true));
        actionButtons.add(btnPreview);

        JButton btnExport = new JButton("GENERAR PDF");
        btnExport.setBackground(new Color(33, 33, 33));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFont(new Font("Arial", Font.BOLD, 14));
        btnExport.setPreferredSize(new Dimension(0, 45));
        btnExport.addActionListener(e -> generatePDF(false));
        actionButtons.add(btnExport);

        summaryCard.add(actionButtons, gbc);

        leftPanel.add(summaryCard, BorderLayout.CENTER);
        contentGrid.add(leftPanel, BorderLayout.WEST);

        // Right Side: Items List
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setBackground(Color.WHITE);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(Color.WHITE);
        JButton btnSearch = new JButton(" BUSCAR PRODUCTO ");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.setBackground(new Color(46, 125, 50));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/search24.png")));
        btnSearch.addActionListener(e -> browseProducts());
        searchBar.add(btnSearch, BorderLayout.WEST);

        JButton btnClear = new JButton("Limpiar Cotización");
        btnClear.setForeground(new Color(192, 57, 43)); // Rojo Elegante
        btnClear.addActionListener(e -> reset());
        searchBar.add(btnClear, BorderLayout.EAST);

        rightPanel.add(searchBar, BorderLayout.NORTH);

        m_invlines = new JInventoryLines();
        m_invlines.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(222, 226, 230)));
        m_invlines.setEditable(true);
        m_invlines.addTableModelListener(e -> updateTotals());
        rightPanel.add(m_invlines, BorderLayout.CENTER);

        // Accessories Legend (As in the image)
        JPanel legendPanel = new JPanel(new BorderLayout(10, 5));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel legendText = new JLabel(
                "<html><b>Kit de entrega:</b> Cada modelo de Bicicleta incluye: pedales, cargador, espejos, 2 llaves, 2 controladores y manual. Scooter incluye cargador y manual.</html>");
        legendText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        legendText.setForeground(new Color(108, 117, 125));
        legendPanel.add(legendText, BorderLayout.CENTER);
        rightPanel.add(legendPanel, BorderLayout.SOUTH);

        contentGrid.add(rightPanel, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(0, 35));
        field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(0, 10, 0, 10)));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Soporte para placeholder (FlatLaf)
        field.putClientProperty("JTextField.placeholderText", placeholder);

        return field;
    }

    private void browseProducts() {
        ProductInfoExt product = JProductFinder.showMessage(this, m_dlSales);
        if (product != null) {
            String qtyStr = JOptionPane.showInputDialog(this, "Ingrese cantidad para: " + product.getName(), "1");
            if (qtyStr != null && !qtyStr.isEmpty()) {
                double qty = Double.parseDouble(qtyStr);
                m_invlines.addLine(new InventoryLine(product, qty, product.getPriceSell()));
                updateTotals();
            }
        }
    }

    private void updateTotals() {
        double subtotal = 0;
        for (int i = 0; i < m_invlines.getCount(); i++) {
            InventoryLine line = m_invlines.getLine(i);
            subtotal += (line.getMultiply() * line.getPrice());
        }

        double shipping = 0;
        try {
            shipping = Double.parseDouble(m_jShipping.getText());
        } catch (Exception e) {
        }

        m_jSubtotal.setText("$ " + String.format("%.2f", subtotal));
        m_jTotal.setText("$ " + String.format("%.2f", subtotal + shipping));
    }

    private void generatePDF(boolean previewOnly) {
        if (m_jClientName.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese el nombre del cliente.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (m_invlines.getCount() == 0) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un producto a la cotización.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Data for the table
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (int i = 0; i < m_invlines.getCount(); i++) {
                InventoryLine line = m_invlines.getLine(i);
                Map<String, Object> item = new HashMap<>();
                item.put("REFERENCE", line.getProductID());

                // Intentar extraer color si viene en el nombre (ej: "Producto - Azul")
                String name = line.getProductName();
                String color = "";
                if (name.contains(" - ")) {
                    String[] parts = name.split(" - ");
                    name = parts[0];
                    color = parts[parts.length - 1];
                }

                item.put("NAME", name);
                item.put("COLOR", color);
                item.put("PRICE", line.getPrice());
                item.put("UNITS", line.getMultiply());
                item.put("TOTAL", line.getMultiply() * line.getPrice());
                dataList.add(item);
            }

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            // Parámetros con el LOGO (Fallback a recurso interno para evitar errores de
            // SVG)
            Map<String, Object> parameters = new HashMap<>();
            // Usar logo compatible (Fallback para evitar errores de Jasper con SVG)
            try {
                parameters.put("LOGO_PATH", getClass().getResource("/com/openbravo/images/logo.png").toString());
            } catch (Exception e) {
                parameters.put("LOGO_PATH", "");
            }

            // Load and compile report
            InputStream reportStream = getClass().getResourceAsStream("/com/openbravo/reports/quotation.jrxml");
            if (reportStream == null) {
                reportStream = getClass().getClassLoader().getResourceAsStream("com/openbravo/reports/quotation.jrxml");
            }

            if (reportStream == null) {
                throw new Exception("No se encontró el archivo de reporte: /com/openbravo/reports/quotation.jrxml");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            if (previewOnly) {
                // Show preview dialog
                net.sf.jasperreports.view.JasperViewer.viewReport(jasperPrint, false);
            } else {
                // Export to Desktop
                String home = System.getProperty("user.home");
                String fileName = home + "/Desktop/Cotizacion_" + m_jQuoteNo.getText() + ".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, fileName);

                JOptionPane.showMessageDialog(this,
                        "<html><body style='width: 300px;'>¡COTIZACIÓN GENERADA EXITOSAMENTE!<br><br>" +
                                "El archivo se ha guardado en su escritorio:<br>" +
                                "<b>" + fileName + "</b><br><br>" +
                                "<i>Nota: Ya puede enviarlo por WhatsApp o correo.</i></body></html>",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error generating PDF", ex);
            JOptionPane.showMessageDialog(this, "Error al procesar la cotización: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
