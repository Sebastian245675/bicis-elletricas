package com.openbravo.pos.util;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppView;
import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailScheduler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(EmailScheduler.class.getName());
    private final AppView app;

    public EmailScheduler(AppView app) {
        this.app = app;
    }

    @Override
    public void run() {
        // Sleep for 30 seconds at startup to avoid blocking startup loading
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            return;
        }

        while (true) {
            try {
                checkAndSendAlertsAndReports();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error en EmailScheduler", ex);
            }

            // Check every hour (3600000 milliseconds)
            try {
                Thread.sleep(3600000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void checkAndSendAlertsAndReports() {
        AppConfig config = new AppConfig(app.getProperties().getConfigFile());
        config.load();

        String host = config.getProperty("notifications.smtp.server");
        if (host == null || host.isEmpty()) {
            return; // SMTP not configured yet
        }

        // 1. Alerts
        try {
            if (Boolean.parseBoolean(config.getProperty("notifications.alert.low_stock"))) {
                String lastSent = config.getProperty("notifications.alert.low_stock.last_sent");
                String today = LocalDate.now().toString();
                if (lastSent == null || !lastSent.equals(today)) {
                    if (sendLowStockAlert(config)) {
                        config.setProperty("notifications.alert.low_stock.last_sent", today);
                        config.save();
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error al enviar alerta de stock bajo", ex);
        }

        try {
            if (Boolean.parseBoolean(config.getProperty("notifications.alert.overdue_debts"))) {
                String lastSent = config.getProperty("notifications.alert.overdue_debts.last_sent");
                String today = LocalDate.now().toString();
                if (lastSent == null || !lastSent.equals(today)) {
                    if (sendOverdueDebtsAlert(config)) {
                        config.setProperty("notifications.alert.overdue_debts.last_sent", today);
                        config.save();
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error al enviar alerta de deudas vencidas", ex);
        }

        // 2. Scheduled Reports
        try {
            if (Boolean.parseBoolean(config.getProperty("notifications.reports.enabled"))) {
                String freq = config.getProperty("notifications.reports.frequency"); // DAILY, WEEKLY, MONTHLY
                String lastSent = config.getProperty("notifications.reports.last_sent");
                String today = LocalDate.now().toString();

                boolean shouldSend = false;
                if (lastSent == null || lastSent.isEmpty()) {
                    shouldSend = true;
                } else {
                    LocalDate lastSentDate = LocalDate.parse(lastSent);
                    LocalDate now = LocalDate.now();
                    if ("DAILY".equals(freq)) {
                        shouldSend = now.isAfter(lastSentDate);
                    } else if ("WEEKLY".equals(freq)) {
                        String targetDay = config.getProperty("notifications.reports.dayofweek"); // MONDAY, etc.
                        String currentDay = now.getDayOfWeek().toString();
                        if (currentDay.equalsIgnoreCase(targetDay)) {
                            shouldSend = now.isAfter(lastSentDate.plusDays(6));
                        }
                    } else if ("MONTHLY".equals(freq)) {
                        shouldSend = now.isAfter(lastSentDate.plusDays(28)) && now.getDayOfMonth() == 1;
                    }
                }

                if (shouldSend) {
                    sendScheduledReports(config);
                    config.setProperty("notifications.reports.last_sent", today);
                    config.save();
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al procesar reportes automáticos programados", ex);
        }
    }

    private boolean sendLowStockAlert(AppConfig config) throws Exception {
        java.sql.Connection conn = app.getSession().getConnection();
        String sql = "SELECT products.name, locations.name, stockcurrent.units, stocklevel.stocksecurity " +
                     "FROM stockcurrent " +
                     "INNER JOIN products ON stockcurrent.product = products.id " +
                     "INNER JOIN locations ON stockcurrent.location = locations.id " +
                     "INNER JOIN stocklevel ON stockcurrent.product = stocklevel.product AND stockcurrent.location = stocklevel.location " +
                     "WHERE stocklevel.stocksecurity > 0 " +
                     "AND stockcurrent.units <= stocklevel.stocksecurity";

        List<String[]> lowStockItems = new ArrayList<>();
        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lowStockItems.add(new String[]{
                    rs.getString(1),
                    rs.getString(2),
                    String.valueOf(rs.getDouble(3)),
                    String.valueOf(rs.getDouble(4))
                });
            }
        }

        if (lowStockItems.isEmpty()) {
            return false; // No products low on stock
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>Alerta de Stock Bajo - Voltium Sanrey</h2>");
        html.append("<p>Los siguientes productos se encuentran en o por debajo de su límite de stock de seguridad:</p>");
        html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
        html.append("<tr style='background-color: #f2f2f2;'><th>Producto</th><th>Ubicación</th><th>Stock Actual</th><th>Mínimo Requerido</th></tr>");
        for (String[] item : lowStockItems) {
            html.append("<tr>");
            html.append("<td>").append(item[0]).append("</td>");
            html.append("<td>").append(item[1]).append("</td>");
            html.append("<td style='color: red; font-weight: bold;'>").append(item[2]).append("</td>");
            html.append("<td>").append(item[3]).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");

        EmailService.sendEmail(config, "Alerta: Productos con Stock Bajo", html.toString(), null);
        return true;
    }

    private boolean sendOverdueDebtsAlert(AppConfig config) throws Exception {
        java.sql.Connection conn = app.getSession().getConnection();
        String sql = "SELECT T.ID, R.DATENEW, C.NAME, T.TICKETID, C.CURDEBT, " +
                     "COALESCE((SELECT SUM(TL.UNITS * TL.PRICE) FROM TICKETLINES TL WHERE TL.TICKET = T.ID), 0) AS TOTAL, " +
                     "(SELECT P.NAME FROM TICKETLINES L JOIN PRODUCTS P ON L.PRODUCT = P.ID WHERE L.TICKET = T.ID LIMIT 1) AS PRODUCT_NAME, " +
                     "C.PHONE, C.EMAIL, R.ATTRIBUTES " +
                     "FROM TICKETS T " +
                     "JOIN RECEIPTS R ON T.ID = R.ID " +
                     "JOIN CUSTOMERS C ON T.CUSTOMER = C.ID " +
                     "WHERE T.CUSTOMER IS NOT NULL AND T.TICKETTYPE = 0";

        List<String[]> overdueItems = new ArrayList<>();
        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date datenew = rs.getTimestamp(2);
                String customerName = rs.getString(3);
                String ticketId = rs.getString(4);
                double curDebt = rs.getDouble(5);
                double total = rs.getDouble(6);
                String productName = rs.getString(7);
                String phone = rs.getString(8);
                String email = rs.getString(9);
                byte[] attrBytes = rs.getBytes(10);

                int months = 0;
                if (attrBytes != null) {
                    try {
                        java.util.Properties props = new java.util.Properties();
                        props.loadFromXML(new java.io.ByteArrayInputStream(attrBytes));
                        String monthsStr = props.getProperty("apartado_meses");
                        if (monthsStr != null) {
                            months = Integer.parseInt(monthsStr);
                        }
                    } catch (Exception ex) { }
                }

                if (datenew != null && months > 0 && curDebt > 0) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(datenew);
                    cal.add(java.util.Calendar.MONTH, months);
                    Date deadline = cal.getTime();

                    if (new Date().after(deadline)) {
                        overdueItems.add(new String[]{
                            customerName,
                            ticketId,
                            productName != null ? productName : "N/A",
                            com.openbravo.format.Formats.DATE.formatValue(deadline),
                            com.openbravo.format.Formats.CURRENCY.formatValue(curDebt),
                            phone != null ? phone : "N/A"
                        });
                    }
                }
            }
        }

        if (overdueItems.isEmpty()) {
            return false;
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>Alerta de Cuotas / Apartados Vencidos - Voltium Sanrey</h2>");
        html.append("<p>Los siguientes clientes tienen apartados vencidos con saldo pendiente:</p>");
        html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
        html.append("<tr style='background-color: #f2f2f2;'><th>Cliente</th><th>Ticket ID</th><th>Producto</th><th>Fecha Límite</th><th>Deuda Pendiente</th><th>Teléfono</th></tr>");
        for (String[] item : overdueItems) {
            html.append("<tr>");
            html.append("<td>").append(item[0]).append("</td>");
            html.append("<td>#").append(item[1]).append("</td>");
            html.append("<td>").append(item[2]).append("</td>");
            html.append("<td>").append(item[3]).append("</td>");
            html.append("<td style='color: red; font-weight: bold;'>").append(item[4]).append("</td>");
            html.append("<td>").append(item[5]).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");

        EmailService.sendEmail(config, "Alerta: Cuotas / Apartados Vencidos de Clientes", html.toString(), null);
        return true;
    }

    public void sendScheduledReports(AppConfig config) throws Exception {
        List<File> attachments = new ArrayList<>();
        List<String> reportNames = new ArrayList<>();

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.closedpos"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/sales_closedpos.bs", "cierre_caja");
                attachments.add(pdf);
                reportNames.add("Cierre de Caja (Ventas)");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de cierre de caja", ex);
            }
        }

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.inventory"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/products.bs", "inventario_productos");
                attachments.add(pdf);
                reportNames.add("Inventario de Productos");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de inventario", ex);
            }
        }

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.debtors"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/customers_debtors.bs", "clientes_deudores");
                attachments.add(pdf);
                reportNames.add("Clientes Deudores");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de deudores", ex);
            }
        }

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.categorysales"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/sales_categorysales.bs", "ventas_por_departamento");
                attachments.add(pdf);
                reportNames.add("Ventas por Departamento");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de ventas por departamento", ex);
            }
        }

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.customers"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/customers.bs", "catalogo_clientes");
                attachments.add(pdf);
                reportNames.add("Catálogo de Clientes");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de catálogo de clientes", ex);
            }
        }

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.productprofit"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/sales_productsalesprofit.bs", "rentabilidad_por_producto");
                attachments.add(pdf);
                reportNames.add("Rentabilidad por Producto");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de rentabilidad por producto", ex);
            }
        }

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.cashflow"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/sales_cashflow.bs", "flujo_de_caja");
                attachments.add(pdf);
                reportNames.add("Flujo de Caja");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de flujo de caja", ex);
            }
        }

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.saletaxes"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/sales_saletaxes.bs", "impuestos_ventas");
                attachments.add(pdf);
                reportNames.add("Impuestos sobre Ventas");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de impuestos sobre ventas", ex);
            }
        }

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.usersales"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/usersales.bs", "ventas_por_cajero");
                attachments.add(pdf);
                reportNames.add("Ventas por Cajero");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de ventas por cajero", ex);
            }
        }

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.uservoids"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/uservoids.bs", "ventas_anuladas");
                attachments.add(pdf);
                reportNames.add("Ventas Anuladas");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de ventas anuladas", ex);
            }
        }

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.inventorydiff"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/inventorydiff.bs", "diferencias_inventario");
                attachments.add(pdf);
                reportNames.add("Diferencias de Inventario");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de diferencias de inventario", ex);
            }
        }

        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.suppliers"))) {
            try {
                File pdf = EmailService.generateReportPdf(app, "/com/openbravo/reports/suppliers_creditors.bs", "proveedores_acreedores");
                attachments.add(pdf);
                reportNames.add("Proveedores Acreedores");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al generar reporte de proveedores acreedores", ex);
            }
        }

        String bodyHtml = "";
        if (Boolean.parseBoolean(config.getProperty("notifications.reports.include.consolidated"))) {
            try {
                bodyHtml = EmailService.generateConsolidatedReportHtml(app);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error al generar reporte consolidado HTML", ex);
                bodyHtml = "<p>Error al generar reporte consolidado HTML: " + ex.getMessage() + "</p>";
            }
        } else {
            if (attachments.isEmpty()) {
                return;
            }
            StringBuilder html = new StringBuilder();
            html.append("<h2>Reporte Programado Periódico - Voltium Sanrey</h2>");
            html.append("<p>Se adjuntan los siguientes reportes en formato PDF:</p>");
            html.append("<ul>");
            for (String name : reportNames) {
                html.append("<li>").append(name).append("</li>");
            }
            html.append("</ul>");
            html.append("<p>Este correo ha sido generado de forma automática por el TPV Voltium Sanrey.</p>");
            bodyHtml = html.toString();
        }

        EmailService.sendEmail(config, "Reportes Periódicos del TPV", bodyHtml, attachments.isEmpty() ? null : attachments);

        // Clean up temp files
        for (File f : attachments) {
            f.delete();
        }
    }
}
