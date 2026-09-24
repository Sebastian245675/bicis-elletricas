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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 *
 * @author JG uniCenta
 */
public class JCalendarPanel extends javax.swing.JPanel {
    
    private final LocaleResources m_resources;


    private Date m_date;    
    private JButtonDate[] m_ListDates;
    private JLabel[] m_jDays;
    
    private JButtonDate m_jCurrent;
    private JButtonDate m_jBtnMonthInc;
    private JButtonDate m_jBtnMonthDec;
    private JButtonDate m_jBtnYearInc;
    private JButtonDate m_jBtnYearDec;
    private JButtonDate m_jBtnToday;
    
    private DateFormat fmtMonthYear = new SimpleDateFormat("MMMMM yyyy");
    
    public JCalendarPanel() {
        this(new Date());
    }

    /**
     *
     * @param dDate
     */
    public JCalendarPanel(Date dDate) {
        super();
    
        m_resources = new LocaleResources();
        m_resources.addBundleName("beans_messages");
        
        initComponents();
        initComponents2();

        m_date = dDate;
        
        renderMonth();
        renderDay();
    }

    /**
     *
     * @param dNewDate
     */
    public void setDate(Date dNewDate) {        
                     
        // cambiamos la fecha
        Date dOldDate = m_date;  
        m_date = dNewDate;

        // pintamos
        renderMonth();
        renderDay();

        // decimos al mundo que ha cambiado la propiedad fecha
        firePropertyChange("Date", dOldDate, dNewDate);
    }

    /**
     *
     * @return
     */
    public Date getDate() {
        return m_date;
    }
    
    public void setEnabled(boolean bValue) {
           
        super.setEnabled(bValue);   
        
        // pintamos
        renderMonth();
        renderDay();
    }
    
    private void renderMonth() {
        
//        GregorianCalendar oCalRender = new GregorianCalendar();
//        oCalRender.setTime(m_CalendarHelper.getTime());

        GregorianCalendar m_CalendarHelper = new GregorianCalendar();
                
        for (int j = 0; j < 7; j++) {
            m_jDays[j].setEnabled(isEnabled());
        }    
        
        // Borramos todos los dias
        for(int i = 0; i < 42; i++) {
            JButtonDate jAux = m_ListDates[i];
            jAux.DateInf = null;
            jAux.setEnabled(false);
            jAux.setText(null);
            jAux.setForeground(new Color(203, 213, 225)); // slate-300
            jAux.setBackground(new Color(248, 250, 252)); // slate-50
            jAux.setBorder(null);
        }
        
        if (m_date == null) {
            m_jLblMonth.setEnabled(isEnabled());
            m_jLblMonth.setText(null);
        } else {
            m_CalendarHelper.setTime(m_date);
            
            m_jLblMonth.setEnabled(isEnabled());
            m_jLblMonth.setFont(new Font("Segoe UI", Font.BOLD, 16));
            m_jLblMonth.setForeground(new Color(15, 23, 42)); // slate-900
            m_jLblMonth.setText(fmtMonthYear.format(m_CalendarHelper.getTime()).toUpperCase());
            
            int iCurrentMonth = m_CalendarHelper.get(Calendar.MONTH);
            m_CalendarHelper.set(Calendar.DAY_OF_MONTH, 1);

            while(m_CalendarHelper.get(Calendar.MONTH) == iCurrentMonth) {

                JButtonDate jAux = getLabelByDate(m_CalendarHelper.getTime());
                jAux.DateInf = m_CalendarHelper.getTime();
                jAux.setEnabled(isEnabled());
                jAux.setText(String.valueOf(m_CalendarHelper.get(Calendar.DAY_OF_MONTH)));
                jAux.setBackground(Color.WHITE);
                jAux.setForeground(new Color(15, 23, 42)); // slate-900

                m_CalendarHelper.add(Calendar.DATE, 1);
            }
        }

        m_jCurrent = null;
    }

    private void renderDay() {
        
        GregorianCalendar m_CalendarHelper = new GregorianCalendar();
        
        m_jBtnToday.setEnabled(isEnabled());
        
        if (m_date == null) {
            m_jBtnMonthDec.setEnabled(false);
            m_jBtnMonthInc.setEnabled(isEnabled());
            m_jBtnYearDec.setEnabled(isEnabled());
            m_jBtnYearInc.setEnabled(isEnabled());
        } else {
            m_CalendarHelper.setTime(m_date);

            m_CalendarHelper.add(Calendar.MONTH, -1);
            m_jBtnMonthDec.DateInf = m_CalendarHelper.getTime();
            m_jBtnMonthDec.setEnabled(isEnabled());
            m_CalendarHelper.add(Calendar.MONTH, 2);
            m_jBtnMonthInc.DateInf = m_CalendarHelper.getTime();
            m_jBtnMonthInc.setEnabled(isEnabled());

            m_CalendarHelper.setTime(m_date);
            m_CalendarHelper.add(Calendar.YEAR, -1);
            m_jBtnYearDec.DateInf = m_CalendarHelper.getTime();
            m_jBtnYearDec.setEnabled(isEnabled());
            m_CalendarHelper.add(Calendar.YEAR, 2);
            m_jBtnYearInc.DateInf = m_CalendarHelper.getTime();
            m_jBtnYearInc.setEnabled(isEnabled());
        
            if(m_jCurrent != null) {
                m_jCurrent.setForeground(new Color(15, 23, 42)); // slate-900
                m_jCurrent.setBackground(Color.WHITE);
                m_jCurrent.setBorder(null);
            }

            JButtonDate jAux = getLabelByDate(m_date);
            jAux.setBackground(new Color(15, 35, 64)); // premium dark navy
            jAux.setForeground(Color.WHITE);
            jAux.setBorder(null);
            m_jCurrent = jAux;
        }
    }

    private JButtonDate getLabelByDate(Date d) {
        
        GregorianCalendar oCalRender = new GregorianCalendar();
        oCalRender.setTime(d);
        int iDayOfMonth = oCalRender.get(Calendar.DAY_OF_MONTH);
        
        oCalRender.set(Calendar.DAY_OF_MONTH, 1);
       
        int iCol = oCalRender.get(Calendar.DAY_OF_WEEK) - oCalRender.getFirstDayOfWeek();
        if (iCol < 0) {
            iCol += 7;
        }
        return m_ListDates[iCol + iDayOfMonth - 1];
    }

    private class DateClick implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JButtonDate oLbl = (JButtonDate)e.getSource();
            if(oLbl.DateInf != null) {
                setDate(oLbl.DateInf);
            }
        }
    }

    private static class JButtonDate extends JButton {

        public Date DateInf;

        public JButtonDate(ActionListener datehandler) {
            super();
            initComponent();
            addActionListener(datehandler);
        }
        public JButtonDate(String sText, ActionListener datehandler) {
            super(sText);
            initComponent();
            addActionListener(datehandler);
        }    
        public JButtonDate(Icon icon, ActionListener datehandler) {
            super(icon);
            initComponent();
            addActionListener(datehandler);
        }   
        
        private void initComponent() {
            DateInf = null;
            setRequestFocusEnabled(false);
            setFocusPainted(false);
            setFocusable(false);
            
            // Modern flat look with content filled to show background color
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBorderPainted(false);
            setContentAreaFilled(true); // MUST BE TRUE to show backgrounds!
            setOpaque(true);
            setBackground(Color.WHITE);
            setForeground(new Color(51, 65, 85)); // slate-700
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
    }

    private void initComponents2() {

        ActionListener dateclick = new DateClick();
        
        m_jBtnYearDec = new JButtonDate("«", dateclick);
        m_jBtnMonthDec = new JButtonDate("‹", dateclick);
        m_jBtnToday = new JButtonDate(m_resources.getString("button.Today"), dateclick);
        m_jBtnMonthInc = new JButtonDate("›", dateclick);
        m_jBtnYearInc = new JButtonDate("»", dateclick);
               
        m_jBtnToday.DateInf = new Date();
        
        Font arrowFont = new Font("Segoe UI", Font.BOLD, 18);
        m_jBtnYearDec.setFont(arrowFont);
        m_jBtnMonthDec.setFont(arrowFont);
        m_jBtnMonthInc.setFont(arrowFont);
        m_jBtnYearInc.setFont(arrowFont);
        
        m_jBtnYearDec.setBackground(new Color(241, 245, 249));
        m_jBtnYearDec.setForeground(new Color(71, 85, 105));
        m_jBtnYearDec.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        
        m_jBtnMonthDec.setBackground(new Color(241, 245, 249));
        m_jBtnMonthDec.setForeground(new Color(71, 85, 105));
        m_jBtnMonthDec.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        
        m_jBtnMonthInc.setBackground(new Color(241, 245, 249));
        m_jBtnMonthInc.setForeground(new Color(71, 85, 105));
        m_jBtnMonthInc.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        
        m_jBtnYearInc.setBackground(new Color(241, 245, 249));
        m_jBtnYearInc.setForeground(new Color(71, 85, 105));
        m_jBtnYearInc.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        
        m_jBtnToday.setBackground(new Color(16, 185, 129)); // emerald
        m_jBtnToday.setForeground(Color.WHITE);
        m_jBtnToday.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        
        // Remove legacy vertical navigation strip and clear header panel
        remove(jPanel3);
        jPanel2.removeAll();
        
        // Re-arrange jPanel2 to contain the buttons horizontally above the days
        jPanel2.setLayout(new BorderLayout(10, 0));
        jPanel2.setBackground(Color.WHITE);
        jPanel2.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        leftNav.setOpaque(false);
        leftNav.add(m_jBtnYearDec);
        leftNav.add(m_jBtnMonthDec);
        
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightNav.setOpaque(false);
        rightNav.add(m_jBtnToday);
        rightNav.add(m_jBtnMonthInc);
        rightNav.add(m_jBtnYearInc);
        
        m_jLblMonth.setHorizontalAlignment(SwingConstants.CENTER);
        
        jPanel2.add(leftNav, BorderLayout.WEST);
        jPanel2.add(m_jLblMonth, BorderLayout.CENTER);
        jPanel2.add(rightNav, BorderLayout.EAST);
        
        // Add 12px inner padding to the entire calendar container panel
        jPanel1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        m_ListDates = new JButtonDate[42];
        for(int i = 0; i < 42; i++) {
            JButtonDate jAux = new JButtonDate(dateclick);
            jAux.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jAux.setText(null);
            jAux.setOpaque(true);
            jAux.setForeground(new Color(51, 65, 85));
            jAux.setBackground(Color.WHITE);
            jAux.setBorder(null);
            m_ListDates[i] = jAux;
            m_jDates.add(jAux);
        }
        
        m_jDays = new JLabel[7];
        for(int iHead = 0; iHead < 7; iHead++) {
            JLabel JAuxHeader = new JLabel();
            JAuxHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
            JAuxHeader.setForeground(new Color(100, 116, 139)); // slate-500
            JAuxHeader.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            m_jDays[iHead] = JAuxHeader;
            m_jWeekDays.add(JAuxHeader);
        }
        
        DateFormat fmtWeekDay = new SimpleDateFormat("E");
        Calendar oCalRender = new GregorianCalendar();
        int iCol;
        for (int j = 0; j < 7; j++) {
            oCalRender.add(Calendar.DATE, 1);
            iCol = oCalRender.get(Calendar.DAY_OF_WEEK) - oCalRender.getFirstDayOfWeek();
            if (iCol < 0) {
                iCol += 7;
            }
            String dayName = fmtWeekDay.format(oCalRender.getTime());
            if (dayName.length() > 3) dayName = dayName.substring(0, 3);
            m_jDays[iCol].setText(dayName.toUpperCase());
        }      
        
        revalidate();
        repaint();
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        m_jMonth = new javax.swing.JPanel();
        m_jWeekDays = new javax.swing.JPanel();
        m_jDates = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        m_jLblMonth = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        m_jActions = new javax.swing.JPanel();

        setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(226, 232, 240)));
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.setBackground(Color.WHITE);

        m_jMonth.setLayout(new java.awt.BorderLayout());
        m_jMonth.setBackground(Color.WHITE);

        m_jWeekDays.setLayout(new java.awt.GridLayout(1, 7));
        m_jWeekDays.setBackground(Color.WHITE);
        m_jMonth.add(m_jWeekDays, java.awt.BorderLayout.NORTH);

        m_jDates.setBackground(Color.WHITE);
        m_jDates.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        m_jDates.setLayout(new java.awt.GridLayout(6, 7));
        m_jMonth.add(m_jDates, java.awt.BorderLayout.CENTER);

        jPanel1.add(m_jMonth, java.awt.BorderLayout.CENTER);

        m_jLblMonth.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        m_jLblMonth.setForeground(new Color(15, 23, 42));
        jPanel2.setBackground(Color.WHITE);
        jPanel2.add(m_jLblMonth);

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.setBackground(Color.WHITE);

        m_jActions.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        m_jActions.setLayout(new java.awt.GridLayout(0, 1, 0, 5));
        m_jActions.setBackground(Color.WHITE);
        jPanel3.add(m_jActions, java.awt.BorderLayout.NORTH);

        add(jPanel3, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel m_jActions;
    private javax.swing.JPanel m_jDates;
    private javax.swing.JLabel m_jLblMonth;
    private javax.swing.JPanel m_jMonth;
    private javax.swing.JPanel m_jWeekDays;
    // End of variables declaration//GEN-END:variables
    
}
