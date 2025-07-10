package edu.cs4730.wearappvoice.view;

public class WayBillStatus {
//{\"waybill_status\":\"卸货中\",\"loading_wait\":-1,\"transit_wait\":-1,\"unloading_wait\":193}
    private String waybill_status;
    private int loading_wait;
    private int unloading_wait;

    public String getWaybill_status() {
        return waybill_status;
    }

    public void setWaybill_status(String waybill_status) {
        this.waybill_status = waybill_status;
    }

    public int getLoading_wait() {
        return loading_wait;
    }

    public void setLoading_wait(int loading_wait) {
        this.loading_wait = loading_wait;
    }

    public int getUnloading_wait() {
        return unloading_wait;
    }

    public void setUnloading_wait(int unloading_wait) {
        this.unloading_wait = unloading_wait;
    }
}
