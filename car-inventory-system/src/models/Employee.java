package models;

public class Employee {
    public String id;
    public String name;
    public String role;
    public String imageUrl;

    public Employee(String id, String name, String role, String imageUrl) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }

    public String toCsv() {
        return id + "," + name + "," + role + "," + imageUrl;
    }

    public String toJson() {
        return "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"role\":\"" + role + "\",\"imageUrl\":\"" + imageUrl + "\"}";
    }

    public static Employee fromCsv(String csv) {
        String[] parts = csv.split(",", -1);
        if (parts.length >= 3) {
            String img = parts.length > 3 ? parts[3] : "";
            return new Employee(parts[0], parts[1], parts[2], img);
        }
        return null;
    }
}
