package models;

public class Car {
    public String id;
    public String make;
    public String model;
    public String year;
    public String price;
    public String quantity;
    public String imageUrl;

    public Car(String id, String make, String model, String year, String price, String quantity, String imageUrl) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.year = year;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }

    public String toCsv() {
        return id + "," + make + "," + model + "," + year + "," + price + "," + quantity + "," + imageUrl;
    }

    public String toJson() {
        return "{\"id\":\"" + id + "\",\"make\":\"" + make + "\",\"model\":\"" + model + "\",\"year\":\"" + year + "\",\"price\":\"" + price + "\",\"quantity\":\"" + quantity + "\",\"imageUrl\":\"" + imageUrl + "\"}";
    }

    public static Car fromCsv(String csv) {
        String[] parts = csv.split(",", -1);
        if (parts.length >= 6) {
            String img = parts.length > 6 ? parts[6] : "";
            return new Car(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], img);
        }
        return null;
    }
}
