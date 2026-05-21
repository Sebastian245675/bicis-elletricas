package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.supabase.SupabaseServiceManager;
import com.openbravo.pos.supabase.SupabaseServiceREST;
import com.openbravo.pos.forms.AppConfig;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * SaveProvider para categorías que anteriormente sincronizaba con Supabase.
 * Ahora es un pass-through hacia el SaveProvider base.
 */
public class CategorySaveProviderWithSync implements SaveProvider<Object[]> {

    private final SaveProvider<Object[]> baseSaveProvider;

    public CategorySaveProviderWithSync(SaveProvider<Object[]> baseSaveProvider, CategoriesPanel categoriesPanel) {
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
