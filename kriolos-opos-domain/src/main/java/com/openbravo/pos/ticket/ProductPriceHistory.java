package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWrite;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Entidad para registrar el historial de variaciones de precio de los productos.
 * Sebastian - Implementado para la función de Variación.
 */
public class ProductPriceHistory implements Serializable, IKeyed {
    private String id;
    private String productId;
    private Double priceBuy;
    private Double priceSell;
    private Date dateNew;
    private String userId;

    public ProductPriceHistory() {
        this.id = UUID.randomUUID().toString();
        this.dateNew = new Date();
    }

    public ProductPriceHistory(String productId, Double priceBuy, Double priceSell, String userId) {
        this();
        this.productId = productId;
        this.priceBuy = priceBuy;
        this.priceSell = priceSell;
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public Double getPriceBuy() { return priceBuy; }
    public void setPriceBuy(Double priceBuy) { this.priceBuy = priceBuy; }
    public Double getPriceSell() { return priceSell; }
    public void setPriceSell(Double priceSell) { this.priceSell = priceSell; }
    public Date getDateNew() { return dateNew; }
    public void setDateNew(Date dateNew) { this.dateNew = dateNew; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @Override
    public Object getKey() { return id; }

    public static SerializerRead getSerializerRead() {
        return new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
                ProductPriceHistory p = new ProductPriceHistory();
                p.id = dr.getString(1);
                p.productId = dr.getString(2);
                p.priceBuy = dr.getDouble(3);
                p.priceSell = dr.getDouble(4);
                p.dateNew = dr.getTimestamp(5);
                p.userId = dr.getString(6);
                return p;
            }
        };
    }

    public static SerializerWrite getSerializerWrite() {
        return new SerializerWrite() {
            @Override
            public void writeValues(DataWrite dw, Object obj) throws BasicException {
                ProductPriceHistory p = (ProductPriceHistory) obj;
                dw.setString(1, p.id);
                dw.setString(2, p.productId);
                dw.setDouble(3, p.priceBuy);
                dw.setDouble(4, p.priceSell);
                dw.setTimestamp(5, p.dateNew);
                dw.setString(6, p.userId);
            }
        };
    }
}
