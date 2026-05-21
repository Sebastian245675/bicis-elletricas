$filePath = "c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx\kriolos-opos-app\src\main\java\com\openbravo\pos\admin\JPanelHR.java"
$content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)

# Replace the target section to add the method closing brace
$targetOld = '            m_lblValidationStatus.setText(sb.toString());
            m_lblValidationStatus.setForeground(new Color(225, 29, 72));
        }

    private void updateSpotlightHeader() {'

$targetNew = '            m_lblValidationStatus.setText(sb.toString());
            m_lblValidationStatus.setForeground(new Color(225, 29, 72));
        }
    }

    private void updateSpotlightHeader() {'

if ($content.Contains($targetOld)) {
    $content = $content.Replace($targetOld, $targetNew)
    Write-Output "Successfully fixed the missing method brace!"
} else {
    Write-Warning "Target code section not found!"
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
Write-Output "File saved successfully!"
