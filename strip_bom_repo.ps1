$rootDir = "c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx"
$files = Get-ChildItem -Path $rootDir -Recurse -File -Include *.bs, *.xml, *.properties, *.txt, *.sql, *.java, *.html

$count = 0
foreach ($file in $files) {
    if ($file.FullName -like "*\.git\*" -or $file.FullName -like "*\target\*") {
        continue
    }
    
    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        Write-Host "Stripping BOM from: $($file.FullName)"
        $newBytes = New-Object byte[] ($bytes.Length - 3)
        [System.Array]::Copy($bytes, 3, $newBytes, 0, $bytes.Length - 3)
        [System.IO.File]::WriteAllBytes($file.FullName, $newBytes)
        $count++
    }
}

Write-Host "Done. Stripped BOM from $count files."
