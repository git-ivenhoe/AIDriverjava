package edu.cs4730.wearappvoice.ai.OrderData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    public String waybillId;
    public String waybillStatus;
    public String waybillDate;
    public long cargoId;
    public String cargoName;
    public String cargoUnit;
    public double cargoFreightUnitPrice;
    public int driverId;
    public String driverName;
    public String driverPhone;
    public String licensePlate;
    public String licensePlatePrefix;
    public String loadingAddress;
    public String unloadingAddress;
    public int loadingContactId;
    public String loadingContactPerson;
    public String loadingContactPhone;
    public int unloadingContactId;
    public String unloadingContactPerson;
    public String unloadingContactPhone;
    public String loadingArrivalTime;
    public String loadingSigningTime;
    public int loadingQuantity;
    public Integer loadingWait;
    public long loadingImgId;
    public String unloadingArrivalTime;
    public String unloadingSigningTime;
    public int unloadingQuantity;
    public Integer unloadingWait;
    public long unloadingImgId;
    public int settlementQuantity;
    public double driverFreight;
    public double totalFreight;
    public double collectOnDeliveryAmount;
    public String settlementStatus;
    public String withdrawalStatus;
    public String paymentTime;
    public String withdrawalTime;
    public String updateTime;
    public double mileage;
    public String responseHandler;

    public String getWaybillId() {
        return waybillId;
    }

    public void setWaybillId(String waybillId) {
        this.waybillId = waybillId;
    }

    public String getWaybillStatus() {
        return waybillStatus;
    }

    public void setWaybillStatus(String waybillStatus) {
        this.waybillStatus = waybillStatus;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public long getCargoId() {
        return cargoId;
    }

    public void setCargoId(long cargoId) {
        this.cargoId = cargoId;
    }

    public String getCargoName() {
        return cargoName;
    }

    public void setCargoName(String cargoName) {
        this.cargoName = cargoName;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getLicensePlatePrefix() {
        return licensePlatePrefix;
    }

    public void setLicensePlatePrefix(String licensePlatePrefix) {
        this.licensePlatePrefix = licensePlatePrefix;
    }

    public String getLoadingAddress() {
        return loadingAddress;
    }

    public void setLoadingAddress(String loadingAddress) {
        this.loadingAddress = loadingAddress;
    }

    public String getUnloadingAddress() {
        return unloadingAddress;
    }

    public void setUnloadingAddress(String unloadingAddress) {
        this.unloadingAddress = unloadingAddress;
    }

    public double getTotalFreight() {
        return totalFreight;
    }

    public void setTotalFreight(double totalFreight) {
        this.totalFreight = totalFreight;
    }

    public String getCargoUnit() {
        return cargoUnit;
    }

    public void setCargoUnit(String cargoUnit) {
        this.cargoUnit = cargoUnit;
    }
}
