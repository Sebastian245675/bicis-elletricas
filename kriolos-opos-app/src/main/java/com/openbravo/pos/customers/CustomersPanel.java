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

package com.openbravo.pos.customers;

import com.openbravo.pos.util.ModernLookAndFeel;
import javax.swing.BorderFactory;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;
import java.awt.Component;
// Sebastian - Importaciones para el sistema de puntos
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.data.loader.Session;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 *
 * @author adrianromero
 */
public class CustomersPanel extends JPanelTable {

    private static final long serialVersionUID = 1L;
    
    private DataLogicCustomers dlCustomers;
    private CustomersView jeditor;
    
    // Sebastian - Variables para el sistema de puntos
    private PuntosDataLogic puntosDataLogic;
    private DataLogicSales dlSales;
    
    public CustomersPanel() {}

    @Override
    protected void init() {
        this.jeditor = new CustomersView(app, dirty);
        this.dlCustomers  = (DataLogicCustomers) app.getBean("com.openbravo.pos.customers.DataLogicCustomers");
        
        // Sebastian - Inicializar sistema de puntos
        try {
            this.dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
            Session session = app.getSession();
            this.puntosDataLogic = new PuntosDataLogic(session);
            
            // Inicializar tablas si no existen
            this.puntosDataLogic.initTables();
        } catch (BasicException e) {
            System.err.println("Error inicializando sistema de puntos: " + e.getMessage());
        }
    }
    
    /**
     * Sebastian - Asegura que dlCustomers esté inicializado
     */
    private void ensureDlCustomersInitialized() {
        if (this.dlCustomers == null && app != null) {
            try {
                this.dlCustomers = (DataLogicCustomers) app.getBean("com.openbravo.pos.customers.DataLogicCustomers");
            } catch (Exception e) {
                System.err.println("Error inicializando dlCustomers: " + e.getMessage());
            }
        }
    }

    @Override
    public void activate() throws BasicException {     
        super.activate();
        ensureDlCustomersInitialized(); // Asegurar inicialización antes de usar
        jeditor.activate();     
        try {
            com.openbravo.pos.sync.VoltiumSyncService.sincronizarClientesAsync();
        } catch (Exception ignored) {}
    }

    @Override
    public ListProvider getListProvider() {
        ensureDlCustomersInitialized(); // Asegurar inicialización antes de usar
        if (dlCustomers == null) {
            throw new IllegalStateException("DataLogicCustomers no está inicializado");
        }
        return new ListProviderCreator(dlCustomers.getTableCustomers());
    }

    @Override
    public SaveProvider getSaveProvider() {
        ensureDlCustomersInitialized(); // Asegurar inicialización antes de usar
        if (dlCustomers == null) {
            throw new IllegalStateException("DataLogicCustomers no está inicializado");
        }
        final SaveProvider orig = dlCustomers.getCustomerSaveProvider();
        return new SaveProvider() {
            @Override
            public boolean canDelete() {
                return orig.canDelete();
            }

            @Override
            public boolean canInsert() {
                return orig.canInsert();
            }

            @Override
            public boolean canUpdate() {
                return orig.canUpdate();
            }

            @Override
            public int insertData(Object value) throws BasicException {
                int r = orig.insertData(value);
                com.openbravo.pos.sync.VoltiumSyncService.sincronizarClientesAsync();
                return r;
            }

            @Override
            public int updateData(Object value) throws BasicException {
                int r = orig.updateData(value);
                com.openbravo.pos.sync.VoltiumSyncService.sincronizarClientesAsync();
                return r;
            }

            @Override
            public int deleteData(Object value) throws BasicException {
                return orig.deleteData(value);
            }
        };
    }

    @Override
    public Vectorer getVectorer() {
        ensureDlCustomersInitialized(); // Asegurar inicialización antes de usar
        if (dlCustomers == null) {
            throw new IllegalStateException("DataLogicCustomers no está inicializado");
        }
        return dlCustomers.getTableCustomers().getVectorerBasic(new int[]{1, 2, 3, 4});
    }

    @Override
    public ComparatorCreator getComparatorCreator() {
        ensureDlCustomersInitialized(); // Asegurar inicialización antes de usar
        if (dlCustomers == null) {
            throw new IllegalStateException("DataLogicCustomers no está inicializado");
        }
        return dlCustomers.getTableCustomers().getComparatorCreator(new int[] {1, 2, 3, 4});
    }

    @Override
    public ListCellRenderer getListCellRenderer() {
        ensureDlCustomersInitialized(); // Asegurar inicialización antes de usar
        if (dlCustomers == null) {
            throw new IllegalStateException("DataLogicCustomers no está inicializado");
        }
        return new ListCellRendererBasic(dlCustomers.getTableCustomers().getRenderStringBasic(new int[]{3}));
    }
    
    // Sebastian - Agregar botón de puntos en la barra de herramientas
    @Override
    public Component getToolbarExtras() {
        JButton btnPuntos = new JButton("Puntos");
        btnPuntos.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 12));
        btnPuntos.setBackground(new Color(202, 159, 65)); // Oro
        btnPuntos.setForeground(Color.WHITE);
        btnPuntos.setFocusPainted(false);
        btnPuntos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 140, 50), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btnPuntos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnPuntos.setToolTipText("Gestionar sistema de puntos de clientes");
        
        btnPuntos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnPuntos.setBackground(new Color(220, 175, 75)); // Hover gold
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnPuntos.setBackground(new Color(202, 159, 65));
            }
        });

        btnPuntos.addActionListener(e -> abrirVentanaPuntos());
        
        return btnPuntos;
    }
    
    /**
     * Sebastian - Abre la ventana de configuración de puntos REAL - Sin ejemplos
     */
    private void abrirVentanaPuntos() {
        try {
            // Cargar configuración actual
            PuntosConfiguracion configActual = puntosDataLogic.getConfiguracionActiva();
            Color creamBg = new Color(250, 247, 242);
            Color goldColor = new Color(202, 159, 65);
            Color slateColor = new Color(100, 116, 139);
            Color crimsonColor = new Color(190, 18, 60);

            JDialog ventanaPuntos = new JDialog();
            ventanaPuntos.setTitle("Sistema de Puntos - Configuración Real");
            ventanaPuntos.setSize(650, 520);
            ventanaPuntos.setLocationRelativeTo(this);
            ventanaPuntos.setModal(true);
            
            JPanel panelPrincipal = new JPanel(new BorderLayout());
            panelPrincipal.setBackground(creamBg);
            panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Panel de título
            JPanel panelTitulo = new JPanel();
            panelTitulo.setBackground(creamBg);
            JLabel titulo = new JLabel("⚙️ CONFIGURACIÓN SISTEMA DE PUNTOS");
            titulo.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 20));
            titulo.setForeground(goldColor);
            panelTitulo.add(titulo);
            
            // Panel principal de configuración
            JPanel panelConfiguracion = new JPanel(new GridLayout(8, 2, 15, 15));
            panelConfiguracion.setBackground(Color.WHITE);
            panelConfiguracion.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                    "Configuración del Sistema",
                    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION,
                    ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 12),
                    new Color(51, 65, 85)
            ));
            
            // Configuración principal
            JLabel lblMonto = new JLabel("💰 Monto requerido (MX):");
            lblMonto.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 12));
            panelConfiguracion.add(lblMonto);
            JTextField txtMontoRequerido = new JTextField(String.valueOf(configActual.getMontoPorPunto()));
            txtMontoRequerido.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 13));
            txtMontoRequerido.setToolTipText("Ejemplo: 400.00 para otorgar puntos cada $400 MX");
            panelConfiguracion.add(txtMontoRequerido);
            
            JLabel lblPuntos = new JLabel("⭐ Puntos a otorgar:");
            lblPuntos.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 12));
            panelConfiguracion.add(lblPuntos);
            JTextField txtCantidadPuntos = new JTextField(String.valueOf(configActual.getPuntosOtorgados()));
            txtCantidadPuntos.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 13));
            txtCantidadPuntos.setToolTipText("Ejemplo: 10 puntos por cada $400 MX");
            panelConfiguracion.add(txtCantidadPuntos);
            
            JLabel lblMoneda = new JLabel("💱 Moneda:");
            lblMoneda.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 12));
            panelConfiguracion.add(lblMoneda);
            JTextField txtMoneda = new JTextField(configActual.getMoneda());
            txtMoneda.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 13));
            panelConfiguracion.add(txtMoneda);
            
            // Sebastian - Campo para límite diario de puntos
            JLabel lblLimite = new JLabel("🚫 Límite diario de puntos:");
            lblLimite.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 12));
            panelConfiguracion.add(lblLimite);
            JTextField txtLimiteDiario = new JTextField(String.valueOf(configActual.getLimiteDiarioPuntos()));
            txtLimiteDiario.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 13));
            txtLimiteDiario.setToolTipText("Máximo de puntos que un cliente puede ganar por día");
            panelConfiguracion.add(txtLimiteDiario);
            
            JLabel lblActivo = new JLabel("✅ Sistema activado:");
            lblActivo.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 12));
            panelConfiguracion.add(lblActivo);
            javax.swing.JCheckBox chkSistemaActivo = new javax.swing.JCheckBox("Activar sistema automático en ventas");
            chkSistemaActivo.setSelected(configActual.isSistemaActivo());
            chkSistemaActivo.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 12));
            chkSistemaActivo.setBackground(Color.WHITE);
            panelConfiguracion.add(chkSistemaActivo);
            
            panelConfiguracion.add(new JLabel("📊 Configuración actual:"));
            JLabel lblEjemplo = new JLabel(String.format("$%.2f %s = %d puntos", 
                configActual.getMontoPorPunto(), configActual.getMoneda(), configActual.getPuntosOtorgados()));
            lblEjemplo.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 13));
            lblEjemplo.setForeground(goldColor);
            panelConfiguracion.add(lblEjemplo);
            
            // Botones de acción (negrita y tamaño 14 para mejor lectura sobre colores vivos)
            JButton btnActualizarEjemplo = new JButton("🔄 Actualizar Vista");
            btnActualizarEjemplo.setBackground(slateColor);
            btnActualizarEjemplo.setForeground(Color.WHITE);
            btnActualizarEjemplo.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 12));
            btnActualizarEjemplo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            panelConfiguracion.add(btnActualizarEjemplo);
            
            JButton btnGuardarConfig = new JButton("💾 Guardar Configuración");
            btnGuardarConfig.setBackground(goldColor);
            btnGuardarConfig.setForeground(Color.WHITE);
            btnGuardarConfig.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 12));
            btnGuardarConfig.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            panelConfiguracion.add(btnGuardarConfig);
            
            // Botón para recrear tablas (troubleshooting)
            JButton btnRecrearTablas = new JButton("🔧 Recrear Tablas");
            btnRecrearTablas.setBackground(crimsonColor);
            btnRecrearTablas.setForeground(Color.WHITE);
            btnRecrearTablas.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 11));
            btnRecrearTablas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnRecrearTablas.setToolTipText("Fuerza la recreación de las tablas de puntos (usar solo si hay problemas)");
            panelConfiguracion.add(btnRecrearTablas);
            
            // Panel de asignación manual de puntos
            JPanel panelAsignacion = new JPanel(new GridLayout(6, 2, 15, 15));
            panelAsignacion.setBackground(Color.WHITE);
            panelAsignacion.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                    "Asignación Manual de Puntos",
                    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION,
                    ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 12),
                    new Color(51, 65, 85)
            ));
            
            JLabel lblCliId = new JLabel("🆔 ID del Cliente:");
            lblCliId.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 12));
            panelAsignacion.add(lblCliId);
            JTextField txtClienteId = new JTextField();
            txtClienteId.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 13));
            txtClienteId.setToolTipText("ID único del cliente en el sistema");
            panelAsignacion.add(txtClienteId);
            
            JLabel lblDesc = new JLabel("📦 Producto/Descripción:");
            lblDesc.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 12));
            panelAsignacion.add(lblDesc);
            JTextField txtDescripcion = new JTextField();
            txtDescripcion.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 13));
            txtDescripcion.setToolTipText("Nombre del producto o descripción de la asignación");
            panelAsignacion.add(txtDescripcion);
            
            JLabel lblPtsAsig = new JLabel("⭐ Puntos a asignar:");
            lblPtsAsig.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 12));
            panelAsignacion.add(lblPtsAsig);
            JTextField txtPuntosAsignar = new JTextField("0");
            txtPuntosAsignar.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 13));
            txtPuntosAsignar.setToolTipText("Cantidad específica de puntos para este producto");
            panelAsignacion.add(txtPuntosAsignar);
            
            JLabel lblCons = new JLabel("🔍 Consultar Cliente:");
            lblCons.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 12));
            panelAsignacion.add(lblCons);
            JButton btnConsultarCliente = new JButton("Ver Puntos Actuales");
            btnConsultarCliente.setBackground(slateColor);
            btnConsultarCliente.setForeground(Color.WHITE);
            btnConsultarCliente.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 11));
            btnConsultarCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            panelAsignacion.add(btnConsultarCliente);
            
            JLabel lblAcc = new JLabel("➕ Acción:");
            lblAcc.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 12));
            panelAsignacion.add(lblAcc);
            JButton btnAsignarPuntos = new JButton("Asignar Puntos");
            btnAsignarPuntos.setBackground(goldColor);
            btnAsignarPuntos.setForeground(Color.WHITE);
            btnAsignarPuntos.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 12));
            btnAsignarPuntos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            panelAsignacion.add(btnAsignarPuntos);
            
            // Panel informativo
            JPanel panelInfo = new JPanel(new BorderLayout());
            panelInfo.setBackground(Color.WHITE);
            panelInfo.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                    "Información del Sistema",
                    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION,
                    ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 12),
                    new Color(51, 65, 85)
            ));
            
            JTextArea txtInfo = new JTextArea(4, 50);
            txtInfo.setEditable(false);
            txtInfo.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.PLAIN, 12));
            txtInfo.setText(
                "FUNCIONAMIENTO:\n" +
                "• El sistema se conectará automáticamente con el módulo de ventas\n" +
                "• Los puntos se asignarán automáticamente según la configuración establecida\n" +
                "• También puedes asignar puntos manualmente por productos específicos\n" +
                "• Los datos se guardan en la base de datos del sistema"
            );
            txtInfo.setBackground(new java.awt.Color(245, 245, 245));
            
            javax.swing.JScrollPane scrollInfo = new javax.swing.JScrollPane(txtInfo);
            panelInfo.add(scrollInfo, BorderLayout.CENTER);
            
            // Panel de botones principales
            JPanel panelBotones = new JPanel(new FlowLayout());
            panelBotones.setBackground(creamBg);
            
            JButton btnCerrar = new JButton("❌ Cerrar");
            btnCerrar.setFont(ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 12));
            btnCerrar.setBackground(slateColor);
            btnCerrar.setForeground(Color.WHITE);
            btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            
            panelBotones.add(btnCerrar);
            
            // EVENTOS DE BOTONES REALES
            
            // Actualizar vista en tiempo real
            btnActualizarEjemplo.addActionListener(e -> {
                try {
                    double monto = Double.parseDouble(txtMontoRequerido.getText());
                    int puntos = Integer.parseInt(txtCantidadPuntos.getText());
                    String moneda = txtMoneda.getText();
                    lblEjemplo.setText(String.format("$%.2f %s = %d puntos", monto, moneda, puntos));
                } catch (NumberFormatException ex) {
                    lblEjemplo.setText("❌ Error en valores numéricos");
                }
            });
            
            // Guardar configuración REAL
            btnGuardarConfig.addActionListener(e -> {
                try {
                    // Parsear monto aceptando coma o punto decimal
                    String montoTexto = txtMontoRequerido.getText().trim().replace(",", ".");
                    double monto = Double.parseDouble(montoTexto);
                    
                    int puntos = Integer.parseInt(txtCantidadPuntos.getText().trim());
                    String moneda = txtMoneda.getText().trim();
                    int limiteDiario = Integer.parseInt(txtLimiteDiario.getText().trim()); // Sebastian - Nuevo campo
                    boolean activo = chkSistemaActivo.isSelected();
                    
                    if (monto <= 0 || puntos <= 0 || limiteDiario <= 0 || moneda.isEmpty()) {
                        JOptionPane.showMessageDialog(ventanaPuntos,
                            "❌ Todos los campos deben tener valores válidos:\n• Monto > 0\n• Puntos > 0\n• Límite diario > 0\n• Moneda no vacía",
                            "Datos Inválidos",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    configActual.setMontoPorPunto(monto);
                    configActual.setPuntosOtorgados(puntos);
                    configActual.setMoneda(moneda);
                    configActual.setLimiteDiarioPuntos(limiteDiario); // Sebastian - Nuevo campo
                    configActual.setSistemaActivo(activo);
                    
                    puntosDataLogic.updateConfiguracion(configActual);
                    
                    JOptionPane.showMessageDialog(ventanaPuntos,
                        String.format("✅ Configuración guardada exitosamente:\n\n" +
                                    "💰 Monto: $%.2f %s\n" +
                                    "⭐ Puntos: %d\n" +
                                    "🚫 Límite diario: %d puntos\n" +
                                    "🔄 Sistema: %s\n\n" +
                                    "Esta configuración se aplicará automáticamente en las ventas.", 
                                    monto, moneda, puntos, limiteDiario,
                                    activo ? "ACTIVO" : "INACTIVO"),
                        "Configuración Guardada",
                        JOptionPane.INFORMATION_MESSAGE);
                        
                    // Actualizar vista
                    lblEjemplo.setText(String.format("$%.2f %s = %d puntos", monto, moneda, puntos));
                        
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ventanaPuntos,
                        "❌ Error en formato numérico:\n• Monto debe ser un número decimal\n• Puntos debe ser un número entero",
                        "Error de Formato",
                        JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ventanaPuntos,
                        "❌ Error guardando configuración: " + ex.getMessage(),
                        "Error del Sistema",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            
            // Recrear tablas de puntos (troubleshooting)
            btnRecrearTablas.addActionListener(e -> {
                int confirmacion = JOptionPane.showConfirmDialog(ventanaPuntos,
                    "⚠️ ADVERTENCIA ⚠️\n\n" +
                    "Esta acción recreará completamente las tablas de puntos.\n" +
                    "Se perderán TODOS los datos de puntos existentes.\n\n" +
                    "¿Estás seguro de continuar?",
                    "Confirmar Recreación de Tablas",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                    
                if (confirmacion == JOptionPane.YES_OPTION) {
                    try {
                        puntosDataLogic.forzarCreacionTablas();
                        JOptionPane.showMessageDialog(ventanaPuntos,
                            "✅ Tablas de puntos recreadas exitosamente.\n\n" +
                            "El sistema está listo para funcionar.",
                            "Tablas Recreadas",
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ventanaPuntos,
                            "❌ Error recreando tablas: " + ex.getMessage(),
                            "Error del Sistema",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            // Consultar puntos de cliente REAL
            btnConsultarCliente.addActionListener(e -> {
                try {
                    String clienteId = txtClienteId.getText().trim();
                    if (clienteId.isEmpty()) {
                        JOptionPane.showMessageDialog(ventanaPuntos,
                            "❌ Ingresa el ID del cliente",
                            "Campo Requerido",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    ClientePuntos puntos = puntosDataLogic.getClientePuntos(clienteId);
                    if (puntos != null) {
                        JOptionPane.showMessageDialog(ventanaPuntos,
                            String.format("📊 INFORMACIÓN DEL CLIENTE: %s\n\n" +
                                        "⭐ Puntos actuales: %d\n" +
                                        "🏆 Puntos totales acumulados: %d\n" +
                                        "📝 Última transacción: %s\n" +
                                        "📅 Fecha última transacción: %s", 
                                        clienteId, 
                                        puntos.getPuntosActuales(),
                                        puntos.getPuntosTotales(),
                                        puntos.getUltimaTransaccion() != null ? puntos.getUltimaTransaccion() : "Ninguna",
                                        puntos.getFechaUltimaTransaccion() != null ? puntos.getFechaUltimaTransaccion().toString() : "N/A"),
                            "Consulta de Puntos",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ventanaPuntos,
                            String.format("ℹ️ El cliente '%s' no tiene puntos registrados.\n\nSe creará un registro automáticamente cuando realice compras.", clienteId),
                            "Cliente Sin Puntos",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ventanaPuntos,
                        "❌ Error consultando puntos: " + ex.getMessage(),
                        "Error del Sistema",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            
            // Asignar puntos manualmente REAL
            btnAsignarPuntos.addActionListener(e -> {
                try {
                    String clienteId = txtClienteId.getText().trim();
                    String descripcion = txtDescripcion.getText().trim();
                    int puntosAsignar = Integer.parseInt(txtPuntosAsignar.getText());
                    
                    if (clienteId.isEmpty() || descripcion.isEmpty() || puntosAsignar <= 0) {
                        JOptionPane.showMessageDialog(ventanaPuntos,
                            "❌ Completa todos los campos:\n• ID del cliente\n• Descripción del producto\n• Puntos > 0",
                            "Campos Incompletos",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Confirmar asignación
                    int confirmacion = JOptionPane.showConfirmDialog(ventanaPuntos,
                        String.format("¿Confirmas asignar %d puntos al cliente '%s' por '%s'?", 
                                    puntosAsignar, clienteId, descripcion),
                        "Confirmar Asignación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                    
                    if (confirmacion == JOptionPane.YES_OPTION) {
                        puntosDataLogic.agregarPuntosPorProducto(clienteId, descripcion, puntosAsignar);
                        
                        JOptionPane.showMessageDialog(ventanaPuntos,
                            String.format("✅ PUNTOS ASIGNADOS EXITOSAMENTE\n\n" +
                                        "👤 Cliente: %s\n" +
                                        "📦 Producto: %s\n" +
                                        "⭐ Puntos: %d\n\n" +
                                        "Los puntos han sido guardados en la base de datos.", 
                                        clienteId, descripcion, puntosAsignar),
                            "Asignación Exitosa",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Limpiar campos después de asignación exitosa
                        txtDescripcion.setText("");
                        txtPuntosAsignar.setText("0");
                    }
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ventanaPuntos,
                        "❌ Los puntos deben ser un número entero válido",
                        "Error de Formato",
                        JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ventanaPuntos,
                        "❌ Error asignando puntos: " + ex.getMessage(),
                        "Error del Sistema",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            
            btnCerrar.addActionListener(e -> ventanaPuntos.dispose());
            
            // Ensamblar la ventana
            JPanel panelContenido = new JPanel(new BorderLayout(10, 10));
            panelContenido.setBackground(creamBg);
            panelContenido.add(panelConfiguracion, BorderLayout.NORTH);
            panelContenido.add(panelAsignacion, BorderLayout.CENTER);
            panelContenido.add(panelInfo, BorderLayout.SOUTH);
            
            panelPrincipal.add(panelTitulo, BorderLayout.NORTH);
            panelPrincipal.add(panelContenido, BorderLayout.CENTER);
            panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
            
            ventanaPuntos.add(panelPrincipal);
            
            // Aplicar estilo moderno al diálogo
            ModernLookAndFeel.aplicarEstiloModernoADialogo(ventanaPuntos);
            
            ventanaPuntos.setVisible(true);
            
        } catch (BasicException e) {
            JOptionPane.showMessageDialog(this,
                "❌ Error inicializando sistema de puntos: " + e.getMessage(),
                "Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public EditorRecord getEditor() {
        return jeditor;
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.CustomersManagement");
    }    
}
