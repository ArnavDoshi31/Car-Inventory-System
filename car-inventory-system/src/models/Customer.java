package models;

public class Customer {
    public String id;
    public String name;
    public String contact;
    public String address;

    public Customer(String id, String name, String contact, String address) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.address = address;
    }

    public String toCsv() {
        return id + "," + name + "," + contact + "," + address;
    }

    public String toJson() {
        return "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"contact\":\"" + contact + "\",\"address\":\"" + address + "\"}";
    }

    public static Customer fromCsv(String csv) {
        String[] parts = csv.split(",", -1);
        if (parts.length >= 4) {
            return new Customer(parts[0], parts[1], parts[2], parts[3]);
        }
        return null;
    }
}
