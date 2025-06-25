package models;

public class Voucher {
    private int id;
    private String code;
    private String description;
    private double discount;
    private String startDate;
    private String endDate;

    //---------------Start Constructor untuk data dari DB (dengan ID)----------------
    public Voucher(int id, String code, String description, double discount, String startDate, String endDate) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    //---------------End Constructor untuk data dari DB (dengan ID)----------------

    //----------------------Start Constructor untuk membuat Voucher baru (tanpa ID)--------------------
    public Voucher(String code, String description, double discount, String startDate, String endDate) {
        this.code = code;
        this.description = description;
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    //----------------------End Constructor untuk membuat Voucher baru (tanpa ID)--------------------

    //---------------Start Getters----------------------
    public int getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public double getDiscount() { return discount; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    //---------------End Getters----------------------

    //---------------Start Setters------------------
    public void setId(int id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setDescription(String description) { this.description = description; }
    public void setDiscount(double discount) { this.discount = discount; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    //---------------Start Setters------------------


    @Override
    public String toString() {
        return "Voucher{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", discount=" + discount +
                '}';
    }
}
