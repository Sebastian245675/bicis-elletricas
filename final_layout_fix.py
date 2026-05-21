import sys
import re

file_path = r'c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx\kriolos-opos-app\src\main\java\com\openbravo\pos\inventory\ProductsEditor.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. First, make sure we are CLEAN (reverting any mess in jPanel2Layout)
# Mess in jPanel2Layout looks like this:
mess_pattern = r'\.addComponent\(jLabelLote, javax\.swing\.GroupLayout\.DEFAULT_SIZE, 150, Short\.MAX_VALUE\)\n\s+\.addComponent\(jLabelColor, javax\.swing\.GroupLayout\.DEFAULT_SIZE, 150, Short\.MAX_VALUE\)\n\s+\.addComponent\(jLabelAccumPoints,'
clean_mess = '.addComponent(jLabelAccumPoints,'
content = re.sub(mess_pattern, clean_mess, content)

# 2. Correctly identify jPanel1Layout block
# We search for jPanel1Layout specifically
# We want to add Lote and Color before jLabelAccumPoints in jPanel1Layout

# Find jPanel1Layout start
p1_start = content.find('jPanel1Layout.setHorizontalGroup(')
p2_start = content.find('jPanel2Layout.setHorizontalGroup(')

if p1_start != -1:
    section = content[p1_start:p2_start] if p2_start != -1 else content[p1_start:]
    
    # In this section, find .addComponent(jLabelAccumPoints,
    target = '.addComponent(jLabelAccumPoints,'
    replacement = '.addComponent(jLabelLote, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)\n                                                         .addComponent(jLabelColor, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)\n                                                         .addComponent(jLabelAccumPoints,'
    
    # Replace ONLY FIRST occurrence in THIS section
    new_section = section.replace(target, replacement, 1)
    
    # Replace the whole section back in content
    content = content[:p1_start] + new_section + (content[p2_start:] if p2_start != -1 else '')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Final Layout Fix applied successfully")
