package services;

import models.Car;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryManager {
    private List<Car> inventory;

    public InventoryManager() {
        this.inventory = new ArrayList<>();
    }

    public void addCar(Car car) {
        inventory.add(car);
    }

    public boolean removeCar(String id) {
        return inventory.removeIf(car -> car.getId().equals(id));
    }

    public List<Car> getAllCars() {
        return new ArrayList<>(inventory);
    }

    public Optional<Car> findCarById(String id) {
        return inventory.stream()
                .filter(car -> car.getId().equals(id))
                .findFirst();
    }
    
    public void displayInventory() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is currently empty.");
            return;
        }
        for (Car car : inventory) {
            System.out.println(car);
        }
    }
}
