package com.tontonprimeur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierRepository {
    private final String url;
    private final String user;
    private final String password;

    public SupplierRepository(String host, int port, String database, String user, String password) {
        this.url = String.format("jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true", host, port, database);
        this.user = user;
        this.password = password;
    }

    public List<Supplier> loadSuppliers() {
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_fournisseur, nom, telephone, email, adresse FROM fournisseur ORDER BY nom")) {
            while (rs.next()) {
                list.add(new Supplier(
                    rs.getInt("id_fournisseur"),
                    rs.getString("nom"),
                    rs.getString("telephone"),
                    rs.getString("email"),
                    rs.getString("adresse")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de charger les fournisseurs", e);
        }
        return list;
    }

    public void addSupplier(Supplier s) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO fournisseur (nom, telephone, email, adresse) VALUES (?, ?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getNom());
            ps.setString(2, s.getTelephone());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getAdresse());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) s.setId(keys.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'ajouter le fournisseur", e);
        }
    }

    public void updateSupplier(Supplier s) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE fournisseur SET nom=?, telephone=?, email=?, adresse=? WHERE id_fournisseur=?")) {
            ps.setString(1, s.getNom());
            ps.setString(2, s.getTelephone());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getAdresse());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de modifier le fournisseur", e);
        }
    }

    public boolean hasArticles(int supplierId) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM article WHERE id_fournisseur = ?")) {
            ps.setInt(1, supplierId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return true;
        }
    }

    public void deleteSupplier(int id) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM fournisseur WHERE id_fournisseur=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de supprimer le fournisseur", e);
        }
    }
}
