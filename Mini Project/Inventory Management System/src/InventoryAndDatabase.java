import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

    class ConnectToDatabase {
        private static final String URL = "jdbc:mysql://localhost:3306/Shop";
        private static final String USER = "root";
        private static final String PASSWORD = "123456789";

        public void connect() {
            try {
                if (connection != null && !connection.isClosed()) {
                    System.out.println("Already connected to the database.");
                    return;
                }

                // Load the JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish a connection
                connection = DriverManager.getConnection(URL, USER, PASSWORD);

                System.out.println("Connected to the MySQL database.");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static Connection getConnection(String databaseName) throws SQLException {
            return DriverManager.getConnection(URL + databaseName, USER, PASSWORD);
        }


    
        public static void createDatabaseAndTableIfNotExist(String databaseName) {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                // Create database
                String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS " + databaseName;
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(createDatabaseSQL);
                }
                connection.setCatalog(databaseName);
                String createTableSQL = "CREATE TABLE IF NOT EXISTS transactions (" +
                        "id INT Item Id PRIMARY KEY," +
                        "Item Name VARCHAR(255)," +
                        "Qunatity int," +
                        "Price DOUBLE," +
                        "balance_after_transaction DOUBLE)";
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(createTableSQL);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        public void insertItemIntoDatabase(String itemName, int itemQuantity, double itemPrice) {
            try {
                if (connection == null || connection.isClosed()) {
                    System.out.println("Not connected to the database. Connect first.");
                    return;
                }

                // Create a PreparedStatement for inserting data
                String insertQuery = "INSERT INTO ShopList (item_name, quantity, price) VALUES (?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    // Set values for the placeholders (?)
                    preparedStatement.setString(1, itemName);
                    preparedStatement.setInt(2, itemQuantity);
                    preparedStatement.setDouble(3, itemPrice);

                    // Execute the insert query
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Item added to the database successfully.");
                    } else {
                        System.out.println("Failed to add item to the database.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void displayRecords() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Not connected to the database. Connect first.");
                return;
            }

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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    

    public static void main(String[] args) {
        int no_of_item;
        InventoryAndDatabase system = new InventoryAndDatabase();

        System.out.print("Enter total No.of item: ");
        no_of_item = system.scanner.nextInt();
        if (no_of_item < 20) {
            ConnectToDatabase databaseConnector = system.new ConnectToDatabase();
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
                    // Add the item to the local inventory
                    system.addItem(itemName, itemQuantity, itemPrice);
                        
                    // Insert the item into the database
                    databaseConnector.insertItemIntoDatabase(itemName, itemQuantity, itemPrice);
                    
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
                databaseConnector.connect();

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
    else{
        System.out.println("LIMIT EXCEEDING !!!!");
    }
}

}
