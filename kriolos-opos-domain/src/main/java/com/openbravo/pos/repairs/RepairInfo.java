package com.openbravo.pos.repairs;

import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializableWrite;
import com.openbravo.basic.BasicException;
import java.util.Date;

public class RepairInfo implements SerializableRead, SerializableWrite {

    private String id;
    private String repairNumber;
    private String customerId;
    private String customerName;
    private Date entryDate;
    private Date exitDate;
    private String observations;
    private String bicycleDetails;
    private String technicianId;
    private String technicianName;
    private String status; // PENDING, IN_PROGRESS, CLOSED, CANCELLED
    private Double totalCost;

    public RepairInfo() {
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getString(1);
        repairNumber = dr.getString(2);
        customerId = dr.getString(3);
        customerName = dr.getString(4);
        entryDate = dr.getTimestamp(5);
        exitDate = dr.getTimestamp(6);
        observations = dr.getString(7);
        bicycleDetails = dr.getString(8);
        technicianId = dr.getString(9);
        technicianName = dr.getString(10);
        status = dr.getString(11);
        totalCost = dr.getDouble(12);
    }

    @Override
    public void writeValues(DataWrite dw) throws BasicException {
        dw.setString(1, id);
        dw.setString(2, repairNumber);
        dw.setString(3, customerId);
        dw.setTimestamp(4, entryDate);
        dw.setTimestamp(5, exitDate);
        dw.setString(6, observations);
        dw.setString(7, bicycleDetails);
        dw.setString(8, technicianId);
        dw.setString(9, status);
        dw.setDouble(10, totalCost);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRepairNumber() { return repairNumber; }
    public void setRepairNumber(String repairNumber) { this.repairNumber = repairNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Date getEntryDate() { return entryDate; }
    public void setEntryDate(Date entryDate) { this.entryDate = entryDate; }

    public Date getExitDate() { return exitDate; }
    public void setExitDate(Date exitDate) { this.exitDate = exitDate; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getBicycleDetails() { return bicycleDetails; }
    public void setBicycleDetails(String bicycleDetails) { this.bicycleDetails = bicycleDetails; }

    public String getTechnicianId() { return technicianId; }
    public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }

    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
}
