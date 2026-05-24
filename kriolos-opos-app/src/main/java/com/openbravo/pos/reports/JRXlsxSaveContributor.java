package com.openbravo.pos.reports;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.filechooser.FileFilter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.view.JRSaveContributor;

/**
 * Custom Excel XLSX Save Contributor for JasperReports.
 */
public class JRXlsxSaveContributor extends JRSaveContributor {
    
    private final String extension = ".xlsx";

    public JRXlsxSaveContributor(Locale locale, ResourceBundle resBundle) {
        super(locale, resBundle);
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        return f.getName().toLowerCase().endsWith(extension);
    }

    @Override
    public String getDescription() {
        return "Libro de Excel (*.xlsx)";
    }

    @Override
    public void save(JasperPrint jasperPrint, File file) throws JRException {
        if (!file.getName().toLowerCase().endsWith(extension)) {
            file = new File(file.getAbsolutePath() + extension);
        }

        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(file));

        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(false);
        configuration.setDetectCellType(true);
        configuration.setCollapseRowSpan(false);
        configuration.setRemoveEmptySpaceBetweenRows(true);
        configuration.setRemoveEmptySpaceBetweenColumns(true);
        configuration.setWhitePageBackground(false);

        exporter.setConfiguration(configuration);
        exporter.exportReport();
    }
}

/**
 * Programmatic vector Excel Icon.
 */
class ExcelIcon implements Icon {
    private final int width;
    private final int height;

    public ExcelIcon() {
        this(16, 16);
    }

    public ExcelIcon(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        int w = width;
        int h = height;
        
        // Draw green background sheet
        g2.setColor(new Color(16, 124, 65)); // Elegant Excel green
        g2.fillRoundRect(x + 1, y + 1, w - 2, h - 2, 3, 3);
        
        // Draw grid lines inside
        g2.setColor(new Color(255, 255, 255, 60));
        g2.setStroke(new java.awt.BasicStroke(1f));
        // Vertical lines
        g2.drawLine(x + w / 2, y + 2, x + w / 2, y + h - 3);
        // Horizontal lines
        g2.drawLine(x + 2, y + h / 2, x + w - 3, y + h / 2);
        
        // Draw the white 'X' centered
        g2.setColor(Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, (int)(h * 0.65)));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        String txt = "X";
        int tx = x + (w - fm.stringWidth(txt)) / 2;
        int ty = y + ((h - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(txt, tx, ty);
        
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
