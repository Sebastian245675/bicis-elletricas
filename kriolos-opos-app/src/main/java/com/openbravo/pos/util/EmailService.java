package com.openbravo.pos.util;

import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.reports.PanelReportBean;
import com.openbravo.pos.reports.JPanelReport;
import com.openbravo.pos.reports.JRDataSourceBasic;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.forms.DataLogicSales;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JRDataSource;

public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    public static void sendEmail(AppConfig config, String subject, String body, List<File> attachments) throws Exception {
        String host = config.getProperty("notifications.smtp.server");
        String port = config.getProperty("notifications.smtp.port");
        String user = config.getProperty("notifications.smtp.user");
        String password = config.getProperty("notifications.smtp.password");
        boolean ssl = Boolean.parseBoolean(config.getProperty("notifications.smtp.ssl"));
        String to = config.getProperty("notifications.email");

        if (host == null || host.isEmpty() || user == null || user.isEmpty() || to == null || to.isEmpty()) {
            throw new IllegalArgumentException("La configuración de correo SMTP o de destino está incompleta.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");

        if (ssl) {
            if ("465".equals(port)) {
                props.put("mail.smtp.socketFactory.port", port);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
            } else {
                props.put("mail.smtp.starttls.enable", "true");
            }
        }

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(user));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(body, "text/html; charset=utf-8");
        multipart.addBodyPart(textPart);

        if (attachments != null) {
            for (File file : attachments) {
                if (file.exists()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.attachFile(file);
                    multipart.addBodyPart(attachmentPart);
                }
            }
        }

        message.setContent(multipart);
        Transport.send(message);
        LOGGER.info("Correo enviado exitosamente a: " + to);
    }

    public static File generateReportPdf(AppView app, String scriptPath, String reportName) throws Exception {
        // Load report bean script via ScriptEngine
        com.openbravo.pos.scripting.ScriptEngine eng = com.openbravo.pos.scripting.ScriptFactory.getScriptEngine(com.openbravo.pos.scripting.ScriptFactory.BEANSHELL);
        eng.put("app", app);

        PanelReportBean reportBean = (PanelReportBean) eng.eval(StringUtils.readResource(scriptPath));
        if (reportBean == null) {
            reportBean = (PanelReportBean) eng.get("bean");
        }
        if (reportBean == null) {
            throw new Exception("No se pudo obtener el bean del reporte: " + scriptPath);
        }

        reportBean.init(app);

        String reportFilename = reportBean.getReport();
        JasperReport jasperReport = JPanelReport.createJasperReport(reportFilename);
        if (jasperReport == null) {
            throw new Exception("No se pudo compilar o cargar el reporte: " + reportFilename);
        }

        // Set default filter parameters (e.g. for last 7 days) if script uses DateInterval
        Object params = null;
        if (scriptPath.contains("closedpos") || scriptPath.contains("diary") || scriptPath.contains("debtors")) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_YEAR, -7);
            Date startDate = cal.getTime();
            Date endDate = new Date();

            params = new Object[] {
                com.openbravo.data.loader.QBFCompareEnum.COMP_GREATEROREQUALS,
                startDate,
                com.openbravo.data.loader.QBFCompareEnum.COMP_LESS,
                endDate
            };
        }

        JRDataSource data = new JRDataSourceBasic(reportBean.getSentence(), reportBean.getReportFields(), params);

        Map<String, Object> reportparams = new HashMap<>();
        reportparams.put("ARG", params);
        String res = reportBean.getResourceBundle();
        if (res != null) {
            reportparams.put("REPORT_RESOURCE_BUNDLE", ResourceBundle.getBundle(res));
        }

        // Add Taxes Logic parameter
        DataLogicSales dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        TaxesLogic taxeslogic = new TaxesLogic(dlSales.getTaxList().list());
        reportparams.put("TAXESLOGIC", taxeslogic);

        JasperPrint jp = JasperFillManager.fillReport(jasperReport, reportparams, data);

        File tempFile = File.createTempFile("report-" + reportName + "-", ".pdf");
        JasperExportManager.exportReportToPdfFile(jp, tempFile.getAbsolutePath());

        return tempFile;
    }

    public static String generateConsolidatedReportHtml(AppView app) throws Exception {
        java.sql.Connection conn = app.getSession().getConnection();
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -7);
        java.sql.Timestamp last7Days = new java.sql.Timestamp(cal.getTimeInMillis());

        // 1. Calculate General KPI Totals
        String totalsSql = "SELECT COALESCE(SUM(ticketlines.UNITS * ticketlines.PRICE), 0.0) AS TOTAL_VENTAS, " +
                           "COALESCE(SUM(ticketlines.UNITS * COALESCE(products.PRICEBUY, 0.0)), 0.0) AS TOTAL_COSTO " +
                           "FROM ticketlines " +
                           "JOIN tickets ON ticketlines.TICKET = tickets.ID " +
                           "JOIN receipts ON tickets.ID = receipts.ID " +
                           "JOIN products ON ticketlines.PRODUCT = products.ID " +
                           "JOIN closedcash ON receipts.MONEY = closedcash.MONEY " +
                           "WHERE closedcash.DATEEND IS NOT NULL AND closedcash.DATEEND >= ?";

        double totalSales = 0;
        double totalCost = 0;
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(totalsSql)) {
            pstmt.setTimestamp(1, last7Days);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalSales = rs.getDouble(1);
                    totalCost = rs.getDouble(2);
                }
            }
        }
        double netProfit = totalSales - totalCost;
        double profitMargin = totalSales > 0 ? (netProfit / totalSales) * 100 : 0;

        // 2. Query Profits and Sales by Category
        String catSql = "SELECT categories.NAME, " +
                        "COALESCE(SUM(ticketlines.UNITS * ticketlines.PRICE), 0.0) AS CAT_SALES, " +
                        "COALESCE(SUM(ticketlines.UNITS * COALESCE(products.PRICEBUY, 0.0)), 0.0) AS CAT_COST " +
                        "FROM ticketlines " +
                        "JOIN tickets ON ticketlines.TICKET = tickets.ID " +
                        "JOIN receipts ON tickets.ID = receipts.ID " +
                        "JOIN products ON ticketlines.PRODUCT = products.ID " +
                        "JOIN categories ON products.CATEGORY = categories.ID " +
                        "JOIN closedcash ON receipts.MONEY = closedcash.MONEY " +
                        "WHERE closedcash.DATEEND IS NOT NULL AND closedcash.DATEEND >= ? " +
                        "GROUP BY categories.NAME " +
                        "ORDER BY CAT_SALES DESC";

        List<Object[]> catData = new ArrayList<>();
        double maxSales = 0;
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(catSql)) {
            pstmt.setTimestamp(1, last7Days);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    double sales = rs.getDouble(2);
                    double cost = rs.getDouble(3);
                    double profit = sales - cost;
                    if (sales > maxSales) {
                        maxSales = sales;
                    }
                    catData.add(new Object[]{name, sales, profit});
                }
            }
        }

        // 3. Query Payment Methods
        String salesSql = "SELECT COALESCE(payments.PAYMENT, 'Sin ventas') AS METODO_PAGO, COALESCE(SUM(payments.TOTAL), 0.0) AS TOTAL_VENTAS " +
                          "FROM closedcash " +
                          "LEFT JOIN receipts ON closedcash.MONEY = receipts.MONEY " +
                          "LEFT JOIN payments ON payments.RECEIPT = receipts.ID " +
                          "WHERE closedcash.DATEEND IS NOT NULL AND closedcash.DATEEND >= ? " +
                          "GROUP BY COALESCE(payments.PAYMENT, 'Sin ventas')";

        List<String[]> salesItems = new ArrayList<>();
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(salesSql)) {
            pstmt.setTimestamp(1, last7Days);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String method = rs.getString(1);
                    double total = rs.getDouble(2);
                    salesItems.add(new String[]{method, com.openbravo.format.Formats.CURRENCY.formatValue(total)});
                }
            }
        }

        // Build HTML Body
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: 'Segoe UI', Arial, sans-serif; color: #334155; line-height: 1.6; margin: 0; padding: 30px; background-color: #f8fafc;\">");
        html.append("<div style=\"max-width: 800px; margin: 0 auto; background-color: #ffffff; padding: 40px; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);\">");
        
        // Header
        html.append("<div style=\"border-bottom: 2px solid #e2e8f0; padding-bottom: 20px; margin-bottom: 30px;\">");
        html.append("<h1 style=\"color: #4f46e5; margin: 0; font-size: 26px; font-weight: 800; letter-spacing: -0.5px;\">Dashboard Consolidado de Negocio</h1>");
        html.append("<p style=\"color: #64748b; margin: 5px 0 0 0; font-size: 14px;\">Voltium Sanrey TPV • Periodo: Últimos 7 Días • Generado el: ").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("</p>");
        html.append("</div>");

        // KPI Cards Grid
        html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width: 100%; margin-bottom: 35px;\"><tr>");
        
        // Card 1: Total Sales
        html.append("<td style=\"width: 31%; padding-right: 15px;\">");
        html.append("<div style=\"background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px;\">");
        html.append("<span style=\"font-size: 11px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px;\">Ventas Totales</span>");
        html.append("<div style=\"font-size: 20px; font-weight: 800; color: #0f172a; margin-top: 6px;\">").append(com.openbravo.format.Formats.CURRENCY.formatValue(totalSales)).append("</div>");
        html.append("<span style=\"display: inline-block; font-size: 10px; font-weight: 700; color: #1e1b4b; background-color: #e0e7ff; padding: 2px 8px; border-radius: 10px; margin-top: 8px;\">Bruto</span>");
        html.append("</div></td>");

        // Card 2: Cost of Sales
        html.append("<td style=\"width: 31%; padding-right: 15px;\">");
        html.append("<div style=\"background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px;\">");
        html.append("<span style=\"font-size: 11px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px;\">Costo de Ventas</span>");
        html.append("<div style=\"font-size: 20px; font-weight: 800; color: #0f172a; margin-top: 6px;\">").append(com.openbravo.format.Formats.CURRENCY.formatValue(totalCost)).append("</div>");
        html.append("<span style=\"display: inline-block; font-size: 10px; font-weight: 700; color: #451a03; background-color: #fef3c7; padding: 2px 8px; border-radius: 10px; margin-top: 8px;\">Costo</span>");
        html.append("</div></td>");

        // Card 3: Net Profit
        html.append("<td style=\"width: 38%;\">");
        html.append("<div style=\"background-color: #f5f3ff; border: 1px solid #ddd6fe; border-radius: 12px; padding: 18px;\">");
        html.append("<span style=\"font-size: 11px; font-weight: 700; color: #6d28d9; text-transform: uppercase; letter-spacing: 0.5px;\">Ganancia Neta</span>");
        html.append("<div style=\"font-size: 20px; font-weight: 800; color: #5b21b6; margin-top: 6px;\">").append(com.openbravo.format.Formats.CURRENCY.formatValue(netProfit)).append("</div>");
        html.append("<span style=\"display: inline-block; font-size: 10px; font-weight: 700; color: #065f46; background-color: #d1fae5; padding: 2px 8px; border-radius: 10px; margin-top: 8px;\">").append(String.format("%.1f", profitMargin)).append("% Margen</span>");
        html.append("</div></td>");
        
        html.append("</tr></table>");

        // 2. Profits by Category (Visual CSS Bar Chart)
        html.append("<h3 style=\"color: #0f172a; font-size: 16px; font-weight: 700; margin: 0 0 15px 0; border-left: 4px solid #4f46e5; padding-left: 10px;\">Ganancias por Departamento / Categoría</h3>");
        html.append("<div style=\"background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin-bottom: 35px;\">");

        if (catData.isEmpty()) {
            html.append("<p style=\"color: #64748b; font-style: italic; margin: 0;\">No se registran datos por categoría.</p>");
        } else {
            for (Object[] row : catData) {
                String catName = (String) row[0];
                double sales = (Double) row[1];
                double profit = (Double) row[2];
                double percentOfMax = maxSales > 0 ? (sales / maxSales) * 100 : 0;
                if (percentOfMax < 2) percentOfMax = 2; // minimum visual bar

                html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width: 100%; margin-bottom: 15px;\">");
                html.append("<tr>");
                html.append("<td style=\"font-size: 13px; font-weight: 700; color: #334155;\">").append(catName).append("</td>");
                html.append("<td style=\"font-size: 12px; font-weight: 600; color: #64748b; text-align: right;\">");
                html.append("Ganancia: <strong style=\"color: #10b981;\">").append(com.openbravo.format.Formats.CURRENCY.formatValue(profit)).append("</strong> / Venta: ").append(com.openbravo.format.Formats.CURRENCY.formatValue(sales));
                html.append("</td></tr>");
                html.append("<tr><td colspan=\"2\" style=\"padding-top: 6px;\">");
                html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width: 100%; background-color: #e2e8f0; height: 10px; border-radius: 5px; overflow: hidden;\"><tr>");
                html.append("<td style=\"background-color: #6366f1; width: ").append((int) percentOfMax).append("%; height: 10px; border-radius: 5px;\"></td>");
                html.append("<td style=\"width: ").append(100 - (int) percentOfMax).append("%; height: 10px;\"></td>");
                html.append("</tr></table>");
                html.append("</td></tr></table>");
            }
        }
        html.append("</div>");

        // 3. Payment Methods / Sales Summary
        html.append("<h3 style=\"color: #0f172a; font-size: 16px; font-weight: 700; margin: 0 0 15px 0; border-left: 4px solid #10b981; padding-left: 10px;\">Cierre de Caja (Ventas por Método de Pago)</h3>");
        html.append("<div style=\"background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin-bottom: 35px;\">");
        if (salesItems.isEmpty()) {
            html.append("<p style=\"color: #64748b; font-style: italic; margin: 0;\">No se registraron ventas en el periodo.</p>");
        } else {
            html.append("<table border=\"0\" cellpadding=\"8\" cellspacing=\"0\" style=\"width: 100%; border-collapse: collapse;\">");
            html.append("<tr style=\"background-color: #f8fafc; text-align: left; font-weight: bold;\">");
            html.append("<th style=\"border-bottom: 2px solid #e2e8f0; padding: 10px; font-size: 13px; color: #475569;\">Método de Pago</th>");
            html.append("<th style=\"border-bottom: 2px solid #e2e8f0; padding: 10px; font-size: 13px; color: #475569; text-align: right;\">Total Recibido</th>");
            html.append("</tr>");
            for (String[] row : salesItems) {
                html.append("<tr style=\"border-bottom: 1px solid #f1f5f9;\">");
                html.append("<td style=\"padding: 10px; font-size: 13px;\">").append(row[0]).append("</td>");
                html.append("<td style=\"padding: 10px; font-size: 13px; text-align: right; font-weight: 700;\">").append(row[1]).append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
        }
        html.append("</div>");

        // 4. Low Stock Alert
        html.append("<h3 style=\"color: #0f172a; font-size: 16px; font-weight: 700; margin: 0 0 15px 0; border-left: 4px solid #ef4444; padding-left: 10px;\">Alertas de Stock Bajo</h3>");
        html.append("<div style=\"background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin-bottom: 35px;\">");
        String stockSql = "SELECT products.name, locations.name, stockcurrent.units, stocklevel.stocksecurity " +
                          "FROM stockcurrent " +
                          "INNER JOIN products ON stockcurrent.product = products.id " +
                          "INNER JOIN locations ON stockcurrent.location = locations.id " +
                          "INNER JOIN stocklevel ON stockcurrent.product = stocklevel.product AND stockcurrent.location = stocklevel.location " +
                          "WHERE stocklevel.stocksecurity > 0 " +
                          "AND stockcurrent.units <= stocklevel.stocksecurity";

        List<String[]> stockItems = new ArrayList<>();
        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(stockSql)) {
            while (rs.next()) {
                stockItems.add(new String[]{
                    rs.getString(1),
                    rs.getString(2),
                    String.valueOf(rs.getDouble(3)),
                    String.valueOf(rs.getDouble(4))
                });
            }
        }

        if (stockItems.isEmpty()) {
            html.append("<p style=\"color: #10b981; font-weight: bold; margin: 0;\">✔️ Inventarios en orden. Sin alertas de stock mínimo.</p>");
        } else {
            html.append("<table border=\"0\" cellpadding=\"8\" cellspacing=\"0\" style=\"width: 100%; border-collapse: collapse;\">");
            html.append("<tr style=\"background-color: #f8fafc; text-align: left; font-weight: bold;\">");
            html.append("<th style=\"border-bottom: 2px solid #e2e8f0; padding: 10px; font-size: 13px; color: #475569;\">Producto</th>");
            html.append("<th style=\"border-bottom: 2px solid #e2e8f0; padding: 10px; font-size: 13px; color: #475569;\">Ubicación</th>");
            html.append("<th style=\"border-bottom: 2px solid #e2e8f0; padding: 10px; font-size: 13px; color: #475569; text-align: right;\">Stock Actual</th>");
            html.append("<th style=\"border-bottom: 2px solid #e2e8f0; padding: 10px; font-size: 13px; color: #475569; text-align: right;\">Mínimo</th>");
            html.append("</tr>");
            for (String[] row : stockItems) {
                html.append("<tr style=\"border-bottom: 1px solid #f1f5f9;\">");
                html.append("<td style=\"padding: 10px; font-size: 13px;\">").append(row[0]).append("</td>");
                html.append("<td style=\"padding: 10px; font-size: 13px;\">").append(row[1]).append("</td>");
                html.append("<td style=\"padding: 10px; font-size: 13px; text-align: right; color: #ef4444; font-weight: bold;\">").append(row[2]).append("</td>");
                html.append("<td style=\"padding: 10px; font-size: 13px; text-align: right;\">").append(row[3]).append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
        }
        html.append("</div>");

        // 5. Overdue Customer layaways (Deudores)
        html.append("<h3 style=\"color: #0f172a; font-size: 16px; font-weight: 700; margin: 0 0 15px 0; border-left: 4px solid #f59e0b; padding-left: 10px;\">Clientes Deudores (Apartados Vencidos)</h3>");
        html.append("<div style=\"background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px;\">");
        String apartadosSql = "SELECT T.ID, R.DATENEW, C.NAME, T.TICKETID, C.CURDEBT, " +
                              "COALESCE((SELECT SUM(TL.UNITS * TL.PRICE) FROM TICKETLINES TL WHERE TL.TICKET = T.ID), 0) AS TOTAL, " +
                              "(SELECT P.NAME FROM TICKETLINES L JOIN PRODUCTS P ON L.PRODUCT = P.ID WHERE L.TICKET = T.ID LIMIT 1) AS PRODUCT_NAME, " +
                              "R.ATTRIBUTES " +
                              "FROM TICKETS T " +
                              "JOIN RECEIPTS R ON T.ID = R.ID " +
                              "JOIN CUSTOMERS C ON T.CUSTOMER = C.ID " +
                              "WHERE T.CUSTOMER IS NOT NULL AND T.TICKETTYPE = 0";

        List<String[]> overdueItems = new ArrayList<>();
        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(apartadosSql)) {
            while (rs.next()) {
                Date datenew = rs.getTimestamp(2);
                String customerName = rs.getString(3);
                String ticketId = rs.getString(4);
                double curDebt = rs.getDouble(5);
                byte[] attrBytes = rs.getBytes(8);

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
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    calendar.setTime(datenew);
                    calendar.add(java.util.Calendar.MONTH, months);
                    Date deadline = calendar.getTime();

                    if (new Date().after(deadline)) {
                        overdueItems.add(new String[]{
                            customerName,
                            ticketId,
                            com.openbravo.format.Formats.DATE.formatValue(deadline),
                            com.openbravo.format.Formats.CURRENCY.formatValue(curDebt)
                        });
                    }
                }
            }
        }

        if (overdueItems.isEmpty()) {
            html.append("<p style=\"color: #10b981; font-weight: bold; margin: 0;\">✔️ Sin apartados vencidos pendientes por cobrar.</p>");
        } else {
            html.append("<table border=\"0\" cellpadding=\"8\" cellspacing=\"0\" style=\"width: 100%; border-collapse: collapse;\">");
            html.append("<tr style=\"background-color: #f8fafc; text-align: left; font-weight: bold;\">");
            html.append("<th style=\"border-bottom: 2px solid #e2e8f0; padding: 10px; font-size: 13px; color: #475569;\">Cliente</th>");
            html.append("<th style=\"border-bottom: 2px solid #e2e8f0; padding: 10px; font-size: 13px; color: #475569;\">Ticket ID</th>");
            html.append("<th style=\"border-bottom: 2px solid #e2e8f0; padding: 10px; font-size: 13px; color: #475569;\">Fecha Límite</th>");
            html.append("<th style=\"border-bottom: 2px solid #e2e8f0; padding: 10px; font-size: 13px; color: #475569; text-align: right;\">Saldo Pendiente</th>");
            html.append("</tr>");
            for (String[] row : overdueItems) {
                html.append("<tr style=\"border-bottom: 1px solid #f1f5f9;\">");
                html.append("<td style=\"padding: 10px; font-size: 13px;\">").append(row[0]).append("</td>");
                html.append("<td style=\"padding: 10px; font-size: 13px;\">#").append(row[1]).append("</td>");
                html.append("<td style=\"padding: 10px; font-size: 13px;\">").append(row[2]).append("</td>");
                html.append("<td style=\"padding: 10px; font-size: 13px; text-align: right; color: #b91c1c; font-weight: bold;\">").append(row[3]).append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
        }
        html.append("</div>");

        html.append("<p style=\"text-align: center; font-size: 11px; color: #94a3b8; margin-top: 40px;\">Este correo ha sido generado de forma automática por Voltium Sanrey TPV.</p>");
        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }
}
