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

package com.openbravo.pos.catalog;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.Component;
import java.awt.event.ActionListener;

/**
 *
 * @author adrianromero
 */
public interface CatalogSelector {
    
    /**
     *
     * @throws BasicException
     */
    public void loadCatalog() throws BasicException;

    /**
     *
     * @param id
     */
    public void showCatalogPanel(String id);

    /**
     *
     * @param value
     */
    public void setComponentEnabled(boolean value);

    /**
     *
     * @return
     */
    public Component getComponent();
    
    /**
     *
     * @param l
     */
    public void addActionListener(ActionListener l);  

    /**
     *
     * @param l
     */
    public void removeActionListener(ActionListener l);

    /**
     * Establece el componente de líneas de ticket para mostrar en la barra lateral.
     * @param comp
     */
    public void setTicketComponent(Component comp);

    /**
     * Muestra la vista del carrito (líneas de ticket) en la barra lateral.
     */
    public void showTicketView();

    /**
     * Muestra la vista de categorías en la barra lateral.
     */
    public void showCategoriesView();

    /**
     * Filtra los productos mostrados en el catálogo en tiempo real.
     * @param query Texto a buscar o null/vacío para restaurar vista normal.
     */
    default public void filterProducts(String query) {}

    /**
     * Retorna el primer producto que coincida con la búsqueda o null si no hay coincidencias.
     * @param query
     * @return
     */
    default public ProductInfoExt getFirstMatchingProduct(String query) {
        return null;
    }
}
