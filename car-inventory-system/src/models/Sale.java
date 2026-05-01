package models;

public class Sale {
    public String id;
    public String carId;
    public String customerId;
    public String employeeId;
    public String date;
    public String amount;

    public Sale(String id, String carId, String customerId, String employeeId, String date, String amount) {
        this.id = id;
        this.carId = carId;
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.date = date;
        this.amount = amount;
    }

    public String toCsv() {
        return id + "," + carId + "," + customerId + "," + employeeId + "," + date + "," + amount;
    }

    public String toJson() {
        return "{\"id\":\"" + id + "\",\"carId\":\"" + carId + "\",\"customerId\":\"" + customerId + "\",\"employeeId\":\"" + employeeId + "\",\"date\":\"" + date + "\",\"amount\":\"" + amount + "\"}";
    }

    public static Sale fromCsv(String csv) {
        String[] parts = csv.split(",", -1);
        if (parts.length >= 6) {
            return new Sale(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        }
        return null;
    }
}
