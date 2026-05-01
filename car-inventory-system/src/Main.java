import models.Car;
import services.InventoryManager;
import java.util.Scanner;
import java.util.Optional;

public class Main {
    private static InventoryManager inventoryManager = new InventoryManager();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        System.out.println("=========================================");
        System.out.println("   Welcome to the Car Inventory System   ");
        System.out.println("=========================================");

        while (running) {
            printMenu();
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addCar();
                    break;
                case "2":
                    removeCar();
                    break;
                case "3":
                    viewAllCars();
                    break;
                case "4":
                    searchCar();
                    break;
                case "5":
                    running = false;
                    System.out.println("Exiting the system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Add a New Car");
        System.out.println("2. Remove a Car");
        System.out.println("3. View All Cars");
        System.out.println("4. Search Car by ID");
        System.out.println("5. Exit");
    }

    private static void addCar() {
        System.out.print("Enter Car ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Make: ");
        String make = scanner.nextLine();
        System.out.print("Enter Model: ");
        String model = scanner.nextLine();
        
        int year = 0;
        while (true) {
            System.out.print("Enter Year: ");
            try {
                year = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid year.");
            }
        }

        double price = 0;
        while (true) {
            System.out.print("Enter Price: ");
            try {
                price = Double.parseDouble(scanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid price.");
            }
        }

        Car newCar = new Car(id, make, model, year, price);
        inventoryManager.addCar(newCar);
        System.out.println("Car added successfully!");
    }

    private static void removeCar() {
        System.out.print("Enter Car ID to remove: ");
        String id = scanner.nextLine();
        boolean removed = inventoryManager.removeCar(id);
        if (removed) {
            System.out.println("Car removed successfully.");
        } else {
            System.out.println("Car not found.");
        }
    }

    private static void viewAllCars() {
        System.out.println("\n--- Current Inventory ---");
        inventoryManager.displayInventory();
    }

    private static void searchCar() {
        System.out.print("Enter Car ID to search: ");
        String id = scanner.nextLine();
        Optional<Car> car = inventoryManager.findCarById(id);
        
        if (car.isPresent()) {
            System.out.println("Car found: " + car.get());
        } else {
            System.out.println("Car not found.");
        }
    }
}
