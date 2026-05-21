package com.openbravo.pos.repairs;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataLogicRepairs extends BeanFactoryDataSingle {

    private static final Logger LOGGER = Logger.getLogger(DataLogicRepairs.class.getName());
    private Session s;
    private static boolean tablesReady = false;

    public DataLogicRepairs() {
    }

    @Override
    public void init(Session s) {
        this.s = s;
        initTables();
    }

    private synchronized void initTables() {
        if (tablesReady) return;
        
        try {
            java.sql.Connection conn = s.getConnection();
            
            // 1. Create TECHNICIANS table
            if (!tableExists(conn, "REPAIR_TECHNICIANS")) {
                s.getConnection().createStatement().executeUpdate(
                    "CREATE TABLE REPAIR_TECHNICIANS (" +
                    "ID VARCHAR(36) NOT NULL PRIMARY KEY, " +
                    "NAME VARCHAR(255) NOT NULL, " +
                    "PHONE VARCHAR(50), " +
                    "EMAIL VARCHAR(100), " +
                    "NOTES VARCHAR(1000))"
                );
                LOGGER.info("Table REPAIR_TECHNICIANS created.");
            }

            // 2. Create REPAIRS table
            if (!tableExists(conn, "REPAIRS")) {
                s.getConnection().createStatement().executeUpdate(
                    "CREATE TABLE REPAIRS (" +
                    "ID VARCHAR(36) NOT NULL PRIMARY KEY, " +
                    "REPAIR_NUMBER VARCHAR(50) NOT NULL, " +
                    "CUSTOMER_ID VARCHAR(36), " +
                    "ENTRY_DATE TIMESTAMP NOT NULL, " +
                    "EXIT_DATE TIMESTAMP, " +
                    "OBSERVATIONS VARCHAR(2000), " +
                    "BICYCLE_DETAILS VARCHAR(1000), " +
                    "TECHNICIAN_ID VARCHAR(36), " +
                    "STATUS VARCHAR(20) DEFAULT 'PENDING', " +
                    "TOTAL_COST DECIMAL(10,2) DEFAULT 0.0, " +
                    "CONSTRAINT REPAIRS_T_FK FOREIGN KEY (TECHNICIAN_ID) REFERENCES REPAIR_TECHNICIANS(ID))"
                );
                LOGGER.info("Table REPAIRS created.");
            }
            
            tablesReady = true;
            
            // Insert default technician if empty
            if (getTechnicians().isEmpty()) {
                TechnicianInfo defaultTech = new TechnicianInfo();
                defaultTech.setName("Taller Central (Aliado)");
                defaultTech.setPhone("000-000-0000");
                defaultTech.setNotes("Técnico por defecto");
                saveTechnician(defaultTech);
                LOGGER.info("Default technician created.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing repair tables", e);
        }
    }

    private boolean tableExists(java.sql.Connection conn, String tableName) {
        try {
            java.sql.ResultSet rs = conn.getMetaData().getTables(null, null, tableName.toUpperCase(), null);
            if (rs.next()) return true;
            rs = conn.getMetaData().getTables(null, null, tableName.toLowerCase(), null);
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    public List<TechnicianInfo> getTechnicians() throws BasicException {
        return new StaticSentence(s, "SELECT ID, NAME, PHONE, EMAIL, NOTES FROM REPAIR_TECHNICIANS ORDER BY NAME", null, new SerializerReadClass(TechnicianInfo.class)).list();
    }

    public void saveTechnician(TechnicianInfo technician) throws BasicException {
        if (technician.getId() == null) {
            technician.setId(UUID.randomUUID().toString());
            new StaticSentence(s, "INSERT INTO REPAIR_TECHNICIANS (ID, NAME, PHONE, EMAIL, NOTES) VALUES (?, ?, ?, ?, ?)", SerializerWriteBuilder.INSTANCE).exec(technician);
        } else {
            new StaticSentence(s, "UPDATE REPAIR_TECHNICIANS SET NAME = ?, PHONE = ?, EMAIL = ?, NOTES = ? WHERE ID = ?", 
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING}))
                .exec(new Object[]{technician.getName(), technician.getPhone(), technician.getEmail(), technician.getNotes(), technician.getId()});
        }
    }

    public List<RepairInfo> getRepairs() throws BasicException {
        return new StaticSentence(s, 
            "SELECT R.ID, R.REPAIR_NUMBER, R.CUSTOMER_ID, C.NAME, R.ENTRY_DATE, R.EXIT_DATE, R.OBSERVATIONS, R.BICYCLE_DETAILS, R.TECHNICIAN_ID, T.NAME, R.STATUS, R.TOTAL_COST " +
            "FROM REPAIRS R " +
            "LEFT JOIN CUSTOMERS C ON R.CUSTOMER_ID = C.ID " +
            "LEFT JOIN REPAIR_TECHNICIANS T ON R.TECHNICIAN_ID = T.ID " +
            "ORDER BY R.ENTRY_DATE DESC", 
            null, new SerializerReadClass(RepairInfo.class)).list();
    }

    public void saveRepair(RepairInfo repair) throws BasicException {
        if (repair.getId() == null) {
            repair.setId(UUID.randomUUID().toString());
            repair.setEntryDate(new java.util.Date());
            if (repair.getRepairNumber() == null) {
                repair.setRepairNumber("REP-" + (System.currentTimeMillis() % 1000000));
            }
            new StaticSentence(s, "INSERT INTO REPAIRS (ID, REPAIR_NUMBER, CUSTOMER_ID, ENTRY_DATE, EXIT_DATE, OBSERVATIONS, BICYCLE_DETAILS, TECHNICIAN_ID, STATUS, TOTAL_COST) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 
                SerializerWriteBuilder.INSTANCE).exec(repair);
        } else {
            new StaticSentence(s, "UPDATE REPAIRS SET CUSTOMER_ID = ?, EXIT_DATE = ?, OBSERVATIONS = ?, BICYCLE_DETAILS = ?, TECHNICIAN_ID = ?, STATUS = ?, TOTAL_COST = ? WHERE ID = ?", 
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.TIMESTAMP, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.STRING}))
                .exec(new Object[]{repair.getCustomerId(), repair.getExitDate(), repair.getObservations(), repair.getBicycleDetails(), repair.getTechnicianId(), repair.getStatus(), repair.getTotalCost(), repair.getId()});
        }
    }
    
    public void deleteRepair(String id) throws BasicException {
        new StaticSentence(s, "DELETE FROM REPAIRS WHERE ID = ?", SerializerWriteString.INSTANCE).exec(id);
    }
}
