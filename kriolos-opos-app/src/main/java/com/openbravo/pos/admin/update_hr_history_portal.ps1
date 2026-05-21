$filePath = "c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx\kriolos-opos-app\src\main\java\com\openbravo\pos\admin\JPanelHR.java"
$content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)

# 1. Update fields declarations to include the new portal components
$oldFields = '    private JTable m_historyTable;
    private DefaultTableModel m_historyModel;'

$newFields = '    private JTable m_historyTable;
    private DefaultTableModel m_historyModel;
    private JComboBox<String> m_cmbHistoryYear;
    private JComboBox<String> m_cmbHistoryMonth;
    private JComboBox<String> m_cmbHistoryStatus;
    private JLabel m_lblSelectedHistoryPeriod;
    private JLabel m_lblValidationStatus;
    private java.util.List<Object[]> m_loadedPayrolls = new java.util.ArrayList<>();'

if ($content.Contains($oldFields)) {
    $content = $content.Replace($oldFields, $newFields)
    Write-Output "Successfully updated history field declarations!"
} else {
    Write-Warning "History fields marker not found!"
}

# 2. Replace createHistoryTab method with the high-fidelity Payroll portal
$historySearch = '    private JComponent createHistoryTab() {'
$historyIdx = $content.IndexOf($historySearch)
if ($historyIdx -ne -1) {
    # Find the end of this method (ends with return panel; })
    $historyEndIdx = $content.IndexOf('}', $content.IndexOf('return panel;', $historyIdx)) + 1
    $oldHistoryBlock = $content.Substring($historyIdx, $historyEndIdx - $historyIdx)
    
    $newHistoryBlock = @"
    private JComponent createHistoryTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setOpaque(false);

        // --- 1. Top Section Card: Search and Filters Bar ---
        JPanel filterCard = createCardPanel();
        filterCard.setLayout(new BorderLayout(0, 12));

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Administraci\u00f3n del Archivo de Planilla");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Consulte, filtre, valide y gestione los archivos de planilla y pagos procesados para el colaborador.");
        subtitle.setFont(BODY_FONT);
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 8, 0));

        headerText.add(title);
        headerText.add(subtitle);
        filterCard.add(headerText, BorderLayout.NORTH);

        // Horizontal filter bar layout
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        filterBar.setOpaque(false);

        filterBar.add(createFieldLabel("A\u00f1o"));
        m_cmbHistoryYear = new JComboBox<>(new String[]{ "Todos", "2024", "2025", "2026" });
        m_cmbHistoryYear.setPreferredSize(new Dimension(100, 30));
        filterBar.add(m_cmbHistoryYear);

        filterBar.add(createFieldLabel("Mes"));
        m_cmbHistoryMonth = new JComboBox<>(new String[]{
            "Todos", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        });
        m_cmbHistoryMonth.setPreferredSize(new Dimension(130, 30));
        filterBar.add(m_cmbHistoryMonth);

        filterBar.add(createFieldLabel("Estado"));
        m_cmbHistoryStatus = new JComboBox<>(new String[]{ "Todos", "Pagado", "Pendiente" });
        m_cmbHistoryStatus.setPreferredSize(new Dimension(120, 30));
        filterBar.add(m_cmbHistoryStatus);

        JButton btnSearch = createCompactButton("Buscar");
        btnSearch.setBackground(new Color(37, 99, 235)); // Brand blue
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSearch.addActionListener(e -> filterAndDisplayPayrolls());
        filterBar.add(btnSearch);

        filterCard.add(filterBar, BorderLayout.CENTER);
        mainPanel.add(filterCard, BorderLayout.NORTH);

        // --- 2. Center Section: Payments Table ---
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        m_historyModel = new DefaultTableModel(
                new String[] { "ID", "Periodo", "Fecha de Pago", "Metodo", "Monto Bruto", "Deducciones", "Monto Neto", "Estado" },
                0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        m_historyTable = new JTable(m_historyModel);
        m_historyTable.setFont(BODY_FONT);
        m_historyTable.setRowHeight(40);
        m_historyTable.setFillsViewportHeight(true);
        m_historyTable.setShowGrid(false);
        m_historyTable.setIntercellSpacing(new Dimension(0, 0));
        m_historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Style the grid header
        m_historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        m_historyTable.getTableHeader().setBackground(new Color(248, 250, 252));
        m_historyTable.getTableHeader().setForeground(TEXT_SECONDARY);
        m_historyTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Hide the ID column (index 0) from visual display but keep in model
        m_historyTable.removeColumn(m_historyTable.getColumnModel().getColumn(0));

        // Align number columns to the right
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        m_historyTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Gross
        m_historyTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Deductions
        m_historyTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Net

        JScrollPane scrollPane = new JScrollPane(m_historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // --- 3. Bottom Section: Actions Toolbar + Live Validation ---
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setOpaque(false);

        // Toolbar for actions (Verify / Print / Delete)
        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsBar.setOpaque(false);
        actionsBar.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        m_lblSelectedHistoryPeriod = new JLabel("Selecciona una planilla de pago de la lista");
        m_lblSelectedHistoryPeriod.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        m_lblSelectedHistoryPeriod.setForeground(TEXT_SECONDARY);

        JButton btnDeletePayroll = createCompactButton("Eliminar Planilla");
        btnDeletePayroll.setBackground(new Color(239, 68, 68)); // Red color
        btnDeletePayroll.setForeground(Color.WHITE);
        btnDeletePayroll.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDeletePayroll.setEnabled(false);

        JButton btnPrintReceipt = createCompactButton("Ver Recibo");
        btnPrintReceipt.setBackground(new Color(37, 99, 235)); // Brand blue
        btnPrintReceipt.setForeground(Color.WHITE);
        btnPrintReceipt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrintReceipt.setEnabled(false);

        actionsBar.add(m_lblSelectedHistoryPeriod);
        actionsBar.add(btnDeletePayroll);
        actionsBar.add(btnPrintReceipt);

        // Live validation card matching bottom error panel of Ministry portal
        JPanel validationCard = createSectionCard("Resultados de Validacion");
        validationCard.setLayout(new BorderLayout(0, 8));

        m_lblValidationStatus = new JLabel("\u25cf  Carga un colaborador para validar su planilla.");
        m_lblValidationStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        m_lblValidationStatus.setForeground(TEXT_SECONDARY);
        validationCard.add(m_lblValidationStatus, BorderLayout.CENTER);

        bottomContainer.add(actionsBar);
        bottomContainer.add(Box.createVerticalStrut(12));
        bottomContainer.add(validationCard);

        tableCard.add(bottomContainer, BorderLayout.SOUTH);
        mainPanel.add(tableCard, BorderLayout.CENTER);

        // Row Selection Listener
        m_historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = m_historyTable.getSelectedRow();
                if (row != -1) {
                    int modelRow = m_historyTable.convertRowIndexToModel(row);
                    String period = (String) m_historyModel.getValueAt(modelRow, 1);
                    String net = (String) m_historyModel.getValueAt(modelRow, 6);

                    m_lblSelectedHistoryPeriod.setText("Planilla: " + period + " (" + net + ")");
                    btnDeletePayroll.setEnabled(true);
                    btnPrintReceipt.setEnabled(true);
                } else {
                    m_lblSelectedHistoryPeriod.setText("Selecciona una planilla de pago de la lista");
                    btnDeletePayroll.setEnabled(false);
                    btnPrintReceipt.setEnabled(false);
                }
            }
        });

        // Wire delete action
        btnDeletePayroll.addActionListener(ev -> {
            int row = m_historyTable.getSelectedRow();
            if (row != -1) {
                int modelRow = m_historyTable.convertRowIndexToModel(row);
                String payrollId = (String) m_historyModel.getValueAt(modelRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "\u00bfEst\u00e1s seguro de que deseas eliminar esta planilla de pago de forma permanente?",
                    "Eliminar Planilla de Pago", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        new PreparedSentence(dlHR.getSession(),
                            "DELETE FROM HR_PAYROLL WHERE ID = ?",
                            com.openbravo.data.loader.SerializerWriteString.INSTANCE).exec(payrollId);
                        
                        PeopleInfo selected = m_employeeList.getSelectedValue();
                        if (selected != null) {
                            loadHistory(selected.getID());
                            refreshDashboard(); // Update metrics
                        }
                        JOptionPane.showMessageDialog(this, "Planilla de pago eliminada correctamente.");
                    } catch (BasicException ex) {
                        showError("No se pudo eliminar la planilla de pago.", ex);
                    }
                }
            }
        });

        // Wire print receipt action
        btnPrintReceipt.addActionListener(ev -> {
            int row = m_historyTable.getSelectedRow();
            if (row != -1) {
                int modelRow = m_historyTable.convertRowIndexToModel(row);
                String period = (String) m_historyModel.getValueAt(modelRow, 1);
                String date = (String) m_historyModel.getValueAt(modelRow, 2);
                String method = (String) m_historyModel.getValueAt(modelRow, 3);
                String gross = (String) m_historyModel.getValueAt(modelRow, 4);
                String deductions = (String) m_historyModel.getValueAt(modelRow, 5);
                String net = (String) m_historyModel.getValueAt(modelRow, 6);
                String status = (String) m_historyModel.getValueAt(modelRow, 7);

                PeopleInfo selected = m_employeeList.getSelectedValue();
                String empName = selected == null ? "Empleado" : selected.getName();

                String msg = "<html><body style='font-family: Segoe UI; padding: 12px;'>"
                    + "<h2 style='color:#2563eb; margin:0 0 12px 0;'>RECIBO DE PLANILLA DE PAGO</h2>"
                    + "<hr style='border:0; border-top:1px solid #e2e8f0; margin-bottom:12px;'>"
                    + "<table style='width:100%; border-collapse:collapse;'>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'><b>Colaborador:</b></td><td style='text-align:right;'>" + empName + "</td></tr>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'><b>Per\u00edodo:</b></td><td style='text-align:right;'>" + period + "</td></tr>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'><b>Fecha Pago:</b></td><td style='text-align:right;'>" + date + "</td></tr>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'><b>M\u00e9todo de Pago:</b></td><td style='text-align:right;'>" + method + "</td></tr>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'><b>Estado:</b></td><td style='text-align:right;'><b style='color:#16a34a;'>" + status + "</b></td></tr>"
                    + "</table>"
                    + "<hr style='border:0; border-top:1px solid #e2e8f0; margin:12px 0;'>"
                    + "<table style='width:100%; border-collapse:collapse;'>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'>Monto Bruto:</td><td style='text-align:right;'>" + gross + "</td></tr>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'>Deducciones:</td><td style='text-align:right; color:#ef4444;'>-" + deductions + "</td></tr>"
                    + "<tr style='font-size:14px; font-weight:bold; border-top:1px solid #cbd5e1;'>"
                    + "<td style='padding:8px 0; color:#1e293b;'>Monto Neto a Pagar:</td>"
                    + "<td style='padding:8px 0; text-align:right; color:#2563eb;'>" + net + "</td></tr>"
                    + "</table>"
                    + "</body></html>";

                JOptionPane.showMessageDialog(this, msg, "Recibo de Pago Procesado", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        return wrapScrollable(mainPanel);
    }
"@
    $content = $content.Replace($oldHistoryBlock, $newHistoryBlock)
    Write-Output "Successfully replaced createHistoryTab method with the high-fidelity Payroll portal!"
} else {
    Write-Warning "createHistoryTab method marker not found!"
}

# 3. Replace loadHistory and add the new filter and validation methods
$loadHistorySearch = '    private void loadHistory(String employeeId) {'
$loadHistoryIdx = $content.IndexOf($loadHistorySearch)
if ($loadHistoryIdx -ne -1) {
    # Find the end of this method
    $loadHistoryEndIdx = $content.IndexOf('}', $content.IndexOf('m_lblMetricLastPayroll.setText', $loadHistoryIdx)) + 1
    # Check if there's a wider catch block ending
    $loadHistoryEndIdx = $content.IndexOf('}', $loadHistoryEndIdx) + 1
    
    $oldLoadHistoryBlock = $content.Substring($loadHistoryIdx, $loadHistoryEndIdx - $loadHistoryIdx)
    
    $newLoadHistoryBlock = @"
    private void loadHistory(String employeeId) {
        m_loadedPayrolls.clear();
        try {
            m_loadedPayrolls = dlHR.getPayrollHistory(employeeId);
            filterAndDisplayPayrolls();
        } catch (BasicException e) {
            showError("No se pudo cargar el historial de pagos.", e);
        }
    }

    private void filterAndDisplayPayrolls() {
        m_historyModel.setRowCount(0);
        if (m_loadedPayrolls == null || m_loadedPayrolls.isEmpty()) {
            m_lblMetricLastPayroll.setText("Sin pagos");
            m_lblValidationStatus.setText("<html><span style='color:#64748b;'>\u25cf</span>  <b>Sin planillas registradas:</b> No se han procesado planillas de pago para este colaborador.</html>");
            m_lblValidationStatus.setForeground(TEXT_SECONDARY);
            return;
        }

        String yearSel = (String) m_cmbHistoryYear.getSelectedItem();
        int monthSel = m_cmbHistoryMonth.getSelectedIndex(); 
        String statusSel = (String) m_cmbHistoryStatus.getSelectedItem();

        java.util.Calendar cal = java.util.Calendar.getInstance();

        for (Object[] row : m_loadedPayrolls) {
            Date paymentDate = (Date) row[DataLogicHR.PAYROLL_PAYMENT_DATE];
            cal.setTime(paymentDate);

            // Filter by Year
            if (!"Todos".equals(yearSel)) {
                int yearVal = cal.get(java.util.Calendar.YEAR);
                if (yearVal != Integer.parseInt(yearSel)) {
                    continue;
                }
            }

            // Filter by Month
            if (monthSel != 0) {
                int monthVal = cal.get(java.util.Calendar.MONTH); 
                if (monthVal != (monthSel - 1)) {
                    continue;
                }
            }

            // Filter by Status
            if (!"Todos".equals(statusSel)) {
                String statusVal = asText(row[DataLogicHR.PAYROLL_STATUS]);
                if (!statusSel.equalsIgnoreCase(statusVal)) {
                    continue;
                }
            }

            m_historyModel.addRow(new Object[] {
                row[DataLogicHR.PAYROLL_ID], 
                asText(row[DataLogicHR.PAYROLL_PERIOD_LABEL]),
                formatDateTime(paymentDate),
                asText(row[DataLogicHR.PAYROLL_PAYMENT_METHOD]),
                formatCurrency(asDouble(row[DataLogicHR.PAYROLL_GROSS_AMOUNT])),
                formatCurrency(asDouble(row[DataLogicHR.PAYROLL_DEDUCTIONS])),
                formatCurrency(asDouble(row[DataLogicHR.PAYROLL_NET_AMOUNT])),
                asText(row[DataLogicHR.PAYROLL_STATUS])
            });
        }

        Object[] latest = m_loadedPayrolls.get(0);
        String latestDate = formatDate((Date) latest[DataLogicHR.PAYROLL_PAYMENT_DATE]);
        m_lblMetricLastPayroll.setText(isBlank(latestDate) ? "Sin pagos" : latestDate);

        updateValidationResultsSection();
    }

    private void updateValidationResultsSection() {
        PeopleInfo selected = m_employeeList.getSelectedValue();
        if (selected == null) {
            m_lblValidationStatus.setText("\u25cf  Carga un colaborador para validar su planilla.");
            m_lblValidationStatus.setForeground(TEXT_SECONDARY);
            return;
        }

        java.util.List<String> warnings = new java.util.ArrayList<>();
        
        double baseSalary = safeParseDouble(m_txtBaseSalary.getText());
        if (baseSalary <= 0.0) {
            warnings.add("El salario base es $0.00. Por favor, revisa la compensaci\u00f3n.");
        }
        
        String socialSec = cleanText(m_txtSocialSecurityId.getText());
        if (socialSec.isEmpty()) {
            warnings.add("El colaborador no tiene asignado un n\u00famero de seguridad social.");
        }

        String emergency = cleanText(m_txtEmergencyContact.getText());
        if (emergency.isEmpty()) {
            warnings.add("Falta el contacto de emergencia en la ficha de personal.");
        }

        String bankAcc = cleanText(m_txtBankAccount.getText());
        if (bankAcc.isEmpty()) {
            warnings.add("No se ha configurado la cuenta bancaria para la transferencia.");
        }

        if (warnings.isEmpty()) {
            m_lblValidationStatus.setText("<html><span style='color:#16a34a;'>\u25cf</span>  <b>Planilla validada correctamente:</b> No se detectaron errores ni advertencias de informaci\u00f3n para este colaborador.</html>");
            m_lblValidationStatus.setForeground(new Color(22, 163, 74));
        } else {
            StringBuilder sb = new StringBuilder("<html><span style='color:#e11d48;'>\u25cf</span>  <b>Planilla con advertencias de informaci\u00f3n (" + warnings.size() + "):</b><br>");
            for (String warn : warnings) {
                sb.append("&nbsp;&nbsp;&nbsp;&nbsp;<span style='color:#b91c1c;'>\u2022</span>&nbsp;").append(warn).append("<br>");
            }
            sb.append("</html>");
            m_lblValidationStatus.setText(sb.toString());
            m_lblValidationStatus.setForeground(new Color(225, 29, 72));
        }
    }
"@
    $content = $content.Replace($oldLoadHistoryBlock, $newLoadHistoryBlock)
    Write-Output "Successfully updated loadHistory and added filter/validation methods!"
} else {
    Write-Warning "loadHistory method marker not found!"
}

# Strip any BOM if present
if ($content.StartsWith("`u{FEFF}")) {
    $content = $content.Substring(1)
} else {
    $chars = $content.ToCharArray()
    if ($chars.Length -gt 0 -and $chars[0] -eq [char]0xFEFF) {
        $content = $content.Substring(1)
    }
}

# Save back to file in UTF-8 without BOM
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($filePath, $content, $utf8NoBom)
Write-Output "PowerShell Payroll Portal update successfully written without BOM!"
