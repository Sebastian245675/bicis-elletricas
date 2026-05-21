# ----------------------------------------------------
# 1. Update JPrincipalApp.java to activate HR breadcrumbs
# ----------------------------------------------------
$appFilePath = "c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx\kriolos-opos-app\src\main\java\com\openbravo\pos\forms\JPrincipalApp.java"
$appLines = [System.IO.File]::ReadAllLines($appFilePath)
$newAppLines = New-Object System.Collections.Generic.List[string]

$fixedConfig = $false
for ($i = 0; $i -lt $appLines.Length; $i++) {
    $line = $appLines[$i]
    $newAppLines.Add($line)
    
    if ($line.Contains('"com.openbravo.pos.config.JPanelConfiguration".equals(sTaskClass)')) {
        # Replace the next return line to fix "Configuración" encoding and copy the closing brace line
        $returnLine = $appLines[++$i]
        $returnLine = $returnLine.Replace('"ConfiguraciÃ³n"', '"Configuraci\u00f3n"')
        $newAppLines.Add($returnLine)
        
        $newAppLines.Add($appLines[++$i]) # Add the closing brace line
        
        # Now insert the HR breadcrumb check!
        $newAppLines.Add('        if ("com.openbravo.pos.admin.JPanelHR".equals(sTaskClass)) {')
        $newAppLines.Add('            return new String[]{ "Recursos Humanos", "com.openbravo.pos.admin.JPanelHR" };')
        $newAppLines.Add('        }')
        $fixedConfig = $true
    }
}

# Save JPrincipalApp.java back without BOM
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
if ($fixedConfig) {
    [System.IO.File]::WriteAllLines($appFilePath, $newAppLines.ToArray(), $utf8NoBom)
    Write-Output "JPrincipalApp.java successfully patched to activate Recursos Humanos breadcrumbs!"
} else {
    Write-Warning "Could not locate JPanelConfiguration block in JPrincipalApp.java!"
}

# ----------------------------------------------------
# 2. Update JPanelHR.java to redesign Expediente columns
# ----------------------------------------------------
$hrFilePath = "c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx\kriolos-opos-app\src\main\java\com\openbravo\pos\admin\JPanelHR.java"
$hrContent = [System.IO.File]::ReadAllText($hrFilePath, [System.Text.Encoding]::UTF8)

# Replace the createProfileTab method body
$oldProfileTab = '    private JComponent createProfileTab() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new GridLayout(1, 2, 16, 0));
        topRow.setOpaque(false);
        topRow.add(createLaborProfileCard());
        topRow.add(createAdministrativeCard());

        body.add(topRow);
        body.add(Box.createVerticalStrut(16));
        body.add(createNotesCard());

        return wrapScrollable(body);
    }'

$newProfileTab = '    private JComponent createProfileTab() {
        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Header Title for the unified card
        JLabel heading = new JLabel("Ficha de Expediente");
        heading.setFont(SECTION_FONT);
        heading.setForeground(TEXT_PRIMARY);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 18, 0);
        card.add(heading, gbc);

        // Fields Initialization
        m_txtEmployeeCode = createTextField();
        m_txtDepartment = createTextField();
        m_txtPositionTitle = createTextField();
        m_cmbContractType = createComboBox("Indefinido", "Fijo", "Temporal", "Por horas", "Comisionista");
        m_cmbEmployeeStatus = createComboBox("Activo", "En permiso", "Suspendido", "Retirado");
        m_txtHireDate = createReadOnlyField();
        m_cmbPayrollFrequency = createComboBox("Mensual", "Quincenal", "Semanal", "Por evento");
        m_txtEmergencyContact = createTextField();
        m_txtTaxId = createTextField();

        // Adding rows with 2 columns
        addFormRow2Col(card, gbc, 0, "Codigo interno", m_txtEmployeeCode, "Fecha de ingreso", createDateFieldGroup(m_txtHireDate, this::chooseHireDate));
        addFormRow2Col(card, gbc, 1, "Departamento", m_txtDepartment, "Frecuencia de nomina", m_cmbPayrollFrequency);
        addFormRow2Col(card, gbc, 2, "Puesto", m_txtPositionTitle, "Contacto de emergencia", m_txtEmergencyContact);
        addFormRow2Col(card, gbc, 3, "Contrato", m_cmbContractType, "Identificacion fiscal", m_txtTaxId);
        addFormRow2Col(card, gbc, 4, "Estado", m_cmbEmployeeStatus, null, null);

        // Add Notes text area full-width at the bottom
        JLabel notesLabel = createFieldLabel("Observaciones y trazabilidad");
        notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 0, 6, 0);
        card.add(notesLabel, gbc);

        m_txtNotes = createTextArea(4);
        JScrollPane scrollNotes = new JScrollPane(m_txtNotes);
        scrollNotes.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollNotes.getVerticalScrollBar().setUnitIncrement(18);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3; // Give it a smaller weight so it fits nicely
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(scrollNotes, gbc);

        return wrapScrollable(card);
    }'

if ($hrContent.Contains($oldProfileTab)) {
    $hrContent = $hrContent.Replace($oldProfileTab, $newProfileTab)
    Write-Output "Successfully replaced old createProfileTab in JPanelHR.java!"
} else {
    Write-Warning "Could not find createProfileTab to replace!"
}

# 3. Add the addFormRow2Col helper method next to addFormRow
$addFormRowMarker = '    private void addFormRow(JPanel panel, GridBagConstraints gbc, int rowIndex, String label, JComponent field) {'
$addFormRowIdx = $hrContent.IndexOf($addFormRowMarker)
if ($addFormRowIdx -ne -1) {
    # Find the end of this method (approx 12 lines down)
    $addFormRowEndIdx = $hrContent.IndexOf('}', $addFormRowIdx) + 1
    
    $oldAddRowBlock = $hrContent.Substring($addFormRowIdx, $addFormRowEndIdx - $addFormRowIdx)
    
    $newAddRowBlock = $oldAddRowBlock + "`r`n`r`n" + '    private void addFormRow2Col(JPanel panel, GridBagConstraints gbc, int rowIndex,
                                String labelLeft, JComponent fieldLeft,
                                String labelRight, JComponent fieldRight) {
        gbc.gridy = rowIndex + 1;
        gbc.gridwidth = 1;

        // Left Label
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 12, 12);
        panel.add(createFieldLabel(labelLeft), gbc);

        // Left Field
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 24); // Extra gap before next column
        panel.add(fieldLeft, gbc);

        // Right Label
        if (labelRight != null) {
            gbc.gridx = 2;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(0, 0, 12, 12);
            panel.add(createFieldLabel(labelRight), gbc);
        }

        // Right Field
        if (fieldRight != null) {
            gbc.gridx = 3;
            gbc.weightx = 0.5;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 12, 0);
            panel.add(fieldRight, gbc);
        }
    }'
    
    $hrContent = $hrContent.Replace($oldAddRowBlock, $newAddRowBlock)
    Write-Output "Successfully added addFormRow2Col helper to JPanelHR.java!"
} else {
    Write-Warning "Could not locate addFormRow marker in JPanelHR.java!"
}

# Strip BOM and write back in UTF-8 without BOM
if ($hrContent.StartsWith("`u{FEFF}")) {
    $hrContent = $hrContent.Substring(1)
} else {
    $chars = $hrContent.ToCharArray()
    if ($chars.Length -gt 0 -and $chars[0] -eq [char]0xFEFF) {
        $hrContent = $hrContent.Substring(1)
    }
}

[System.IO.File]::WriteAllText($hrFilePath, $hrContent, $utf8NoBom)
Write-Output "JPanelHR.java successfully updated with visual column layout!"
