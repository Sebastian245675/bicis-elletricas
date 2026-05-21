package com.openbravo.pos.repairs;

import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializableWrite;
import com.openbravo.basic.BasicException;

public class TechnicianInfo implements SerializableRead, SerializableWrite {

    private String id;
    private String name;
    private String phone;
    private String email;
    private String notes;

    public TechnicianInfo() {
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getString(1);
        name = dr.getString(2);
        phone = dr.getString(3);
        email = dr.getString(4);
        notes = dr.getString(5);
    }

    @Override
    public void writeValues(DataWrite dw) throws BasicException {
        dw.setString(1, id);
        dw.setString(2, name);
        dw.setString(3, phone);
        dw.setString(4, email);
        dw.setString(5, notes);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    @Override
    public String toString() {
        return name;
    }
}
