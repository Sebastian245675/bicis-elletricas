import os

mapping = {
    'Ã³': 'ó',
    'Ã­': 'í',
    'Ã¡': 'á',
    'Ã©': 'é',
    'Ãº': 'ú',
    'Ã±': 'ñ',
    'Â¡': '¡',
    'Â¿': '¿',
    'ÃƒÂ³': 'ó',
    'ÃƒÂ­': 'í',
    'ÃƒÂ¡': 'á',
    'ÃƒÂ«': 'é',
    'ÃƒÂ±': 'ñ',
    'Ã£ï¿½': 'ó',
    'Ã£ï¿½': 'í',
    'Ã£ï¿½': 'á'
}

file_path = r'c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx\kriolos-opos-app\src\main\java\com\openbravo\pos\sales\JPanelTicket.java'

with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

for bad, good in mapping.items():
    content = content.replace(bad, good)

# Fix specific broken strings observed in screenshot
content = content.replace('CÃ³digo', 'Código')
content = content.replace('dÃ­a', 'día')
content = content.replace('DescripciÃ³n', 'Descripción')
content = content.replace('pestaÃ±a', 'pestaña')
content = content.replace('vehÃ­culos', 'vehículos')
content = content.replace('artÃ­culo', 'artículo')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fix completed.")
