package com.openbravo.pos.sync;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.ticket.TicketTaxInfo;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio nativo de sincronización automática en segundo plano entre el POS
 * local (Kriolos POS / Bicis MX) y el panel CRM-IA para la subcuenta "voltium-sanrey".
 *
 * Funciona de forma 100% asíncrona, tolerante a fallos y transparente para el cajero:
 * - No bloquea la interfaz gráfica ni la impresión de tickets.
 * - Registra la venta con su desglose, cliente, costo de mercadería y ganancia neta.
 * - Sincroniza bidireccionalmente productos y clientes entre CRM-IA y el POS local.
 * - Actualiza stock en PRODUCTS y STOCKCURRENT para el catálogo táctil de ventas.
 * - Si no hay internet, opera en modo 100% offline (circuit-breaker) encolando localmente en < 5ms.
 * - Al reconectar, vacía automáticamente los tickets y sincroniza datos faltantes.
 */
public class VoltiumSyncService {

    private static final Logger LOGGER = Logger.getLogger(VoltiumSyncService.class.getName());
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

    // Configuración por defecto del panel en el servidor
    private static final String DEFAULT_HOST = "2.24.100.82";
    private static final int DEFAULT_PORT = 5432;
    private static final String DEFAULT_DB = "tienda";
    private static final String DEFAULT_USER = "posgrest";
    private static final String DEFAULT_AGENCY = "voltium-sanrey";

    // Sesión a la base de datos local POS
    private static volatile Session localSession;

    // Pool de hilos en segundo plano para no congelar la caja
    private static final ExecutorService SYNC_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Voltium-Sync-Worker");
        t.setDaemon(true);
        return t;
    });

    // Reintentador automático de ventas fuera de línea y sync periódico cada 60 segundos
    private static final ScheduledExecutorService RETRY_EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Voltium-Offline-Retry-Worker");
        t.setDaemon(true);
        return t;
    });

    private static final Object QUEUE_LOCK = new Object();
    private static volatile boolean initialized = false;

    // Control de estado de conectividad (Circuit-Breaker para modo 100% offline)
    private static volatile long lastConnectionFailure = 0L;
    private static final long OFFLINE_COOLDOWN_MS = 30_000L; // 30 segundos de espera tras fallo de conexión

    public static boolean isOfflineMode() {
        return (System.currentTimeMillis() - lastConnectionFailure) < OFFLINE_COOLDOWN_MS;
    }

    public static void markOffline() {
        lastConnectionFailure = System.currentTimeMillis();
    }

    public static void markOnline() {
        lastConnectionFailure = 0L;
    }

    /**
     * Generador de fechas en formato ISO-8601 UTC thread-safe sin contención de bloqueos.
     */
    public static String formatIsoUtc(Date date) {
        if (date == null) {
            date = new Date();
        }
        return Instant.ofEpochMilli(date.getTime()).toString();
    }

    static {
        init();
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        // Registrar driver PostgreSQL
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Driver PostgreSQL no disponible: " + e.getMessage());
        }

        // Programar sincronización periódica completa (cola offline, clientes y productos) cada 60s
        RETRY_EXECUTOR.scheduleWithFixedDelay(() -> {
            try {
                procesarSincronizacionCompleta();
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Error en ciclo periódico de sincronización Voltium: " + t.getMessage());
            }
        }, 15, 60, TimeUnit.SECONDS);

        LOGGER.info("VoltiumSyncService inicializado correctamente con subcuenta 'voltium-sanrey'.");
    }

    /**
     * Registra la sesión local de base de datos de Kriolos POS.
     * Programa la sincronización inicial con una espera de 5 segundos para permitir
     * que Liquibase y la inicialización de la app concluyan sin bloqueos.
     */
    public static void setLocalSession(Session session) {
        localSession = session;
        LOGGER.info("[VoltiumSync] Sesión local POS registrada. Sincronización programada en 5 segundos...");
        RETRY_EXECUTOR.schedule(() -> {
            try {
                procesarSincronizacionCompleta();
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[VoltiumSync] Error en sincronización inicial tras setLocalSession: " + t.getMessage());
            }
        }, 5, TimeUnit.SECONDS);
    }

    public static Session getLocalSession() {
        return localSession;
    }

    /**
     * Modelo interno para transferir datos de la venta.
     */
    public static class TicketPayload {
        public String orderId;
        public String agencyId;
        public int ticketId;
        public int ticketType; // 0=normal, 1=devolución/cancelación
        public String fecha;
        public double total;
        public double subtotal;
        public double tax;
        public double totalCost;
        public double totalProfit;
        public String cliente;
        public String customerId;
        public String customerEmail;
        public String customerPhone;
        public String cajero;
        public String metodoPago;
        public List<PaymentPayload> pagos = new ArrayList<>();
        public int retryCount = 0;
        public List<ItemPayload> items = new ArrayList<>();
        public List<Map<String, Object>> taxes = new ArrayList<>();
    }

    public static class PaymentPayload {
        public String metodo;
        public double monto;
    }

    public static class ItemPayload {
        public String id;
        public String productId;
        public String name;
        public String title;
        public String productName;
        public double quantity;
        public double price;
        public double cost;
        public double subtotal;
        public double tax;
        public double taxRate;
        public double totalCost;
        public double profit;
        public String category;
    }

    public static class ExpensePayload {
        public String id;
        public String agencyId;
        public String fecha;
        public double monto;
        public String tipo; // "egreso" o "ingreso"
        public String categoria; // "Operativos", "Caja Menor", "Mercadería", "Servicios", etc.
        public String concepto;
        public String metodoPago;
        public String responsable;
        public String notas;
        public String referencia;
        public int retryCount = 0;
    }

    public static class PayrollPayload {
        public String payrollId;
        public String agencyId;
        public String employeeId;
        public String employeeName;
        public String periodLabel;
        public String periodStart;
        public String periodEnd;
        public String paymentDate;
        public double baseSalary;
        public double commissions;
        public double allowances;
        public double bonusAmount;
        public double grossAmount;
        public double deductions;
        public double netAmount;
        public String paymentMethod;
        public String status;
        public String notes;
        public String processedBy;
        public int retryCount = 0;
    }

    public static class EmployeePayload {
        public String employeeId;
        public String agencyId;
        public String employeeCode;
        public String name;
        public String role;
        public String area;
        public double salary;
        public String date;
        public String status;
        public String email;
        public String phone;
    }

    /**
     * Sincroniza una venta de forma asíncrona inmediatamente después de que se guarda el ticket.
     */
    public static void sincronizarVentaAsync(final TicketInfo ticket, final DataLogicSales dlSales) {
        if (ticket == null) {
            return;
        }

        SYNC_EXECUTOR.submit(() -> {
            try {
                TicketPayload payload = construirPayload(ticket, dlSales, false);
                enviarOEncolar(payload);
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Error inesperado al construir/enviar venta a Voltium: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Sincroniza una cancelación o devolución de ticket de forma asíncrona.
     */
    public static void sincronizarCancelacionAsync(final TicketInfo ticket, final DataLogicSales dlSales) {
        if (ticket == null) {
            return;
        }

        SYNC_EXECUTOR.submit(() -> {
            try {
                TicketPayload payload = construirPayload(ticket, dlSales, true);
                enviarOEncolar(payload);
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Error inesperado al cancelar venta en Voltium: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Sincroniza una nómina de pago con la contabilidad y gastos del panel Voltium Sanrey de forma asíncrona.
     */
    public static void sincronizarNominaAsync(final PayrollPayload p) {
        if (p == null) {
            return;
        }

        SYNC_EXECUTOR.submit(() -> {
            try {
                if (isOfflineMode()) {
                    LOGGER.info("[VoltiumSync] Modo fuera de línea activo. Encolando nómina #" + p.payrollId + " localmente.");
                    encolarNominaLocal(p);
                    return;
                }
                try {
                    guardarNominaEnPostgres(p);
                    markOnline();
                } catch (Exception e) {
                    markOffline();
                    LOGGER.log(Level.WARNING, "[VoltiumSync] Error al enviar nómina a PostgreSQL (" + e.getMessage() + "). Guardando en cola offline.");
                    encolarNominaLocal(p);
                }
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Error inesperado sincronizando nómina con contabilidad: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Elimina una nómina de la contabilidad y gastos del panel Voltium Sanrey de forma asíncrona.
     */
    public static void eliminarNominaAsync(final String payrollId) {
        if (payrollId == null || payrollId.trim().isEmpty()) {
            return;
        }

        SYNC_EXECUTOR.submit(() -> {
            try {
                // Quitar de la cola offline si estuviera pendiente
                synchronized (PAYROLL_QUEUE_LOCK) {
                    try {
                        File file = getPayrollQueueFile();
                        List<PayrollPayload> queue = leerColaOfflineNominas(file);
                        boolean removed = queue.removeIf(item -> item.payrollId != null && item.payrollId.equals(payrollId));
                        if (removed) {
                            escribirColaOfflineNominas(file, queue);
                        }
                    } catch (Exception ignored) {}
                }

                if (!isOfflineMode()) {
                    try {
                        eliminarNominaRemota(payrollId);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "[VoltiumSync] Error al eliminar nómina remota: " + e.getMessage());
                    }
                }
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[VoltiumSync] Error en eliminarNominaAsync: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Sincroniza un expediente de empleado con erp_rrhh en el panel de forma asíncrona.
     */
    public static void sincronizarEmpleadoRRHHAsync(final EmployeePayload emp) {
        if (emp == null) {
            return;
        }

        SYNC_EXECUTOR.submit(() -> {
            try {
                if (!isOfflineMode()) {
                    try {
                        guardarEmpleadoEnPostgres(emp);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "[VoltiumSync] Error sincronizando empleado en erp_rrhh: " + e.getMessage());
                    }
                }
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[VoltiumSync] Error en sincronizarEmpleadoRRHHAsync: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Sincroniza los clientes entre CRM y el POS local de forma asíncrona en segundo plano.
     */
    public static void sincronizarClientesAsync() {
        SYNC_EXECUTOR.submit(() -> {
            try {
                sincronizarClientes();
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[VoltiumSync] Error en tarea asíncrona de clientes: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Ejecuta la sincronización bidireccional de clientes entre CRM (PostgreSQL) y POS local (HSQLDB).
     */
    public static synchronized void sincronizarClientes() {
        if (localSession == null) {
            return;
        }

        Connection localConn = null;
        try {
            localConn = localSession.getConnection();
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[VoltiumSync] Conexión local no disponible para sync clientes: " + e.getMessage());
            return;
        }

        if (localConn == null) {
            return;
        }

        String pgUrl = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=8";

        try (Connection remoteConn = DriverManager.getConnection(pgUrl, getUser(), getPass())) {
            markOnline();

            synchronized (localSession) {
                // 1. Descargar clientes desde CRM hacia el POS local
                descargarClientesDesdeCRM(remoteConn, localConn);

                // 2. Subir clientes creados localmente en el POS hacia CRM
                subirClientesLocalesACRM(remoteConn, localConn);
            }

        } catch (SQLException e) {
            markOffline();
            LOGGER.log(Level.FINE, "[VoltiumSync] Servidor remoto no disponible para sincronización de clientes: " + e.getMessage());
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] Error general en sincronización de clientes: " + t.getMessage(), t);
        }
    }

    /**
     * Sincroniza los proveedores entre CRM y el POS local de forma asíncrona en segundo plano.
     */
    public static void sincronizarProveedoresAsync() {
        SYNC_EXECUTOR.submit(() -> {
            try {
                sincronizarProveedores();
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[VoltiumSync] Error en tarea asíncrona de proveedores: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Ejecuta la sincronización bidireccional de proveedores entre CRM (PostgreSQL) y POS local (HSQLDB).
     */
    public static synchronized void sincronizarProveedores() {
        if (localSession == null || isOfflineMode()) {
            return;
        }

        Connection localConn = null;
        try {
            localConn = localSession.getConnection();
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[VoltiumSync] Conexión local no disponible para sync proveedores: " + e.getMessage());
            return;
        }

        if (localConn == null) {
            return;
        }

        String pgUrl = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=8";

        try (Connection remoteConn = DriverManager.getConnection(pgUrl, getUser(), getPass())) {
            markOnline();

            synchronized (localSession) {
                // 1. Descargar proveedores desde CRM hacia el POS local
                descargarProveedoresDesdeCRM(remoteConn, localConn);

                // 2. Subir proveedores creados localmente en el POS hacia CRM
                subirProveedoresLocalesACRM(remoteConn, localConn);
            }

        } catch (SQLException e) {
            markOffline();
            LOGGER.log(Level.FINE, "[VoltiumSync] Servidor remoto no disponible para sincronización de proveedores: " + e.getMessage());
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] Error general en sincronización de proveedores: " + t.getMessage(), t);
        }
    }

    /**
     * Sincroniza los productos entre CRM y el POS local de forma asíncrona en segundo plano.
     */
    public static void sincronizarProductosAsync() {
        SYNC_EXECUTOR.submit(() -> {
            try {
                sincronizarProductos();
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[VoltiumSync] Error en tarea asíncrona de productos: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Ejecuta la sincronización bidireccional de productos entre CRM (PostgreSQL) y POS local (HSQLDB).
     * Retorna el número de productos procesados/descargados, o -1 si no hubo conexión.
     */
    public static synchronized int sincronizarProductos() {
        if (localSession == null) {
            return -1;
        }

        Connection localConn = null;
        try {
            localConn = localSession.getConnection();
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[VoltiumSync] Conexión local no disponible para sync productos: " + e.getMessage());
            return -1;
        }

        if (localConn == null) {
            return -1;
        }

        String pgUrl = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=8";

        int totalCount = 0;
        try (Connection remoteConn = DriverManager.getConnection(pgUrl, getUser(), getPass())) {
            markOnline();

            synchronized (localSession) {
                // 1. Descargar productos de Voltium desde CRM hacia el POS local
                totalCount += descargarProductosDesdeCRM(remoteConn, localConn);

                // 2. Subir productos creados localmente en el POS hacia CRM
                subirProductosLocalesACRM(remoteConn, localConn);
            }

        } catch (SQLException e) {
            markOffline();
            LOGGER.log(Level.FINE, "[VoltiumSync] Servidor remoto no disponible para sincronización de productos: " + e.getMessage());
            return -1;
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] Error general en sincronización de productos: " + t.getMessage(), t);
            return -1;
        }
        return totalCount;
    }

    /**
     * Descarga productos registrados en el CRM (Voltium Sanrey) e inserta/actualiza en la base de datos POS local.
     */
    private static int descargarProductosDesdeCRM(Connection remoteConn, Connection localConn) {
        String agency = getAgencyId();
        String sql = "SELECT id, datos FROM documentos WHERE tabla_nombre = 'products' AND datos LIKE ?";
        int count = 0;

        try (PreparedStatement ps = remoteConn.prepareStatement(sql)) {
            ps.setString(1, "%" + agency + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String remoteId = rs.getString("id");
                    String datosJson = rs.getString("datos");
                    if (remoteId == null || datosJson == null || datosJson.trim().isEmpty()) {
                        continue;
                    }

                    try {
                        Map<String, Object> map = GSON.fromJson(datosJson, new TypeToken<Map<String, Object>>() {}.getType());
                        if (map == null) continue;

                        String agencyInDatos = map.get("agency_id") != null ? String.valueOf(map.get("agency_id")) : null;
                        if (agencyInDatos == null || "null".equals(agencyInDatos) || agencyInDatos.trim().isEmpty()) {
                            agencyInDatos = map.get("agencyId") != null ? String.valueOf(map.get("agencyId")) : null;
                        }
                        if (agencyInDatos == null || !agency.equalsIgnoreCase(agencyInDatos.trim())) {
                            continue;
                        }

                        String name = map.get("name") != null ? String.valueOf(map.get("name")).trim() : "";
                        if (name.isEmpty() && map.get("title") != null) {
                            name = String.valueOf(map.get("title")).trim();
                        }
                        if (name.isEmpty()) {
                            name = remoteId;
                        }
                        if (name.length() > 250) {
                            name = name.substring(0, 250);
                        }

                        // Categoría (preferir nombre amigable si existe)
                        String catName = map.get("category_name") != null ? String.valueOf(map.get("category_name")).trim() : "";
                        if (catName.isEmpty() && map.get("category") != null) {
                            catName = String.valueOf(map.get("category")).trim();
                        }
                        if (catName.isEmpty()) {
                            catName = "General";
                        }

                        String catId = map.get("category_id") != null ? String.valueOf(map.get("category_id")).trim() : "";
                        if (catId.isEmpty() && map.get("category") != null) {
                            catId = String.valueOf(map.get("category")).trim();
                        }
                        if (catId.isEmpty()) {
                            catId = catName.toLowerCase().replaceAll("[^a-z0-9_-]", "-");
                        }
                        if (catId.isEmpty()) {
                            catId = "000";
                            catName = "General";
                        }

                        // Asegurar categoría en CATEGORIES (resolviendo por ID o por Nombre)
                        String finalCatId = asegurarCategoriaLocal(localConn, catId, catName);

                        // Precios de compra y venta
                        double priceSell = 0.0;
                        if (map.get("price") != null) {
                            try {
                                priceSell = Double.parseDouble(String.valueOf(map.get("price")).replace(",", ".").trim());
                            } catch (Exception ignored) {}
                        }
                        double priceBuy = 0.0;
                        if (map.get("costPrice") != null) {
                            try {
                                priceBuy = Double.parseDouble(String.valueOf(map.get("costPrice")).replace(",", ".").trim());
                            } catch (Exception ignored) {}
                        } else if (map.get("cost") != null) {
                            try {
                                priceBuy = Double.parseDouble(String.valueOf(map.get("cost")).replace(",", ".").trim());
                            } catch (Exception ignored) {}
                        }

                        double stock = 0.0;
                        if (map.get("stock") != null) {
                            try {
                                stock = Double.parseDouble(String.valueOf(map.get("stock")).replace(",", ".").trim());
                            } catch (Exception ignored) {}
                        }

                        // Código / Referencia
                        String prodCode = map.get("barcode") != null ? String.valueOf(map.get("barcode")).trim() : "";
                        if (prodCode.isEmpty() && map.get("code") != null) {
                            prodCode = String.valueOf(map.get("code")).trim();
                        }
                        if (prodCode.isEmpty()) {
                            prodCode = remoteId;
                        }
                        String prodRef = map.get("reference") != null ? String.valueOf(map.get("reference")).trim() : "";
                        if (prodRef.isEmpty()) {
                            prodRef = prodCode;
                        }

                        // Validar que code y reference no colisionen con otro producto diferente en HSQLDB
                        String chkCodeSql = "SELECT ID FROM PRODUCTS WHERE (CODE = ? OR REFERENCE = ?) AND ID <> ?";
                        try (PreparedStatement chkCode = localConn.prepareStatement(chkCodeSql)) {
                            chkCode.setString(1, prodCode);
                            chkCode.setString(2, prodRef);
                            chkCode.setString(3, remoteId);
                            try (ResultSet rsCode = chkCode.executeQuery()) {
                                if (rsCode.next()) {
                                    prodCode = remoteId;
                                    prodRef = remoteId;
                                }
                            }
                        }

                        // Verificar si ya existe en PRODUCTS
                        boolean existe = false;
                        String checkSql = "SELECT ID FROM PRODUCTS WHERE ID = ?";
                        try (PreparedStatement chk = localConn.prepareStatement(checkSql)) {
                            chk.setString(1, remoteId);
                            try (ResultSet crs = chk.executeQuery()) {
                                existe = crs.next();
                            }
                        }

                        if (existe) {
                            String updSql = "UPDATE PRODUCTS SET NAME = ?, PRICESELL = ?, PRICEBUY = ?, CATEGORY = ?, DISPLAY = ?, STOCKUNITS = ? WHERE ID = ?";
                            try (PreparedStatement upd = localConn.prepareStatement(updSql)) {
                                upd.setString(1, name);
                                upd.setDouble(2, priceSell);
                                upd.setDouble(3, priceBuy);
                                upd.setString(4, finalCatId);
                                upd.setString(5, name);
                                upd.setDouble(6, stock);
                                upd.setString(7, remoteId);
                                upd.executeUpdate();
                                count++;
                            }
                        } else {
                            String insSql = "INSERT INTO PRODUCTS (ID, REFERENCE, CODE, NAME, PRICEBUY, PRICESELL, CATEGORY, TAXCAT, ISCOM, ISSCALE, ISCONSTANT, PRINTKB, SENDSTATUS, ISSERVICE, DISPLAY, ISVPRICE, ISVERPATRIB, WARRANTY, STOCKUNITS, ACCUMULATES_POINTS) "
                                    + "VALUES (?, ?, ?, ?, ?, ?, ?, '001', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, ?, FALSE, FALSE, FALSE, ?, TRUE)";
                            try (PreparedStatement ins = localConn.prepareStatement(insSql)) {
                                ins.setString(1, remoteId);
                                ins.setString(2, prodRef);
                                ins.setString(3, prodCode);
                                ins.setString(4, name);
                                ins.setDouble(5, priceBuy);
                                ins.setDouble(6, priceSell);
                                ins.setString(7, finalCatId);
                                ins.setString(8, name);
                                ins.setDouble(9, stock);
                                ins.executeUpdate();
                                count++;
                            }
                        }

                        // Actualizar o insertar inventario en STOCKCURRENT para reflejarlo en ventas
                        asegurarStockCurrentLocal(localConn, remoteId, stock);

                        // Asegurar registro en PRODUCTS_CAT para visibilidad en botonera táctil
                        asegurarProductoCatLocal(localConn, remoteId);

                    } catch (Exception ex) {
                        LOGGER.log(Level.FINE, "[VoltiumSync] Error importando producto individual " + remoteId + ": " + ex.getMessage());
                    }
                }
            }
            if (count > 0) {
                LOGGER.info("[VoltiumSync] Productos CRM sincronizados hacia POS local: " + count + " productos procesados.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] Error al descargar productos desde CRM: " + e.getMessage());
        }
        return count;
    }

    /**
     * Asegura que la categoría exista en la tabla local CATEGORIES.
     * Retorna el ID de la categoría (reutiliza categoría existente si el nombre ya existe para evitar colisión de índice único).
     */
    private static String asegurarCategoriaLocal(Connection localConn, String catId, String catName) {
        try {
            // Verificar por ID primero
            String chkById = "SELECT ID FROM CATEGORIES WHERE ID = ?";
            try (PreparedStatement chk = localConn.prepareStatement(chkById)) {
                chk.setString(1, catId);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) {
                        return catId;
                    }
                }
            }

            // Verificar por Nombre para respetar el índice único CATEGORIES_NAME_INX
            String chkByName = "SELECT ID FROM CATEGORIES WHERE LOWER(NAME) = LOWER(?)";
            try (PreparedStatement chk = localConn.prepareStatement(chkByName)) {
                chk.setString(1, catName);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("ID");
                    }
                }
            }

            // Si no existe, insertar nueva categoría
            String insSql = "INSERT INTO CATEGORIES (ID, NAME, CATSHOWNAME) VALUES (?, ?, TRUE)";
            try (PreparedStatement ins = localConn.prepareStatement(insSql)) {
                ins.setString(1, catId);
                ins.setString(2, catName);
                ins.executeUpdate();
                return catId;
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[VoltiumSync] Error asegurando categoría local: " + e.getMessage());
        }
        return catId != null && !catId.isEmpty() ? catId : "000";
    }

    /**
     * Asegura que el stock del producto se actualice en la tabla STOCKCURRENT para que el POS lo reconozca en vivo.
     */
    private static void asegurarStockCurrentLocal(Connection localConn, String productId, double stock) {
        try {
            String locId = "0";
            String getLocSql = "SELECT ID FROM LOCATIONS LIMIT 1";
            try (Statement stmtLoc = localConn.createStatement();
                 ResultSet rsLoc = stmtLoc.executeQuery(getLocSql)) {
                if (rsLoc.next()) {
                    locId = rsLoc.getString("ID");
                }
            }

            boolean existeStock = false;
            String chkStockSql = "SELECT 1 FROM STOCKCURRENT WHERE LOCATION = ? AND PRODUCT = ?";
            try (PreparedStatement chkStock = localConn.prepareStatement(chkStockSql)) {
                chkStock.setString(1, locId);
                chkStock.setString(2, productId);
                try (ResultSet rsStock = chkStock.executeQuery()) {
                    existeStock = rsStock.next();
                }
            }

            if (existeStock) {
                String updStockSql = "UPDATE STOCKCURRENT SET UNITS = ? WHERE LOCATION = ? AND PRODUCT = ?";
                try (PreparedStatement updStock = localConn.prepareStatement(updStockSql)) {
                    updStock.setDouble(1, stock);
                    updStock.setString(2, locId);
                    updStock.setString(3, productId);
                    updStock.executeUpdate();
                }
            } else {
                String insStockSql = "INSERT INTO STOCKCURRENT (LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS) VALUES (?, ?, NULL, ?)";
                try (PreparedStatement insStock = localConn.prepareStatement(insStockSql)) {
                    insStock.setString(1, locId);
                    insStock.setString(2, productId);
                    insStock.setDouble(3, stock);
                    insStock.executeUpdate();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[VoltiumSync] Error actualizando STOCKCURRENT local: " + e.getMessage());
        }
    }

    /**
     * Asegura que el producto esté registrado en PRODUCTS_CAT para que aparezca en el catálogo táctil de ventas.
     */
    private static void asegurarProductoCatLocal(Connection localConn, String productId) {
        try {
            boolean existe = false;
            String chkSql = "SELECT PRODUCT FROM PRODUCTS_CAT WHERE PRODUCT = ?";
            try (PreparedStatement chk = localConn.prepareStatement(chkSql)) {
                chk.setString(1, productId);
                try (ResultSet rs = chk.executeQuery()) {
                    existe = rs.next();
                }
            }
            if (!existe) {
                String insSql = "INSERT INTO PRODUCTS_CAT (PRODUCT, CATORDER) VALUES (?, NULL)";
                try (PreparedStatement ins = localConn.prepareStatement(insSql)) {
                    ins.setString(1, productId);
                    ins.executeUpdate();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[VoltiumSync] Error asegurando PRODUCTS_CAT local: " + e.getMessage());
        }
    }

    /**
     * Sube productos creados localmente en el POS hacia el CRM PostgreSQL para la subcuenta voltium-sanrey.
     * Utiliza consulta previa en bloque para evitar cientos de peticiones remotas lentas.
     */
    private static void subirProductosLocalesACRM(Connection remoteConn, Connection localConn) {
        String agency = getAgencyId();

        // 1. Obtener todos los IDs de productos existentes en CRM para esta agencia de una sola vez
        Set<String> existingRemoteIds = new HashSet<>();
        Map<String, Map<String, Object>> existingRemoteProducts = new HashMap<>();
        String chkRemoteAll = "SELECT id, datos FROM documentos WHERE tabla_nombre = 'products' AND datos LIKE ?";
        try (PreparedStatement chkAll = remoteConn.prepareStatement(chkRemoteAll)) {
            chkAll.setString(1, "%" + agency + "%");
            try (ResultSet rrs = chkAll.executeQuery()) {
                while (rrs.next()) {
                    String rId = rrs.getString("id");
                    String data = rrs.getString("datos");
                    if (rId == null || data == null) continue;
                    Map<String, Object> product = GSON.fromJson(data, new TypeToken<Map<String, Object>>() {}.getType());
                    if (product == null) continue;
                    Object productAgency = product.get("agency_id");
                    if (productAgency == null) productAgency = product.get("agencyId");
                    if (productAgency == null || !agency.equalsIgnoreCase(String.valueOf(productAgency))) continue;
                    existingRemoteIds.add(rId);
                    existingRemoteProducts.put(rId, product);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] No se pudo verificar lista remota de productos: " + e.getMessage());
            return;
        }

        String selSql = "SELECT p.ID, p.REFERENCE, p.CODE, p.NAME, p.PRICEBUY, p.PRICESELL, c.NAME as CATNAME, p.CATEGORY, "
                + "COALESCE((SELECT sc.UNITS FROM STOCKCURRENT sc WHERE sc.PRODUCT = p.ID LIMIT 1), p.STOCKUNITS, 0) as REALSTOCK "
                + "FROM PRODUCTS p LEFT JOIN CATEGORIES c ON p.CATEGORY = c.ID WHERE p.ISSERVICE = FALSE";
        int count = 0;

        try (Statement stmt = localConn.createStatement();
             ResultSet rs = stmt.executeQuery(selSql)) {

            while (rs.next()) {
                String id = rs.getString("ID");
                String name = rs.getString("NAME");
                double priceSell = rs.getDouble("PRICESELL");
                double priceBuy = rs.getDouble("PRICEBUY");
                String catName = rs.getString("CATNAME");
                String catId = rs.getString("CATEGORY");
                double stock = rs.getDouble("REALSTOCK");

                if (id == null || id.trim().isEmpty() || name == null || name.trim().isEmpty()) {
                    continue;
                }

                Map<String, Object> pMap = new LinkedHashMap<>();
                pMap.put("id", id);
                pMap.put("name", name);
                pMap.put("title", name);
                pMap.put("price", priceSell);
                pMap.put("costPrice", priceBuy);
                pMap.put("cost", priceBuy);
                pMap.put("category", catName != null ? catName : "General");
                pMap.put("category_id", catId != null ? catId : "000");
                pMap.put("agency_id", agency);
                pMap.put("agencyId", agency);
                pMap.put("stock", stock);
                pMap.put("is_published", true);
                pMap.put("sync_source", "pos");
                pMap.put("created_at", formatIsoUtc(new Date()));
                pMap.put("updated_at", formatIsoUtc(new Date()));

                if (existingRemoteIds.contains(id)) {
                    Map<String, Object> remoteProduct = existingRemoteProducts.get(id);
                    // El Panel sigue siendo maestro de sus productos. Los creados aquí se mantienen editables desde el POS.
                    if (remoteProduct == null || !"pos".equalsIgnoreCase(String.valueOf(remoteProduct.get("sync_source")))) {
                        continue;
                    }
                    remoteProduct.putAll(pMap);
                    try (PreparedStatement update = remoteConn.prepareStatement(
                            "UPDATE documentos SET datos = ? WHERE tabla_nombre = 'products' AND id = ?")) {
                        update.setString(1, GSON.toJson(remoteProduct));
                        update.setString(2, id);
                        update.executeUpdate();
                        count++;
                    }
                    continue;
                }

                String insRemote = "INSERT INTO documentos (tabla_nombre, id, datos, created_at) "
                        + "VALUES ('products', ?, ?, NOW()) "
                        + "ON CONFLICT (tabla_nombre, id) DO UPDATE SET datos = EXCLUDED.datos";
                try (PreparedStatement ins = remoteConn.prepareStatement(insRemote)) {
                    ins.setString(1, id);
                    ins.setString(2, GSON.toJson(pMap));
                    ins.executeUpdate();
                    existingRemoteIds.add(id);
                    count++;
                }
            }

            if (count > 0) {
                LOGGER.info("[VoltiumSync] Productos locales exportados hacia CRM Voltium: " + count + " nuevos productos.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] Error al subir productos locales hacia CRM: " + e.getMessage());
        }
    }

    /**
     * Descarga contactos registrados en el CRM (Voltium Sanrey) e inserta/actualiza en la tabla CUSTOMERS local.
     */
    private static void descargarClientesDesdeCRM(Connection remoteConn, Connection localConn) {
        String agency = getAgencyId();
        String sql = "SELECT id, datos FROM documentos WHERE tabla_nombre = 'contacts' AND datos LIKE ?";
        int count = 0;

        try (PreparedStatement ps = remoteConn.prepareStatement(sql)) {
            ps.setString(1, "%" + agency + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String remoteId = rs.getString("id");
                    String datosJson = rs.getString("datos");
                    if (remoteId == null || datosJson == null || datosJson.trim().isEmpty()) {
                        continue;
                    }

                    try {
                        Map<String, Object> map = GSON.fromJson(datosJson, new TypeToken<Map<String, Object>>() {}.getType());
                        if (map == null) continue;

                        String agencyInDatos = map.get("agency_id") != null ? String.valueOf(map.get("agency_id")) : null;
                        if (agencyInDatos == null || "null".equals(agencyInDatos) || agencyInDatos.trim().isEmpty()) {
                            agencyInDatos = map.get("agencyId") != null ? String.valueOf(map.get("agencyId")) : null;
                        }
                        if (agencyInDatos == null || !agency.equalsIgnoreCase(agencyInDatos.trim())) {
                            continue;
                        }

                        String name = map.get("name") != null ? String.valueOf(map.get("name")).trim() : "";
                        if (name.isEmpty() && map.get("nombre") != null) {
                            name = String.valueOf(map.get("nombre")).trim();
                        }
                        if (name.isEmpty() && (map.get("first_name") != null || map.get("last_name") != null)) {
                            name = (map.get("first_name") != null ? String.valueOf(map.get("first_name")) : "") + " "
                                    + (map.get("last_name") != null ? String.valueOf(map.get("last_name")) : "");
                            name = name.trim();
                        }

                        String email = map.get("email") != null ? String.valueOf(map.get("email")).trim() : "";
                        String phone = map.get("phone") != null ? String.valueOf(map.get("phone")).trim() : "";
                        if (phone.isEmpty() && map.get("telefono") != null) {
                            phone = String.valueOf(map.get("telefono")).trim();
                        }
                        String address = map.get("company") != null ? String.valueOf(map.get("company")).trim() : "";
                        if (address.isEmpty() && map.get("address") != null) {
                            address = String.valueOf(map.get("address")).trim();
                        }
                        if (address.isEmpty() && map.get("direccion") != null) {
                            address = String.valueOf(map.get("direccion")).trim();
                        }

                        if (name.isEmpty()) {
                            if (!email.isEmpty()) name = email;
                            else if (!phone.isEmpty()) name = phone;
                            else name = "Cliente CRM";
                        }
                        if (name.length() > 250) {
                            name = name.substring(0, 250);
                        }

                        String searchkey = (!phone.isEmpty()) ? phone : remoteId;

                        boolean existe = false;
                        String checkSql = "SELECT ID FROM CUSTOMERS WHERE ID = ?";
                        try (PreparedStatement chk = localConn.prepareStatement(checkSql)) {
                            chk.setString(1, remoteId);
                            try (ResultSet crs = chk.executeQuery()) {
                                existe = crs.next();
                            }
                        }

                        if (existe) {
                            String updSql = "UPDATE CUSTOMERS SET NAME = ?, EMAIL = ?, PHONE = ?, ADDRESS = ?, VISIBLE = TRUE WHERE ID = ?";
                            try (PreparedStatement upd = localConn.prepareStatement(updSql)) {
                                upd.setString(1, name);
                                upd.setString(2, email);
                                upd.setString(3, phone);
                                upd.setString(4, address);
                                upd.setString(5, remoteId);
                                upd.executeUpdate();
                                count++;
                            }
                        } else {
                            // Validar unicidad de SEARCHKEY en HSQLDB para evitar violación de CUSTOMERS_SKEY_INX
                            String checkKeySql = "SELECT ID FROM CUSTOMERS WHERE SEARCHKEY = ?";
                            try (PreparedStatement chkKey = localConn.prepareStatement(checkKeySql)) {
                                chkKey.setString(1, searchkey);
                                try (ResultSet crs = chkKey.executeQuery()) {
                                    if (crs.next()) {
                                        searchkey = remoteId;
                                    }
                                }
                            }

                            String insSql = "INSERT INTO CUSTOMERS (ID, SEARCHKEY, TAXID, NAME, ADDRESS, EMAIL, PHONE, NOTES, VISIBLE, CURDATE, MAXDEBT, CURDEBT, ISVIP, DISCOUNT) "
                                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE, ?, 0.0, 0.0, FALSE, 0.0)";
                            try (PreparedStatement ins = localConn.prepareStatement(insSql)) {
                                ins.setString(1, remoteId);
                                ins.setString(2, searchkey);
                                ins.setString(3, searchkey);
                                ins.setString(4, name);
                                ins.setString(5, address);
                                ins.setString(6, email);
                                ins.setString(7, phone);
                                ins.setString(8, "Sincronizado desde CRM Voltium");
                                ins.setTimestamp(9, new java.sql.Timestamp(System.currentTimeMillis()));
                                ins.executeUpdate();
                                count++;
                            } catch (SQLException ex) {
                                // Reintentar con clave garantizada única si hubo colisión imprevista
                                String uniqueFallbackKey = remoteId + "_" + (System.currentTimeMillis() % 100000);
                                try (PreparedStatement insRetry = localConn.prepareStatement(insSql)) {
                                    insRetry.setString(1, remoteId);
                                    insRetry.setString(2, uniqueFallbackKey);
                                    insRetry.setString(3, uniqueFallbackKey);
                                    insRetry.setString(4, name);
                                    insRetry.setString(5, address);
                                    insRetry.setString(6, email);
                                    insRetry.setString(7, phone);
                                    insRetry.setString(8, "Sincronizado desde CRM Voltium");
                                    insRetry.setTimestamp(9, new java.sql.Timestamp(System.currentTimeMillis()));
                                    insRetry.executeUpdate();
                                    count++;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.FINE, "[VoltiumSync] Error importando contacto individual " + remoteId + ": " + ex.getMessage());
                    }
                }
            }
            if (count > 0) {
                LOGGER.info("[VoltiumSync] Clientes CRM sincronizados hacia POS local: " + count + " clientes procesados.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] Error al descargar clientes desde CRM: " + e.getMessage());
        }
    }

    /**
     * Sube clientes creados localmente en el POS hacia el CRM PostgreSQL para la subcuenta voltium-sanrey.
     * Utiliza consulta previa en bloque para evitar cientos de peticiones remotas lentas.
     */
    private static void subirClientesLocalesACRM(Connection remoteConn, Connection localConn) {
        String agency = getAgencyId();

        // 1. Obtener todos los IDs de contactos existentes en CRM para esta agencia de una sola vez
        Set<String> existingRemoteIds = new HashSet<>();
        String chkRemoteAll = "SELECT id FROM documentos WHERE tabla_nombre = 'contacts' AND datos LIKE ?";
        try (PreparedStatement chkAll = remoteConn.prepareStatement(chkRemoteAll)) {
            chkAll.setString(1, "%" + agency + "%");
            try (ResultSet rrs = chkAll.executeQuery()) {
                while (rrs.next()) {
                    String rId = rrs.getString("id");
                    if (rId != null) {
                        existingRemoteIds.add(rId);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] No se pudo verificar lista remota de clientes: " + e.getMessage());
            return;
        }

        String selSql = "SELECT ID, SEARCHKEY, TAXID, NAME, ADDRESS, EMAIL, PHONE, NOTES FROM CUSTOMERS WHERE VISIBLE = TRUE";
        int count = 0;

        try (Statement stmt = localConn.createStatement();
             ResultSet rs = stmt.executeQuery(selSql)) {

            while (rs.next()) {
                String id = rs.getString("ID");
                String name = rs.getString("NAME");
                String address = rs.getString("ADDRESS");
                String email = rs.getString("EMAIL");
                String phone = rs.getString("PHONE");
                String notes = rs.getString("NOTES");

                if (id == null || id.trim().isEmpty() || name == null || name.trim().isEmpty()) {
                    continue;
                }

                // Si ya existe en CRM, omitir
                if (existingRemoteIds.contains(id)) {
                    continue;
                }

                Map<String, Object> cMap = new LinkedHashMap<>();
                cMap.put("id", id);
                cMap.put("name", name);
                cMap.put("phone", phone != null ? phone : "");
                cMap.put("email", email != null ? email : "");
                cMap.put("company", address != null ? address : "");
                cMap.put("tags", Arrays.asList("Voltium Sanrey", "POS Local"));
                cMap.put("agency_id", agency);
                cMap.put("agencyId", agency);
                cMap.put("avatar", "");
                cMap.put("created_at", formatIsoUtc(new Date()));
                cMap.put("last_activity", formatIsoUtc(new Date()));
                cMap.put("origen", "Punto de Venta Local");
                if (notes != null && !notes.isEmpty()) {
                    cMap.put("notas", notes);
                }

                String insRemote = "INSERT INTO documentos (tabla_nombre, id, datos, created_at) "
                        + "VALUES ('contacts', ?, ?, NOW()) "
                        + "ON CONFLICT (tabla_nombre, id) DO UPDATE SET datos = EXCLUDED.datos";
                try (PreparedStatement ins = remoteConn.prepareStatement(insRemote)) {
                    ins.setString(1, id);
                    ins.setString(2, GSON.toJson(cMap));
                    ins.executeUpdate();
                    existingRemoteIds.add(id);
                    count++;
                }
            }

            if (count > 0) {
                LOGGER.info("[VoltiumSync] Clientes locales exportados hacia CRM Voltium: " + count + " nuevos clientes.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] Error al subir clientes locales hacia CRM: " + e.getMessage());
        }
    }

    /**
     * Descarga proveedores registrados en el CRM (Voltium Sanrey) e inserta/actualiza en la tabla SUPPLIERS local.
     */
    private static void descargarProveedoresDesdeCRM(Connection remoteConn, Connection localConn) {
        String agency = getAgencyId();
        String sql = "SELECT id, datos FROM documentos WHERE tabla_nombre = 'erp_proveedores' AND (datos LIKE ? OR datos LIKE ?)";
        int count = 0;

        try (PreparedStatement ps = remoteConn.prepareStatement(sql)) {
            ps.setString(1, "%" + agency + "%");
            ps.setString(2, "%voltium-sanrey%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String remoteId = rs.getString("id");
                    String datosJson = rs.getString("datos");
                    if (remoteId == null || datosJson == null || datosJson.trim().isEmpty()) {
                        continue;
                    }

                    try {
                        Map<String, Object> map = GSON.fromJson(datosJson, new TypeToken<Map<String, Object>>() {}.getType());
                        if (map == null) continue;

                        String agencyInDatos = map.get("agency_id") != null ? String.valueOf(map.get("agency_id")) : null;
                        if (agencyInDatos == null || "null".equals(agencyInDatos) || agencyInDatos.trim().isEmpty()) {
                            agencyInDatos = map.get("agencyId") != null ? String.valueOf(map.get("agencyId")) : null;
                        }
                        if (agencyInDatos == null || !agency.equalsIgnoreCase(agencyInDatos.trim())) {
                            continue;
                        }

                        String name = map.get("name") != null ? String.valueOf(map.get("name")).trim() : "";
                        if (name.isEmpty() && map.get("supplierName") != null) {
                            name = String.valueOf(map.get("supplierName")).trim();
                        }
                        if (name.isEmpty()) {
                            continue;
                        }
                        if (name.length() > 250) {
                            name = name.substring(0, 250);
                        }

                        String supplierCode = map.get("supplierCode") != null ? String.valueOf(map.get("supplierCode")).trim() : "";
                        String taxId = map.get("taxId") != null ? String.valueOf(map.get("taxId")).trim() : "";
                        String contact = map.get("contact") != null ? String.valueOf(map.get("contact")).trim() : "";
                        String phone = map.get("phone") != null ? String.valueOf(map.get("phone")).trim() : "";
                        String email = map.get("email") != null ? String.valueOf(map.get("email")).trim() : "";
                        String paymentTerms = map.get("paymentTerms") != null ? String.valueOf(map.get("paymentTerms")).trim() : "";
                        String status = map.get("status") != null ? String.valueOf(map.get("status")).trim() : "Activo";
                        boolean visible = !"Inactivo".equalsIgnoreCase(status);

                        double creditLimit = 0.0;
                        Object clObj = map.get("creditLimit");
                        if (clObj instanceof Number) {
                            creditLimit = ((Number) clObj).doubleValue();
                        } else if (clObj instanceof String) {
                            try { creditLimit = Double.parseDouble((String) clObj); } catch (Exception ignored) {}
                        }

                        String searchkey = (!supplierCode.isEmpty()) ? supplierCode : remoteId;

                        String existingId = null;
                        String checkSql = "SELECT ID FROM SUPPLIERS WHERE ID = ? OR SEARCHKEY = ? OR LOWER(NAME) = LOWER(?)";
                        try (PreparedStatement chk = localConn.prepareStatement(checkSql)) {
                            chk.setString(1, remoteId);
                            chk.setString(2, searchkey);
                            chk.setString(3, name);
                            try (ResultSet crs = chk.executeQuery()) {
                                if (crs.next()) {
                                    existingId = crs.getString(1);
                                }
                            }
                        }

                        if (existingId != null) {
                            String checkKeyOtherSql = "SELECT ID FROM SUPPLIERS WHERE SEARCHKEY = ? AND ID <> ?";
                            try (PreparedStatement chkKeyOther = localConn.prepareStatement(checkKeyOtherSql)) {
                                chkKeyOther.setString(1, searchkey);
                                chkKeyOther.setString(2, existingId);
                                try (ResultSet crs = chkKeyOther.executeQuery()) {
                                    if (crs.next()) {
                                        searchkey = existingId;
                                    }
                                }
                            }

                            String updSql = "UPDATE SUPPLIERS SET SEARCHKEY = ?, TAXID = ?, NAME = ?, MAXDEBT = ?, FIRSTNAME = ?, EMAIL = ?, PHONE = ?, NOTES = ?, VISIBLE = ? WHERE ID = ?";
                            try (PreparedStatement upd = localConn.prepareStatement(updSql)) {
                                upd.setString(1, searchkey);
                                upd.setString(2, taxId);
                                upd.setString(3, name);
                                upd.setDouble(4, creditLimit);
                                upd.setString(5, contact);
                                upd.setString(6, email);
                                upd.setString(7, phone);
                                upd.setString(8, paymentTerms);
                                upd.setBoolean(9, visible);
                                upd.setString(10, existingId);
                                upd.executeUpdate();
                                count++;
                            }
                        } else {
                            // Validar unicidad de SEARCHKEY en HSQLDB para evitar violación de SUPPLIERS_SKEY_INX
                            String checkKeySql = "SELECT ID FROM SUPPLIERS WHERE SEARCHKEY = ?";
                            try (PreparedStatement chkKey = localConn.prepareStatement(checkKeySql)) {
                                chkKey.setString(1, searchkey);
                                try (ResultSet crs = chkKey.executeQuery()) {
                                    if (crs.next()) {
                                        searchkey = remoteId;
                                    }
                                }
                            }

                            try (PreparedStatement chkKey2 = localConn.prepareStatement(checkKeySql)) {
                                chkKey2.setString(1, searchkey);
                                try (ResultSet crs2 = chkKey2.executeQuery()) {
                                    if (crs2.next()) {
                                        searchkey = "PRV-" + (System.currentTimeMillis() % 100000);
                                    }
                                }
                            }

                            String insSql = "INSERT INTO SUPPLIERS (ID, SEARCHKEY, TAXID, NAME, MAXDEBT, FIRSTNAME, EMAIL, PHONE, NOTES, VISIBLE, CURDATE, CURDEBT) "
                                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 0.0)";
                            try (PreparedStatement ins = localConn.prepareStatement(insSql)) {
                                ins.setString(1, remoteId);
                                ins.setString(2, searchkey);
                                ins.setString(3, taxId);
                                ins.setString(4, name);
                                ins.setDouble(5, creditLimit);
                                ins.setString(6, contact);
                                ins.setString(7, email);
                                ins.setString(8, phone);
                                ins.setString(9, paymentTerms);
                                ins.setBoolean(10, visible);
                                ins.executeUpdate();
                                count++;
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.FINE, "[VoltiumSync] Error importando proveedor individual " + remoteId + ": " + ex.getMessage());
                    }
                }
            }
            try {
                if (!localConn.getAutoCommit()) {
                    localConn.commit();
                }
            } catch (Exception ignored) {}

            if (count > 0) {
                LOGGER.info("[VoltiumSync] Proveedores CRM sincronizados hacia POS local: " + count + " proveedores procesados.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] Error al descargar proveedores desde CRM: " + e.getMessage());
        }
    }

    /**
     * Sube proveedores creados localmente en el POS hacia el CRM PostgreSQL (erp_proveedores) para la subcuenta voltium-sanrey.
     */
    private static void subirProveedoresLocalesACRM(Connection remoteConn, Connection localConn) {
        String agency = getAgencyId();

        Set<String> existingRemoteIds = new HashSet<>();
        String chkRemoteAll = "SELECT id FROM documentos WHERE tabla_nombre = 'erp_proveedores' AND (datos LIKE ? OR datos LIKE ?)";
        try (PreparedStatement chkAll = remoteConn.prepareStatement(chkRemoteAll)) {
            chkAll.setString(1, "%" + agency + "%");
            chkAll.setString(2, "%voltium-sanrey%");
            try (ResultSet rrs = chkAll.executeQuery()) {
                while (rrs.next()) {
                    String rId = rrs.getString("id");
                    if (rId != null) {
                        existingRemoteIds.add(rId);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] No se pudo verificar lista remota de proveedores: " + e.getMessage());
            return;
        }

        String selSql = "SELECT ID, SEARCHKEY, TAXID, NAME, MAXDEBT, FIRSTNAME, EMAIL, PHONE, NOTES, VISIBLE FROM SUPPLIERS WHERE ID <> '0'";
        int count = 0;

        try (Statement stmt = localConn.createStatement();
             ResultSet rs = stmt.executeQuery(selSql)) {

            while (rs.next()) {
                String id = rs.getString("ID");
                String searchkey = rs.getString("SEARCHKEY");
                String taxid = rs.getString("TAXID");
                String name = rs.getString("NAME");
                double maxdebt = rs.getDouble("MAXDEBT");
                String contact = rs.getString("FIRSTNAME");
                String email = rs.getString("EMAIL");
                String phone = rs.getString("PHONE");
                String notes = rs.getString("NOTES");
                boolean visible = rs.getBoolean("VISIBLE");

                if (id == null || id.trim().isEmpty() || name == null || name.trim().isEmpty() || "Anonimo".equalsIgnoreCase(name)) {
                    continue;
                }

                String docId = id.startsWith("proveedores-") ? id : ("proveedores-" + (searchkey != null && !searchkey.trim().isEmpty() ? searchkey.toLowerCase().replaceAll("[^a-z0-9_-]", "") : id));

                Map<String, Object> pMap = new LinkedHashMap<>();
                pMap.put("id", docId);
                pMap.put("created_at", formatIsoUtc(new Date()));
                pMap.put("createdAt", formatIsoUtc(new Date()));
                pMap.put("agency_id", agency);
                pMap.put("agencyId", agency);
                pMap.put("status", visible ? "Activo" : "Inactivo");
                pMap.put("supplierCode", (searchkey != null && !searchkey.trim().isEmpty()) ? searchkey : ("PRV-" + id));
                pMap.put("name", name);
                pMap.put("taxId", taxid != null ? taxid : "");
                pMap.put("contact", contact != null ? contact : "");
                pMap.put("phone", phone != null ? phone : "");
                pMap.put("email", email != null ? email : "");
                pMap.put("paymentTerms", notes != null ? notes : "");
                pMap.put("creditLimit", String.valueOf(maxdebt));

                String insRemote = "INSERT INTO documentos (tabla_nombre, id, datos, created_at) "
                        + "VALUES ('erp_proveedores', ?, ?, NOW()) "
                        + "ON CONFLICT (tabla_nombre, id) DO UPDATE SET datos = EXCLUDED.datos, created_at = NOW()";
                try (PreparedStatement ins = remoteConn.prepareStatement(insRemote)) {
                    ins.setString(1, docId);
                    ins.setString(2, GSON.toJson(pMap));
                    ins.executeUpdate();
                    existingRemoteIds.add(docId);
                    count++;
                }
            }

            if (count > 0) {
                LOGGER.info("[VoltiumSync] Proveedores locales exportados hacia CRM Voltium: " + count + " proveedores.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] Error al subir proveedores locales hacia CRM: " + e.getMessage());
        }
    }

    /**
     * Extrae información del ticket y calcula el costo de compra y ganancia neta.
     */
    private static TicketPayload construirPayload(TicketInfo ticket, DataLogicSales dlSales, boolean esCancelacion) {
        TicketPayload p = new TicketPayload();
        p.agencyId = getAgencyId();
        p.ticketId = ticket.getTicketId();
        p.ticketType = esCancelacion ? 1 : ticket.getTicketType();
        String ticketUid = ticket.getId();
        if (ticketUid == null || ticketUid.trim().isEmpty()) ticketUid = String.valueOf(ticket.getTicketId());
        p.orderId = (esCancelacion ? "voltium_pos_cancel_" : "voltium_pos_") + ticketUid;
        Date saleDate = ticket.getDate() != null ? ticket.getDate() : new Date();
        p.fecha = formatIsoUtc(saleDate);

        // Cliente asignado al ticket
        if (ticket.getCustomer() != null) {
            p.cliente = (ticket.getCustomer().getName() != null && !ticket.getCustomer().getName().trim().isEmpty())
                    ? ticket.getCustomer().getName() : "Venta Mostrador";
            p.customerId = ticket.getCustomer().getId();
            try {
                p.customerEmail = ticket.getCustomer().getEmail();
            } catch (Exception ignored) {}
            try {
                p.customerPhone = ticket.getCustomer().getPhone();
            } catch (Exception ignored) {}
        } else {
            p.cliente = "Venta Mostrador";
        }

        // Cajero
        if (ticket.getUser() != null && ticket.getUser().getName() != null) {
            p.cajero = ticket.getUser().getName();
        } else {
            p.cajero = "Cajero POS";
        }

        // Método de pago principal
        p.metodoPago = "Efectivo";
        if (ticket.getPayments() != null && !ticket.getPayments().isEmpty()) {
            LinkedHashSet<String> paymentMethods = new LinkedHashSet<>();
            for (PaymentInfo pay : ticket.getPayments()) {
                if (pay == null) continue;
                PaymentPayload payment = new PaymentPayload();
                payment.metodo = pay.getName() != null ? pay.getName() : "Efectivo";
                payment.monto = round(pay.getTotal());
                p.pagos.add(payment);
                paymentMethods.add(payment.metodo);
            }
            if (!paymentMethods.isEmpty()) {
                p.metodoPago = String.join(" + ", paymentMethods);
            }
        }

        double totalVenta = 0.0;
        try {
            totalVenta = ticket.getTotal();
        } catch (Exception ignored) {}

        double subtotalVenta = 0.0;
        try {
            subtotalVenta = ticket.getSubTotal();
        } catch (Exception ignored) {}

        double taxVenta = 0.0;
        try {
            taxVenta = ticket.getTax();
        } catch (Exception ignored) {}

        if (Math.abs(totalVenta) < 0.0001 && ticket.getLines() != null) {
            for (TicketLineInfo line : ticket.getLines()) {
                if (line != null) {
                    totalVenta += line.getValue();
                    subtotalVenta += line.getSubValue();
                    taxVenta += line.getTax();
                }
            }
        }
        if (Math.abs(totalVenta) < 0.0001 && ticket.getPayments() != null && !ticket.getPayments().isEmpty()) {
            for (PaymentInfo pay : ticket.getPayments()) {
                if (pay != null) {
                    totalVenta += pay.getTotal();
                }
            }
        }
        if (subtotalVenta <= 0.0 && totalVenta > 0.0) {
            if (taxVenta > 0.0) {
                subtotalVenta = totalVenta - taxVenta;
            } else {
                subtotalVenta = totalVenta;
            }
        }
        double totalCosto = 0.0;

        if (ticket.getLines() != null) {
            for (TicketLineInfo line : ticket.getLines()) {
                if (line == null) continue;

                ItemPayload item = new ItemPayload();
                String pId = line.getProductID() != null ? line.getProductID() : "GEN";
                String pName = line.getProductName() != null ? line.getProductName() : "Producto";
                item.id = pId;
                item.productId = pId;
                item.name = pName;
                item.title = pName;
                item.productName = pName;
                item.quantity = line.getMultiply();
                item.price = line.getPrice();
                item.subtotal = round(line.getSubValue() > 0 ? line.getSubValue() : (item.price * item.quantity));
                item.taxRate = line.getTaxRate();
                item.tax = round(line.getTax());
                if (line.getProductTaxCategoryID() != null) {
                    item.category = line.getProductTaxCategoryID();
                }

                // Obtener costo de compra (precio de compra registrado en el catálogo o propiedad)
                double unitCost = 0.0;
                String propCost = line.getProperty("product.pricebuy");
                if (propCost != null && !propCost.trim().isEmpty()) {
                    try {
                        unitCost = Double.parseDouble(propCost.trim());
                    } catch (Exception ignored) {}
                }

                if (unitCost <= 0.0 && dlSales != null && line.getProductID() != null) {
                    try {
                        ProductInfoExt prodInfo = dlSales.getProductInfo(line.getProductID());
                        if (prodInfo != null) {
                            unitCost = prodInfo.getPriceBuy();
                        }
                    } catch (Exception e) {
                        // Ignorar fallo al consultar costo unitario específico
                    }
                }

                item.cost = unitCost;
                item.totalCost = round(unitCost * item.quantity);
                item.profit = round(item.subtotal - item.totalCost);

                totalCosto += item.totalCost;
                p.items.add(item);
            }
        }

        // Impuestos detallados
        try {
            List<TicketTaxInfo> taxList = ticket.getTaxes();
            if (taxList != null) {
                for (TicketTaxInfo tTax : taxList) {
                    if (tTax != null && tTax.getTaxInfo() != null) {
                        Map<String, Object> tMap = new LinkedHashMap<>();
                        tMap.put("name", tTax.getTaxInfo().getName());
                        tMap.put("rate", tTax.getTaxInfo().getRate());
                        tMap.put("base", round(tTax.getSubTotal()));
                        tMap.put("amount", round(tTax.getTax()));
                        p.taxes.add(tMap);
                    }
                }
            }
        } catch (Exception ignored) {}

        // Si es cancelación, se reflejan valores invertidos
        if (esCancelacion) {
            p.total = -Math.abs(totalVenta);
            p.subtotal = -Math.abs(subtotalVenta);
            p.tax = -Math.abs(taxVenta);
            p.totalCost = -Math.abs(totalCosto);
            p.totalProfit = -Math.abs(subtotalVenta - totalCosto);
            for (PaymentPayload payment : p.pagos) {
                payment.monto = -Math.abs(payment.monto);
            }
        } else {
            p.total = round(totalVenta);
            p.subtotal = round(subtotalVenta > 0 ? subtotalVenta : totalVenta);
            p.tax = round(taxVenta);
            p.totalCost = round(totalCosto);
            // La utilidad se calcula sobre el subtotal, excluyendo el impuesto cobrado.
            p.totalProfit = round(subtotalVenta - totalCosto);
        }

        return p;
    }

    /**
     * Intenta enviar la venta al servidor. Si no hay conexión o falla, la encola localmente.
     */
    private static void enviarOEncolar(TicketPayload payload) {
        if (isOfflineMode()) {
            LOGGER.info("[VoltiumSync] Modo fuera de línea activo (sin internet). Encolando Ticket #" + payload.ticketId + " de inmediato.");
            encolarTicketLocal(payload);
            return;
        }

        try {
            guardarEnPostgres(payload);
            markOnline();
            LOGGER.info("[VoltiumSync] Venta #" + payload.ticketId + " enviada exitosamente a 'voltium-sanrey'. Total: $"
                    + payload.total + " | Costo: $" + payload.totalCost + " | Ganancia: $" + payload.totalProfit);
        } catch (Exception e) {
            markOffline();
            LOGGER.log(Level.WARNING, "[VoltiumSync] Servidor remoto no disponible para Ticket #" + payload.ticketId
                    + " (" + e.getMessage() + "). Activando modo fuera de línea. Guardando en cola local.");
            encolarTicketLocal(payload);
        }
    }

    /**
     * Guarda la venta directamente en las tablas 'documentos' (orders y gastos) y 'contabilidad'
     * de la base de datos PostgreSQL de CRM-IA.
     */
    private static void guardarEnPostgres(TicketPayload p) throws SQLException {
        String url = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=6";

        try (Connection conn = DriverManager.getConnection(url, getUser(), getPass())) {
            markOnline();
            conn.setAutoCommit(false);

            try {
                Map<String, Object> syncEvent = new LinkedHashMap<>();
                syncEvent.put("agency_id", p.agencyId);
                syncEvent.put("order_id", p.orderId);
                syncEvent.put("ticket_id", p.ticketId);
                String claimSql = "INSERT INTO documentos (tabla_nombre, id, datos, created_at) "
                        + "VALUES ('pos_sync_events', ?, ?, NOW()) ON CONFLICT (tabla_nombre, id) DO NOTHING";
                try (PreparedStatement claim = conn.prepareStatement(claimSql)) {
                    claim.setString(1, p.orderId);
                    claim.setString(2, GSON.toJson(syncEvent));
                    if (claim.executeUpdate() == 0) {
                        conn.commit();
                        return;
                    }
                }
                actualizarInventarioRemoto(conn, p);

                // 1. Guardar en 'documentos' como 'orders'
                Map<String, Object> orderMap = new LinkedHashMap<>();
                orderMap.put("id", p.orderId);
                orderMap.put("order_number", (p.ticketType == 1 ? "CANCEL-" : "POS-") + p.ticketId);
                orderMap.put("agency_id", p.agencyId);
                orderMap.put("agencyId", p.agencyId);
                orderMap.put("pos_ticket_id", p.orderId);
                orderMap.put("status", p.ticketType == 1 ? "cancelled" : "confirmed");
                orderMap.put("created_at", p.fecha);
                orderMap.put("createdAt", p.fecha);
                orderMap.put("confirmedAt", p.fecha);
                orderMap.put("fecha", p.fecha);
                orderMap.put("total", p.total);
                orderMap.put("subtotal", p.subtotal);
                orderMap.put("tax", p.tax);
                orderMap.put("impuestos", p.tax);
                orderMap.put("taxes", p.taxes);
                orderMap.put("total_cost", p.totalCost);
                orderMap.put("total_profit", p.totalProfit);
                orderMap.put("cliente", p.cliente);
                orderMap.put("customer_name", p.cliente);
                orderMap.put("userName", p.cliente);
                if (p.customerId != null && !p.customerId.trim().isEmpty()) {
                    orderMap.put("customerId", p.customerId);
                    orderMap.put("customer_id", p.customerId);
                }
                if (p.customerEmail != null && !p.customerEmail.trim().isEmpty()) {
                    orderMap.put("customer_email", p.customerEmail);
                    orderMap.put("email", p.customerEmail);
                }
                if (p.customerPhone != null && !p.customerPhone.trim().isEmpty()) {
                    orderMap.put("customer_phone", p.customerPhone);
                    orderMap.put("phone", p.customerPhone);
                }
                orderMap.put("cajero", p.cajero);
                orderMap.put("metodo_pago", p.metodoPago);
                orderMap.put("paymentMethod", p.metodoPago);
                orderMap.put("payment_method", p.metodoPago);
                orderMap.put("payments", p.pagos);
                orderMap.put("paymentDetails", p.pagos);
                orderMap.put("orderType", "physical");
                orderMap.put("order_type", "physical");
                orderMap.put("physicalSale", true);
                orderMap.put("physical_sale", true);
                orderMap.put("tipo", "pos");
                orderMap.put("origen", "Kriolos POS Local");
                orderMap.put("items", p.items);

                String sqlOrder = "INSERT INTO documentos (tabla_nombre, id, datos, created_at) "
                        + "VALUES ('orders', ?, ?, NOW()) "
                        + "ON CONFLICT (tabla_nombre, id) DO UPDATE SET datos = EXCLUDED.datos, created_at = NOW()";

                try (PreparedStatement psOrder = conn.prepareStatement(sqlOrder)) {
                    psOrder.setString(1, p.orderId);
                    psOrder.setString(2, GSON.toJson(orderMap));
                    psOrder.executeUpdate();
                }

                // 2. Guardar en 'documentos' como 'gastos' (Costo de Mercadería) si hubo costo
                String gastoId = "gasto_" + p.orderId;
                if (Math.abs(p.totalCost) > 0.001) {
                    Map<String, Object> gastoMap = new LinkedHashMap<>();
                    gastoMap.put("id", gastoId);
                    gastoMap.put("agency_id", p.agencyId);
                    gastoMap.put("fecha", p.fecha);
                    gastoMap.put("monto", Math.abs(p.totalCost));
                    gastoMap.put("tipo", p.totalCost >= 0 ? "egreso" : "ingreso");
                    gastoMap.put("categoria", "Mercadería");
                    gastoMap.put("concepto", (p.ticketType == 1 ? "Reversión costo Ticket #" : "Costo mercadería Ticket #") + p.ticketId);
                    gastoMap.put("metodo_pago", p.metodoPago);
                    gastoMap.put("referencia", p.orderId);

                    String sqlGasto = "INSERT INTO documentos (tabla_nombre, id, datos, created_at) "
                            + "VALUES ('gastos', ?, ?, NOW()) "
                            + "ON CONFLICT (tabla_nombre, id) DO UPDATE SET datos = EXCLUDED.datos, created_at = NOW()";

                    try (PreparedStatement psGasto = conn.prepareStatement(sqlGasto)) {
                        psGasto.setString(1, gastoId);
                        psGasto.setString(2, GSON.toJson(gastoMap));
                        psGasto.executeUpdate();
                    }
                }

                // 3. Registrar en la tabla 'contabilidad' (ingreso y costo)
                String sqlDelContab = "DELETE FROM contabilidad WHERE referencia_id IN (?, ?) "
                        + "OR LEFT(referencia_id, LENGTH(?) + LENGTH(':payment:')) = ? || ':payment:'";
                try (PreparedStatement psDel = conn.prepareStatement(sqlDelContab)) {
                    psDel.setString(1, p.orderId);
                    psDel.setString(2, gastoId);
                    psDel.setString(3, p.orderId);
                    psDel.setString(4, p.orderId);
                    psDel.executeUpdate();
                }

                String sqlContab = "INSERT INTO contabilidad (tipo, concepto, monto, fecha, metodo_pago, referencia_id) "
                        + "VALUES (?, ?, ?, NOW(), ?, ?)";

                double paymentTotal = p.pagos.stream().mapToDouble(payment -> Math.abs(payment.monto)).sum();
                boolean validPaymentBreakdown = !p.pagos.isEmpty()
                        && Math.abs(paymentTotal - Math.abs(p.total)) < 0.05;
                String incomeConcept = (p.ticketType == 1 ? "Cancelacion POS Ticket #" : "Venta POS Ticket #") + p.ticketId;
                if (validPaymentBreakdown) {
                    for (int i = 0; i < p.pagos.size(); i++) {
                        PaymentPayload payment = p.pagos.get(i);
                        try (PreparedStatement psContab = conn.prepareStatement(sqlContab)) {
                            psContab.setString(1, p.ticketType == 1 ? "egreso" : "ingreso");
                            psContab.setString(2, incomeConcept + " (" + payment.metodo + ")");
                            psContab.setBigDecimal(3, BigDecimal.valueOf(Math.abs(payment.monto)));
                            psContab.setString(4, payment.metodo);
                            psContab.setString(5, p.pagos.size() == 1 ? p.orderId : p.orderId + ":payment:" + i);
                            psContab.executeUpdate();
                        }
                    }
                } else {
                    try (PreparedStatement psContab = conn.prepareStatement(sqlContab)) {
                        psContab.setString(1, p.total >= 0 ? "ingreso" : "egreso");
                        psContab.setString(2, incomeConcept + " (" + p.cliente + ")");
                        psContab.setBigDecimal(3, BigDecimal.valueOf(Math.abs(p.total)));
                        psContab.setString(4, p.metodoPago);
                        psContab.setString(5, p.orderId);
                        psContab.executeUpdate();
                    }
                }
                // Registro de Egreso / Reversión de costo
                if (Math.abs(p.totalCost) > 0.001) {
                    try (PreparedStatement psContab = conn.prepareStatement(sqlContab)) {
                        psContab.setString(1, p.totalCost >= 0 ? "egreso" : "ingreso");
                        psContab.setString(2, (p.ticketType == 1 ? "Reversión costo Ticket #" : "Costo mercadería Ticket #") + p.ticketId);
                        psContab.setBigDecimal(3, BigDecimal.valueOf(Math.abs(p.totalCost)));
                        psContab.setString(4, p.metodoPago);
                        psContab.setString(5, gastoId);
                        psContab.executeUpdate();
                    }
                }

                String sqlVenta = "INSERT INTO ventas (producto_id, cantidad, total, fecha) VALUES (?, ?, ?, ?)";
                double saleDirection = p.ticketType == 1 ? -1.0 : 1.0;
                try (PreparedStatement psVenta = conn.prepareStatement(sqlVenta)) {
                    for (ItemPayload item : p.items) {
                        if (item == null || item.id == null) continue;
                        int productId;
                        try {
                            productId = Integer.parseInt(item.id);
                        } catch (NumberFormatException ignored) {
                            productId = Math.floorMod(item.id.hashCode(), Integer.MAX_VALUE);
                    }
                        double quantity = Math.abs(item.quantity) * saleDirection;
                        double lineTotal = (item.subtotal > 0 ? item.subtotal : Math.abs(item.price * item.quantity)) * saleDirection;
                        psVenta.setInt(1, productId);
                        psVenta.setBigDecimal(2, BigDecimal.valueOf(quantity));
                        psVenta.setBigDecimal(3, BigDecimal.valueOf(lineTotal));
                        psVenta.setTimestamp(4, Timestamp.from(Instant.parse(p.fecha)));
                        psVenta.addBatch();
                    }
                    psVenta.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {}
                throw e;
            }
        }
    }

    /**
     * Guarda un ticket en el archivo JSON local de cola offline.
     */
    private static void actualizarInventarioRemoto(Connection conn, TicketPayload p) throws SQLException {
        if (p.items == null) return;
        double direction = p.ticketType == 1 ? 1.0 : -1.0;
        for (ItemPayload item : p.items) {
            if (item == null || item.id == null || item.id.trim().isEmpty() || "GEN".equalsIgnoreCase(item.id)) continue;
            String selectSql = "SELECT datos FROM documentos WHERE tabla_nombre = 'products' AND id = ? FOR UPDATE";
            try (PreparedStatement select = conn.prepareStatement(selectSql)) {
                select.setString(1, item.id);
                try (ResultSet rs = select.executeQuery()) {
                    if (!rs.next()) continue;
                    String json = rs.getString(1);
                    Map<String, Object> product = GSON.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
                    if (product == null) continue;
                    Object agencyValue = product.get("agency_id");
                    if (agencyValue == null) agencyValue = product.get("agencyId");
                    if (agencyValue == null || !p.agencyId.equalsIgnoreCase(String.valueOf(agencyValue))) continue;
                    Object stockValue = product.get("stock");
                    double stock = 0.0;
                    if (stockValue instanceof Number) stock = ((Number) stockValue).doubleValue();
                    else if (stockValue != null) {
                        try { stock = Double.parseDouble(String.valueOf(stockValue)); } catch (NumberFormatException ignored) {}
                    }
                    product.put("stock", round(stock + direction * Math.abs(item.quantity)));
                    product.put("updated_at", formatIsoUtc(new Date()));
                    try (PreparedStatement update = conn.prepareStatement(
                            "UPDATE documentos SET datos = ? WHERE tabla_nombre = 'products' AND id = ?")) {
                        update.setString(1, GSON.toJson(product));
                        update.setString(2, item.id);
                        update.executeUpdate();
                    }
                }
            }
        }
    }

    private static void encolarTicketLocal(TicketPayload p) {
        synchronized (QUEUE_LOCK) {
            try {
                File file = getQueueFile();
                List<TicketPayload> queue = leerColaOffline(file);
                queue.removeIf(item -> item.orderId != null && item.orderId.equals(p.orderId));
                queue.add(p);
                escribirColaOffline(file, queue);
                LOGGER.info("[VoltiumSync] Ticket #" + p.ticketId + " encolado en archivo offline (" + queue.size() + " pendientes).");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "[VoltiumSync] No se pudo encolar ticket offline: " + e.getMessage(), e);
            }
        }
    }

    // =========================================================================
    // MÓDULO GASTOS Y MOVIMIENTOS DE CAJA -> CONTABILIDAD Y GASTOS EN PANEL CENTRAL
    // =========================================================================

    private static final Object EXPENSE_QUEUE_LOCK = new Object();

    /**
     * Sincroniza un gasto o movimiento de caja con la contabilidad y gastos del panel Voltium Sanrey de forma asíncrona.
     */
    public static void sincronizarGastoAsync(final ExpensePayload exp) {
        if (exp == null || exp.id == null) {
            return;
        }

        SYNC_EXECUTOR.submit(() -> {
            try {
                if (isOfflineMode()) {
                    LOGGER.info("[VoltiumSync] Modo fuera de línea activo. Encolando gasto #" + exp.id + " localmente.");
                    encolarGastoLocal(exp);
                    return;
                }
                try {
                    guardarGastoEnPostgres(exp);
                    markOnline();
                } catch (Exception e) {
                    markOffline();
                    LOGGER.log(Level.WARNING, "[VoltiumSync] Error al enviar gasto a PostgreSQL (" + e.getMessage() + "). Guardando en cola offline.");
                    encolarGastoLocal(exp);
                }
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Error inesperado sincronizando gasto con contabilidad: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Elimina un gasto o movimiento de la contabilidad y gastos del panel Voltium Sanrey de forma asíncrona.
     */
    public static void eliminarGastoRemotoAsync(final String expenseId) {
        if (expenseId == null || expenseId.trim().isEmpty()) {
            return;
        }

        SYNC_EXECUTOR.submit(() -> {
            try {
                // Quitar de la cola offline si estuviera pendiente
                synchronized (EXPENSE_QUEUE_LOCK) {
                    try {
                        File file = getExpenseQueueFile();
                        List<ExpensePayload> queue = leerColaOfflineGastos(file);
                        queue.removeIf(item -> item.id != null && (item.id.equals(expenseId) || item.id.equals("gasto_pos_" + expenseId) || item.id.equals("cash_" + expenseId)));
                        escribirColaOfflineGastos(file, queue);
                    } catch (Exception ignored) {}
                }

                if (isOfflineMode()) {
                    return;
                }

                String url = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                        + "?connectTimeout=3&socketTimeout=6";

                try (Connection conn = DriverManager.getConnection(url, getUser(), getPass())) {
                    markOnline();
                    conn.setAutoCommit(false);
                    try {
                        String docId = expenseId.startsWith("gasto_") || expenseId.startsWith("cash_") ? expenseId : ("gasto_pos_" + expenseId);
                        String sqlDelGasto = "DELETE FROM documentos WHERE tabla_nombre = 'gastos' AND id IN (?, ?, ?)";
                        try (PreparedStatement ps = conn.prepareStatement(sqlDelGasto)) {
                            ps.setString(1, docId);
                            ps.setString(2, expenseId);
                            ps.setString(3, "cash_" + expenseId);
                            ps.executeUpdate();
                        }

                        String sqlDelContab = "DELETE FROM contabilidad WHERE referencia_id IN (?, ?, ?)";
                        try (PreparedStatement psContab = conn.prepareStatement(sqlDelContab)) {
                            psContab.setString(1, docId);
                            psContab.setString(2, expenseId);
                            psContab.setString(3, "cash_" + expenseId);
                            psContab.executeUpdate();
                        }

                        conn.commit();
                        LOGGER.info("[VoltiumSync] Gasto/movimiento " + expenseId + " eliminado exitosamente del panel central.");
                    } catch (SQLException ex) {
                        try {
                            conn.rollback();
                        } catch (Exception ignored) {}
                        throw ex;
                    }
                }
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[VoltiumSync] Error al eliminar gasto remoto: " + t.getMessage());
            }
        });
    }

    /**
     * Guarda un gasto o movimiento de caja en la contabilidad y gastos del panel CRM-IA.
     */
    public static void guardarGastoEnPostgres(ExpensePayload exp) throws SQLException {
        if (exp == null || exp.id == null) {
            return;
        }
        if (exp.agencyId == null || exp.agencyId.trim().isEmpty()) {
            exp.agencyId = getAgencyId();
        }

        String url = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=6";

        try (Connection conn = DriverManager.getConnection(url, getUser(), getPass())) {
            markOnline();
            conn.setAutoCommit(false);

            try {
                String docId = exp.id.startsWith("gasto_") || exp.id.startsWith("cash_") ? exp.id : ("gasto_pos_" + exp.id);
                String refId = exp.referencia != null && !exp.referencia.trim().isEmpty() ? exp.referencia : docId;
                String dateStr = exp.fecha != null && !exp.fecha.trim().isEmpty() ? exp.fecha : formatIsoUtc(new Date());
                String concepto = exp.concepto != null && !exp.concepto.trim().isEmpty() ? exp.concepto : "Movimiento POS";
                String paymentMethod = exp.metodoPago != null && !exp.metodoPago.trim().isEmpty() ? exp.metodoPago : "Efectivo";
                String tipo = exp.tipo != null && !exp.tipo.trim().isEmpty() ? exp.tipo : "egreso";
                String categoria = exp.categoria != null && !exp.categoria.trim().isEmpty() ? exp.categoria : "Operativos";

                // 1. Guardar en 'documentos' con tabla_nombre = 'gastos'
                Map<String, Object> gastoMap = new LinkedHashMap<>();
                gastoMap.put("id", docId);
                gastoMap.put("agency_id", exp.agencyId);
                gastoMap.put("agencyId", exp.agencyId);
                gastoMap.put("fecha", dateStr);
                gastoMap.put("created_at", dateStr);
                gastoMap.put("createdAt", dateStr);
                gastoMap.put("monto", Math.abs(exp.monto));
                gastoMap.put("tipo", tipo);
                gastoMap.put("categoria", categoria);
                gastoMap.put("concepto", concepto);
                gastoMap.put("metodo_pago", paymentMethod);
                gastoMap.put("paymentMethod", paymentMethod);
                gastoMap.put("referencia", refId);
                gastoMap.put("referencia_id", refId);
                if (exp.responsable != null && !exp.responsable.trim().isEmpty()) {
                    gastoMap.put("responsable", exp.responsable);
                }
                if (exp.notas != null && !exp.notas.trim().isEmpty()) {
                    gastoMap.put("notes", exp.notas);
                    gastoMap.put("notas", exp.notas);
                }

                String sqlGasto = "INSERT INTO documentos (tabla_nombre, id, datos, created_at) "
                        + "VALUES ('gastos', ?, ?, NOW()) "
                        + "ON CONFLICT (tabla_nombre, id) DO UPDATE SET datos = EXCLUDED.datos, created_at = NOW()";

                try (PreparedStatement psGasto = conn.prepareStatement(sqlGasto)) {
                    psGasto.setString(1, docId);
                    psGasto.setString(2, GSON.toJson(gastoMap));
                    psGasto.executeUpdate();
                }

                // 2. Registrar en la tabla 'contabilidad'
                String sqlDelContab = "DELETE FROM contabilidad WHERE referencia_id IN (?, ?)";
                try (PreparedStatement psDel = conn.prepareStatement(sqlDelContab)) {
                    psDel.setString(1, docId);
                    psDel.setString(2, refId);
                    psDel.executeUpdate();
                }

                String sqlContab = "INSERT INTO contabilidad (tipo, concepto, monto, fecha, metodo_pago, referencia_id) "
                        + "VALUES (?, ?, ?, NOW(), ?, ?)";
                try (PreparedStatement psContab = conn.prepareStatement(sqlContab)) {
                    psContab.setString(1, tipo);
                    psContab.setString(2, concepto + " - Voltium Sanrey");
                    psContab.setBigDecimal(3, BigDecimal.valueOf(Math.abs(exp.monto)));
                    psContab.setString(4, paymentMethod);
                    psContab.setString(5, docId);
                    psContab.executeUpdate();
                }

                conn.commit();
                LOGGER.info("[VoltiumSync] Gasto/Movimiento sincronizado con éxito: " + docId + " ($" + exp.monto + ")");
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {}
                throw e;
            }
        }
    }

    /**
     * Sincroniza todos los gastos operativos y movimientos de caja registrados localmente en HSQLDB hacia PostgreSQL.
     */
    public static synchronized void sincronizarGastosYMovimientosLocales() {
        if (localSession == null || isOfflineMode()) {
            return;
        }

        Connection localConn = null;
        try {
            localConn = localSession.getConnection();
        } catch (Exception e) {
            return;
        }
        if (localConn == null) return;

        String pgUrl = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=8";

        try (Connection remoteConn = DriverManager.getConnection(pgUrl, getUser(), getPass())) {
            markOnline();
            sincronizarGastosYMovimientosLocales(remoteConn, localConn);
        } catch (SQLException e) {
            markOffline();
            LOGGER.log(Level.FINE, "[VoltiumSync] Servidor remoto no disponible para sincronizar gastos locales: " + e.getMessage());
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "[VoltiumSync] Error general en sincronización de gastos locales: " + t.getMessage(), t);
        }
    }

    private static void sincronizarGastosYMovimientosLocales(Connection remoteConn, Connection localConn) {
        String agency = getAgencyId();

        // 1. Sincronizar tabla EXPENSES
        try {
            boolean tableExists = false;
            try (ResultSet rs = localConn.getMetaData().getTables(null, null, "EXPENSES", null)) {
                tableExists = rs.next();
            }
            if (tableExists) {
                String sql = "SELECT ID, DATENEW, NAME, AMOUNT, RESPONSIBLE, NOTES FROM EXPENSES";
                try (Statement stmt = localConn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        String id = rs.getString("ID");
                        if (id == null || id.trim().isEmpty()) continue;
                        Timestamp ts = rs.getTimestamp("DATENEW");
                        String name = rs.getString("NAME");
                        double amount = rs.getDouble("AMOUNT");
                        String resp = rs.getString("RESPONSIBLE");
                        String notes = rs.getString("NOTES");

                        ExpensePayload exp = new ExpensePayload();
                        exp.id = "gasto_pos_" + id;
                        exp.agencyId = agency;
                        exp.fecha = ts != null ? formatIsoUtc(new Date(ts.getTime())) : formatIsoUtc(new Date());
                        exp.monto = amount;
                        exp.tipo = "egreso";
                        exp.categoria = "Operativos";
                        exp.concepto = (name != null && !name.trim().isEmpty()) ? name : "Gasto Operativo POS";
                        exp.metodoPago = "Efectivo";
                        exp.responsable = resp;
                        exp.notas = notes;
                        exp.referencia = "EXPENSE-" + id;

                        try {
                            guardarGastoEnPostgres(exp);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "[VoltiumSync] Error sincronizando EXPENSES locales: " + ex.getMessage());
        }

        // 2. Sincronizar tabla payments (cashin / cashout)
        try {
            String sql = "SELECT p.ID, r.DATENEW, p.PAYMENT, p.TOTAL, p.NOTES FROM payments p LEFT JOIN receipts r ON p.RECEIPT = r.ID WHERE p.PAYMENT IN ('cashin', 'cashout')";
            try (Statement stmt = localConn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String pId = rs.getString(1);
                    if (pId == null || pId.trim().isEmpty()) continue;
                    Timestamp ts = rs.getTimestamp(2);
                    String payType = rs.getString(3);
                    double total = rs.getDouble(4);
                    String notes = rs.getString(5);

                    boolean isCashIn = "cashin".equalsIgnoreCase(payType);
                    ExpensePayload exp = new ExpensePayload();
                    exp.id = "cash_" + pId;
                    exp.agencyId = agency;
                    exp.fecha = ts != null ? formatIsoUtc(new Date(ts.getTime())) : formatIsoUtc(new Date());
                    exp.monto = Math.abs(total);
                    exp.tipo = isCashIn ? "ingreso" : "egreso";
                    exp.categoria = "Caja Menor";
                    exp.concepto = (isCashIn ? "Entrada de caja: " : "Salida de caja: ") + (notes != null && !notes.trim().isEmpty() ? notes : (isCashIn ? "Entrada Efectivo" : "Salida Efectivo"));
                    exp.metodoPago = "Efectivo";
                    exp.responsable = "Cajero POS";
                    exp.notas = notes;
                    exp.referencia = "CASH-" + pId;

                    try {
                        guardarGastoEnPostgres(exp);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "[VoltiumSync] Error sincronizando pagos cashin/cashout locales: " + ex.getMessage());
        }
    }

    private static File getExpenseQueueFile() {
        String userHome = System.getProperty("user.home", ".");
        File dir = new File(userHome, "kriolopos");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "voltium_expense_offline_queue.json");
    }

    private static List<ExpensePayload> leerColaOfflineGastos(File file) {
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
            Type listType = new TypeToken<ArrayList<ExpensePayload>>() {}.getType();
            List<ExpensePayload> list = GSON.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void escribirColaOfflineGastos(File file, List<ExpensePayload> list) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
            GSON.toJson(list, writer);
        }
    }

    public static void encolarGastoLocal(ExpensePayload p) {
        if (p == null || p.id == null) return;
        synchronized (EXPENSE_QUEUE_LOCK) {
            try {
                File file = getExpenseQueueFile();
                List<ExpensePayload> queue = leerColaOfflineGastos(file);
                queue.removeIf(item -> item.id != null && item.id.equals(p.id));
                queue.add(p);
                escribirColaOfflineGastos(file, queue);
                LOGGER.info("[VoltiumSync] Gasto #" + p.id + " encolado en archivo offline (" + queue.size() + " pendientes).");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "[VoltiumSync] No se pudo encolar gasto offline: " + e.getMessage(), e);
            }
        }
    }

    public static void procesarColaOfflineGastos() {
        synchronized (EXPENSE_QUEUE_LOCK) {
            File file = getExpenseQueueFile();
            if (!file.exists() || file.length() == 0) {
                return;
            }

            List<ExpensePayload> queue;
            try {
                queue = leerColaOfflineGastos(file);
            } catch (Exception e) {
                return;
            }

            if (queue.isEmpty()) {
                return;
            }

            LOGGER.info("[VoltiumSync] Intentando procesar " + queue.size() + " gastos offline acumulados...");
            List<ExpensePayload> pendientes = new ArrayList<>();
            boolean falloConexion = false;

            for (ExpensePayload p : queue) {
                if (falloConexion) {
                    pendientes.add(p);
                    continue;
                }

                try {
                    guardarGastoEnPostgres(p);
                    LOGGER.info("[VoltiumSync] Gasto offline #" + p.id + " sincronizado con éxito.");
                } catch (Exception e) {
                    if (isConnectionError(e)) {
                        markOffline();
                        falloConexion = true;
                        pendientes.add(p);
                    } else {
                        p.retryCount++;
                        if (p.retryCount >= 5) {
                            LOGGER.log(Level.SEVERE, "[VoltiumSync] Gasto offline #" + p.id + " descartado tras 5 fallos: " + e.getMessage());
                        } else {
                            pendientes.add(p);
                        }
                    }
                }
            }

            if (!falloConexion) {
                markOnline();
            }

            try {
                escribirColaOfflineGastos(file, pendientes);
            } catch (Exception ignored) {}
        }
    }

    // =========================================================================
    // MÓDULO RECURSOS HUMANOS Y NÓMINA -> CONTABILIDAD Y GASTOS EN PANEL CENTRAL
    // =========================================================================

    private static final Object PAYROLL_QUEUE_LOCK = new Object();

    /**
     * Guarda una nómina en la contabilidad y gastos del panel CRM-IA.
     */
    public static void guardarNominaEnPostgres(PayrollPayload p) throws SQLException {
        if (p == null || p.payrollId == null) {
            return;
        }
        if (p.agencyId == null || p.agencyId.trim().isEmpty()) {
            p.agencyId = getAgencyId();
        }

        String url = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=6";

        try (Connection conn = DriverManager.getConnection(url, getUser(), getPass())) {
            markOnline();
            conn.setAutoCommit(false);

            try {
                String gastoId = "gasto_payroll_" + p.payrollId;
                String refId = "payroll_" + p.payrollId;
                String empName = (p.employeeName != null && !p.employeeName.trim().isEmpty()) ? p.employeeName : "Colaborador";
                String period = (p.periodLabel != null && !p.periodLabel.trim().isEmpty()) ? p.periodLabel : "General";
                String concepto = "Pago de Nómina - " + empName + " (" + period + ") - Voltium Sanrey";
                String paymentMethod = (p.paymentMethod != null && !p.paymentMethod.trim().isEmpty()) ? p.paymentMethod : "Transferencia";
                String dateStr = (p.paymentDate != null && !p.paymentDate.trim().isEmpty()) ? p.paymentDate : formatIsoUtc(new Date());

                // 1. Guardar en 'documentos' con tabla_nombre = 'gastos'
                Map<String, Object> gastoMap = new LinkedHashMap<>();
                gastoMap.put("id", gastoId);
                gastoMap.put("agency_id", p.agencyId);
                gastoMap.put("agencyId", p.agencyId);
                gastoMap.put("fecha", dateStr);
                gastoMap.put("created_at", dateStr);
                gastoMap.put("createdAt", dateStr);
                gastoMap.put("monto", p.netAmount);
                gastoMap.put("tipo", "egreso");
                gastoMap.put("categoria", "Nómina");
                gastoMap.put("concepto", concepto);
                gastoMap.put("metodo_pago", paymentMethod);
                gastoMap.put("payment_method", paymentMethod);
                gastoMap.put("paymentMethod", paymentMethod);
                gastoMap.put("referencia", refId);
                gastoMap.put("referencia_id", refId);
                gastoMap.put("empleado", empName);
                gastoMap.put("empleado_id", p.employeeId != null ? p.employeeId : "");
                gastoMap.put("periodo", period);
                gastoMap.put("period_label", period);
                gastoMap.put("salario_base", p.baseSalary);
                gastoMap.put("comisiones", p.commissions);
                gastoMap.put("auxilios", p.allowances);
                gastoMap.put("bonos", p.bonusAmount);
                gastoMap.put("total_bruto", p.grossAmount);
                gastoMap.put("deducciones", p.deductions);
                gastoMap.put("total_neto", p.netAmount);
                gastoMap.put("status", p.status != null ? p.status : "Pagado");
                gastoMap.put("notas", p.notes != null ? p.notes : "");
                gastoMap.put("procesado_por", p.processedBy != null ? p.processedBy : "POS");

                String sqlGasto = "INSERT INTO documentos (tabla_nombre, id, datos, created_at) "
                        + "VALUES ('gastos', ?, ?, NOW()) "
                        + "ON CONFLICT (tabla_nombre, id) DO UPDATE SET datos = EXCLUDED.datos, created_at = NOW()";

                try (PreparedStatement psGasto = conn.prepareStatement(sqlGasto)) {
                    psGasto.setString(1, gastoId);
                    psGasto.setString(2, GSON.toJson(gastoMap));
                    psGasto.executeUpdate();
                }

                // 2. Registrar en la tabla 'contabilidad' (egreso)
                String sqlDelContab = "DELETE FROM contabilidad WHERE referencia_id = ?";
                try (PreparedStatement psDel = conn.prepareStatement(sqlDelContab)) {
                    psDel.setString(1, refId);
                    psDel.executeUpdate();
                }

                String sqlContab = "INSERT INTO contabilidad (tipo, concepto, monto, fecha, metodo_pago, referencia_id) "
                        + "VALUES ('egreso', ?, ?, ?, ?, ?)";

                try (PreparedStatement psContab = conn.prepareStatement(sqlContab)) {
                    psContab.setString(1, concepto);
                    psContab.setBigDecimal(2, BigDecimal.valueOf(Math.abs(p.netAmount)));

                    Timestamp ts;
                    try {
                        ts = Timestamp.from(Instant.parse(dateStr));
                    } catch (Exception e) {
                        ts = new Timestamp(System.currentTimeMillis());
                    }
                    psContab.setTimestamp(3, ts);
                    psContab.setString(4, paymentMethod);
                    psContab.setString(5, refId);
                    psContab.executeUpdate();
                }

                conn.commit();
                LOGGER.info("[VoltiumSync] Nómina " + p.payrollId + " (" + empName + " - $" + p.netAmount + ") sincronizada exitosamente con contabilidad y gastos.");
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {}
                throw e;
            }
        }
    }

    /**
     * Elimina una nómina de contabilidad y gastos remotos.
     */
    public static void eliminarNominaRemota(String payrollId) throws SQLException {
        if (payrollId == null || payrollId.trim().isEmpty()) {
            return;
        }

        String url = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=6";

        try (Connection conn = DriverManager.getConnection(url, getUser(), getPass())) {
            markOnline();
            conn.setAutoCommit(false);

            try {
                String gastoId = "gasto_payroll_" + payrollId;
                String refId = "payroll_" + payrollId;

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM contabilidad WHERE referencia_id = ?")) {
                    ps.setString(1, refId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM documentos WHERE tabla_nombre = 'gastos' AND id = ?")) {
                    ps.setString(1, gastoId);
                    ps.executeUpdate();
                }

                conn.commit();
                LOGGER.info("[VoltiumSync] Nómina " + payrollId + " eliminada de contabilidad y gastos remotos.");
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {}
                throw e;
            }
        }
    }

    /**
     * Upsert de empleado en la tabla documentos con tabla_nombre = 'erp_rrhh'.
     */
    public static void guardarEmpleadoEnPostgres(EmployeePayload emp) throws SQLException {
        if (emp == null || emp.employeeId == null) {
            return;
        }
        if (emp.agencyId == null || emp.agencyId.trim().isEmpty()) {
            emp.agencyId = getAgencyId();
        }

        String url = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=6";

        try (Connection conn = DriverManager.getConnection(url, getUser(), getPass())) {
            markOnline();

            String docId = emp.employeeId.startsWith("rrhh-emp-") ? emp.employeeId : ("rrhh-emp-" + emp.employeeId);
            String nowIso = formatIsoUtc(new Date());

            Map<String, Object> empMap = new LinkedHashMap<>();
            empMap.put("id", docId);
            empMap.put("created_at", nowIso);
            empMap.put("createdAt", nowIso);
            empMap.put("agency_id", emp.agencyId);
            empMap.put("agencyId", emp.agencyId);
            empMap.put("employee", (emp.employeeCode != null && !emp.employeeCode.trim().isEmpty()) ? emp.employeeCode : ("EMP-" + emp.employeeId));
            empMap.put("name", emp.name != null ? emp.name : "Colaborador");
            empMap.put("role", emp.role != null ? emp.role : "Operaciones");
            empMap.put("area", emp.area != null ? emp.area : "Operaciones");
            empMap.put("salary", String.valueOf(emp.salary));
            empMap.put("date", emp.date != null ? emp.date : nowIso.substring(0, 10));
            empMap.put("status", emp.status != null ? emp.status : "Activo");
            if (emp.email != null && !emp.email.trim().isEmpty()) {
                empMap.put("email", emp.email);
            }
            if (emp.phone != null && !emp.phone.trim().isEmpty()) {
                empMap.put("phone", emp.phone);
            }

            String sql = "INSERT INTO documentos (tabla_nombre, id, datos, created_at) "
                    + "VALUES ('erp_rrhh', ?, ?, NOW()) "
                    + "ON CONFLICT (tabla_nombre, id) DO UPDATE SET datos = EXCLUDED.datos, created_at = NOW()";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, docId);
                ps.setString(2, GSON.toJson(empMap));
                ps.executeUpdate();
            }

            LOGGER.info("[VoltiumSync] Empleado " + emp.name + " (" + docId + ") sincronizado con erp_rrhh en panel.");
        }
    }

    /**
     * Sincroniza nóminas locales pendientes con la contabilidad remota.
     */
    public static synchronized void sincronizarNominasLocales() {
        if (localSession == null || isOfflineMode()) {
            return;
        }

        Connection localConn = null;
        try {
            localConn = localSession.getConnection();
        } catch (Exception e) {
            return;
        }
        if (localConn == null) {
            return;
        }

        String url = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=6";

        try (Connection remoteConn = DriverManager.getConnection(url, getUser(), getPass())) {
            markOnline();

            // 1. Obtener nóminas ya registradas en PostgreSQL contabilidad
            Set<String> payrollsEnRemoto = new HashSet<>();
            try (PreparedStatement ps = remoteConn.prepareStatement("SELECT referencia_id FROM contabilidad WHERE referencia_id LIKE 'payroll_%'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String ref = rs.getString(1);
                        if (ref != null) {
                            payrollsEnRemoto.add(ref);
                        }
                    }
                }
            }

            // 2. Leer nóminas locales desde HR_PAYROLL
            String sqlLocal = "SELECT p.ID, p.EMPLOYEE_ID, pe.NAME, p.PERIOD_LABEL, p.PERIOD_START, p.PERIOD_END, "
                    + "p.PAYMENT_DATE, p.BASE_AMOUNT, p.COMMISSIONS, p.ALLOWANCES, p.BONUS_AMOUNT, "
                    + "p.GROSS_AMOUNT, p.DEDUCTIONS, p.NET_AMOUNT, p.PAYMENT_METHOD, p.STATUS, "
                    + "p.NOTES, p.PROCESSED_BY "
                    + "FROM HR_PAYROLL p "
                    + "LEFT JOIN PEOPLE pe ON p.EMPLOYEE_ID = pe.ID "
                    + "WHERE UPPER(COALESCE(p.STATUS, '')) <> 'CANCELADO'";

            List<PayrollPayload> faltantes = new ArrayList<>();
            try (Statement st = localConn.createStatement();
                 ResultSet rs = st.executeQuery(sqlLocal)) {
                while (rs.next()) {
                    String pid = rs.getString("ID");
                    String refId = "payroll_" + pid;
                    if (!payrollsEnRemoto.contains(refId)) {
                        PayrollPayload pay = new PayrollPayload();
                        pay.payrollId = pid;
                        pay.employeeId = rs.getString("EMPLOYEE_ID");
                        pay.employeeName = rs.getString("NAME");
                        pay.periodLabel = rs.getString("PERIOD_LABEL");

                        Timestamp pStart = rs.getTimestamp("PERIOD_START");
                        pay.periodStart = (pStart != null) ? formatIsoUtc(new Date(pStart.getTime())) : null;

                        Timestamp pEnd = rs.getTimestamp("PERIOD_END");
                        pay.periodEnd = (pEnd != null) ? formatIsoUtc(new Date(pEnd.getTime())) : null;

                        Timestamp pDate = rs.getTimestamp("PAYMENT_DATE");
                        pay.paymentDate = (pDate != null) ? formatIsoUtc(new Date(pDate.getTime())) : formatIsoUtc(new Date());

                        pay.baseSalary = rs.getDouble("BASE_AMOUNT");
                        pay.commissions = rs.getDouble("COMMISSIONS");
                        pay.allowances = rs.getDouble("ALLOWANCES");
                        pay.bonusAmount = rs.getDouble("BONUS_AMOUNT");
                        pay.grossAmount = rs.getDouble("GROSS_AMOUNT");
                        pay.deductions = rs.getDouble("DEDUCTIONS");
                        pay.netAmount = rs.getDouble("NET_AMOUNT");
                        pay.paymentMethod = rs.getString("PAYMENT_METHOD");
                        pay.status = rs.getString("STATUS");
                        pay.notes = rs.getString("NOTES");
                        pay.processedBy = rs.getString("PROCESSED_BY");
                        pay.agencyId = getAgencyId();

                        faltantes.add(pay);
                    }
                }
            }

            if (!faltantes.isEmpty()) {
                LOGGER.info("[VoltiumSync] Sincronizando " + faltantes.size() + " nóminas pendientes hacia contabilidad del panel...");
                for (PayrollPayload p : faltantes) {
                    try {
                        guardarNominaEnPostgres(p);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "[VoltiumSync] Error subiendo nómina " + p.payrollId + ": " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[VoltiumSync] Error en sincronizarNominasLocales: " + e.getMessage());
        }
    }

    /**
     * Sincroniza bidireccionalmente los colaboradores entre ERP RRHH del panel y el POS local.
     */
    public static synchronized void sincronizarEmpleadosRRHH() {
        if (localSession == null || isOfflineMode()) {
            return;
        }

        Connection localConn = null;
        try {
            localConn = localSession.getConnection();
        } catch (Exception e) {
            return;
        }
        if (localConn == null) {
            return;
        }

        String url = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=6";

        try (Connection remoteConn = DriverManager.getConnection(url, getUser(), getPass())) {
            markOnline();

            // 1. Subir empleados locales a PostgreSQL (documentos con tabla_nombre = 'erp_rrhh')
            String sqlLocal = "SELECT p.ID, p.NAME, h.EMPLOYEE_CODE, h.DEPARTMENT, h.POSITION_TITLE, "
                    + "h.STATUS, h.HIRE_DATE, h.BASE_SALARY "
                    + "FROM PEOPLE p "
                    + "LEFT JOIN HR_EMPLOYEES h ON p.ID = h.ID";

            try (Statement st = localConn.createStatement();
                 ResultSet rs = st.executeQuery(sqlLocal)) {
                while (rs.next()) {
                    String empId = rs.getString("ID");
                    String empName = rs.getString("NAME");
                    String code = rs.getString("EMPLOYEE_CODE");
                    String dept = rs.getString("DEPARTMENT");
                    String pos = rs.getString("POSITION_TITLE");
                    String stt = rs.getString("STATUS");
                    Timestamp hDate = rs.getTimestamp("HIRE_DATE");
                    double salary = rs.getDouble("BASE_SALARY");

                    EmployeePayload emp = new EmployeePayload();
                    emp.employeeId = empId;
                    emp.employeeCode = (code != null && !code.trim().isEmpty()) ? code : ("EMP-" + empId);
                    emp.name = empName;
                    emp.area = (dept != null && !dept.trim().isEmpty()) ? dept : "Operaciones";
                    emp.role = (pos != null && !pos.trim().isEmpty()) ? pos : "Colaborador";
                    emp.status = (stt != null && !stt.trim().isEmpty()) ? stt : "Activo";
                    emp.date = (hDate != null) ? formatIsoUtc(new Date(hDate.getTime())).substring(0, 10) : formatIsoUtc(new Date()).substring(0, 10);
                    emp.salary = salary;
                    emp.agencyId = getAgencyId();

                    try {
                        guardarEmpleadoEnPostgres(emp);
                    } catch (Exception ignored) {}
                }
            }

            // 2. Descargar empleados de CRM-IA erp_rrhh hacia el POS local (si existen en remoto y no en local)
            String sqlRemote = "SELECT id, datos FROM documentos WHERE tabla_nombre = 'erp_rrhh' AND (datos LIKE '%voltium-sanrey%')";
            try (PreparedStatement ps = remoteConn.prepareStatement(sqlRemote);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rawDatos = rs.getString("datos");
                    if (rawDatos == null || rawDatos.trim().isEmpty()) continue;
                    try {
                        Map<String, Object> map = GSON.fromJson(rawDatos, new TypeToken<Map<String, Object>>(){}.getType());
                        String agency = (String) map.get("agency_id");
                        if (agency == null) agency = (String) map.get("agencyId");
                        if (!getAgencyId().equalsIgnoreCase(agency)) continue;

                        String empCode = (String) map.get("employee");
                        String empName = (String) map.get("name");
                        String role = (String) map.get("role");
                        String area = (String) map.get("area");
                        String status = (String) map.get("status");
                        double salary = 0.0;
                        Object salObj = map.get("salary");
                        if (salObj instanceof Number) {
                            salary = ((Number) salObj).doubleValue();
                        } else if (salObj instanceof String) {
                            try {
                                salary = Double.parseDouble((String) salObj);
                            } catch (Exception ignored) {}
                        }

                        if (empName != null && !empName.trim().isEmpty()) {
                            // Verificar si ya existe en PEOPLE
                            boolean existe = false;
                            try (PreparedStatement chk = localConn.prepareStatement("SELECT ID FROM PEOPLE WHERE LOWER(NAME) = LOWER(?)")) {
                                chk.setString(1, empName.trim());
                                try (ResultSet chkRs = chk.executeQuery()) {
                                    if (chkRs.next()) {
                                        existe = true;
                                    }
                                }
                            }

                            if (!existe) {
                                String newId = UUID.randomUUID().toString();
                                try (PreparedStatement insPeople = localConn.prepareStatement(
                                        "INSERT INTO PEOPLE (ID, NAME, APPPASSWORD, CARD, ROLE, VISIBLE) VALUES (?, ?, '', '', '6', TRUE)")) {
                                    insPeople.setString(1, newId);
                                    insPeople.setString(2, empName.trim());
                                    insPeople.executeUpdate();
                                }
                                try (PreparedStatement insHR = localConn.prepareStatement(
                                        "INSERT INTO HR_EMPLOYEES (ID, EMPLOYEE_CODE, DEPARTMENT, POSITION_TITLE, CONTRACT_TYPE, STATUS, HIRE_DATE, PAYROLL_FREQUENCY, BASE_SALARY, DEDUCTION_RATE) VALUES (?, ?, ?, ?, 'Indefinido', ?, NOW(), 'Mensual', ?, 9.0)")) {
                                    insHR.setString(1, newId);
                                    insHR.setString(2, empCode != null ? empCode : "EMP-001");
                                    insHR.setString(3, area != null ? area : "Operaciones");
                                    insHR.setString(4, role != null ? role : "Colaborador");
                                    insHR.setString(5, status != null ? status : "Activo");
                                    insHR.setDouble(6, salary);
                                    insHR.executeUpdate();
                                }
                                LOGGER.info("[VoltiumSync] Empleado remoto " + empName + " importado a POS local.");
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.FINE, "[VoltiumSync] Error parseando empleado remoto: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[VoltiumSync] Error en sincronizarEmpleadosRRHH: " + e.getMessage());
        }
    }

    private static File getPayrollQueueFile() {
        String userHome = System.getProperty("user.home", ".");
        File dir = new File(userHome, "kriolopos");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "voltium_payroll_offline_queue.json");
    }

    private static List<PayrollPayload> leerColaOfflineNominas(File file) {
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
            Type listType = new TypeToken<ArrayList<PayrollPayload>>() {}.getType();
            List<PayrollPayload> list = GSON.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void escribirColaOfflineNominas(File file, List<PayrollPayload> list) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
            GSON.toJson(list, writer);
        }
    }

    public static void encolarNominaLocal(PayrollPayload p) {
        if (p == null || p.payrollId == null) return;
        synchronized (PAYROLL_QUEUE_LOCK) {
            try {
                File file = getPayrollQueueFile();
                List<PayrollPayload> queue = leerColaOfflineNominas(file);
                queue.removeIf(item -> item.payrollId != null && item.payrollId.equals(p.payrollId));
                queue.add(p);
                escribirColaOfflineNominas(file, queue);
                LOGGER.info("[VoltiumSync] Nómina #" + p.payrollId + " (" + p.employeeName + ") encolada en archivo offline (" + queue.size() + " pendientes).");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "[VoltiumSync] No se pudo encolar nómina offline: " + e.getMessage(), e);
            }
        }
    }

    public static void procesarColaOfflineNominas() {
        synchronized (PAYROLL_QUEUE_LOCK) {
            File file = getPayrollQueueFile();
            if (!file.exists() || file.length() == 0) {
                return;
            }

            List<PayrollPayload> queue;
            try {
                queue = leerColaOfflineNominas(file);
            } catch (Exception e) {
                return;
            }

            if (queue.isEmpty()) {
                return;
            }

            LOGGER.info("[VoltiumSync] Intentando procesar " + queue.size() + " nóminas offline acumuladas...");
            List<PayrollPayload> pendientes = new ArrayList<>();
            boolean falloConexion = false;

            for (PayrollPayload p : queue) {
                if (falloConexion) {
                    pendientes.add(p);
                    continue;
                }

                try {
                    guardarNominaEnPostgres(p);
                    LOGGER.info("[VoltiumSync] Nómina offline #" + p.payrollId + " sincronizada con éxito.");
                } catch (Exception e) {
                    if (isConnectionError(e)) {
                        markOffline();
                        falloConexion = true;
                        pendientes.add(p);
                        LOGGER.log(Level.FINE, "[VoltiumSync] Interrupción por conexión en nómina offline #" + p.payrollId);
                    } else {
                        p.retryCount++;
                        if (p.retryCount >= 5) {
                            LOGGER.log(Level.SEVERE, "[VoltiumSync] Nómina offline #" + p.payrollId + " descartada tras 5 fallos: " + e.getMessage());
                        } else {
                            pendientes.add(p);
                        }
                    }
                }
            }

            if (!falloConexion) {
                markOnline();
            }

            try {
                escribirColaOfflineNominas(file, pendientes);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Ejecuta una sincronización completa en segundo plano:
     * 1. Vacía tickets y nóminas pendientes en las colas locales offline.
     * 2. Si la conexión a internet está activa, sincroniza clientes, productos, nóminas y empleados con el CRM.
     */
    public static synchronized void procesarSincronizacionCompleta() {
        if (localSession == null) {
            return;
        }

        // 1. Vaciar colas offline pendientes
        procesarColaOffline();
        procesarColaOfflineNominas();
        procesarColaOfflineGastos();

        // 2. Si hay conexión activa con el servidor, sincronizar clientes, productos, nóminas, personal, proveedores y gastos
        if (!isOfflineMode()) {
            sincronizarClientes();
            sincronizarProductos();
            sincronizarNominasLocales();
            sincronizarEmpleadosRRHH();
            sincronizarProveedores();
            sincronizarGastosYMovimientosLocales();
            sincronizarFacturasLocales();
        }
    }

    /**
     * Publica en el Panel las facturas ya timbradas por el POS. Se vuelve a leer
     * el historial local en cada ciclo, por lo que también reintenta al recuperar
     * la conexión y propaga cambios de estatus (por ejemplo, cancelaciones).
     */
    private static void sincronizarFacturasLocales() {
        Session session = localSession;
        if (session == null) return;

        String url = "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDb()
                + "?connectTimeout=3&socketTimeout=6";
        String agencyId = getAgencyId();
        String sql = "INSERT INTO facturas_electronicas "
                + "(id, order_id, uuid, fecha, total, estatus, agency_id) VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (id) DO UPDATE SET order_id = EXCLUDED.order_id, uuid = EXCLUDED.uuid, "
                + "fecha = EXCLUDED.fecha, total = EXCLUDED.total, estatus = EXCLUDED.estatus, "
                + "agency_id = EXCLUDED.agency_id";

        try {
            Connection local = session.getConnection();
            try (PreparedStatement invoices = local.prepareStatement(
                     "SELECT id, ticket_id, uuid, fecha, total, estatus FROM facturas_electronicas");
             ResultSet rows = invoices.executeQuery();
             Connection remote = DriverManager.getConnection(url, getUser(), getPass());
             PreparedStatement upsert = remote.prepareStatement(sql)) {
                remote.setAutoCommit(false);
                int pending = 0;
                while (rows.next()) {
                    String localId = rows.getString("id");
                    String uuid = rows.getString("uuid");
                    if (localId == null || localId.trim().isEmpty() || uuid == null
                            || uuid.trim().isEmpty() || "N/A".equalsIgnoreCase(uuid.trim())) {
                        continue;
                    }

                    String ticketId = rows.getString("ticket_id");
                    upsert.setString(1, "pos-voltium-" + localId);
                    upsert.setString(2, "POS-" + (ticketId == null ? localId : ticketId));
                    upsert.setString(3, uuid.trim());
                    Timestamp issuedAt = rows.getTimestamp("fecha");
                    upsert.setTimestamp(4, issuedAt == null ? new Timestamp(System.currentTimeMillis()) : issuedAt);
                    upsert.setBigDecimal(5, rows.getBigDecimal("total"));
                    upsert.setString(6, rows.getString("estatus"));
                    upsert.setString(7, agencyId);
                    upsert.addBatch();
                    pending++;
                }
                if (pending > 0) upsert.executeBatch();
                remote.commit();
                markOnline();
                LOGGER.info("[VoltiumSync] Historial de facturas POS sincronizado (" + pending + " registros).");
            }
        } catch (Exception e) {
            markOffline();
            LOGGER.log(Level.WARNING, "[VoltiumSync] No se pudo sincronizar el historial de facturas: " + e.getMessage());
        }
    }

    /**
     * Procesa y vacía los tickets pendientes guardados en el archivo local cuando vuelve internet.
     * Incorpora protección contra poison-pills para que un ticket corrupto no bloquee el resto.
     */
    public static void procesarColaOffline() {
        synchronized (QUEUE_LOCK) {
            File file = getQueueFile();
            if (!file.exists() || file.length() == 0) {
                return;
            }

            List<TicketPayload> queue;
            try {
                queue = leerColaOffline(file);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "[VoltiumSync] Error leyendo cola offline: " + e.getMessage());
                return;
            }

            if (queue.isEmpty()) {
                return;
            }

            LOGGER.info("[VoltiumSync] Intentando procesar " + queue.size() + " tickets offline acumulados...");
            List<TicketPayload> pendientes = new ArrayList<>();
            boolean falloConexion = false;

            for (int i = 0; i < queue.size(); i++) {
                TicketPayload p = queue.get(i);
                if (falloConexion) {
                    pendientes.add(p);
                    continue;
                }

                try {
                    guardarEnPostgres(p);
                    LOGGER.info("[VoltiumSync] Ticket offline #" + p.ticketId + " sincronizado con éxito.");
                } catch (Exception e) {
                    if (isConnectionError(e)) {
                        markOffline();
                        falloConexion = true;
                        pendientes.add(p);
                        LOGGER.log(Level.FINE, "[VoltiumSync] Interrupción por conexión al vaciar cola en ticket #" + p.ticketId + ": " + e.getMessage());
                    } else {
                        // Error de payload/datos, incrementar contador de reintentos
                        p.retryCount++;
                        if (p.retryCount >= 5) {
                            LOGGER.log(Level.SEVERE, "[VoltiumSync] Ticket offline #" + p.ticketId + " descartado a lista muerta tras 5 fallos irrecuperables: " + e.getMessage());
                            guardarEnDeadLetter(p, e.getMessage());
                        } else {
                            pendientes.add(p);
                        }
                    }
                }
            }

            if (!falloConexion) {
                markOnline();
            }

            try {
                escribirColaOffline(file, pendientes);
                if (pendientes.isEmpty()) {
                    LOGGER.info("[VoltiumSync] Todos los tickets offline se sincronizaron exitosamente.");
                } else {
                    LOGGER.info("[VoltiumSync] Quedan " + pendientes.size() + " tickets offline por sincronizar.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "[VoltiumSync] Error guardando estado de cola: " + e.getMessage());
            }
        }
    }

    private static boolean isConnectionError(Exception e) {
        if (e == null) return false;
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("timeout") || msg.contains("connection refused") || msg.contains("terminating connection")
                || msg.contains("could not connect") || msg.contains("connection closed") || msg.contains("connection reset")
                || msg.contains("broken pipe") || msg.contains("network")) {
            return true;
        }
        Throwable cause = e.getCause();
        if (cause instanceof java.net.SocketException || cause instanceof java.net.SocketTimeoutException || cause instanceof java.net.ConnectException) {
            return true;
        }
        if (e instanceof SQLException) {
            String sqlState = ((SQLException) e).getSQLState();
            if (sqlState != null && sqlState.startsWith("08")) {
                return true;
            }
        }
        return false;
    }

    private static void guardarEnDeadLetter(TicketPayload p, String errorMsg) {
        try {
            File dir = getQueueFile().getParentFile();
            File deadFile = new File(dir, "voltium_dead_letter.json");
            List<Map<String, Object>> deadList = new ArrayList<>();
            if (deadFile.exists() && deadFile.length() > 0) {
                try (Reader r = new InputStreamReader(new FileInputStream(deadFile), "UTF-8")) {
                    Type t = new TypeToken<ArrayList<Map<String, Object>>>() {}.getType();
                    List<Map<String, Object>> existing = GSON.fromJson(r, t);
                    if (existing != null) deadList = existing;
                } catch (Exception ignored) {}
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("timestamp", formatIsoUtc(new Date()));
            entry.put("error", errorMsg);
            entry.put("ticket", p);
            deadList.add(entry);
            try (Writer w = new OutputStreamWriter(new FileOutputStream(deadFile), "UTF-8")) {
                GSON.toJson(deadList, w);
            }
        } catch (Exception ignored) {}
    }

    private static File getQueueFile() {
        String userHome = System.getProperty("user.home", ".");
        File dir = new File(userHome, "kriolopos");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "voltium_offline_queue.json");
    }

    private static List<TicketPayload> leerColaOffline(File file) {
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
            Type listType = new TypeToken<ArrayList<TicketPayload>>() {}.getType();
            List<TicketPayload> list = GSON.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void escribirColaOffline(File file, List<TicketPayload> list) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
            GSON.toJson(list, writer);
        }
    }

    private static double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // Getters configurables con soporte para kriolopos.properties
    private static String getHost() {
        String val = AppConfig.getInstance().getProperty("voltium.db.host");
        return (val != null && !val.trim().isEmpty()) ? val.trim() : DEFAULT_HOST;
    }

    private static int getPort() {
        try {
            String val = AppConfig.getInstance().getProperty("voltium.db.port");
            return (val != null && !val.trim().isEmpty()) ? Integer.parseInt(val.trim()) : DEFAULT_PORT;
        } catch (Exception e) {
            return DEFAULT_PORT;
        }
    }

    private static String getDb() {
        String val = AppConfig.getInstance().getProperty("voltium.db.name");
        return (val != null && !val.trim().isEmpty()) ? val.trim() : DEFAULT_DB;
    }

    private static String getUser() {
        String val = AppConfig.getInstance().getProperty("voltium.db.user");
        return (val != null && !val.trim().isEmpty()) ? val.trim() : DEFAULT_USER;
    }

    private static String getPass() {
        String val = AppConfig.getInstance().getProperty("voltium.db.pass");
        if (val == null || val.trim().isEmpty()) {
            val = System.getenv("VOLTIUM_DB_PASS");
        }
        return val != null ? val.trim() : "";
    }

    public static String getAgencyId() {
        String val = AppConfig.getInstance().getProperty("voltium.agency.id");
        return (val != null && !val.trim().isEmpty()) ? val.trim() : DEFAULT_AGENCY;
    }
}
