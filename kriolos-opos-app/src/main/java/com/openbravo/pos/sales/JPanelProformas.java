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
import javax.swing.*;
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

/**
 * Nueva Proforma / Nota de Pedido personalizada según requerimientos del usuario.
 * Permite capturar SAP ID, E-Commerce ID, Direcciones y generar reporte idéntico a la imagen provista.
 */
public class JPanelProformas extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Logger LOGGER = Logger.getLogger(JPanelProformas.class.getName());
    private AppView m_App;
    private DataLogicSales m_dlSales;
    private DataLogicSystem m_dlSystem;
    private JInventoryLines m_invlines;
    
    // Header Fields
    private JTextField m_jSapId;
    private JTextField m_jEcommerceId;
    private JTextField m_jDate;
    private JTextField m_jOrderNo;
    
    // Client & Address Fields
    private JTextField m_jClientName;
    private JTextArea m_jAddressFiscal;
    private JTextArea m_jAddressShipping;
    
    // Financial Labels
    private JLabel m_jSubtotal;
    private JLabel m_jTax;
    private JLabel m_jTotal;
    private JTextField m_jDiscount;

    public JPanelProformas() {
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
        return "Proforma de Pedido";
    }

    private void reset() {
        m_jSapId.setText("");
        m_jEcommerceId.setText("");
        m_jDate.setText(Formats.DATE.formatValue(new Date()));
        m_jOrderNo.setText("ORD-" + Long.toString(System.currentTimeMillis() / 10000).substring(5));
        m_jClientName.setText("");
        m_jAddressFiscal.setText("");
        m_jAddressShipping.setText("ENVIAR A DIRECCION FISCAL");
        m_jDiscount.setText("0.00");
        m_invlines.clear();
        updateTotals();
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        try {
            m_App = app;
            m_dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
            m_dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
            
            initComponents();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error crítico al inicializar Proforma", e);
            JOptionPane.showMessageDialog(null, "Error detallado de carga:\n" + e.toString() + "\n" + e.getMessage(), "Diagnóstico Evobike", JOptionPane.ERROR_MESSAGE);
            throw new BeanFactoryException(e);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE); // Restaurado a Blanco Institucional
        
        // --- MAIN CONTENT ---
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        // --- LEFT SIDE: Form & Totals ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 20));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(400, 0));
        
        // Data Form Card
        JPanel formCard = new JPanel(new java.awt.GridBagLayout());
        formCard.setBackground(Color.WHITE); // Restaurado a Blanco Institucional
        formCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        // Group: Identifiers
        addFormSubtitle(formCard, gbc, "IDENTIFICADORES", row++);
        
        m_jDate = new JTextField(); // Inicialización faltante
        m_jOrderNo = new JTextField(); // Inicialización faltante
        
        gbc.gridy = row++;
        m_jSapId = createStyledTextField("Pedidio SAP...");
        formCard.add(m_jSapId, gbc);
        
        gbc.gridy = row++;
        m_jEcommerceId = createStyledTextField("E-Commerce ID...");
        formCard.add(m_jEcommerceId, gbc);
        
        // Group: Client
        gbc.insets = new java.awt.Insets(15, 5, 5, 5);
        addFormSubtitle(formCard, gbc, "DATOS DEL CLIENTE", row++);
        
        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        m_jClientName = createStyledTextField("Nombre o Empresa...");
        formCard.add(m_jClientName, gbc);
        
        gbc.gridy = row++;
        formCard.add(new JLabel("DIRECCIÓN FISCAL", JLabel.LEFT) {{ 
            setForeground(Color.BLACK); 
            setFont(new Font("Arial", Font.BOLD, 10)); 
        }}, gbc);
        
        gbc.gridy = row++;
        m_jAddressFiscal = createStyledTextArea(3);
        formCard.add(new JScrollPane(m_jAddressFiscal) {{ setBorder(null); }}, gbc);
        
        gbc.gridy = row++;
        formCard.add(new JLabel("DIRECCIÓN DE ENVÍO", JLabel.LEFT) {{ 
            setForeground(Color.BLACK); 
            setFont(new Font("Arial", Font.BOLD, 10)); 
        }}, gbc);
        
        gbc.gridy = row++;
        m_jAddressShipping = createStyledTextArea(2);
        m_jAddressShipping.setText("ENVIAR A DIRECCION FISCAL");
        formCard.add(new JScrollPane(m_jAddressShipping) {{ setBorder(null); }}, gbc);
        
        leftPanel.add(formCard, BorderLayout.NORTH);
        
        // --- SUMMARY CARD ---
        JPanel summaryCard = new JPanel(new java.awt.GridBagLayout());
        summaryCard.setBackground(new Color(248, 249, 250)); // Gris Muy Claro Premium
        summaryCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        int sRow = 0;
        gbc.insets = new java.awt.Insets(2, 5, 2, 5);
        
        summaryCard.add(new JLabel("SUBTOTAL") {{ setFont(new Font("Arial", Font.BOLD, 12)); }}, createGbc(0, sRow++));
        m_jSubtotal = new JLabel("$ 0.00", JLabel.RIGHT);
        m_jSubtotal.setFont(new Font("Arial", Font.BOLD, 18));
        summaryCard.add(m_jSubtotal, createGbc(0, sRow++));
        
        summaryCard.add(new JLabel("DESCUENTO (0.00 %)") {{ setFont(new Font("Arial", Font.BOLD, 12)); }}, createGbc(0, sRow++));
        m_jDiscount = new JTextField("0.00") {{ 
            setHorizontalAlignment(JTextField.RIGHT);
            setBorder(new EmptyBorder(0, 5, 0, 5));
            addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent e) { updateTotals(); }
            });
        }};
        summaryCard.add(m_jDiscount, createGbc(0, sRow++));
        
        summaryCard.add(new JLabel("IMPUESTO (16%)") {{ setFont(new Font("Arial", Font.BOLD, 12)); }}, createGbc(0, sRow++));
        m_jTax = new JLabel("$ 0.00", JLabel.RIGHT);
        m_jTax.setFont(new Font("Arial", Font.BOLD, 18));
        summaryCard.add(m_jTax, createGbc(0, sRow++));
        
        gbc.insets = new java.awt.Insets(10, 5, 5, 5);
        summaryCard.add(new JLabel("TOTAL NETO") {{ setFont(new Font("Arial", Font.BOLD, 14)); }}, createGbc(0, sRow++));
        m_jTotal = new JLabel("$ 0.00", JLabel.RIGHT);
        m_jTotal.setFont(new Font("Arial", Font.BOLD, 32));
        m_jTotal.setForeground(Color.BLACK);
        summaryCard.add(m_jTotal, createGbc(0, sRow++));
        
        gbc.insets = new java.awt.Insets(20, 5, 5, 5);
        JPanel actionButtons = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        actionButtons.setOpaque(false);
        
        JButton btnPreview = new JButton("PREVISUALIZAR");
        btnPreview.setBackground(new Color(46, 125, 50)); // Verde Esmeralda
        btnPreview.setForeground(Color.WHITE);
        btnPreview.setFont(new Font("Arial", Font.BOLD, 14));
        btnPreview.addActionListener(e -> generateProforma(true));
        actionButtons.add(btnPreview);
        
        JButton btnGenerate = new JButton("GENERAR PDF");
        btnGenerate.setBackground(new Color(33, 33, 33)); // Gris Oscuro Premium
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFont(new Font("Arial", Font.BOLD, 14));
        btnGenerate.addActionListener(e -> generateProforma(false));
        actionButtons.add(btnGenerate);
        
        summaryCard.add(actionButtons, createGbc(0, sRow++));
        
        leftPanel.add(summaryCard, BorderLayout.CENTER);
        mainContent.add(leftPanel, BorderLayout.WEST);
        
        // --- RIGHT SIDE: Items List ---
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);
        
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);
        
        JButton btnAdd = new JButton(" BUSCAR ARTÍCULO (MOTOPARTES) ");
        btnAdd.setBackground(new Color(46, 125, 50)); // Verde Esmeralda Corporativo
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Arial", Font.BOLD, 12));
        btnAdd.addActionListener(e -> browseProducts());
        toolbar.add(btnAdd, BorderLayout.WEST);
        
        JButton btnReset = new JButton("Limpiar Todo");
        btnReset.addActionListener(e -> reset());
        toolbar.add(btnReset, BorderLayout.EAST);
        
        rightPanel.add(toolbar, BorderLayout.NORTH);
        
        m_invlines = new JInventoryLines();
        m_invlines.setBackground(Color.WHITE);
        m_invlines.setOpaque(true);
        m_invlines.setEditable(true);
        m_invlines.addTableModelListener(e -> updateTotals());
        rightPanel.add(m_invlines, BorderLayout.CENTER);
        
        mainContent.add(rightPanel, BorderLayout.CENTER);
        
        add(mainContent, BorderLayout.CENTER);
    }

    private void addFormSubtitle(JPanel p, java.awt.GridBagConstraints gbc, String text, int row) {
        gbc.gridy = row;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(Color.BLACK);
        p.add(lbl, gbc);
    }

    private java.awt.GridBagConstraints createGbc(int x, int y) {
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.gridx = x; g.gridy = y; g.fill = java.awt.GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        g.insets = new java.awt.Insets(2, 5, 2, 5);
        return g;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(0, 35));
        f.setBackground(Color.WHITE);
        f.setForeground(Color.BLACK);
        f.setCaretColor(Color.BLACK);
        f.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new Color(0, 150, 0)),
            new EmptyBorder(0, 10, 0, 10)
        ));
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    private JTextArea createStyledTextArea(int rows) {
        JTextArea a = new JTextArea(rows, 0);
        a.setBackground(Color.WHITE);
        a.setForeground(Color.BLACK);
        a.setCaretColor(Color.BLACK);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(5, 10, 5, 10));
        return a;
    }

    private void browseProducts() {
        ProductInfoExt product = JProductFinder.showMessage(this, m_dlSales);
        if (product != null) {
            String qtyStr = JOptionPane.showInputDialog(this, "Cantidad para: " + product.getName(), "1");
            if (qtyStr != null && !qtyStr.isEmpty()) {
                try {
                    double qty = Double.parseDouble(qtyStr);
                    m_invlines.addLine(new InventoryLine(product, qty, product.getPriceSell()));
                    updateTotals();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Cantidad inválida");
                }
            }
        }
    }

    private void updateTotals() {
        double subtotal = 0;
        for (int i = 0; i < m_invlines.getCount(); i++) {
            InventoryLine line = m_invlines.getLine(i);
            subtotal += (line.getMultiply() * line.getPrice());
        }
        
        double discountPct = 0;
        try {
            discountPct = Double.parseDouble(m_jDiscount.getText());
        } catch (Exception e) {}
        
        double discountedTotal = subtotal * (1 - (discountPct / 100));
        double taxValue = discountedTotal * 0.16; // 16% IVA
        double total = discountedTotal + taxValue;
        
        m_jSubtotal.setText("$ " + String.format("%.2f", subtotal));
        m_jTax.setText("$ " + String.format("%.2f", taxValue));
        m_jTotal.setText("$ " + String.format("%.2f", total));
    }

    private void generateProforma(boolean previewOnly) {
        if (m_jClientName.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese el nombre del cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (m_invlines.getCount() == 0) {
            JOptionPane.showMessageDialog(this, "La proforma está vacía.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Recolectar datos para el reporte
            List<Map<String, Object>> items = new ArrayList<>();
            for (int i = 0; i < m_invlines.getCount(); i++) {
                InventoryLine line = m_invlines.getLine(i);
                Map<String, Object> item = new HashMap<>();
                item.put("ARTICULO", line.getProductID());
                item.put("DESCRIPCION", line.getProductName());
                item.put("CANTIDAD", line.getMultiply());
                item.put("PRECIO", line.getPrice());
                item.put("TOTAL", line.getMultiply() * line.getPrice());
                items.add(item);
            }

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);

            Map<String, Object> params = new HashMap<>();
            params.put("SAP_ID", m_jSapId.getText());
            params.put("ECOMMERCE_ID", m_jEcommerceId.getText());
            params.put("CLIENTE", m_jClientName.getText());
            params.put("DIRECCION_FISCAL", m_jAddressFiscal.getText());
            params.put("DIRECCION_ENVIO", m_jAddressShipping.getText());
            params.put("SUBTOTAL", m_jSubtotal.getText());
            params.put("DESCUENTO", m_jDiscount.getText() + " %");
            params.put("IVA", m_jTax.getText());
            params.put("TOTAL", m_jTotal.getText());
            params.put("FECHA", m_jDate.getText());
            params.put("NUMERO_ORDEN", m_jOrderNo.getText());
            
            // Hardcoded bank details
            params.put("BANCO1", "Banamex CONVENIO: 369-5057457 CLABE: 002180036950574578");
            params.put("BANCO2", "BBVA CONVENIO: CIE 01715046 CLABE: 012914002017150465");
            params.put("BANCO3", "Banco Azteca CONVENIO: CIE 10806 CLABE: 127180450000108067");

            // Usar logo compatible (Fallback para evitar errores de Jasper con SVG)
            try {
                params.put("LOGO_PATH", getClass().getResource("/com/openbravo/images/logo.png").toString());
            } catch (Exception e) {
                params.put("LOGO_PATH", "");
            }

            // Cargar y compilar (en este entorno usamos el recurso de proforma)
            // NOTA: Para previsualización igual que cotizaciones
            InputStream reportStream = getClass().getResourceAsStream("/com/openbravo/reports/proforma.jrxml");
            if (reportStream == null) {
                // Si no existe el jrxml de proforma, intentamos el de cotización como fallback o mostramos aviso
                JOptionPane.showMessageDialog(this, "Aviso: No se encontró el archivo /com/openbravo/reports/proforma.jrxml. Se requiere instalar este archivo para la impresión real.", "Archivo Faltante", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

            if (previewOnly) {
                // Previsualización igual que Cotizaciones
                net.sf.jasperreports.view.JasperViewer.viewReport(jasperPrint, false);
            } else {
                // Exportar a Escritorio
                String home = System.getProperty("user.home");
                String fileName = home + "/Desktop/Proforma_" + m_jOrderNo.getText() + ".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, fileName);

                JOptionPane.showMessageDialog(this, 
                    "<html><body style='width: 300px;'><b>¡PROFORMA GENERADA!</b><br><br>" +
                    "Cliente: " + m_jClientName.getText() + "<br>" +
                    "Guardado en: <b>" + fileName + "</b></body></html>", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error en Proforma", ex);
            JOptionPane.showMessageDialog(this, "Error al procesar Proforma: " + ex.getMessage());
        }
    }
}
