//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.SaveProvider;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * SaveProvider que anteriormente validaba con administrador y sincronizaba con Supabase.
 * Ahora es un pass-through hacia el SaveProvider base para simplificar el sistema.
 */
public class ProductSaveProviderWithAdminValidation implements SaveProvider<Object[]> {

    private final SaveProvider<Object[]> baseSaveProvider;

    public ProductSaveProviderWithAdminValidation(SaveProvider<Object[]> baseSaveProvider,
            ProductsPanel productsPanel) {
        this.baseSaveProvider = baseSaveProvider;
    }

    @Override
    public boolean canDelete() {
        return baseSaveProvider.canDelete();
    }

    @Override
    public boolean canInsert() {
        return baseSaveProvider.canInsert();
    }

    @Override
    public boolean canUpdate() {
        return baseSaveProvider.canUpdate();
    }

    @Override
    public int deleteData(Object[] value) throws BasicException {
        return baseSaveProvider.deleteData(value);
    }

    @Override
    public int insertData(Object[] value) throws BasicException {
        return baseSaveProvider.insertData(value);
    }

    @Override
    public int updateData(Object[] value) throws BasicException {
        return baseSaveProvider.updateData(value);
    }
}
