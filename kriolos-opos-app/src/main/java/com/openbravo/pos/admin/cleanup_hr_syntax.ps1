$filePath = "c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx\kriolos-opos-app\src\main\java\com\openbravo\pos\admin\JPanelHR.java"
$content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)

# Locate and replace the residual syntax junk
$junk = '        }
    });
            }

            Object[] latest = historyRows.get(0);
            String latestDate = formatDate((Date) latest[DataLogicHR.PAYROLL_PAYMENT_DATE]);
            m_lblMetricLastPayroll.setText(isBlank(latestDate) ? "Sin pagos" : latestDate);
        } catch (BasicException e) {
            showError("No se pudo cargar el historial de pagos.", e);
        }
    }'

$content = $content.Replace($junk, '        }')

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
Write-Output "Syntax cleanup successfully written!"
