package models;

public class Villa {
    private int id;
    private String name;
    private String description;
    private String address;

    //---------------Start Constructor untuk data dari DB (dengan ID)----------------
    public Villa(int id, String name, String description, String address) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
    }
    //---------------End Constructor untuk data dari DB (dengan ID)----------------

    //---------------Start Getters dan Setters----------------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    //---------------End Getters dan Setters----------------------


    @Override
    public String toString() {
        return "Villa{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}