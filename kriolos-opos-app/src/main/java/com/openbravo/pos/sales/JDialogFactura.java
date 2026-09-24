package com.openbravo.pos.sales;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.customers.CustomerInfoExt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Diálogo para emitir factura electrónica con la API de Facturama (Sandbox).
 * 
 * @author Sebastian / Antigravity
 */
public class JDialogFactura extends JDialog {

    private static final Logger LOGGER = Logger.getLogger(JDialogFactura.class.getName());
    private static final long serialVersionUID = 1L;

    private static final String SANDBOX_URL = "https://apisandbox.facturama.mx/3/cfdis";

    private final AppView app;
    private final TicketInfo ticket;

    // Componentes del UI
    private JTextField txtRfc;
    private JTextField txtNombre;
    private JTextField txtCodigoPostal;
    private JComboBox<String> comboUsoCfdi;
    private JComboBox<String> comboRegimen;
    private JComboBox<String> comboFormaPago;
    private JComboBox<String> comboMetodoPago;
    private JTextField txtLugarExpedicion;
    
    private JButton btnFacturar;
    private JButton btnCerrar;
    private JProgressBar progressBar;
    private JTextArea txtLogs;
    private JLabel lblStatus;

    public JDialogFactura(Window parent, AppView app, TicketInfo ticket) {
        super(parent, "Facturación Electrónica - Ticket #" + ticket.getTicketId(), ModalityType.APPLICATION_MODAL);
        this.app = app;
        this.ticket = ticket;
        
        initComponents();
        prefillData();
        updateConfigurationStatus();
        
        setSize(750, 680);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font titleFont = new Font("Segoe UI", Font.BOLD, 18);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // --- Título del Diálogo ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblTitle = new JLabel("Emitir factura electrónica CFDI 4.0");
        lblTitle.setFont(titleFont);
        lblTitle.setForeground(new Color(7, 55, 43));
        mainPanel.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        // --- RFC ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblRfc = new JLabel("RFC Receptor *");
        lblRfc.setFont(labelFont);
        mainPanel.add(lblRfc, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtRfc = new JTextField();
        txtRfc.setFont(fieldFont);
        mainPanel.add(txtRfc, gbc);

        // --- Nombre / Razón Social ---
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel lblNombre = new JLabel("Nombre / Razón Social *");
        lblNombre.setFont(labelFont);
        mainPanel.add(lblNombre, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtNombre = new JTextField();
        txtNombre.setFont(fieldFont);
        mainPanel.add(txtNombre, gbc);

        // --- Código Postal ---
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        JLabel lblCp = new JLabel("Código Postal Receptor *");
        lblCp.setFont(labelFont);
        mainPanel.add(lblCp, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtCodigoPostal = new JTextField();
        txtCodigoPostal.setFont(fieldFont);
        mainPanel.add(txtCodigoPostal, gbc);

        // --- Régimen Fiscal ---
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        JLabel lblRegimen = new JLabel("Régimen Fiscal *");
        lblRegimen.setFont(labelFont);
        mainPanel.add(lblRegimen, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comboRegimen = new JComboBox<>(new String[] {
            "616 - Sin obligaciones fiscales",
            "601 - General de Ley Personas Morales",
            "603 - Personas Morales con Fines no Lucrativos",
            "605 - Sueldos y Salarios e Ingresos Asimilados a Salarios",
            "612 - Personas Físicas con Actividades Empresariales y Profesionales",
            "626 - Régimen Simplificado de Confianza (RESICO)"
        });
        comboRegimen.setFont(fieldFont);
        comboRegimen.setBackground(Color.WHITE);
        mainPanel.add(comboRegimen, gbc);

        // --- Uso de CFDI ---
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.3;
        JLabel lblUso = new JLabel("Uso de CFDI *");
        lblUso.setFont(labelFont);
        mainPanel.add(lblUso, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comboUsoCfdi = new JComboBox<>(new String[] {
            "S01 - Sin efectos fiscales",
            "CP01 - Pagos",
            "G03 - Gastos en general",
            "G01 - Adquisición de mercancías"
        });
        comboUsoCfdi.setFont(fieldFont);
        comboUsoCfdi.setBackground(Color.WHITE);
        mainPanel.add(comboUsoCfdi, gbc);

        // --- Forma de Pago ---
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.3;
        JLabel lblForma = new JLabel("Forma de Pago *");
        lblForma.setFont(labelFont);
        mainPanel.add(lblForma, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comboFormaPago = new JComboBox<>(new String[] {
            "01 - Efectivo",
            "02 - Cheque nominativo",
            "03 - Transferencia electrónica de fondos",
            "04 - Tarjeta de crédito",
            "28 - Tarjeta de débito",
            "05 - Monedero electrónico",
            "08 - Vales de despensa",
            "99 - Por definir"
        });
        comboFormaPago.setFont(fieldFont);
        comboFormaPago.setBackground(Color.WHITE);
        mainPanel.add(comboFormaPago, gbc);

        // --- Método de Pago ---
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0.3;
        JLabel lblMetodo = new JLabel("Método de Pago *");
        lblMetodo.setFont(labelFont);
        mainPanel.add(lblMetodo, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comboMetodoPago = new JComboBox<>(new String[] {
            "PUE - Pago en una sola exhibición",
            "PPD - Pago en parcialidades o diferido"
        });
        comboMetodoPago.setFont(fieldFont);
        comboMetodoPago.setBackground(Color.WHITE);
        mainPanel.add(comboMetodoPago, gbc);

        // --- Lugar de Expedición (Código Postal Emisor) ---
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 0.3;
        JLabel lblExp = new JLabel("Lugar de Expedición (C.P.) *");
        lblExp.setFont(labelFont);
        mainPanel.add(lblExp, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtLugarExpedicion = new JTextField();
        txtLugarExpedicion.setFont(fieldFont);
        mainPanel.add(txtLugarExpedicion, gbc);

        // --- Barra de Progreso y Estatus ---
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        lblStatus = new JLabel("Listo para facturar");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        mainPanel.add(lblStatus, gbc);

        gbc.gridy = 10;
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        mainPanel.add(progressBar, gbc);

        // --- Consola / Logs para Depuración ---
        gbc.gridy = 11;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtLogs = new JTextArea();
        txtLogs.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtLogs.setEditable(false);
        txtLogs.setBackground(new Color(248, 250, 252)); // Slate-50
        JScrollPane scrollLogs = new JScrollPane(txtLogs);
        scrollLogs.setBorder(BorderFactory.createTitledBorder("Detalle de la Operación"));
        mainPanel.add(scrollLogs, gbc);

        // --- Panel de Botones ---
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        JPanel panelButtons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        panelButtons.setOpaque(false);

        btnFacturar = new JButton("Emitir Factura");
        btnFacturar.setFont(labelFont);
        btnFacturar.setBackground(new Color(7, 55, 43));
        btnFacturar.setForeground(Color.WHITE);
        btnFacturar.setFocusPainted(false);
        btnFacturar.putClientProperty("JButton.buttonType", "roundRect");
        btnFacturar.addActionListener(e -> emitirFactura());

        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(labelFont);
        btnCerrar.setFocusPainted(false);
        btnCerrar.putClientProperty("JButton.buttonType", "roundRect");
        btnCerrar.addActionListener(e -> dispose());

        panelButtons.add(btnCerrar);
        panelButtons.add(btnFacturar);
        mainPanel.add(panelButtons, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Listener para cambios en RFC para auto-ajustar uso CFDI y Régimen
        txtRfc.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                String rfc = txtRfc.getText().trim().toUpperCase();
                if (rfc.equals("XAXX010101000")) {
                    comboUsoCfdi.setSelectedIndex(0); // Sin efectos fiscales
                    comboRegimen.setSelectedIndex(0); // Sin obligaciones
                } else if (!rfc.isEmpty()) {
                    if (rfc.length() == 12) {
                        // Persona Moral
                        comboRegimen.setSelectedItem("601 - General de Ley Personas Morales");
                        comboUsoCfdi.setSelectedItem("G03 - Gastos en general");
                    } else if (rfc.length() == 13) {
                        // Persona Física
                        comboRegimen.setSelectedItem("612 - Personas Físicas con Actividades Empresariales y Profesionales");
                        comboUsoCfdi.setSelectedItem("G03 - Gastos en general");
                    }
                }
            }
        });
    }

    private void prefillData() {
        CustomerInfoExt customer = ticket.getCustomer();
        if (customer != null) {
            String rfc = customer.getTaxid();
            if (rfc == null || rfc.trim().isEmpty()) {
                rfc = customer.getTaxCustomerID();
            }
            if (rfc != null && !rfc.trim().isEmpty()) {
                txtRfc.setText(rfc.trim().toUpperCase());
            } else {
                txtRfc.setText("XAXX010101000");
            }

            String name = customer.getName();
            if (name != null && !name.trim().isEmpty()) {
                txtNombre.setText(name.trim().toUpperCase());
            } else {
                txtNombre.setText("PUBLICO EN GENERAL");
            }

            String cp = customer.getPostal();
            if (cp != null && !cp.trim().isEmpty()) {
                txtCodigoPostal.setText(cp.trim());
            } else {
                txtCodigoPostal.setText("26015");
            }
        } else {
            txtRfc.setText("XAXX010101000");
            txtNombre.setText("PUBLICO EN GENERAL");
            txtCodigoPostal.setText("26015");
        }

        // Auto-detectar forma de pago
        String detectedForma = "01"; // Efectivo por defecto
        if (ticket.getPayments() != null) {
            for (com.openbravo.pos.payment.PaymentInfo p : ticket.getPayments()) {
                String name = p.getName();
                if ("ccard".equals(name) || "magcard".equals(name) || "magcardrefund".equals(name)) {
                    detectedForma = "28"; // Tarjeta de débito (común)
                    break;
                } else if ("cheque".equals(name) || "chequerefund".equals(name)) {
                    detectedForma = "02";
                    break;
                } else if ("bank".equals(name) || "paper".equals(name) || "slip".equals(name)) {
                    detectedForma = "03"; // Transferencia
                    break;
                } else if ("voucher".equals(name) || "voucherin".equals(name) || "voucherout".equals(name)) {
                    detectedForma = "08"; // Vales de despensa
                    break;
                }
            }
        }
        selectComboKey(comboFormaPago, detectedForma);

        txtLugarExpedicion.setText(getSetting(
                "facturama.expeditionPostalCode", "FACTURAMA_EXPEDITION_POSTAL_CODE"));

        // Si es venta genérica, uso CFDI = S01 y Régimen = 616
        if ("XAXX010101000".equals(txtRfc.getText())) {
            comboUsoCfdi.setSelectedIndex(0);
            comboRegimen.setSelectedIndex(0);
        } else {
            comboUsoCfdi.setSelectedItem("G03 - Gastos en general");
            if (txtRfc.getText().length() == 12) {
                comboRegimen.setSelectedItem("601 - General de Ley Personas Morales");
            } else {
                comboRegimen.setSelectedItem("612 - Personas Físicas con Actividades Empresariales y Profesionales");
            }
        }
    }

    private String getSetting(String propertyName, String environmentName) {
        String value = app.getProperties().getProperty(propertyName);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentName);
        }
        return value == null ? "" : value.trim();
    }

    private void updateConfigurationStatus() {
        boolean configured = !getSetting("facturama.user", "FACTURAMA_USER").isBlank()
                && !getSetting("facturama.password", "FACTURAMA_PASSWORD").isBlank();
        if (configured) {
            lblStatus.setText("Configuración fiscal disponible. Revisa los datos antes de emitir.");
            lblStatus.setForeground(new Color(22, 101, 52));
        } else {
            lblStatus.setText("Configura facturama.user y facturama.password para habilitar la emisión.");
            lblStatus.setForeground(new Color(180, 83, 9));
            btnFacturar.setEnabled(false);
            btnFacturar.setToolTipText("Faltan las credenciales del proveedor de facturación");
        }
    }

    private void selectComboKey(JComboBox<String> combo, String key) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item.startsWith(key)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLogs.append(message + "\n");
            txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
        });
    }

    private void setUIEnabled(boolean enabled) {
        txtRfc.setEnabled(enabled);
        txtNombre.setEnabled(enabled);
        txtCodigoPostal.setEnabled(enabled);
        comboUsoCfdi.setEnabled(enabled);
        comboRegimen.setEnabled(enabled);
        comboFormaPago.setEnabled(enabled);
        comboMetodoPago.setEnabled(enabled);
        txtLugarExpedicion.setEnabled(enabled);
        btnFacturar.setEnabled(enabled);
        btnCerrar.setEnabled(enabled);
    }

    private void emitirFactura() {
        // Validaciones
        final String rfc = txtRfc.getText().trim().toUpperCase();
        final String nombre = txtNombre.getText().trim().toUpperCase();
        final String cp = txtCodigoPostal.getText().trim();
        final String lugarExp = txtLugarExpedicion.getText().trim();
        final String apiUser = getSetting("facturama.user", "FACTURAMA_USER");
        final String apiPassword = getSetting("facturama.password", "FACTURAMA_PASSWORD");
        final String apiUrl = getSetting("facturama.url", "FACTURAMA_URL").isBlank()
                ? SANDBOX_URL : getSetting("facturama.url", "FACTURAMA_URL");

        if (apiUser.isBlank() || apiPassword.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Falta configurar el usuario o la contraseña de Facturama.\n"
                            + "Define facturama.user y facturama.password en la configuración del PDV.",
                    "Facturación no configurada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (rfc.isEmpty() || nombre.isEmpty() || cp.isEmpty() || lugarExp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos marcados con * son requeridos.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!("XAXX010101000".equals(rfc) || rfc.matches("[A-Z&Ñ]{3,4}[0-9]{6}[A-Z0-9]{3}"))) {
            JOptionPane.showMessageDialog(this, "El RFC no tiene un formato válido.",
                    "Revisar RFC", JOptionPane.WARNING_MESSAGE);
            txtRfc.requestFocusInWindow();
            return;
        }
        if (!cp.matches("[0-9]{5}") || !lugarExp.matches("[0-9]{5}")) {
            JOptionPane.showMessageDialog(this, "Los códigos postales deben contener exactamente 5 dígitos.",
                    "Revisar códigos postales", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ticket.getLinesCount() == 0 || ticket.getTotal() <= 0.0) {
            JOptionPane.showMessageDialog(this, "El ticket no contiene conceptos facturables.",
                    "Ticket inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            com.openbravo.pos.forms.DataLogicSales logic =
                    (com.openbravo.pos.forms.DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
            if (logic.isTicketInvoiced(ticket.getId())) {
                JOptionPane.showMessageDialog(this,
                        "Este ticket ya tiene una factura vigente. Consúltala en Facturas Electrónicas.",
                        "Factura duplicada", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "No se pudo verificar si el ticket ya fue facturado", ex);
            JOptionPane.showMessageDialog(this,
                    "No fue posible validar el historial del ticket. Intenta nuevamente.",
                    "Validación no disponible", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtener códigos seleccionados
        final String usoCfdi = ((String) comboUsoCfdi.getSelectedItem()).split(" ")[0];
        final String regimen = ((String) comboRegimen.getSelectedItem()).split(" ")[0];
        final String formaPago = ((String) comboFormaPago.getSelectedItem()).split(" ")[0];
        final String metodoPago = ((String) comboMetodoPago.getSelectedItem()).split(" ")[0];

        boolean production = apiUrl.contains("api.facturama.mx") && !apiUrl.contains("apisandbox");
        String environment = production ? "PRODUCCIÓN — CFDI REAL" : "PRUEBAS — SIN VALIDEZ FISCAL";
        int confirmation = JOptionPane.showConfirmDialog(this,
                "<html><b>Confirma la emisión</b><br><br>Ambiente: " + environment
                + "<br>Receptor: " + nombre + "<br>RFC: " + rfc
                + "<br>Total: " + Formats.CURRENCY.formatValue(ticket.getTotal())
                + "<br><br>Una factura timbrada no debe emitirse dos veces.</html>",
                "Confirmar factura", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) return;

        setUIEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        lblStatus.setText("Generando JSON y enviando petición a Facturama...");
        txtLogs.setText("");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                // 1. Crear el payload JSON utilizando Gson
                log("Constructing JSON payload for CFDI 4.0...");
                
                JsonObject payload = new JsonObject();
                payload.addProperty("Currency", "MXN");
                payload.addProperty("CfdiType", "I");
                payload.addProperty("PaymentForm", formaPago);
                payload.addProperty("PaymentMethod", metodoPago);
                payload.addProperty("ExpeditionPlace", lugarExp);
                payload.addProperty("Exportation", "01"); // No aplica
                
                // Fecha actual formateada
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                payload.addProperty("Date", sdf.format(new Date()));

                // Si es RFC genérico de Público en General, CFDI 4.0 exige Nodo GlobalInformation
                if ("XAXX010101000".equals(rfc)) {
                    JsonObject globalInfo = new JsonObject();
                    globalInfo.addProperty("Periodicity", "01"); // Diario
                    globalInfo.addProperty("Months", new SimpleDateFormat("MM").format(new Date()));
                    globalInfo.addProperty("Year", Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date())));
                    payload.add("GlobalInformation", globalInfo);
                }

                // Nodo Receptor
                JsonObject receiver = new JsonObject();
                receiver.addProperty("Rfc", rfc);
                receiver.addProperty("Name", nombre);
                receiver.addProperty("CfdiUse", usoCfdi);
                receiver.addProperty("FiscalRegime", regimen);
                receiver.addProperty("TaxZipCode", cp);
                payload.add("Receiver", receiver);

                // Nodo Items
                JsonArray itemsArray = new JsonArray();
                for (int i = 0; i < ticket.getLinesCount(); i++) {
                    TicketLineInfo line = ticket.getLine(i);
                    JsonObject item = new JsonObject();
                    
                    item.addProperty("ProductCode", "01010101"); // Clave genérica SAT
                    item.addProperty("Description", line.getProductName());
                    item.addProperty("Unit", "Pieza");
                    item.addProperty("UnitCode", "H87");
                    item.addProperty("Quantity", line.getMultiply());
                    
                    // Facturama requiere el precio unitario sin IVA
                    double unitPriceNoTax = line.getPrice();
                    double subtotalNoTax = line.getSubValue();
                    
                    item.addProperty("UnitPrice", unitPriceNoTax);
                    item.addProperty("Subtotal", subtotalNoTax);
                    item.addProperty("TaxObject", "02"); // Sí objeto de impuesto

                    // Impuesto trasladado (IVA)
                    JsonArray taxesArray = new JsonArray();
                    JsonObject tax = new JsonObject();
                    tax.addProperty("Name", "IVA");
                    tax.addProperty("Rate", line.getTaxRate());
                    tax.addProperty("Base", subtotalNoTax);
                    tax.addProperty("Total", line.getTax());
                    tax.addProperty("IsRetention", false);
                    taxesArray.add(tax);
                    item.add("Taxes", taxesArray);
                    
                    // Total del item
                    item.addProperty("Total", line.getValue());
                    
                    itemsArray.add(item);
                }
                payload.add("Items", itemsArray);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonStr = gson.toJson(payload);
                log("Solicitud preparada: " + ticket.getLinesCount() + " concepto(s), total "
                        + Formats.CURRENCY.formatValue(ticket.getTotal()) + ".");

                // 2. Enviar petición HTTP POST
                log("Enviando petición a la API de Sandbox Facturama...");
                String auth = apiUser + ":" + apiPassword;
                String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(15)).build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .timeout(Duration.ofSeconds(45))
                        .header("Authorization", authHeader)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonStr, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                int code = response.statusCode();
                log("Código de respuesta de la API: " + code);
                
                String responseBody = response.body();
                if (code == 200 || code == 201) {
                    JsonObject respJson = gson.fromJson(responseBody, JsonObject.class);
                    String uuid = respJson.get("Uuid") != null ? respJson.get("Uuid").getAsString() : "N/A";
                    String id = respJson.get("Id") != null ? respJson.get("Id").getAsString() : "N/A";
                    
                    log("\n--- FACTURA CREADA CON ÉXITO ---");
                    log("ID Facturama: " + id);
                    log("Folio Fiscal (UUID): " + uuid);
                    log("Estatus: " + (respJson.get("Status") != null ? respJson.get("Status").getAsString() : "Vigente"));
                    
                    return "SUCCESS:" + uuid + "|" + id;
                } else {
                    log("\nError al timbrar factura. Respuesta de la API:");
                    log(responseBody);
                    return "ERROR:" + responseBody;
                }
            }

            @Override
            protected void done() {
                setUIEnabled(true);
                progressBar.setVisible(false);
                try {
                    String result = get();
                    if (result.startsWith("SUCCESS:")) {
                        String[] issuedData = result.substring(8).split("\\|", 2);
                        String uuid = issuedData[0];
                        String providerId = issuedData.length > 1 ? issuedData[1] : "";
                        lblStatus.setText("Facturación exitosa. UUID: " + uuid);
                        JOptionPane.showMessageDialog(JDialogFactura.this,
                                "<html><center><h3>¡Factura Timbrada con Éxito!</h3>" +
                                "<p><b>Folio Fiscal (UUID):</b></p>" +
                                "<p style='font-family:monospaced; color:green; font-weight:bold;'>" + uuid + "</p></center></html>",
                                "Timbrado Exitoso", JOptionPane.INFORMATION_MESSAGE);
                        
                        try {
                            com.openbravo.pos.forms.DataLogicSales dlSales = (com.openbravo.pos.forms.DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
                            dlSales.insertFacturaElectronica(ticket.getId(), uuid, providerId, ticket.getTotal(), "TIMBRADA");
                            log("Factura guardada en base de datos local exitosamente.");
                        } catch (Exception ex) {
                            LOGGER.log(Level.WARNING, "No se pudo guardar el registro de la factura en BD", ex);
                            log("ADVERTENCIA: No se pudo guardar la factura en la base de datos.");
                        }
                        
                        dispose(); // Cerrar diálogo al terminar con éxito
                    } else {
                        String errorMsg = result.substring(6);
                        lblStatus.setText("Error en la facturación");
                        JOptionPane.showMessageDialog(JDialogFactura.this,
                                "Ocurrió un error al timbrar la factura:\n" + errorMsg,
                                "Error de Facturación", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Excepción en el hilo de timbrado", e);
                    lblStatus.setText("Excepción al facturar");
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    log("EXCEPCIÓN:\n" + sw.toString());
                    JOptionPane.showMessageDialog(JDialogFactura.this,
                            "Ocurrió un error crítico: " + e.getMessage(),
                            "Error de Red/Sistema", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
