package test;

import catalogue.Basket;
import catalogue.Product;
import clients.cashier.CashierModel;
import middle.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import javax.swing.ImageIcon;
import java.util.List;
import java.util.Map;

public class CashierModelUndoTest {
    private CashierModel cashierModel;
    private TestStockReadWriter testStock;
    private TestMiddleFactory testFactory;

    // Test implementation of StockReadWriter that implements both StockReader and StockReadWriter interfaces
    private class TestStockReadWriter implements StockReadWriter {
        private Product testProduct;
        private int currentStock = 5;

        public TestStockReadWriter() {
            testProduct = new Product("TEST1", "Test Product", 10.0, currentStock);
        }

        @Override
        public List<Product> getProductsByCategory(String category) throws StockException {
            return List.of();
        }

        @Override
        public boolean exists(String productNum) throws StockException {
            return "TEST1".equals(productNum);
        }

        @Override
        public Product getDetails(String productNum) throws StockException {
            if (exists(productNum)) {
                return new Product(testProduct.getProductNum(),
                        testProduct.getDescription(),
                        testProduct.getPrice(),
                        currentStock);
            }
            throw new StockException("Product not found");
        }

        @Override
        public boolean buyStock(String productNum, int amount) throws StockException {
            if (exists(productNum) && currentStock >= amount) {
                currentStock -= amount;
                return true;
            }
            return false;
        }

        @Override
        public void addStock(String productNum, int amount) throws StockException {
            if (exists(productNum)) {
                currentStock += amount;
            } else {
                throw new StockException("Product not found");
            }
        }

        @Override
        public void modifyStock(Product detail) throws StockException {
            if (exists(detail.getProductNum())) {
                testProduct = detail;
            } else {
                throw new StockException("Product not found");
            }
        }

        @Override
        public ImageIcon getImage(String pNum) throws StockException {
            return null; // Not needed for undo testing
        }

        // Helper method for testing
        public int getCurrentStock() {
            return currentStock;
        }
    }

    // Test implementation of MiddleFactory
    private class TestMiddleFactory implements MiddleFactory {
        private final TestStockReadWriter stockReadWriter;

        public TestMiddleFactory(TestStockReadWriter stockReadWriter) {
            this.stockReadWriter = stockReadWriter;
        }

        @Override
        public StockReader makeStockReader() throws StockException {
            return stockReadWriter;
        }

        @Override
        public StockReadWriter makeStockReadWriter() throws StockException {
            return stockReadWriter;
        }

        @Override
        public OrderProcessing makeOrderProcessing() throws OrderException {
            return new TestOrderProcessing();
        }
    }

    // Test implementation of OrderProcessing
    private class TestOrderProcessing implements OrderProcessing {
        private int orderNumber = 1;

        @Override
        public void newOrder(Basket basket) throws OrderException {
            // Simple implementation for testing
        }

        @Override
        public int uniqueNumber() throws OrderException {
            return orderNumber++;
        }

        @Override
        public Basket getOrderToPack() throws OrderException {
            return null; // Not needed for undo testing
        }

        @Override
        public boolean informOrderPacked(int orderNum) throws OrderException {
            return true; // Not needed for undo testing
        }

        @Override
        public boolean informOrderCollected(int orderNum) throws OrderException {
            return true; // Not needed for undo testing
        }

        @Override
        public Map<String, List<Integer>> getOrderState() throws OrderException {
            return null; // Not needed for undo testing
        }

        @Override
        public String generateOrderReport() throws OrderException {
            return "Test Report"; // Not needed for undo testing
        }
    }

    @Before
    public void setUp() {
        testStock = new TestStockReadWriter();
        testFactory = new TestMiddleFactory(testStock);
        cashierModel = new CashierModel(testFactory);
    }

    @Test
    public void testUndoLastWithEmptyBasket() throws StockException {
        // Get initial stock level
        int initialStock = testStock.getCurrentStock();

        // Try to undo with empty basket
        cashierModel.undoLast();

        // Verify stock hasn't changed
        assertEquals("Stock level should not change when undoing with empty basket",
                initialStock, testStock.getCurrentStock());
    }

    @Test
    public void testUndoLastWithOneItem() throws StockException {
        // Record initial stock
        int initialStock = testStock.getCurrentStock();

        // Add item to basket
        cashierModel.doCheck("TEST1", "1");
        cashierModel.doBuy();

        // Verify stock decreased
        assertEquals("Stock should decrease after buying item",
                initialStock - 1, testStock.getCurrentStock());

        // Perform undo
        cashierModel.undoLast();

        // Verify stock returned to initial level
        assertEquals("Stock should return to initial level after undo",
                initialStock, testStock.getCurrentStock());
    }

    @Test
    public void testUndoLastWithMultipleItems() throws StockException {
        // Add two items
        cashierModel.doCheck("TEST1", "1");
        cashierModel.doBuy();
        int stockAfterFirstPurchase = testStock.getCurrentStock();

        cashierModel.doCheck("TEST1", "1");
        cashierModel.doBuy();

        // Undo last item
        cashierModel.undoLast();

        // Verify only last item was returned
        assertEquals("Stock should return to level after first purchase",
                stockAfterFirstPurchase, testStock.getCurrentStock());
    }

    @Test
    public void testUndoWithInvalidQuantity() throws StockException {
        int initialStock = testStock.getCurrentStock();

        // Try to buy more than available stock
        cashierModel.doCheck("TEST1", "10");

        // Attempt undo (should have no effect as purchase didn't succeed)
        cashierModel.undoLast();

        // Verify stock remains unchanged
        assertEquals("Stock should not change when undoing invalid purchase",
                initialStock, testStock.getCurrentStock());
    }

    @Test
    public void testUndoWithNonexistentProduct() throws StockException {
        int initialStock = testStock.getCurrentStock();

        // Try to buy non-existent product
        cashierModel.doCheck("INVALID", "1");

        // Attempt undo
        cashierModel.undoLast();

        // Verify stock remains unchanged
        assertEquals("Stock should not change when undoing nonexistent product purchase",
                initialStock, testStock.getCurrentStock());
    }
}