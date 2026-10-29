import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Smart Product Expiry & Inventory Management System
 * Student: Sreehari B | Register No: 720325244025
 *
 * Run:
 *   javac SmartProductExpiryInventorySystem.java
 *   java SmartProductExpiryInventorySystem
 */
public class SmartProductExpiryInventorySystem extends JFrame {
    private static final String DATA_FILE = "inventory_data.dat";
    private final List<Product> products = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Product ID", "Product Name", "Category", "Quantity",
                    "Price (₹)", "Expiry Date", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable productTable = new JTable(tableModel);
    private final JTextField searchField = new JTextField(18);
    private final JLabel totalLabel = new JLabel("Total Products: 0");
    private final JLabel expiredLabel = new JLabel("Expired: 0");
    private final JLabel soonLabel = new JLabel("Expiring Soon: 0");

    public SmartProductExpiryInventorySystem() {
        setTitle("Smart Product Expiry & Inventory Management System");
        setSize(1050, 620);
        setMinimumSize(new Dimension(850, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildInterface();
        loadProducts();
        refreshTable("");
    }

    private void buildInterface() {
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel("SMART PRODUCT EXPIRY & INVENTORY SYSTEM", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setBorder(BorderFactory.createEmptyBorder(16, 8, 12, 8));
        add(heading, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));

        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 8));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        expiredLabel.setFont(new Font("Arial", Font.BOLD, 14));
        soonLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryPanel.add(totalLabel);
        summaryPanel.add(expiredLabel);
        summaryPanel.add(soonLabel);
        centerPanel.add(summaryPanel, BorderLayout.NORTH);

        productTable.setRowHeight(25);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setAutoCreateRowSorter(true);
        centerPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        JButton addButton = new JButton("Add Product");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton refreshButton = new JButton("Refresh");
        JButton reportButton = new JButton("Expiry Report");

        addButton.addActionListener(e -> showProductDialog(null));
        updateButton.addActionListener(e -> updateSelectedProduct());
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        refreshButton.addActionListener(e -> refreshTable(searchField.getText().trim()));
        reportButton.addActionListener(e -> showExpiryReport());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(reportButton);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        searchButton.addActionListener(e -> refreshTable(searchField.getText().trim()));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable("");
        });
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        bottomPanel.add(searchPanel, BorderLayout.SOUTH);

        centerPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        JLabel footer = new JLabel("Student: Sreehari B  |  Register No: 720325244025", SwingConstants.CENTER);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        add(footer, BorderLayout.SOUTH);
    }

    private void showProductDialog(Product existing) {
        JTextField idField = new JTextField(existing == null ? "" : existing.id);
        JTextField nameField = new JTextField(existing == null ? "" : existing.name);
        JTextField categoryField = new JTextField(existing == null ? "" : existing.category);
        JTextField quantityField = new JTextField(existing == null ? "" : String.valueOf(existing.quantity));
        JTextField priceField = new JTextField(existing == null ? "" : String.valueOf(existing.price));
        JTextField expiryField = new JTextField(existing == null ? "" : existing.expiryDate.toString());

        if (existing != null) {
            idField.setEditable(false);
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Product ID:"));
        form.add(idField);
        form.add(new JLabel("Product Name:"));
        form.add(nameField);
        form.add(new JLabel("Category:"));
        form.add(categoryField);
        form.add(new JLabel("Quantity:"));
        form.add(quantityField);
        form.add(new JLabel("Price (₹):"));
        form.add(priceField);
        form.add(new JLabel("Expiry Date (YYYY-MM-DD):"));
        form.add(expiryField);

        int result = JOptionPane.showConfirmDialog(this, form,
                existing == null ? "Add Product" : "Update Product",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            int quantity = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            LocalDate expiryDate = LocalDate.parse(expiryField.getText().trim());

            if (id.isEmpty() || name.isEmpty() || category.isEmpty()) {
                throw new IllegalArgumentException("Product ID, name and category cannot be empty.");
            }
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative.");
            }
            if (!Double.isFinite(price) || price < 0) {
                throw new IllegalArgumentException("Price must be a valid non-negative number.");
            }

            if (existing == null) {
                if (findProductById(id) != null) {
                    throw new IllegalArgumentException("A product with this ID already exists.");
                }
                products.add(new Product(id, name, category, quantity, price, expiryDate));
            } else {
                existing.name = name;
                existing.category = category;
                existing.quantity = quantity;
                existing.price = price;
                existing.expiryDate = expiryDate;
            }

            saveProducts();
            refreshTable(searchField.getText().trim());
            JOptionPane.showMessageDialog(this, "Product saved successfully.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Quantity must be a whole number and price must be numeric.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter expiry date in YYYY-MM-DD format, for example 2027-05-30.",
                    "Invalid Date", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedProduct() {
        Product selected = getSelectedProduct();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.");
            return;
        }
        showProductDialog(selected);
    }

    private void deleteSelectedProduct() {
        Product selected = getSelectedProduct();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete product \"" + selected.name + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            products.remove(selected);
            saveProducts();
            refreshTable(searchField.getText().trim());
        }
    }

    private Product getSelectedProduct() {
        int viewRow = productTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = productTable.convertRowIndexToModel(viewRow);
        String id = String.valueOf(tableModel.getValueAt(modelRow, 0));
        return findProductById(id);
    }

    private Product findProductById(String id) {
        for (Product product : products) {
            if (product.id.equalsIgnoreCase(id)) {
                return product;
            }
        }
        return null;
    }

    private void refreshTable(String searchText) {
        tableModel.setRowCount(0);
        int expiredCount = 0;
        int soonCount = 0;
        LocalDate today = LocalDate.now();

        for (Product product : products) {
            String status = getStatus(product, today);
            if ("Expired".equals(status)) {
                expiredCount++;
            } else if ("Expiring Soon".equals(status)) {
                soonCount++;
            }

            String searchable = (product.id + " " + product.name + " " + product.category)
                    .toLowerCase();
            if (searchText.isEmpty() || searchable.contains(searchText.toLowerCase())) {
                tableModel.addRow(new Object[]{
                        product.id, product.name, product.category, product.quantity,
                        String.format("%.2f", product.price), product.expiryDate, status
                });
            }
        }

        totalLabel.setText("Total Products: " + products.size());
        expiredLabel.setText("Expired: " + expiredCount);
        soonLabel.setText("Expiring Soon: " + soonCount);
    }

    private String getStatus(Product product, LocalDate today) {
        if (product.expiryDate.isBefore(today)) {
            return "Expired";
        }
        long daysLeft = ChronoUnit.DAYS.between(today, product.expiryDate);
        if (daysLeft <= 30) {
            return "Expiring Soon";
        }
        return "Good";
    }

    private void showExpiryReport() {
        StringBuilder report = new StringBuilder();
        LocalDate today = LocalDate.now();
        report.append("EXPIRY REPORT\nDate: ").append(today).append("\n\n");

        boolean found = false;
        for (Product product : products) {
            String status = getStatus(product, today);
            if (!"Good".equals(status)) {
                found = true;
                report.append("Product: ").append(product.name)
                        .append(" (ID: ").append(product.id).append(")\n")
                        .append("Expiry Date: ").append(product.expiryDate).append("\n")
                        .append("Status: ").append(status).append("\n\n");
            }
        }

        if (!found) {
            report.append("No expired or soon-to-expire products found.");
        }

        JTextArea textArea = new JTextArea(report.toString(), 16, 45);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea),
                "Expiry Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveProducts() {
        try (ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream(DATA_FILE))) {
            output.writeObject(new ArrayList<>(products));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save inventory data: " + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadProducts() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            Object savedData = input.readObject();
            if (savedData instanceof ArrayList<?>) {
                products.clear();
                for (Object item : (ArrayList<?>) savedData) {
                    if (item instanceof Product) {
                        products.add((Product) item);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load saved inventory data. A new inventory will be used.\n"
                            + ex.getMessage(),
                    "File Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static class Product implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String id;
        private String name;
        private String category;
        private int quantity;
        private double price;
        private LocalDate expiryDate;

        Product(String id, String name, String category, int quantity,
                double price, LocalDate expiryDate) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.price = price;
            this.expiryDate = expiryDate;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use Java's default look and feel if the system look and feel is unavailable.
            }
            new SmartProductExpiryInventorySystem().setVisible(true);
        });
    }
}
