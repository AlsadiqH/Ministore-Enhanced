package middle;

import catalogue.Product;

import java.util.*;

/**
 * Service to monitor stock levels and track products that need attention
 */
public class StockMonitorService {
    private final StockReadWriter stockReadWriter;
    private static final int LOW_STOCK_THRESHOLD = 5;

    // Map to store known products and their last known quantities
    private final Map<String, ProductInfo> knownProducts;
    // Map to store restock history
    private final List<RestockRecord> restockHistory;

    public StockMonitorService(StockReadWriter stockReadWriter) {
        this.stockReadWriter = stockReadWriter;
        this.knownProducts = new HashMap<>();
        this.restockHistory = new ArrayList<>();
    }

    /**
     * Record of a restock operation
     */
    private static class RestockRecord {
        final String productNum;
        final String description;
        final int quantity;
        final Date timestamp;

        RestockRecord(String productNum, String description, int quantity) {
            this.productNum = productNum;
            this.description = description;
            this.quantity = quantity;
            this.timestamp = new Date();
        }
    }

    /**
     * Product information including monitoring status
     */
    private static class ProductInfo {
        String description;
        int lastKnownQuantity;
        Date lastChecked;

        ProductInfo(String description, int quantity) {
            this.description = description;
            this.lastKnownQuantity = quantity;
            this.lastChecked = new Date();
        }
    }

    /**
     * Check a product's stock level and record its information
     * @param productNum Product number to check
     * @return String message indicating status
     */
    public String checkProduct(String productNum) {
        try {
            if (!stockReadWriter.exists(productNum)) {
                return "Product " + productNum + " does not exist.";
            }

            Product product = stockReadWriter.getDetails(productNum);
            ProductInfo info = new ProductInfo(product.getDescription(), product.getQuantity());
            knownProducts.put(productNum, info);

            if (product.getQuantity() == 0) {
                return String.format("ALERT: %s (%s) is OUT OF STOCK!",
                        product.getDescription(), productNum);
            } else if (product.getQuantity() < LOW_STOCK_THRESHOLD) {
                return String.format("WARNING: %s (%s) is LOW ON STOCK! Only %d remaining",
                        product.getDescription(), productNum, product.getQuantity());
            }

            return String.format("%s (%s) stock level: %d",
                    product.getDescription(), productNum, product.getQuantity());

        } catch (StockException e) {
            return "Error checking product " + productNum + ": " + e.getMessage();
        }
    }

    /**
     * Record a restock operation
     * @param productNum Product number
     * @param quantity Amount restocked
     */
    public void recordRestock(String productNum, int quantity) {
        try {
            if (stockReadWriter.exists(productNum)) {
                Product product = stockReadWriter.getDetails(productNum);
                restockHistory.add(new RestockRecord(productNum, product.getDescription(), quantity));

                // Update known products
                ProductInfo info = new ProductInfo(product.getDescription(), product.getQuantity());
                knownProducts.put(productNum, info);
            }
        } catch (StockException e) {
            // Log error if needed
        }
    }

    /**
     * Generate a complete stock report
     * @return Formatted report string
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Stock Status Report ===\n\n");

        // Critical Items Section
        report.append("ITEMS NEEDING ATTENTION:\n");
        report.append("----------------------\n");
        boolean hasCriticalItems = false;

        for (Map.Entry<String, ProductInfo> entry : knownProducts.entrySet()) {
            String productNum = entry.getKey();
            try {
                if (stockReadWriter.exists(productNum)) {
                    Product product = stockReadWriter.getDetails(productNum);
                    if (product.getQuantity() < LOW_STOCK_THRESHOLD) {
                        hasCriticalItems = true;
                        report.append(String.format("Product: %s\n", product.getDescription()));
                        report.append(String.format("ID: %s\n", productNum));
                        report.append(String.format("Current stock: %d %s\n",
                                product.getQuantity(),
                                product.getQuantity() == 0 ? "(OUT OF STOCK)" : "(LOW STOCK)"));
                        report.append("----------------------\n");
                    }
                }
            } catch (StockException e) {
                // Skip problematic products
                continue;
            }
        }

        if (!hasCriticalItems) {
            report.append("No items currently need attention.\n");
            report.append("----------------------\n");
        }

        // Restock History Section
        report.append("\nRESTOCK HISTORY:\n");
        report.append("----------------------\n");
        if (restockHistory.isEmpty()) {
            report.append("No restocks recorded.\n");
        } else {
            report.append(String.format("Total restocks: %d\n\n", restockHistory.size()));
            for (RestockRecord record : restockHistory) {
                report.append(String.format("Product: %s\n", record.description));
                report.append(String.format("ID: %s\n", record.productNum));
                report.append(String.format("Added: %d units\n", record.quantity));
                report.append(String.format("Time: %s\n",
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                                .format(record.timestamp)));
                report.append("----------------------\n");
            }
        }

        return report.toString();
    }
}