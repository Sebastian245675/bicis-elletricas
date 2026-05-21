import sys

file_path = r'c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx\kriolos-opos-app\src\main\java\com\openbravo\pos\inventory\ProductsEditor.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Pattern for label alignment in column 1
old_acc = '.addComponent(jLabelAccumPoints,'
new_acc = '.addComponent(jLabelLote, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)\n                                                         .addComponent(jLabelColor, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)\n                                                         .addComponent(jLabelAccumPoints,'

if old_acc in content:
    content = content.replace(old_acc, new_acc)
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Replacement successful")
else:
    print("Pattern not found")
