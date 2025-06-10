package models;

public class Booking {
    private int id;
    private int customerId;
    private int roomTypeId;
    private String checkinDate;
    private String checkoutDate;
    private int price;
    private Integer voucherId;
    private int finalPrice;
    private String paymentStatus;
    private boolean hasCheckedIn;
    private boolean hasCheckedOut;


    public Booking(int id, int customerId, int roomTypeId, String checkinDate, String checkoutDate,
                   int price, Integer voucherId, int finalPrice, String paymentStatus,
                   int hasCheckedIn, int hasCheckedOut) {
        this.id = id;
        this.customerId = customerId;
        this.roomTypeId = roomTypeId;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
        this.price = price;
        this.voucherId = voucherId;
        this.finalPrice = finalPrice;
        this.paymentStatus = paymentStatus;
        this.hasCheckedIn = (hasCheckedIn == 1);
        this.hasCheckedOut = (hasCheckedOut == 1);
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(int roomTypeId) { this.roomTypeId = roomTypeId; }
    public String getCheckinDate() { return checkinDate; }
    public void setCheckinDate(String checkinDate) { this.checkinDate = checkinDate; }
    public String getCheckoutDate() { return checkoutDate; }
    public void setCheckoutDate(String checkoutDate) { this.checkoutDate = checkoutDate; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public Integer getVoucherId() { return voucherId; }
    public void setVoucherId(Integer voucherId) { this.voucherId = voucherId; }
    public int getFinalPrice() { return finalPrice; }
    public void setFinalPrice(int finalPrice) { this.finalPrice = finalPrice; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public boolean isHasCheckedIn() { return hasCheckedIn; }
    public void setHasCheckedIn(boolean hasCheckedIn) { this.hasCheckedIn = hasCheckedIn; }
    public boolean isHasCheckedOut() { return hasCheckedOut; }
    public void setHasCheckedOut(boolean hasCheckedOut) { this.hasCheckedOut = hasCheckedOut; }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", roomTypeId=" + roomTypeId +
                ", checkinDate='" + checkinDate + '\'' +
                ", checkoutDate='" + checkoutDate + '\'' +
                '}';
    }
}
