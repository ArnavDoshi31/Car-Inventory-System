package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.Car;
import models.Customer;
import models.Employee;
import models.Sale;
import storage.CsvRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class ApiHandler implements HttpHandler {

    private static final String IMAGES_DIR = "public/images";

    static {
        File dir = new File(IMAGES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        exchange.getResponseHeaders().set("Content-Type", "application/json");

        try {
            if (path.startsWith("/api/cars")) {
                handleCars(exchange, method, path);
            } else if (path.startsWith("/api/customers")) {
                handleCustomers(exchange, method, path);
            } else if (path.startsWith("/api/employees")) {
                handleEmployees(exchange, method, path);
            } else if (path.startsWith("/api/sales")) {
                handleSales(exchange, method, path);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
        }
    }
    
    private String extractIdFromPath(String path, String prefix) {
        if (path.length() > prefix.length() + 1) {
            return path.substring(prefix.length() + 1);
        }
        return null;
    }

    private String saveImage(String base64Image, String prefix, String id) {
        if (base64Image == null || base64Image.isEmpty()) return "";
        try {
            String[] parts = base64Image.split(",");
            String imageString = parts.length > 1 ? parts[1] : parts[0];
            byte[] imageBytes = Base64.getDecoder().decode(imageString);
            String extension = ".jpg";
            if (parts.length > 1 && parts[0].contains("png")) extension = ".png";
            
            String filename = prefix + "_" + id + extension;
            File file = new File(IMAGES_DIR + File.separator + filename);
            Files.write(file.toPath(), imageBytes);
            return "/images/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void handleCars(HttpExchange exchange, String method, String path) throws IOException {
        String targetId = extractIdFromPath(path, "/api/cars");
        
        if ("GET".equals(method)) {
            List<String> lines = CsvRepository.readLines("cars.csv");
            List<String> jsonItems = new ArrayList<>();
            for (String line : lines) {
                Car car = Car.fromCsv(line);
                if (car != null) jsonItems.add(car.toJson());
            }
            sendResponse(exchange, 200, "[" + String.join(",", jsonItems) + "]");
        } else if ("POST".equals(method) || "PUT".equals(method)) {
            String body = readBody(exchange);
            String id = extractJsonString(body, "id");
            String make = extractJsonString(body, "make");
            String model = extractJsonString(body, "model");
            String year = extractJsonString(body, "year");
            String price = extractJsonString(body, "price");
            String quantity = extractJsonString(body, "quantity");
            String existingImageUrl = extractJsonString(body, "imageUrl");
            String base64Image = extractJsonString(body, "base64Image");
            
            String imageUrl = existingImageUrl;
            if (base64Image != null && !base64Image.isEmpty()) {
                imageUrl = saveImage(base64Image, "car", id);
            }
            
            Car car = new Car(id, make, model, year, price, quantity, imageUrl);
            
            if ("PUT".equals(method)) {
                CsvRepository.updateLine("cars.csv", targetId, car.toCsv());
                sendResponse(exchange, 200, car.toJson());
            } else {
                List<String> lines = CsvRepository.readLines("cars.csv");
                lines.add(car.toCsv());
                CsvRepository.saveLines("cars.csv", lines);
                sendResponse(exchange, 201, car.toJson());
            }
        } else if ("DELETE".equals(method) && targetId != null) {
            CsvRepository.deleteLine("cars.csv", targetId);
            sendResponse(exchange, 200, "{\"success\": true}");
        }
    }

    private void handleCustomers(HttpExchange exchange, String method, String path) throws IOException {
        String targetId = extractIdFromPath(path, "/api/customers");
        
        if ("GET".equals(method)) {
            List<String> lines = CsvRepository.readLines("customers.csv");
            List<String> jsonItems = new ArrayList<>();
            for (String line : lines) {
                Customer c = Customer.fromCsv(line);
                if (c != null) jsonItems.add(c.toJson());
            }
            sendResponse(exchange, 200, "[" + String.join(",", jsonItems) + "]");
        } else if ("POST".equals(method) || "PUT".equals(method)) {
            String body = readBody(exchange);
            String id = extractJsonString(body, "id");
            String name = extractJsonString(body, "name");
            String contact = extractJsonString(body, "contact");
            String address = extractJsonString(body, "address");
            
            Customer c = new Customer(id, name, contact, address);
            
            if ("PUT".equals(method)) {
                CsvRepository.updateLine("customers.csv", targetId, c.toCsv());
                sendResponse(exchange, 200, c.toJson());
            } else {
                List<String> lines = CsvRepository.readLines("customers.csv");
                lines.add(c.toCsv());
                CsvRepository.saveLines("customers.csv", lines);
                sendResponse(exchange, 201, c.toJson());
            }
        } else if ("DELETE".equals(method) && targetId != null) {
            CsvRepository.deleteLine("customers.csv", targetId);
            sendResponse(exchange, 200, "{\"success\": true}");
        }
    }

    private void handleEmployees(HttpExchange exchange, String method, String path) throws IOException {
        String targetId = extractIdFromPath(path, "/api/employees");
        
        if ("GET".equals(method)) {
            List<String> lines = CsvRepository.readLines("employees.csv");
            List<String> jsonItems = new ArrayList<>();
            for (String line : lines) {
                Employee e = Employee.fromCsv(line);
                if (e != null) jsonItems.add(e.toJson());
            }
            sendResponse(exchange, 200, "[" + String.join(",", jsonItems) + "]");
        } else if ("POST".equals(method) || "PUT".equals(method)) {
            String body = readBody(exchange);
            String id = extractJsonString(body, "id");
            String name = extractJsonString(body, "name");
            String role = extractJsonString(body, "role");
            String existingImageUrl = extractJsonString(body, "imageUrl");
            String base64Image = extractJsonString(body, "base64Image");
            
            String imageUrl = existingImageUrl;
            if (base64Image != null && !base64Image.isEmpty()) {
                imageUrl = saveImage(base64Image, "emp", id);
            }
            
            Employee e = new Employee(id, name, role, imageUrl);
            
            if ("PUT".equals(method)) {
                CsvRepository.updateLine("employees.csv", targetId, e.toCsv());
                sendResponse(exchange, 200, e.toJson());
            } else {
                List<String> lines = CsvRepository.readLines("employees.csv");
                lines.add(e.toCsv());
                CsvRepository.saveLines("employees.csv", lines);
                sendResponse(exchange, 201, e.toJson());
            }
        } else if ("DELETE".equals(method) && targetId != null) {
            CsvRepository.deleteLine("employees.csv", targetId);
            sendResponse(exchange, 200, "{\"success\": true}");
        }
    }

    private void handleSales(HttpExchange exchange, String method, String path) throws IOException {
        String targetId = extractIdFromPath(path, "/api/sales");
        
        if ("GET".equals(method)) {
            List<String> lines = CsvRepository.readLines("sales.csv");
            List<String> jsonItems = new ArrayList<>();
            for (String line : lines) {
                Sale s = Sale.fromCsv(line);
                if (s != null) jsonItems.add(s.toJson());
            }
            sendResponse(exchange, 200, "[" + String.join(",", jsonItems) + "]");
        } else if ("POST".equals(method) || "PUT".equals(method)) {
            String body = readBody(exchange);
            String id = extractJsonString(body, "id");
            String carId = extractJsonString(body, "carId");
            String customerId = extractJsonString(body, "customerId");
            String employeeId = extractJsonString(body, "employeeId");
            String date = extractJsonString(body, "date");
            String amount = extractJsonString(body, "amount");
            
            Sale s = new Sale(id, carId, customerId, employeeId, date, amount);
            
            if ("PUT".equals(method)) {
                CsvRepository.updateLine("sales.csv", targetId, s.toCsv());
                sendResponse(exchange, 200, s.toJson());
            } else {
                List<String> carLines = CsvRepository.readLines("cars.csv");
                boolean carFound = false;
                for (int i = 0; i < carLines.size(); i++) {
                    Car c = Car.fromCsv(carLines.get(i));
                    if (c != null && c.id.equals(carId)) {
                        carFound = true;
                        int currentQty = 0;
                        try { currentQty = Integer.parseInt(c.quantity); } catch (Exception ex) {}
                        if (currentQty <= 0) {
                            sendResponse(exchange, 400, "{\"error\": \"Car is sold out\"}");
                            return;
                        }
                        c.quantity = String.valueOf(currentQty - 1);
                        carLines.set(i, c.toCsv());
                        break;
                    }
                }
                if (!carFound) {
                    sendResponse(exchange, 400, "{\"error\": \"Car not found\"}");
                    return;
                }
                CsvRepository.saveLines("cars.csv", carLines);

                List<String> lines = CsvRepository.readLines("sales.csv");
                lines.add(s.toCsv());
                CsvRepository.saveLines("sales.csv", lines);
                sendResponse(exchange, 201, s.toJson());
            }
        } else if ("DELETE".equals(method) && targetId != null) {
            List<String> salesLines = CsvRepository.readLines("sales.csv");
            String carIdToRefund = null;
            for (String line : salesLines) {
                Sale s = Sale.fromCsv(line);
                if (s != null && s.id.equals(targetId)) {
                    carIdToRefund = s.carId;
                    break;
                }
            }
            if (carIdToRefund != null) {
                List<String> carLines = CsvRepository.readLines("cars.csv");
                for (int i = 0; i < carLines.size(); i++) {
                    Car c = Car.fromCsv(carLines.get(i));
                    if (c != null && c.id.equals(carIdToRefund)) {
                        int currentQty = 0;
                        try { currentQty = Integer.parseInt(c.quantity); } catch (Exception ex) {}
                        c.quantity = String.valueOf(currentQty + 1);
                        carLines.set(i, c.toCsv());
                        break;
                    }
                }
                CsvRepository.saveLines("cars.csv", carLines);
            }

            CsvRepository.deleteLine("sales.csv", targetId);
            sendResponse(exchange, 200, "{\"success\": true}");
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            search = "\"" + key + "\":";
            start = json.indexOf(search);
            if (start == -1) return "";
            start += search.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end == -1) return "";
            return json.substring(start, end).replace("\"", "").trim();
        }
        start += search.length();
        int end = json.indexOf("\"", start);
        while(end != -1 && json.charAt(end-1) == '\\') {
            end = json.indexOf("\"", end + 1);
        }
        if (end == -1) return "";
        return json.substring(start, end);
    }
}
