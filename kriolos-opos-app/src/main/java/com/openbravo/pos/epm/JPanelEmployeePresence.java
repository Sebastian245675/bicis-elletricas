//    websy POS
//    Copyright (c) 2019-2026 websy
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.

package com.openbravo.pos.epm;

import com.openbravo.pos.forms.*;
import com.openbravo.basic.BasicException;
import com.openbravo.beans.JCalendarDialog;
import com.openbravo.format.Formats;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.util.ModernActionIcon;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class JPanelEmployeePresence extends JPanel implements JPanelView, BeanFactoryApp {

    private AppView app;
    private DataLogicPresenceManagement dlPresence;
    
    // Expanded List of Functions (Cargos)
    private final String[] FUNCTIONS = {
        "Vendedor", 
        "Vacaciones / festivo", 
        "Marketing", 
        "Contabilidad", 
        "Administración", 
        "Sistemas", 
        "Almacén", 
        "Atención al Cliente"
    };
    
    private final List<String> sessionCargos = new ArrayList<>();
    
    // UI Elements
    private JTabbedPane tabbedPane;
    private JLabel lblUserSpotlight;
    
    // TAB 1: Registro Semanal (5 días)
    private JPanel tabDaily;
    private JTextField txtDailyDate;
    private JButton btnDailyCalendar;
    private JTable tblDaily;
    private DefaultTableModel modelDaily;
    private JLabel lblDailySummary;
    private JButton btnDailySave;
    private Date selectedDailyDate;
    
    // TAB 2: Resumen Mensual
    private JPanel tabMonthly;
    private JComboBox<String> cmbMonth;
    private JComboBox<String> cmbYear;
    private JComboBox<Object> cmbEmployee;
    private JTable tblMonthly;
    private DefaultTableModel modelMonthly;
    private JLabel lblMonthlySummary;
    
    private final SimpleDateFormat dfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat dfHeader = new SimpleDateFormat("EEE dd/MM", new Locale("es", "ES"));
    
    private boolean isUpdatingTable = false;

    public JPanelEmployeePresence() {
        // Constructor
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        this.app = app;
        this.dlPresence = (DataLogicPresenceManagement) app.getBean("com.openbravo.pos.epm.DataLogicPresenceManagement");
        initComponents();
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTitle() {
        return "Reportar Presencia";
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    @Override
    public Object getBean() {
        return this;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        
        // User Spotlight Panel at the top
        JPanel topInfoPanel = new JPanel(new BorderLayout());
        topInfoPanel.setBackground(new Color(7, 55, 43));
        topInfoPanel.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        
        lblUserSpotlight = new JLabel("Control de asistencia · " + app.getAppUserView().getUser().getName());
        lblUserSpotlight.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblUserSpotlight.setForeground(Color.WHITE);
        topInfoPanel.add(lblUserSpotlight, BorderLayout.WEST);
        
        add(topInfoPanel, BorderLayout.NORTH);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // TAB 1
        createTabDaily();
        tabbedPane.addTab("Registro Semanal", tabDaily);
        
        // TAB 2
        createTabMonthly();
        tabbedPane.addTab("Resumen Mensual", tabMonthly);
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void createTabDaily() {
        tabDaily = new JPanel(new BorderLayout(12, 12));
        tabDaily.setBackground(Color.WHITE);
        tabDaily.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        
        // Top Panel: Date Selection & Add Cargo
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setOpaque(false);
        
        JLabel lblDate = new JLabel("Seleccionar Fecha de Inicio (Semana):");
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        topPanel.add(lblDate);
        
        txtDailyDate = new JTextField(12);
        txtDailyDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDailyDate.setEditable(false);
        txtDailyDate.setBackground(new Color(245, 247, 250));
        topPanel.add(txtDailyDate);
        
        btnDailyCalendar = new JButton(new ModernActionIcon(ModernActionIcon.Type.CALENDAR, 20));
        btnDailyCalendar.setToolTipText("Seleccionar inicio de semana");
        btnDailyCalendar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDailyCalendar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Date d = JCalendarDialog.showCalendar(JPanelEmployeePresence.this, selectedDailyDate != null ? selectedDailyDate : new Date());
                if (d != null) {
                    selectedDailyDate = d;
                    txtDailyDate.setText(dfDate.format(selectedDailyDate));
                    updateDailyTableHeaders();
                    loadDailyPresenceData();
                }
            }
        });
        topPanel.add(btnDailyCalendar);

        // Espacio y botón Agregar Cargo
        topPanel.add(Box.createHorizontalStrut(15));
        
        JButton btnAddCargo = new JButton("Agregar actividad",
                new ModernActionIcon(ModernActionIcon.Type.ADD, 18, Color.WHITE));
        btnAddCargo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddCargo.setBackground(new Color(15, 35, 64));
        btnAddCargo.setForeground(Color.WHITE);
        btnAddCargo.setFocusPainted(false);
        btnAddCargo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newCargo = JOptionPane.showInputDialog(
                    JPanelEmployeePresence.this,
                    "Ingrese el nombre de la nueva actividad / cargo:",
                    "Agregar Nuevo Cargo",
                    JOptionPane.PLAIN_MESSAGE
                );
                if (newCargo != null && !newCargo.trim().isEmpty()) {
                    newCargo = newCargo.trim();
                    boolean exists = false;
                    for (int r = 0; r < modelDaily.getRowCount() - 1; r++) {
                        String existing = (String) modelDaily.getValueAt(r, 0);
                        if (existing.equalsIgnoreCase(newCargo)) {
                            exists = true;
                            break;
                        }
                    }
                    if (exists) {
                        JOptionPane.showMessageDialog(JPanelEmployeePresence.this,
                            "El cargo '" + newCargo + "' ya existe en la lista.",
                            "Cargo Duplicado",
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        sessionCargos.add(newCargo);
                        loadDailyPresenceData();
                    }
                }
            }
        });
        topPanel.add(btnAddCargo);
        
        tabDaily.add(topPanel, BorderLayout.NORTH);
        
        // Middle Panel: Table
        modelDaily = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // The last row is the Totals row, which is not editable
                if (row == getRowCount() - 1) {
                    return false;
                }
                // Columns 1 to 5 (the days) and Column 7 (Notes) are editable
                return (column >= 1 && column <= 5) || column == 7;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        
        tblDaily = new JTable(modelDaily);
        tblDaily.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblDaily.setRowHeight(36);
        tblDaily.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblDaily.getTableHeader().setBackground(new Color(240, 244, 248));
        tblDaily.getTableHeader().setForeground(new Color(50, 60, 70));
        
        // Listen to model changes to update totals dynamically
        modelDaily.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int col = e.getColumn();
                if (col >= 1 && col <= 5) {
                    updateTotals();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tblDaily);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        tabDaily.add(scrollPane, BorderLayout.CENTER);
        
        // Bottom Panel: Summary Labels and Save Button
        JPanel bottomPanel = new JPanel(new BorderLayout(12, 12));
        bottomPanel.setOpaque(false);
        
        lblDailySummary = new JLabel("");
        lblDailySummary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDailySummary.setForeground(new Color(15, 35, 64));
        bottomPanel.add(lblDailySummary, BorderLayout.CENTER);
        
        btnDailySave = new JButton("Guardar semana",
                new ModernActionIcon(ModernActionIcon.Type.SAVE, 18, Color.WHITE));
        btnDailySave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDailySave.setBackground(new Color(46, 125, 50));
        btnDailySave.setForeground(Color.WHITE);
        btnDailySave.setFocusPainted(false);
        btnDailySave.setPreferredSize(new Dimension(220, 40));
        btnDailySave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveWeeklyPresenceData();
            }
        });
        bottomPanel.add(btnDailySave, BorderLayout.EAST);
        
        tabDaily.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateDailyTableHeaders() {
        Date[] dates = getWeekDates();
        String[] headers = new String[]{
            "Función / Actividad",
            dfHeader.format(dates[0]),
            dfHeader.format(dates[1]),
            dfHeader.format(dates[2]),
            dfHeader.format(dates[3]),
            dfHeader.format(dates[4]),
            "Total (hrs)",
            "Notas"
        };
        
        modelDaily.setColumnIdentifiers(headers);
        
        tblDaily.getColumnModel().getColumn(0).setPreferredWidth(160);
        tblDaily.getColumnModel().getColumn(1).setPreferredWidth(95);
        tblDaily.getColumnModel().getColumn(2).setPreferredWidth(95);
        tblDaily.getColumnModel().getColumn(3).setPreferredWidth(95);
        tblDaily.getColumnModel().getColumn(4).setPreferredWidth(95);
        tblDaily.getColumnModel().getColumn(5).setPreferredWidth(95);
        tblDaily.getColumnModel().getColumn(6).setPreferredWidth(95);
        tblDaily.getColumnModel().getColumn(7).setPreferredWidth(220);
    }

    private Date[] getWeekDates() {
        Date[] weekDates = new Date[5];
        Calendar c = Calendar.getInstance();
        c.setTime(selectedDailyDate != null ? selectedDailyDate : new Date());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        
        for (int i = 0; i < 5; i++) {
            weekDates[i] = c.getTime();
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        return weekDates;
    }

    private void createTabMonthly() {
        tabMonthly = new JPanel(new BorderLayout(12, 12));
        tabMonthly.setBackground(Color.WHITE);
        tabMonthly.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        
        // Top Panel: Filters
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Labels & Combos
        JLabel lblMonth = new JLabel("Mes:");
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(lblMonth, gbc);
        
        String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        cmbMonth = new JComboBox<>(months);
        cmbMonth.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbMonth.setBackground(Color.WHITE);
        Calendar cal = Calendar.getInstance();
        cmbMonth.setSelectedIndex(cal.get(Calendar.MONTH));
        cmbMonth.addActionListener(e -> loadMonthlySummaryData());
        gbc.gridx = 1;
        topPanel.add(cmbMonth, gbc);
        
        JLabel lblYear = new JLabel("Año:");
        lblYear.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 2;
        topPanel.add(lblYear, gbc);
        
        String[] years = new String[10];
        int currentYear = cal.get(Calendar.YEAR);
        for (int i = 0; i < 10; i++) {
            years[i] = String.valueOf(currentYear - 5 + i);
        }
        cmbYear = new JComboBox<>(years);
        cmbYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbYear.setBackground(Color.WHITE);
        cmbYear.setSelectedItem(String.valueOf(currentYear));
        cmbYear.addActionListener(e -> loadMonthlySummaryData());
        gbc.gridx = 3;
        topPanel.add(cmbYear, gbc);
        
        JLabel lblEmp = new JLabel("Colaborador:");
        lblEmp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 4;
        topPanel.add(lblEmp, gbc);
        
        cmbEmployee = new JComboBox<>();
        cmbEmployee.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbEmployee.setBackground(Color.WHITE);
        cmbEmployee.addActionListener(e -> loadMonthlySummaryData());
        gbc.gridx = 5;
        topPanel.add(cmbEmployee, gbc);
        
        tabMonthly.add(topPanel, BorderLayout.NORTH);
        
        // Middle Panel: Table
        modelMonthly = new DefaultTableModel(new String[]{"Fecha", "Vendedor", "Holiday", "Marketing", "Contabilidad", "Total", "Notas"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Month view is read-only
            }
        };
        
        tblMonthly = new JTable(modelMonthly);
        tblMonthly.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblMonthly.setRowHeight(28);
        tblMonthly.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblMonthly.getTableHeader().setBackground(new Color(240, 244, 248));
        tblMonthly.getTableHeader().setForeground(new Color(50, 60, 70));
        
        JScrollPane scrollPane = new JScrollPane(tblMonthly);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        tabMonthly.add(scrollPane, BorderLayout.CENTER);
        
        // Bottom Panel: Summary Labels
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        
        lblMonthlySummary = new JLabel("");
        lblMonthlySummary.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMonthlySummary.setForeground(new Color(15, 35, 64));
        bottomPanel.add(lblMonthlySummary, BorderLayout.WEST);
        
        tabMonthly.add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void activate() throws BasicException {
        lblUserSpotlight.setText("Control de asistencia · " + app.getAppUserView().getUser().getName());
        selectedDailyDate = new Date();
        txtDailyDate.setText(dfDate.format(selectedDailyDate));
        updateDailyTableHeaders();
        loadDailyPresenceData();
        loadEmployeesCombo();
        loadMonthlySummaryData();
    }

    private void updateTotals() {
        if (isUpdatingTable) return;
        isUpdatingTable = true;
        
        try {
            double[] dayTotals = new double[5];
            double grandTotal = 0;
            
            int rowCount = modelDaily.getRowCount();
            if (rowCount > 1) {
                // Sum rows 0 to rowCount - 2 (exclude totals row)
                for (int r = 0; r < rowCount - 1; r++) {
                    double rowSum = 0;
                    for (int c = 1; c <= 5; c++) {
                        String valStr = (String) modelDaily.getValueAt(r, c);
                        double val = 0;
                        if (valStr != null && !valStr.trim().isEmpty()) {
                            try {
                                val = Double.parseDouble(valStr.replace(',', '.'));
                                dayTotals[c - 1] += val;
                                rowSum += val;
                            } catch (NumberFormatException e) {
                                // ignore invalid format
                            }
                        }
                    }
                    modelDaily.setValueAt(rowSum > 0 ? String.format(Locale.US, "%.2f", rowSum) : "", r, 6);
                    grandTotal += rowSum;
                }
                
                // Write totals to the last row (Totals row)
                modelDaily.setValueAt("Total", rowCount - 1, 0);
                for (int c = 1; c <= 5; c++) {
                    modelDaily.setValueAt(dayTotals[c - 1] > 0 ? String.format(Locale.US, "%.2f", dayTotals[c - 1]) : "0.00", rowCount - 1, c);
                }
                modelDaily.setValueAt(String.format(Locale.US, "%.2f", grandTotal), rowCount - 1, 6);
                modelDaily.setValueAt("", rowCount - 1, 7);
            }
            
            // Build summary label HTML text
            StringBuilder sb = new StringBuilder("<html><b>Totales por Día</b> &nbsp;|&nbsp; ");
            for (int i = 0; i < 5; i++) {
                String dayName = tblDaily.getColumnName(i + 1);
                sb.append(dayName).append(": ").append(String.format(Locale.US, "%.2f", dayTotals[i])).append(" hrs &nbsp;&nbsp;");
            }
            sb.append("&nbsp;&nbsp;<b>||&nbsp;&nbsp; Total Semanal: ").append(String.format(Locale.US, "%.2f", grandTotal)).append(" hrs</b></html>");
            
            lblDailySummary.setText(sb.toString());
        } finally {
            isUpdatingTable = false;
        }
    }

    private List<String> getWeeklyCargos() {
        List<String> cargos = new ArrayList<>();
        // Add default functions
        for (String f : FUNCTIONS) {
            cargos.add(f);
        }
        // Add from session list
        for (String sc : sessionCargos) {
            if (!cargos.contains(sc)) {
                cargos.add(sc);
            }
        }
        // Add unique roles ever saved by this user in database
        try {
            String pplId = app.getAppUserView().getUser().getId();
            List<String> dbFuncs = dlPresence.getUniqueRoleFunctions(pplId);
            for (String df : dbFuncs) {
                if (df != null && !df.trim().isEmpty()) {
                    boolean exists = false;
                    for (String existing : cargos) {
                        if (existing.equalsIgnoreCase(df)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        cargos.add(df);
                    }
                }
            }
        } catch (BasicException ex) {
            // Ignore
        }
        return cargos;
    }

    private void loadDailyPresenceData() {
        try {
            Date[] dates = getWeekDates();
            String pplId = app.getAppUserView().getUser().getId();
            
            // Fetch all shifts for the 5-day week
            Calendar cal = Calendar.getInstance();
            cal.setTime(dates[0]);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startWeek = cal.getTime();
            
            cal.setTime(dates[4]);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            Date endWeek = cal.getTime();
            
            List<Object[]> dbShifts = dlPresence.getDailyPresenceByUser(pplId, startWeek, endWeek);
            
            // Map structure: FunctionName -> [Day0_Shift, Day1_Shift, Day2_Shift, Day3_Shift, Day4_Shift]
            Map<String, Object[][]> functionWeekMap = new HashMap<>();
            
            for (Object[] row : dbShifts) {
                String func = (String) row[0];
                Date startShift = (Date) row[1];
                if (func != null && startShift != null) {
                    String key = func.toLowerCase();
                    Object[][] weekRow = functionWeekMap.computeIfAbsent(key, k -> new Object[5][]);
                    
                    // Determine which day index it belongs to
                    Calendar shiftCal = Calendar.getInstance();
                    shiftCal.setTime(startShift);
                    shiftCal.set(Calendar.HOUR_OF_DAY, 0);
                    shiftCal.set(Calendar.MINUTE, 0);
                    shiftCal.set(Calendar.SECOND, 0);
                    shiftCal.set(Calendar.MILLISECOND, 0);
                    long shiftTime = shiftCal.getTimeInMillis();
                    
                    for (int i = 0; i < 5; i++) {
                        if (dates[i].getTime() == shiftTime) {
                            weekRow[i] = row;
                            break;
                        }
                    }
                }
            }
            
            isUpdatingTable = true;
            modelDaily.setRowCount(0);
            
            List<String> activeCargos = getWeeklyCargos();
            for (String func : activeCargos) {
                Object[][] weekRow = functionWeekMap.get(func.toLowerCase());
                
                Object[] tableRow = new Object[8];
                tableRow[0] = func; // Col 0: Function Name
                
                String commonNotes = "";
                
                for (int i = 0; i < 5; i++) {
                    String hrsStr = "";
                    if (weekRow != null && weekRow[i] != null) {
                        Object[] rowData = weekRow[i];
                        Date start = (Date) rowData[1];
                        Date end = (Date) rowData[2];
                        String note = (String) rowData[4];
                        
                        if (start != null && end != null) {
                            long diff = end.getTime() - start.getTime();
                            double hrs = (double) diff / (3600.0 * 1000.0);
                            if (hrs > 0.0) {
                                hrsStr = String.format(Locale.US, "%.2f", hrs);
                            }
                        }
                        if (note != null && !note.trim().isEmpty()) {
                            commonNotes = note; // Use notes from existing shifts as row notes
                        }
                    }
                    tableRow[i + 1] = hrsStr; // Col 1 to 5: Day Hours
                }
                tableRow[6] = ""; // Col 6: Total (recomputed below)
                tableRow[7] = commonNotes; // Col 7: Notes
                
                modelDaily.addRow(tableRow);
            }
            
            // Add Totals row at the very end
            Object[] totalRow = new Object[8];
            totalRow[0] = "Total";
            for (int i = 1; i <= 7; i++) {
                totalRow[i] = "";
            }
            modelDaily.addRow(totalRow);
            
            isUpdatingTable = false;
            updateTotals();
        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, "No se pudo cargar el registro semanal.", ex);
            msg.show(this);
        }
    }

    private void saveWeeklyPresenceData() {
        try {
            Date[] dates = getWeekDates();
            String pplId = app.getAppUserView().getUser().getId();
            
            int rowCount = modelDaily.getRowCount();
            double[] dailyTotals = new double[5];
            for (int r = 0; r < rowCount - 1; r++) {
                String activity = String.valueOf(modelDaily.getValueAt(r, 0));
                for (int c = 1; c <= 5; c++) {
                    Object raw = modelDaily.getValueAt(r, c);
                    String value = raw == null ? "" : raw.toString().trim();
                    if (value.isEmpty()) continue;
                    final double hours;
                    try {
                        hours = Double.parseDouble(value.replace(',', '.'));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Las horas de " + activity + " no tienen un formato válido.",
                                "Revisar horas", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (!Double.isFinite(hours) || hours < 0.0 || hours > 24.0) {
                        JOptionPane.showMessageDialog(this,
                                "Cada registro debe estar entre 0 y 24 horas. Revisa " + activity + ".",
                                "Revisar horas", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    dailyTotals[c - 1] += hours;
                    if (dailyTotals[c - 1] > 24.0) {
                        JOptionPane.showMessageDialog(this,
                                "El total del día " + tblDaily.getColumnName(c) + " supera las 24 horas.",
                                "Revisar jornada", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }
            for (int r = 0; r < rowCount - 1; r++) { // Exclude the Totals row!
                String func = (String) modelDaily.getValueAt(r, 0);
                String notes = (String) modelDaily.getValueAt(r, 7);
                
                for (int c = 1; c <= 5; c++) {
                    Date date = dates[c - 1];
                    String hoursStr = (String) modelDaily.getValueAt(r, c);
                    
                    double hours = 0.0;
                    if (hoursStr != null && !hoursStr.trim().isEmpty()) {
                        try {
                            hours = Double.parseDouble(hoursStr.replace(',', '.'));
                        } catch (NumberFormatException ex) {
                            return; // Los datos ya fueron validados antes de iniciar las escrituras.
                        }
                    }
                    
                    // Daily Date Boundaries
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    Date start = cal.getTime();
                    
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                    Date end = cal.getTime();
                    
                    boolean holiday = "Holiday".equalsIgnoreCase(func)
                            || func.toLowerCase(Locale.ROOT).startsWith("vacaciones");
                    dlPresence.saveDailyPresenceByUser(pplId, func, start, end, hours, holiday, notes);
                }
            }
            
            JOptionPane.showMessageDialog(this, "Registros de presencia guardados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            loadDailyPresenceData();
            loadMonthlySummaryData();
        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, "No se pudo guardar la presencia semanal.", ex);
            msg.show(this);
        }
    }

    private void loadEmployeesCombo() {
        try {
            Object selected = cmbEmployee.getSelectedItem();
            cmbEmployee.removeAllItems();
            
            boolean isAdminOrManager = app.getAppUserView().getUser().hasPermission("hr.ViewAllAttendance");
            
            if (isAdminOrManager) {
                cmbEmployee.addItem("Todos");
                Object[] filter = new Object[] { com.openbravo.data.loader.QBFCompareEnum.COMP_NONE, null };
                List<EmployeeInfo> emps = dlPresence.getEmployeeList().list(filter);
                for (EmployeeInfo emp : emps) {
                    cmbEmployee.addItem(emp);
                }
                cmbEmployee.setEnabled(true);
            } else {
                EmployeeInfo self = new EmployeeInfo(app.getAppUserView().getUser().getId());
                self.setName(app.getAppUserView().getUser().getName());
                cmbEmployee.addItem(self);
                cmbEmployee.setEnabled(false);
            }
            
            if (selected != null) {
                cmbEmployee.setSelectedItem(selected);
            }
        } catch (BasicException ex) {
            // Ignore
        }
    }

    private List<String> getMonthlyCargos(List<Object[]> shifts) {
        List<String> list = new ArrayList<>();
        // Add defaults
        for (String f : FUNCTIONS) {
            list.add(f);
        }
        // Add any cargo saved in the database shifts for this month
        for (Object[] row : shifts) {
            String func = (String) row[6];
            if (func != null && !func.trim().isEmpty()) {
                boolean exists = false;
                for (String existing : list) {
                    if (existing.equalsIgnoreCase(func)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    list.add(func);
                }
            }
        }
        return list;
    }

    private void loadMonthlySummaryData() {
        if (cmbMonth == null || cmbYear == null || cmbEmployee == null) {
            return;
        }
        
        try {
            int monthIdx = cmbMonth.getSelectedIndex();
            int year = Integer.parseInt((String) cmbYear.getSelectedItem());
            
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, monthIdx);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startMonth = cal.getTime();
            
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            Date endMonth = cal.getTime();
            
            Object selected = cmbEmployee.getSelectedItem();
            List<Object[]> shifts = dlPresence.getMonthlyShifts(startMonth, endMonth);
            
            List<String> monthlyCargos = getMonthlyCargos(shifts);
            int numCargos = monthlyCargos.size();
            
            if (selected == null || "Todos".equals(selected)) {
                String[] headers = new String[numCargos + 2];
                headers[0] = "Vendedor / Empleado";
                for (int i = 0; i < numCargos; i++) {
                    headers[i + 1] = monthlyCargos.get(i) + " (hrs)";
                }
                headers[headers.length - 1] = "Total (hrs)";
                
                modelMonthly.setDataVector(new Object[][]{}, headers);
                
                Map<String, EmployeeMonthStats> statsMap = new LinkedHashMap<>();
                Object[] filter = new Object[] { com.openbravo.data.loader.QBFCompareEnum.COMP_NONE, null };
                List<EmployeeInfo> emps = dlPresence.getEmployeeList().list(filter);
                for (EmployeeInfo emp : emps) {
                    statsMap.put(emp.getId(), new EmployeeMonthStats(emp.getName()));
                }
                
                for (Object[] row : shifts) {
                    String pplId = (String) row[0];
                    Date start = (Date) row[2];
                    Date end = (Date) row[3];
                    String func = (String) row[6];
                    
                    EmployeeMonthStats stats = statsMap.get(pplId);
                    if (stats != null && start != null && end != null && func != null) {
                        long diff = end.getTime() - start.getTime();
                        double hrs = (double) diff / (3600.0 * 1000.0);
                        stats.addHours(func, hrs);
                    }
                }
                
                double[] grandTotals = new double[numCargos];
                double overallTotal = 0;
                
                for (EmployeeMonthStats stats : statsMap.values()) {
                    Object[] rowData = new Object[numCargos + 2];
                    rowData[0] = stats.name;
                    
                    double rowTotal = 0;
                    for (int i = 0; i < numCargos; i++) {
                        double hrs = stats.getHours(monthlyCargos.get(i));
                        rowData[i + 1] = hrs > 0 ? String.format(Locale.US, "%.2f", hrs) : "";
                        grandTotals[i] += hrs;
                        rowTotal += hrs;
                    }
                    rowData[rowData.length - 1] = rowTotal > 0 ? String.format(Locale.US, "%.2f", rowTotal) : "";
                    overallTotal += rowTotal;
                    
                    modelMonthly.addRow(rowData);
                }
                
                StringBuilder sb = new StringBuilder("<html><b>Totales Generales</b> &nbsp;|&nbsp; ");
                for (int i = 0; i < numCargos; i++) {
                    sb.append(monthlyCargos.get(i)).append(": ").append(String.format(Locale.US, "%.2f", grandTotals[i])).append(" hrs &nbsp;&nbsp;");
                }
                sb.append("&nbsp;&nbsp;<b>||&nbsp;&nbsp; Total General: ").append(String.format(Locale.US, "%.2f", overallTotal)).append(" hrs</b></html>");
                lblMonthlySummary.setText(sb.toString());
                
            } else {
                // Header: Fecha | Func1 | Func2 | ... | Total | Notas
                String[] headers = new String[numCargos + 3];
                headers[0] = "Fecha";
                for (int i = 0; i < numCargos; i++) {
                    headers[i + 1] = monthlyCargos.get(i) + " (hrs)";
                }
                headers[headers.length - 2] = "Total (hrs)";
                headers[headers.length - 1] = "Notas";
                
                modelMonthly.setDataVector(new Object[][]{}, headers);
                
                EmployeeInfo emp = (EmployeeInfo) selected;
                Calendar dayCal = Calendar.getInstance();
                dayCal.setTime(startMonth);
                int maxDays = dayCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                
                // Group shifts by day and function
                Map<Integer, Map<String, Object[]>> dayFuncShifts = new HashMap<>();
                for (Object[] row : shifts) {
                    String pplId = (String) row[0];
                    if (pplId.equals(emp.getId())) {
                        Date start = (Date) row[2];
                        String func = (String) row[6];
                        if (start != null && func != null) {
                            Calendar c = Calendar.getInstance();
                            c.setTime(start);
                            int day = c.get(Calendar.DAY_OF_MONTH);
                            
                            Map<String, Object[]> funcMap = dayFuncShifts.computeIfAbsent(day, k -> new HashMap<>());
                            funcMap.put(func.toLowerCase(), row);
                        }
                    }
                }
                
                double[] monthlyFuncTotals = new double[numCargos];
                double monthlyOverallTotal = 0;
                
                for (int day = 1; day <= maxDays; day++) {
                    dayCal.set(Calendar.DAY_OF_MONTH, day);
                    String dateStr = dfDate.format(dayCal.getTime());
                    
                    Map<String, Object[]> funcMap = dayFuncShifts.get(day);
                    
                    Object[] rowData = new Object[numCargos + 3];
                    rowData[0] = dateStr;
                    
                    double dayTotal = 0;
                    StringBuilder combinedNotes = new StringBuilder();
                    
                    for (int i = 0; i < numCargos; i++) {
                        String f = monthlyCargos.get(i);
                        double hrs = 0;
                        if (funcMap != null) {
                            Object[] row = funcMap.get(f.toLowerCase());
                            if (row != null) {
                                Date start = (Date) row[2];
                                Date end = (Date) row[3];
                                String note = (String) row[4];
                                
                                if (start != null && end != null) {
                                    long diff = end.getTime() - start.getTime();
                                    hrs = (double) diff / (3600.0 * 1000.0);
                                }
                                if (note != null && !note.trim().isEmpty()) {
                                    if (combinedNotes.length() > 0) combinedNotes.append(" | ");
                                    combinedNotes.append(f).append(": ").append(note);
                                }
                            }
                        }
                        rowData[i + 1] = hrs > 0 ? String.format(Locale.US, "%.2f", hrs) : "";
                        monthlyFuncTotals[i] += hrs;
                        dayTotal += hrs;
                    }
                    rowData[rowData.length - 2] = dayTotal > 0 ? String.format(Locale.US, "%.2f", dayTotal) : "";
                    rowData[rowData.length - 1] = combinedNotes.toString();
                    monthlyOverallTotal += dayTotal;
                    
                    modelMonthly.addRow(rowData);
                }
                
                StringBuilder sb = new StringBuilder("<html>Resumen para <b>").append(emp.getName()).append("</b> &nbsp;|&nbsp; ");
                for (int i = 0; i < numCargos; i++) {
                    sb.append(monthlyCargos.get(i)).append(": ").append(String.format(Locale.US, "%.2f", monthlyFuncTotals[i])).append(" hrs &nbsp;&nbsp;");
                }
                sb.append("&nbsp;&nbsp;<b>||&nbsp;&nbsp; Total: ").append(String.format(Locale.US, "%.2f", monthlyOverallTotal)).append(" hrs</b></html>");
                lblMonthlySummary.setText(sb.toString());
            }
            
            adjustMonthlyTableColumns();
        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, "No se pudo cargar el resumen mensual.", ex);
            msg.show(this);
        }
    }

    private void adjustMonthlyTableColumns() {
        int cols = tblMonthly.getColumnCount();
        if (tblMonthly.getColumnModel().getColumnCount() == cols) {
            tblMonthly.getColumnModel().getColumn(0).setPreferredWidth(140);
            for (int i = 1; i < cols - 1; i++) {
                tblMonthly.getColumnModel().getColumn(i).setPreferredWidth(90);
            }
            tblMonthly.getColumnModel().getColumn(cols - 1).setPreferredWidth(220);
        }
    }

    private static class EmployeeMonthStats {
        String name;
        Map<String, Double> hoursMap = new HashMap<>();
        
        public EmployeeMonthStats(String name) {
            this.name = name;
        }
        
        public void addHours(String function, double hours) {
            String key = function.toLowerCase();
            hoursMap.put(key, hoursMap.getOrDefault(key, 0.0) + hours);
        }
        
        public double getHours(String function) {
            return hoursMap.getOrDefault(function.toLowerCase(), 0.0);
        }
        
        public String getHoursStr(String function) {
            double hrs = getHours(function);
            return hrs > 0 ? String.format(Locale.US, "%.2f", hrs) : "";
        }
        
        public double getTotal() {
            double tot = 0;
            for (double h : hoursMap.values()) {
                tot += h;
            }
            return tot;
        }
    }
}
