package com.tontonprimeur;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        String dbHost     = System.getenv().getOrDefault("DB_HOST",     "localhost");
        int    dbPort     = Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "3306"));
        String dbName     = System.getenv().getOrDefault("DB_NAME",     "tonton_primeur");
        String dbUser     = System.getenv().getOrDefault("DB_USER",     "root");
        String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "");

        SwingUtilities.invokeLater(() -> {
            ProductRepository  productRepo  = new ProductRepository(dbHost, dbPort, dbName, dbUser, dbPassword);
            SupplierRepository supplierRepo = new SupplierRepository(dbHost, dbPort, dbName, dbUser, dbPassword);

            List<Product>  products  = new ArrayList<>(productRepo.loadProducts());
            List<Supplier> suppliers = new ArrayList<>(supplierRepo.loadSuppliers());

            ProductManagerFrame frame = new ProductManagerFrame(productRepo, products, supplierRepo, suppliers);
            frame.setVisible(true);
        });
    }
}
