$filePath = "c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx\kriolos-opos-app\src\main\java\com\openbravo\pos\admin\JPanelHR.java"
$content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)

# 1. Replace createPayrollTab method body
$oldPayrollTab = '    private JComponent createPayrollTab() {
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setOpaque(false);
        body.add(createCompensationColumn());
        body.add(createPayrollExecutionColumn());
        return wrapScrollable(body);
    }'

$newPayrollTab = '    private JComponent createPayrollTab() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(createCompensationCard2Col());
        body.add(Box.createVerticalStrut(16));
        body.add(createPayrollPeriodCard2Col());
        body.add(Box.createVerticalStrut(16));
        body.add(createPreviewCard2Col());

        return wrapScrollable(body);
    }'

if ($content.Contains($oldPayrollTab)) {
    $content = $content.Replace($oldPayrollTab, $newPayrollTab)
    Write-Output "Successfully updated createPayrollTab method!"
} else {
    Write-Warning "createPayrollTab method block not found!"
}

# 2. Replace the old vertical column methods (lines 1366 to 1509)
$oldVerticalColumnsBlock = '    private JPanel createCompensationColumn() {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.add(createCompensationCard());
        column.add(Box.createVerticalStrut(16));
        column.add(createBenefitsCard());
        return column;
    }

    private JPanel createPayrollExecutionColumn() {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.add(createPayrollPeriodCard());
        column.add(Box.createVerticalStrut(16));
        column.add(createPreviewCard());
        return column;
    }

    private JPanel createCompensationCard() {
        JPanel card = createSectionCard("Compensacion");
        GridBagConstraints gbc = createFormConstraints();

        m_txtBaseSalary = createTextField();
        m_txtCommissionRate = createTextField();
        m_txtTransportAllowance = createTextField();
        m_txtOtherAllowances = createTextField();
        m_txtBonusAmount = createTextField();
        m_txtDeductionRate = createTextField();

        addFormRow(card, gbc, 0, "Salario base", m_txtBaseSalary);
        addFormRow(card, gbc, 1, "Comision (%)", m_txtCommissionRate);
        addFormRow(card, gbc, 2, "Auxilio transporte", m_txtTransportAllowance);
        addFormRow(card, gbc, 3, "Asignaciones extra", m_txtOtherAllowances);
        addFormRow(card, gbc, 4, "Bonificacion", m_txtBonusAmount);
        addFormRow(card, gbc, 5, "Deducciones (%)", m_txtDeductionRate);
        return card;
    }

    private JPanel createBenefitsCard() {
        JPanel card = createSectionCard("Prestaciones y pago");
        GridBagConstraints gbc = createFormConstraints();

        m_cmbPensionType = createComboBox("N/A", "AFP Integra", "AFP Prima", "AFP Profuturo", "ONP", "Privado");
        m_txtHealthInsurance = createTextField();
        m_txtSocialSecurityId = createTextField();
        m_txtBankName = createTextField();
        m_txtBankAccount = createTextField();

        addFormRow(card, gbc, 0, "Pension", m_cmbPensionType);
        addFormRow(card, gbc, 1, "Seguro de salud", m_txtHealthInsurance);
        addFormRow(card, gbc, 2, "Seguridad social", m_txtSocialSecurityId);
        addFormRow(card, gbc, 3, "Banco", m_txtBankName);
        addFormRow(card, gbc, 4, "Cuenta bancaria", m_txtBankAccount);
        return card;
    }

    private JPanel createPayrollPeriodCard() {
        JPanel card = createSectionCard("Ejecucion de nomina");
        GridBagConstraints gbc = createFormConstraints();

        m_txtPeriodLabel = createTextField();
        m_txtPeriodStart = createReadOnlyField();
        m_txtPeriodEnd = createReadOnlyField();
        m_txtPaymentDate = createReadOnlyField();
        m_cmbPaymentMethod = createComboBox("Transferencia", "Efectivo", "Cheque", "Deposito");
        m_cmbPayrollStatus = createComboBox("Pagado", "Programado", "En revision");
        m_txtPayrollNotes = createTextArea(4);

        addFormRow(card, gbc, 0, "Periodo", m_txtPeriodLabel);
        addFormRow(card, gbc, 1, "Inicio", createDateFieldGroup(m_txtPeriodStart, this::choosePeriodStart));
        addFormRow(card, gbc, 2, "Fin", createDateFieldGroup(m_txtPeriodEnd, this::choosePeriodEnd));
        addFormRow(card, gbc, 3, "Fecha de pago", createDateFieldGroup(m_txtPaymentDate, this::choosePaymentDate));
        addFormRow(card, gbc, 4, "Metodo", m_cmbPaymentMethod);
        addFormRow(card, gbc, 5, "Estado de pago", m_cmbPayrollStatus);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel notesLabel = createFieldLabel("Notas del periodo");
        card.add(notesLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JScrollPane scrollPane = new JScrollPane(m_txtPayrollNotes);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        card.add(scrollPane, gbc);
        return card;
    }

    private JPanel createPreviewCard() {
        JPanel card = createSectionCard("Vista previa");
        card.setLayout(new BorderLayout(0, 16));

        JPanel rows = new JPanel(new GridLayout(7, 2, 12, 10));
        rows.setOpaque(false);

        m_lblPreviewBase = createPreviewValueLabel();
        m_lblPreviewCommission = createPreviewValueLabel();
        m_lblPreviewAllowances = createPreviewValueLabel();
        m_lblPreviewBonus = createPreviewValueLabel();
        m_lblPreviewGross = createPreviewValueLabel();
        m_lblPreviewDeductions = createPreviewValueLabel();
        m_lblPreviewNet = createPreviewValueLabel();
        m_lblPreviewNet.setFont(new Font("Segoe UI", Font.BOLD, 24));
        m_lblPreviewNet.setForeground(SUCCESS_COLOR);

        rows.add(createPreviewLabel("Base salarial"));
        rows.add(m_lblPreviewBase);
        rows.add(createPreviewLabel("Comisiones"));
        rows.add(m_lblPreviewCommission);
        rows.add(createPreviewLabel("Asignaciones"));
        rows.add(m_lblPreviewAllowances);
        rows.add(createPreviewLabel("Bonificaciones"));
        rows.add(m_lblPreviewBonus);
        rows.add(createPreviewLabel("Ingreso bruto"));
        rows.add(m_lblPreviewGross);
        rows.add(createPreviewLabel("Deducciones"));
        rows.add(m_lblPreviewDeductions);
        rows.add(createPreviewLabel("Pago neto"));
        rows.add(m_lblPreviewNet);

        JLabel tip = new JLabel("<html>El neto se calcula con base en salario, comisiones, asignaciones, bonificaciones y el porcentaje de deduccion configurado.</html>");
        tip.setFont(BODY_FONT);
        tip.setForeground(TEXT_SECONDARY);

        JButton processButton = createActionButton("Registrar pago de nomina", SUCCESS_COLOR);
        processButton.addActionListener(e -> processPayroll());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(tip, BorderLayout.CENTER);
        bottom.add(processButton, BorderLayout.SOUTH);
        bottom.setBorder(new EmptyBorder(4, 0, 0, 0));

        card.add(rows, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        return card;
    }'

$newVerticalColumnsBlock = '    private JPanel createCompensationCard2Col() {
        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Compensacion y Prestaciones");
        heading.setFont(SECTION_FONT);
        heading.setForeground(TEXT_PRIMARY);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 18, 0);
        card.add(heading, gbc);

        // Fields Initialization
        m_txtBaseSalary = createTextField();
        m_txtCommissionRate = createTextField();
        m_txtTransportAllowance = createTextField();
        m_txtOtherAllowances = createTextField();
        m_txtBonusAmount = createTextField();
        m_txtDeductionRate = createTextField();

        m_cmbPensionType = createComboBox("N/A", "AFP Integra", "AFP Prima", "AFP Profuturo", "ONP", "Privado");
        m_txtHealthInsurance = createTextField();
        m_txtSocialSecurityId = createTextField();
        m_txtBankName = createTextField();
        m_txtBankAccount = createTextField();

        // 2 Column Form Rows
        addFormRow2Col(card, gbc, 0, "Salario base", m_txtBaseSalary, "Pension", m_cmbPensionType);
        addFormRow2Col(card, gbc, 1, "Comision (%)", m_txtCommissionRate, "Seguro de salud", m_txtHealthInsurance);
        addFormRow2Col(card, gbc, 2, "Auxilio transporte", m_txtTransportAllowance, "Seguridad social", m_txtSocialSecurityId);
        addFormRow2Col(card, gbc, 3, "Asignaciones extra", m_txtOtherAllowances, "Banco", m_txtBankName);
        addFormRow2Col(card, gbc, 4, "Bonificacion", m_txtBonusAmount, "Cuenta bancaria", m_txtBankAccount);
        addFormRow2Col(card, gbc, 5, "Deducciones (%)", m_txtDeductionRate, null, null);

        return card;
    }

    private JPanel createPayrollPeriodCard2Col() {
        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Programacion del Periodo de Pago");
        heading.setFont(SECTION_FONT);
        heading.setForeground(TEXT_PRIMARY);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 18, 0);
        card.add(heading, gbc);

        // Fields Initialization
        m_txtPeriodLabel = createTextField();
        m_txtPeriodStart = createReadOnlyField();
        m_txtPeriodEnd = createReadOnlyField();
        m_txtPaymentDate = createReadOnlyField();
        m_cmbPaymentMethod = createComboBox("Transferencia", "Efectivo", "Cheque", "Deposito");
        m_cmbPayrollStatus = createComboBox("Pagado", "Programado", "En revision");
        m_txtPayrollNotes = createTextArea(3);

        // 2 Column Form Rows
        addFormRow2Col(card, gbc, 0, "Periodo", m_txtPeriodLabel, "Fecha de pago", createDateFieldGroup(m_txtPaymentDate, this::choosePaymentDate));
        addFormRow2Col(card, gbc, 1, "Inicio", createDateFieldGroup(m_txtPeriodStart, this::choosePeriodStart), "Metodo", m_cmbPaymentMethod);
        addFormRow2Col(card, gbc, 2, "Fin", createDateFieldGroup(m_txtPeriodEnd, this::choosePeriodEnd), "Estado de pago", m_cmbPayrollStatus);

        // Period Notes full width at the bottom
        JLabel notesLabel = createFieldLabel("Notas del periodo");
        notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 0, 6, 0);
        card.add(notesLabel, gbc);

        JScrollPane scrollNotes = new JScrollPane(m_txtPayrollNotes);
        scrollNotes.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollNotes.getVerticalScrollBar().setUnitIncrement(18);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(scrollNotes, gbc);

        return card;
    }

    private JPanel createPreviewCard2Col() {
        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Calculo y Registro de Pago");
        heading.setFont(SECTION_FONT);
        heading.setForeground(TEXT_PRIMARY);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 18, 0);
        card.add(heading, gbc);

        // Vista previa values
        m_lblPreviewBase = createPreviewValueLabel();
        m_lblPreviewCommission = createPreviewValueLabel();
        m_lblPreviewAllowances = createPreviewValueLabel();
        m_lblPreviewBonus = createPreviewValueLabel();
        m_lblPreviewGross = createPreviewValueLabel();
        m_lblPreviewDeductions = createPreviewValueLabel();
        m_lblPreviewNet = createPreviewValueLabel();
        m_lblPreviewNet.setFont(new Font("Segoe UI", Font.BOLD, 24));
        m_lblPreviewNet.setForeground(SUCCESS_COLOR);

        // Grid format for Vista Previa
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.weightx = 0.0; card.add(createPreviewLabel("Base salarial"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; card.add(m_lblPreviewBase, gbc);
        gbc.gridx = 2; gbc.weightx = 0.0; card.add(createPreviewLabel("Ingreso bruto"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; card.add(m_lblPreviewGross, gbc);

        // Row 1
        gbc.gridy = 2;
        gbc.gridx = 0; gbc.weightx = 0.0; card.add(createPreviewLabel("Comisiones"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; card.add(m_lblPreviewCommission, gbc);
        gbc.gridx = 2; gbc.weightx = 0.0; card.add(createPreviewLabel("Deducciones"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; card.add(m_lblPreviewDeductions, gbc);

        // Row 2
        gbc.gridy = 3;
        gbc.gridx = 0; gbc.weightx = 0.0; card.add(createPreviewLabel("Asignaciones"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; card.add(m_lblPreviewAllowances, gbc);
        gbc.gridx = 2; gbc.weightx = 0.0; card.add(createPreviewLabel("Pago neto"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; card.add(m_lblPreviewNet, gbc);

        // Row 3
        gbc.gridy = 4;
        gbc.gridx = 0; gbc.weightx = 0.0; card.add(createPreviewLabel("Bonificaciones"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; card.add(m_lblPreviewBonus, gbc);
        gbc.gridx = 2; gbc.gridwidth = 2; gbc.gridx = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel tip = new JLabel("<html><i>El neto se calcula en base al salario base, comisiones, asignaciones, bonos y deducciones.</i></html>");
        tip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tip.setForeground(TEXT_SECONDARY);
        card.add(tip, gbc);

        // Row 4: Process Button
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(16, 0, 0, 0);

        JButton processButton = createActionButton("Registrar pago de nomina", SUCCESS_COLOR);
        processButton.addActionListener(e -> processPayroll());
        card.add(processButton, gbc);

        return card;
    }'

if ($content.Contains($oldVerticalColumnsBlock)) {
    $content = $content.Replace($oldVerticalColumnsBlock, $newVerticalColumnsBlock)
    Write-Output "Successfully updated the payroll forms block to dual-columns!"
} else {
    Write-Warning "The old vertical forms block was not found for exact replacement!"
    
    # Try normalized replacement in case line ending styles differ
    $normOld = $oldVerticalColumnsBlock.Replace("`r`n", "`n")
    $normContent = $content.Replace("`r`n", "`n")
    if ($normContent.Contains($normOld)) {
        $normContent = $normContent.Replace($normOld, $newVerticalColumnsBlock.Replace("`r`n", "`n"))
        $content = $normContent
        Write-Output "Successfully updated using normalized LF line endings!"
    } else {
        Write-Warning "Normalized match failed too!"
    }
}

# Strip BOM and write back in UTF-8 without BOM
if ($content.StartsWith("`u{FEFF}")) {
    $content = $content.Substring(1)
} else {
    $chars = $content.ToCharArray()
    if ($chars.Length -gt 0 -and $chars[0] -eq [char]0xFEFF) {
        $content = $content.Substring(1)
    }
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($filePath, $content, $utf8NoBom)
Write-Output "JPanelHR.java successfully updated with visual column payroll tab!"
