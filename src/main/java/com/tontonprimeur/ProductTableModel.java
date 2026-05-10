package com.tontonprimeur;

import java.util.List;
import javax.swing.table.AbstractTableModel;

public class ProductTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Nom", "Type", "Quantité", "Prix (€)", "Fournisseur"};
    private final List<Product> products;

    public ProductTableModel(List<Product> products) {
        this.products = products;
    }

    @Override public int getRowCount() { return products.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Product p = products.get(row);
        return switch (col) {
            case 0 -> p.getName();
            case 1 -> p.getDescription();
            case 2 -> p.getQuantity();
            case 3 -> String.format("%.2f", p.getPrice());
            case 4 -> p.getSupplierName() != null ? p.getSupplierName() : "—";
            default -> null;
        };
    }

    public Product getProduct(int row) { return products.get(row); }
    public void refresh() { fireTableDataChanged(); }
}
