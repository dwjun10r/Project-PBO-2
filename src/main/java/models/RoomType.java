package models;

public class RoomType {
    private int id;
    private int villaId;
    private String name;
    private int quantity;
    private int capacity;
    private int price;
    private String bedSize;
    private boolean hasDesk;
    private boolean hasAc;
    private boolean hasTv;
    private boolean hasWifi;
    private boolean hasShower;
    private boolean hasHotwater;
    private boolean hasFridge;


    public RoomType(int id, int villaId, String name, int quantity, int capacity, int price,
                    String bedSize, int hasDesk, int hasAc, int hasTv, int hasWifi,
                    int hasShower, int hasHotwater, int hasFridge) {
        this.id = id;
        this.villaId = villaId;
        this.name = name;
        this.quantity = quantity;
        this.capacity = capacity;
        this.price = price;
        this.bedSize = bedSize;
        this.hasDesk = (hasDesk == 1);
        this.hasAc = (hasAc == 1);
        this.hasTv = (hasTv == 1);
        this.hasWifi = (hasWifi == 1);
        this.hasShower = (hasShower == 1);
        this.hasHotwater = (hasHotwater == 1);
        this.hasFridge = (hasFridge == 1);
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVillaId() { return villaId; }
    public void setVillaId(int villaId) { this.villaId = villaId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getBedSize() { return bedSize; }
    public void setBedSize(String bedSize) { this.bedSize = bedSize; }
    public boolean isHasDesk() { return hasDesk; }
    public void setHasDesk(boolean hasDesk) { this.hasDesk = hasDesk; }
    public boolean isHasAc() { return hasAc; }
    public void setHasAc(boolean hasAc) { this.hasAc = hasAc; }
    public boolean isHasTv() { return hasTv; }
    public void setHasTv(boolean hasTv) { this.hasTv = hasTv; }
    public boolean isHasWifi() { return hasWifi; }
    public void setHasWifi(boolean hasWifi) { this.hasWifi = hasWifi; }
    public boolean isHasShower() { return hasShower; }
    public void setHasShower(boolean hasShower) { this.hasShower = hasShower; }
    public boolean isHasHotwater() { return hasHotwater; }
    public void setHasHotwater(boolean hasHotwater) { this.hasHotwater = hasHotwater; }
    public boolean isHasFridge() { return hasFridge; }
    public void setHasFridge(boolean hasFridge) { this.hasFridge = hasFridge; }


    @Override
    public String toString() {
        return "RoomType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
