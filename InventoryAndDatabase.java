import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

class InventoryItem {
    String name;
    int quantity;
    double price;

    public InventoryItem(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }
}

public class InventoryAndDatabase {
    private static final int MAX_INVENTORY_SIZE = 100;
    private InventoryItem[] inventory;
    private int itemCount;
    private Scanner scanner;
    private Connection connection;

    public InventoryAndDatabase() {
        this.inventory = new InventoryItem[MAX_INVENTORY_SIZE];
        this.itemCount = 0;
        this.scanner = new Scanner(System.in);
        this.connection = null;
    }

    public void addItem(String name, int quantity, double price) {
        for (int i = 0; i < itemCount; i++) {
            if (inventory[i].name.equals(name)) {
                inventory[i].quantity += quantity;
                return;
            }
        }

        if (itemCount < MAX_INVENTORY_SIZE) {
            inventory[itemCount++] = new InventoryItem(name, quantity, price);
            checkReorder(name);
        } else {
            System.out.println("Inventory is full. Cannot add more items.");
        }
    }

    private void checkReorder(String itemName) {
        for (int i = 0; i < itemCount; i++) {
            if (inventory[i].name.equals(itemName) && inventory[i].quantity <= 10) {
                generateReorderReport(itemName, 10 - inventory[i].quantity);
                return;
            }
        }
    }

    private void generateReorderReport(String itemName, int quantityToOrder) {
        System.out.println("Itemised Inventory Reorder Level Report: Order " + quantityToOrder + " units of" + itemName);
    }

    public void displayInventory() {
        System.out.println("Itemised Inventory Report:");
        for (int i = 0; i < itemCount; i++) {
            InventoryItem item = inventory[i];
            System.out.println("Name:" + item.name + " Quantity:" + item.quantity + ", Price: " + item.price);
        }

        displayReorderReports();
    }

    private void displayReorderReports() {
        System.out.println("\nItemised Inventory Reorder Reports:");
        for (int i = 0; i < itemCount; i++) {
            if (inventory[i].quantity <= 10) {
                System.out.println("Name:" + inventory[i].name + ", Quantity to Reorder: " + (10 - inventory[i].quantity));
            }
        }
    }

    public void orderItems(String itemName, int quantity) {
        for (int i = 0; i < itemCount; i++) {
            if (inventory[i].name.equals(itemName) && inventory[i].quantity >= quantity) {
                inventory[i].quantity = quantity;
                System.out.println("Ordered " + quantity + " units of" + itemName);
                return;
            }
        }
        System.out.println("Item" + itemName + " not available in sufficient quantity.");
    }

    public void displayBillingReport() {
        System.out.println("\nBilling Report on Ordered Items: ");
        for (int i = 0; i < itemCount; i++) {
            double totalPrice = inventory[i].quantity * inventory[i].price;
            System.out.println("Name:" + inventory[i].name + ", Quantity: " + inventory[i].quantity + "Total Price: $" + totalPrice);
        }
    }

    public void connectToDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                System.out.println("Already connected to the database.");
                return;
            }
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Establish a connection with Windows authentication
            String url = "jdbc:sqlserver://localhost:1433;databaseName=shopDatabase;integratedSecurity=true";
            connection = DriverManager.getConnection(url);

            System.out.println("Connected to the database.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public void displayRecords() {
            try {
                if (connection == null || connection.isClosed()) {
                    System.out.println("Not connected to the database. Connect first.");
                    return;
                }

                // Load the JDBC driver
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    
                // Establish a connection with Windows authentication
                String url = "jdbc:sqlserver://localhost:1433;databaseName=shopDatabase;integratedSecurity=true";
                Connection connection = DriverManager.getConnection(url);
    
                // Create a statement
                Statement statement = connection.createStatement();
    
                // Execute a query to select all records from ShopList
                String selectQuery = "SELECT * FROM ShopList";
                ResultSet resultSet = statement.executeQuery(selectQuery);
    
                // Process the results
                System.out.println("Records from ShopList:");
                System.out.printf("%-15s %-10s %-10s%n", "Item Name", "Quantity", "Price");
    
                while (resultSet.next()) {
                    String itemName = resultSet.getString("item_name");
                    int itemQuantity = resultSet.getInt("quantity");
                    double itemPrice = resultSet.getDouble("price");
    
                    System.out.printf("%-15s %-10s %-10s%n", itemName, itemQuantity, itemPrice);
                }
    
                // Close resources
                resultSet.close();
                statement.close();
                connection.close();
    
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    

    

    public static void main(String[] args) {
        InventoryAndDatabase system = new InventoryAndDatabase();

        while (true) {
            System.out.println("\n1. Add Item\n2. Display Inventory\n3. Order Items\n4. Display Billing Report\n5. Connect to Database\n6. Display DataBase\n7. Exit");
            System.out.print("Enter your choice: ");
            int choice = system.scanner.nextInt();

            try{
            switch (choice) {
                case 1:
                    System.out.print("Enter item name: ");
                    String itemName = system.scanner.next();
                    System.out.print("Enter quantity: ");
                    int itemQuantity = system.scanner.nextInt();
                    System.out.print("Enter price: ");
                    double itemPrice = system.scanner.nextDouble();
                    system.addItem(itemName, itemQuantity, itemPrice);
                    System.out.println("Item added successfully!");
                    break;

                case 2:
                    system.displayInventory();
                    break;

                case 3:
                    System.out.print("Enter item name to order: ");
                    String orderItemName = system.scanner.next();
                    System.out.print("Enter quantity to order: ");
                    int orderQuantity = system.scanner.nextInt();
                    system.orderItems(orderItemName, orderQuantity);
                    break;

                case 4:
                    system.displayBillingReport();
                    break;

                case 5:
                    system.connectToDatabase();
                    break;

                case 6:
                    system.displayRecords();
                    break;
                
                case 7:
                    System.out.println("Exiting...");
                    System.exit(0);


                default:
                    System.out.println("Invalid choice. Please try again.");
            }}catch (Exception e) {
                // Handle input-related exceptions
                System.out.println("Invalid input. Please try again.");
                system.scanner.nextLine(); // Consume the invalid input
            }
        }
    }
}
