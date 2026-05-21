import sys

file_path = r'c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx\kriolos-opos-app\src\main\java\com\openbravo\pos\inventory\ProductsEditor.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Move labels to first column
old_h1 = '.addComponent(jLabelAccumPoints,'
new_h1 = '.addComponent(jLabelAccumPoints,\n                                                                 javax.swing.GroupLayout.DEFAULT_SIZE, 150, \n                                                                 Short.MAX_VALUE)\n                                                         .addComponent(jLabelLote, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)\n                                                         .addComponent(jLabelColor, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)'

# We need to be careful with the next lines if we replace the whole block
# Target the specific block of H-Group first column
start_tag = 'addComponent(jLabelAccumPoints,'
end_tag = 'addComponent(jLabelNoSerie,'

# Let's use a simpler approach: multiple replaces for the specific patterns

# Remove labels from the second column
content = content.replace('.addComponent(jLabelLote, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)', '')
content = content.replace('.addComponent(jLabelColor, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)', '')

# Fix potential double slashes/newlines/dots
# This is getting complex. 

# Let's just do a string replacement for the side-by-side arrangement
# Row Lote
old_lote_row = '.addComponent(jLabelLote, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)\n                                                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n                                                                 .addComponent(m_jLote,'
new_lote_row = '.addComponent(m_jLote,'

# Row Color
old_color_row = '.addComponent(jLabelColor, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)\n                                                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n                                                                 .addComponent(m_jColor,'
new_color_row = '.addComponent(m_jColor,'

# Add to first column
old_acc = '.addComponent(jLabelAccumPoints,\n                                                                 javax.swing.GroupLayout.DEFAULT_SIZE, 150,\n                                                                 Short.MAX_VALUE)'
new_acc = old_acc + '\n                                                         .addComponent(jLabelLote, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)\n                                                         .addComponent(jLabelColor, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)'

content = content.replace(old_lote_row, new_lote_row)
content = content.replace(old_color_row, new_color_row)
content = content.replace(old_acc, new_acc)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Done")
