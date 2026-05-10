package com.tontonprimeur;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class ProductDialog extends JDialog {

    private static final Color CARD      = Color.WHITE;
    private static final Color TEXT      = new Color(17, 24, 39);
    private static final Color MUTED     = new Color(107, 114, 128);
    private static final Color ACCENT    = new Color(34, 197, 94);
    private static final Color ACCENT_H  = new Color(21, 163, 74);
    private static final Color BORDER_C  = new Color(218, 225, 232);
    private static final Color HEADER_BG = new Color(22, 63, 45);

    private Product productResult;
    private final JTextField   nameField   = new JTextField(26);
    private final JTextField   typeField   = new JTextField(26);
    private final JSpinner     qtySpinner  = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    private final JTextField   priceField  = new JTextField(12);
    private final JComboBox<Supplier> supplierCombo;

    public ProductDialog(Frame owner, Product existing, List<Supplier> suppliers) {
        super(owner, true);
        setTitle(existing == null ? "Ajouter un article" : "Modifier l'article");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        supplierCombo = new JComboBox<>();
        supplierCombo.addItem(null);
        for (Supplier s : suppliers) supplierCombo.addItem(s);
        supplierCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                setText(value == null ? "— Aucun —" : ((Supplier) value).getNom());
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                return this;
            }
        });

        if (existing != null) {
            nameField.setText(existing.getName());
            typeField.setText(existing.getDescription());
            qtySpinner.setValue(existing.getQuantity());
            priceField.setText(String.format("%.2f", existing.getPrice()));
            if (existing.getSupplierId() > 0) {
                for (int i = 0; i < supplierCombo.getItemCount(); i++) {
                    Supplier s = supplierCombo.getItemAt(i);
                    if (s != null && s.getId() == existing.getSupplierId()) {
                        supplierCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        JPanel accentStrip = new JPanel();
        accentStrip.setBackground(HEADER_BG);
        accentStrip.setPreferredSize(new Dimension(0, 6));

        JPanel card = new JPanel(new BorderLayout(16, 16));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(24, 28, 20, 28));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        JLabel titleLbl = new JLabel(existing == null ? "Ajouter un article" : "Modifier l'article");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(TEXT);
        JLabel subLbl = new JLabel("Remplis les informations de l'article.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(MUTED);
        subLbl.setBorder(new EmptyBorder(3, 0, 0, 0));
        header.add(titleLbl);
        header.add(subLbl);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(12, 0, 0, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 8, 16);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        styleField(nameField);
        styleField(typeField);
        styleField(priceField);
        styleSpinner(qtySpinner);
        styleCombo(supplierCombo);

        c.gridx = 0; c.gridy = 0; c.weightx = 0; form.add(label("Nom"),         c);
        c.gridx = 1; c.weightx = 1;                form.add(nameField,            c);
        c.gridx = 0; c.gridy = 1; c.weightx = 0; form.add(label("Type"),        c);
        c.gridx = 1; c.weightx = 1;                form.add(typeField,            c);
        c.gridx = 0; c.gridy = 2; c.weightx = 0; form.add(label("Quantité"),    c);
        c.gridx = 1; c.weightx = 1;                form.add(qtySpinner,           c);
        c.gridx = 0; c.gridy = 3; c.weightx = 0; form.add(label("Prix (€)"),    c);
        c.gridx = 1; c.weightx = 1;                form.add(priceField,           c);
        c.gridx = 0; c.gridy = 4; c.weightx = 0; form.add(label("Fournisseur"), c);
        c.gridx = 1; c.weightx = 1;                form.add(supplierCombo,        c);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(4, 0, 0, 0));
        AnimatedButton cancelBtn = new AnimatedButton("Annuler",     new Color(120, 124, 130), new Color(80, 84, 90), Color.WHITE);
        AnimatedButton saveBtn   = new AnimatedButton("Enregistrer", ACCENT, ACCENT_H, Color.WHITE);
        cancelBtn.addActionListener(e -> { productResult = null; dispose(); });
        saveBtn.addActionListener(e -> onSave(existing));
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.SOUTH);

        add(accentStrip, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);

        pack();
        setMinimumSize(new Dimension(440, 0));
        setLocationRelativeTo(owner);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT);
        l.setPreferredSize(new Dimension(90, 42));
        return l;
    }

    private void styleField(JTextField f) {
        f.setPreferredSize(new Dimension(260, 42));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        f.setBackground(CARD);
        f.setForeground(TEXT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(34, 197, 94), 2, true),
                    BorderFactory.createEmptyBorder(9, 11, 9, 11)
                ));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_C, 1, true),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));
            }
        });
    }

    private void styleSpinner(JSpinner s) {
        s.setPreferredSize(new Dimension(260, 42));
        s.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        s.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(260, 42));
        combo.setBackground(CARD);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBorder(BorderFactory.createLineBorder(BORDER_C, 1, true));
    }

    private void onSave(Product existing) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom est requis.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String type = typeField.getText().trim();
        int qty = (Integer) qtySpinner.getValue();
        double price;
        try {
            price = Double.parseDouble(priceField.getText().trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Prix invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Supplier selectedSupplier = (Supplier) supplierCombo.getSelectedItem();
        int id = existing != null ? existing.getId() : 0;
        productResult = new Product(id, name, type, qty, price);
        if (selectedSupplier != null) {
            productResult.setSupplierId(selectedSupplier.getId());
            productResult.setSupplierName(selectedSupplier.getNom());
        }
        dispose();
    }

    public Product getProductResult() { return productResult; }

    // ─── AnimatedButton ───────────────────────────────────────────────────────

    private static final class AnimatedButton extends JButton {
        private final Color base, hov;
        private Color cur;
        private float t = 0f;
        private boolean over = false;
        private Timer animTimer;

        AnimatedButton(String text, Color base, Color hov, Color fg) {
            super(text);
            this.base = base; this.hov = hov; this.cur = base;
            setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setFocusPainted(false); setBorderPainted(false);
            setContentAreaFilled(false); setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(130, 42));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { over = true;  startAnim(); }
                public void mouseExited(MouseEvent e)  { over = false; startAnim(); }
            });
        }

        private void startAnim() {
            if (animTimer != null) animTimer.stop();
            animTimer = new Timer(14, e -> {
                t = over ? Math.min(t + 0.14f, 1f) : Math.max(t - 0.14f, 0f);
                cur = blend(base, hov, t);
                repaint();
                if ((over && t >= 1f) || (!over && t <= 0f)) ((Timer) e.getSource()).stop();
            });
            animTimer.start();
        }

        private Color blend(Color a, Color b, float f) {
            return new Color(
                (int)(a.getRed()   + (b.getRed()   - a.getRed())   * f),
                (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * f),
                (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * f)
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cur);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
