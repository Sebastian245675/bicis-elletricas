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

package com.openbravo.beans;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author JG uniCenta
 */
public class JClockPanel extends javax.swing.JPanel {
    
    private static Calendar m_calendar = new GregorianCalendar(); // solo de ayuda...
    
    private Date m_date;
    private boolean m_bSeconds;
    private long m_lPeriod;
    
    /** Creates new form JClockPanel */
    public JClockPanel() {
        this(true);
    }
    
    /**
     *
     * @param bSeconds
     */
    public JClockPanel(boolean bSeconds) {
        
        initComponents();
        
        m_bSeconds = bSeconds;
        m_date = null;
        m_lPeriod = 0L;
    }
    
    /**
     *
     * @param bValue
     */
    public void setSecondsVisible(boolean bValue) {
        m_bSeconds = bValue;
        repaint();
    }

    /**
     *
     * @return
     */
    public boolean isSecondsVisible() {
        return m_bSeconds;
    }

    /**
     *
     * @param period
     */
    public void setPeriod(long period) {
        if (period >= 0L) {
            m_lPeriod = period;
            repaint();
        }
    }

    /**
     *
     * @return
     */
    public long getPeriod() {
        return m_lPeriod;
    }
    
    /**
     *
     * @param dDate
     */
    public void setTime(Date dDate){
        m_date = dDate;
        repaint();
    }
    
    /**
     *
     * @return
     */
    public Date getTime() {
        return m_date;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        
        super.paintComponent(g);
        
        int width = getWidth();
        int height = getHeight();
        
        double dhour = 0.0;
        double dminute = 0.0;
        double dsecond = 0.0;
            
        // Calculo los atributos de la hora que voy a pintar
        if (m_date != null) {            
            m_calendar.setTime(m_date);
            dhour = (double) m_calendar.get(Calendar.HOUR_OF_DAY);
            dminute = (double) m_calendar.get(Calendar.MINUTE);
            dsecond = (double) m_calendar.get(Calendar.SECOND);
        }
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        // guardo los valores iniciales
        Paint oldPainter = g2.getPaint();
        AffineTransform oldt = g2.getTransform();
        
        // Calculo el centro y el tamano del reloj
        int icenterx = width / 2;
        int icentery = height / 2;
        int iradius = Math.min(icenterx, icentery);        
        
        // Centro las coordenadas y ajusto la transformacion del tamano del reloj
        g2.transform(AffineTransform.getTranslateInstance(icenterx, icentery));
        g2.transform(AffineTransform.getScaleInstance(iradius / 1100.0 , iradius / 1100.0));       
        AffineTransform mytrans = g2.getTransform();
        
        // Modern clock face (flat minimalist circle)
        Color faceBg = this.isEnabled() ? new Color(248, 250, 252) : new Color(241, 245, 249); // slate 50 / slate 100
        Color borderCol = this.isEnabled() ? new Color(203, 213, 225) : new Color(226, 232, 240); // slate 300 / slate 200
        Color markCol = this.isEnabled() ? new Color(71, 85, 105) : new Color(148, 163, 184); // slate 600 / slate 400
        Color hourHandCol = this.isEnabled() ? new Color(15, 23, 42) : new Color(100, 116, 139); // slate 900 / slate 500
        Color minHandCol = this.isEnabled() ? new Color(71, 85, 105) : new Color(148, 163, 184); // slate 600 / slate 400
        Color secHandCol = new Color(239, 68, 68); // Red 500
        
        g2.setColor(borderCol);
        g2.fillOval(-1000, -1000, 2000, 2000);
        g2.setColor(faceBg);
        g2.fillOval(-950, -950, 1900, 1900);
        
        // Pinto las marcas pequenas, los minutos
        g2.setColor(markCol);
        for (int i = 0; i < 60; i++) {
            if (i % 5 != 0) {
                g2.fillRect(880, -2, 40, 4);
            }
            g2.transform(AffineTransform.getRotateInstance(Math.PI / 30.0));
        }
        
        // Pinto las marcas grandes, las horas.
        g2.setTransform(mytrans);
        for (int i = 0; i < 12; i++) {
            g2.fillRect(830, -8, 90, 16);
            g2.transform(AffineTransform.getRotateInstance(Math.PI / 6.0));
        }
        
        if (m_date != null) {
            if (m_lPeriod > 0L) { // pintamos la marca del periodo...
                int iArc = (int) (m_lPeriod / 120000L);
                g2.setTransform(mytrans);
                g2.setColor(new Color(37, 99, 235, 40)); // Blue 600 with alpha
                g2.fillArc(-950, -950, 1900, 1900, 90 - iArc, iArc);
                g2.setColor(new Color(37, 99, 235, 100));
                g2.drawArc(-950, -950, 1900, 1900, 90 - iArc, iArc);
            } else {
                // Aguja de las horas (sleek flat rounded line)
                g2.setTransform(mytrans);       
                g2.transform(AffineTransform.getRotateInstance((dhour + dminute / 60.0) * Math.PI / 6.0));
                g2.setColor(hourHandCol);
                g2.fillRoundRect(-25, -600, 50, 700, 25, 25);

                // Aguja de los minutos (sleek flat rounded line)
                g2.setTransform(mytrans);       
                g2.transform(AffineTransform.getRotateInstance((dminute) * Math.PI / 30.0));
                g2.setColor(minHandCol);
                g2.fillRoundRect(-18, -850, 36, 950, 18, 18);
        
                // Aguja de los segundos
                if (m_bSeconds) {
                    g2.setTransform(mytrans);       
                    g2.transform(AffineTransform.getRotateInstance(dsecond * Math.PI / 30.0));
                    g2.setColor(secHandCol);
                    g2.fillRoundRect(-8, -900, 16, 1100, 8, 8);
                    
                    // center dot
                    g2.setColor(secHandCol);
                    g2.fillOval(-35, -35, 70, 70);
                }
            }
        }
        
        // Center cap
        g2.setTransform(mytrans);
        g2.setColor(hourHandCol);
        g2.fillOval(-45, -45, 90, 90);
        g2.setColor(faceBg);
        g2.fillOval(-15, -15, 30, 30);
        
        // restauro los valores iniciales
        g2.setTransform(oldt);
        g2.setPaint(oldPainter);
    }   
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
