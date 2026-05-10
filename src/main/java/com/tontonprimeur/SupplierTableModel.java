package com.tontonprimeur;

import java.util.List;
import javax.swing.table.AbstractTableModel;

public class SupplierTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Nom", "Téléphone", "Email", "Adresse"};
    private final List<Supplier> suppliers;

    public SupplierTableModel(List<Supplier> suppliers) {
        this.suppliers = suppliers;
    }

    @Override public int getRowCount() { return suppliers.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Supplier s = suppliers.get(row);
        return switch (col) {
            case 0 -> s.getNom();
            case 1 -> s.getTelephone();
            case 2 -> s.getEmail();
            case 3 -> s.getAdresse();
            default -> null;
        };
    }

    public Supplier getSupplier(int row) { return suppliers.get(row); }
    public void refresh() { fireTableDataChanged(); }
}
