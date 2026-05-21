$filePath = "c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx\kriolos-opos-app\src\main\java\com\openbravo\pos\admin\JPanelHR.java"
$content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)

# Target line to fix PreparedSentence and session retrieval
$oldLine = '                        new PreparedSentence(dlHR.getSession(),'
$newLine = '                        new com.openbravo.data.loader.PreparedSentence(m_App.getSession(),'

if ($content.Contains($oldLine)) {
    $content = $content.Replace($oldLine, $newLine)
    Write-Output "Successfully replaced with fully qualified com.openbravo.data.loader.PreparedSentence and m_App.getSession()!"
} else {
    Write-Warning "Target PreparedSentence line not found!"
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
