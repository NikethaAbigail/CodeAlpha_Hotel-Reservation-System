package Code;

import java.io.*;
import java.util.*;

class Room {
    private int roomNumber;
    private String category; // Standard, Deluxe, Suite
    private double pricePerNight;
    private boolean isAvailable;

    public Room(int roomNumber, String category, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.isAvailable = true;
    }

    public int getRoomNumber() { return roomNumber; }
    public String getCategory() { return category; }
    public double getPricePerNight() { return pricePerNight; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }
}

class Reservation {
    private String guestName;
    private int roomNumber;
    private int nights;
    private double totalCost;
    private String status; // Confirmed, Cancelled

    public Reservation(String guestName, int roomNumber, int nights, double totalCost) {
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.nights = nights;
        this.totalCost = totalCost;
        this.status = "Confirmed";
    }

    public String getGuestName() { return guestName; }
    public int getRoomNumber() { return roomNumber; }
    public int getNights() { return nights; }
    public double getTotalCost() { return totalCost; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

class Hotel {
    private List<Room> rooms;
    private List<Reservation> reservations;
    private static final String ROOMS_FILE = "src/Code/rooms.txt";
    private static final String RESERVATIONS_FILE = "src/Code/reservations.txt";

    public Hotel() {
        rooms = new ArrayList<>();
        reservations = new ArrayList<>();
        initializeRooms();
        loadData();
    }

    private void initializeRooms() {
        rooms.add(new Room(101, "Standard", 100.0));
        rooms.add(new Room(102, "Standard", 100.0));
        rooms.add(new Room(201, "Deluxe", 200.0));
        rooms.add(new Room(202, "Deluxe", 200.0));
        rooms.add(new Room(301, "Suite", 350.0));
    }

    private void loadData() {
        try (Scanner scanner = new Scanner(new File(ROOMS_FILE))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                int roomNumber = Integer.parseInt(parts[0]);
                boolean isAvailable = Boolean.parseBoolean(parts[2]);
                for (Room room : rooms) {
                    if (room.getRoomNumber() == roomNumber) {
                        room.setAvailable(isAvailable);
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No room data found. Using defaults.");
        }

        try (Scanner scanner = new Scanner(new File(RESERVATIONS_FILE))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                reservations.add(new Reservation(parts[0], Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]), Double.parseDouble(parts[3])));
            }
        } catch (FileNotFoundException e) {
            System.out.println("No reservation data found.");
        }
    }

    private void saveData() {
        try (PrintWriter writer = new PrintWriter(ROOMS_FILE)) {
            for (Room room : rooms) {
                writer.println(room.getRoomNumber() + "," + room.getCategory() + "," + room.isAvailable());
            }
        } catch (IOException e) {
            System.out.println("Error saving room data: " + e.getMessage());
        }

        try (PrintWriter writer = new PrintWriter(RESERVATIONS_FILE)) {
            for (Reservation res : reservations) {
                if (res.getStatus().equals("Confirmed")) {
                    writer.println(res.getGuestName() + "," + res.getRoomNumber() + "," +
                        res.getNights() + "," + res.getTotalCost());
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving reservation data: " + e.getMessage());
        }
    }

    public void displayAvailableRooms() {
        System.out.println("\n=== Available Rooms ===");
        boolean found = false;
        for (Room room : rooms) {
            if (room.isAvailable()) {
                System.out.printf("Room %d (%s): $%.2f/night\n",
                    room.getRoomNumber(), room.getCategory(), room.getPricePerNight());
                found = true;
            }
        }
        if (!found) System.out.println("No rooms available.");
    }

    public boolean makeReservation(String guestName, int roomNumber, int nights) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber && room.isAvailable()) {
                double totalCost = room.getPricePerNight() * nights;
                // Simulate payment
                System.out.printf("Processing payment of $%.2f...\n", totalCost);
                System.out.println("Payment successful!");
                room.setAvailable(false);
                reservations.add(new Reservation(guestName, roomNumber, nights, totalCost));
                saveData();
                return true;
            }
        }
        return false;
    }

    public boolean cancelReservation(String guestName, int roomNumber) {
        for (Reservation res : reservations) {
            if (res.getGuestName().equalsIgnoreCase(guestName) &&
                res.getRoomNumber() == roomNumber && res.getStatus().equals("Confirmed")) {
                res.setStatus("Cancelled");
                for (Room room : rooms) {
                    if (room.getRoomNumber() == roomNumber) {
                        room.setAvailable(true);
                        break;
                    }
                }
                saveData();
                return true;
            }
        }
        return false;
    }

    public void viewBookingDetails(String guestName) {
        System.out.println("\n=== Booking Details ===");
        boolean found = false;
        for (Reservation res : reservations) {
            if (res.getGuestName().equalsIgnoreCase(guestName)) {
                String category = rooms.stream()
                    .filter(r -> r.getRoomNumber() == res.getRoomNumber())
                    .findFirst().map(Room::getCategory).orElse("Unknown");
                System.out.printf("Guest: %s, Room: %d (%s), Nights: %d, Total: $%.2f, Status: %s\n",
                    res.getGuestName(), res.getRoomNumber(), category, res.getNights(),
                    res.getTotalCost(), res.getStatus());
                found = true;
            }
        }
        if (!found) System.out.println("No bookings found for " + guestName);
    }
}

public class HotelReservationSystem {
    public static void main(String[] args) {
        Hotel hotel = new Hotel();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Hotel Reservation System ===");
            System.out.println("1. View Available Rooms");
            System.out.println("2. Make Reservation");
            System.out.println("3. Cancel Reservation");
            System.out.println("4. View Booking Details");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); 

            if (choice == 1) {
                hotel.displayAvailableRooms();
            } else if (choice == 2) {
                System.out.print("Enter guest name: ");
                String guestName = scanner.nextLine();
                System.out.print("Enter room number: ");
                int roomNumber = scanner.nextInt();
                System.out.print("Enter number of nights: ");
                int nights = scanner.nextInt();
                if (hotel.makeReservation(guestName, roomNumber, nights)) {
                    System.out.println("Reservation successful!");
                } else {
                    System.out.println("Room unavailable or invalid.");
                }
            } else if (choice == 3) {
                System.out.print("Enter guest name: ");
                String guestName = scanner.nextLine();
                System.out.print("Enter room number: ");
                int roomNumber = scanner.nextInt();
                if (hotel.cancelReservation(guestName, roomNumber)) {
                    System.out.println("Reservation cancelled!");
                } else {
                    System.out.println("Reservation not found.");
                }
            } else if (choice == 4) {
                System.out.print("Enter guest name: ");
                String guestName = scanner.nextLine();
                hotel.viewBookingDetails(guestName);
            } else if (choice == 5) {
                System.out.println("Exiting...");
                break;
            } else {
                System.out.println("Invalid option. Try again.");
            }
        }
        scanner.close();
    }
}

