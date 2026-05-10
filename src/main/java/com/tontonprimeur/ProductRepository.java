package com.tontonprimeur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductRepository {
    private final String url;
    private final String user;
    private final String password;

    public ProductRepository(String host, int port, String database, String user, String password) {
        this.url = String.format("jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true", host, port, database);
        this.user = user;
        this.password = password;
    }

    public List<Product> loadProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT a.id_article, a.nom, a.`type`, a.prix_unitaire, a.quantite_stock, " +
                     "a.id_fournisseur, f.nom AS fournisseur_nom " +
                     "FROM article a LEFT JOIN fournisseur f ON a.id_fournisseur = f.id_fournisseur " +
                     "ORDER BY a.nom";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product p = new Product(
                    rs.getInt("id_article"),
                    rs.getString("nom"),
                    rs.getString("type"),
                    rs.getInt("quantite_stock"),
                    rs.getDouble("prix_unitaire")
                );
                p.setSupplierId(rs.getInt("id_fournisseur"));
                p.setSupplierName(rs.getString("fournisseur_nom"));
                list.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de charger les produits depuis la base de données MySQL", e);
        }
        return list;
    }

    public void saveProducts(List<Product> products) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);

            // Supprimer les articles qui ne sont plus dans la liste
            List<Integer> existingIds = products.stream()
                .filter(p -> p.getId() > 0)
                .map(Product::getId)
                .collect(Collectors.toList());

            if (existingIds.isEmpty()) {
                conn.createStatement().executeUpdate("DELETE FROM article");
            } else {
                String inClause = existingIds.stream().map(String::valueOf).collect(Collectors.joining(", "));
                conn.createStatement().executeUpdate("DELETE FROM article WHERE id_article NOT IN (" + inClause + ")");
            }

            for (Product p : products) {
                if (p.getId() > 0) {
                    // Mise à jour
                    try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE article SET nom=?, `type`=?, prix_unitaire=?, quantite_stock=?, id_fournisseur=? WHERE id_article=?")) {
                        ps.setString(1, p.getName());
                        ps.setString(2, p.getDescription());
                        ps.setDouble(3, p.getPrice());
                        ps.setInt(4, p.getQuantity());
                        if (p.getSupplierId() > 0) ps.setInt(5, p.getSupplierId());
                        else ps.setNull(5, Types.INTEGER);
                        ps.setInt(6, p.getId());
                        ps.executeUpdate();
                    }
                } else {
                    // Insertion
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO article (nom, `type`, prix_unitaire, quantite_stock, id_fournisseur) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, p.getName());
                        ps.setString(2, p.getDescription());
                        ps.setDouble(3, p.getPrice());
                        ps.setInt(4, p.getQuantity());
                        if (p.getSupplierId() > 0) ps.setInt(5, p.getSupplierId());
                        else ps.setNull(5, Types.INTEGER);
                        ps.executeUpdate();
                        ResultSet keys = ps.getGeneratedKeys();
                        if (keys.next()) p.setId(keys.getInt(1));
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'enregistrer les produits dans la base de données MySQL", e);
        }
    }
}
