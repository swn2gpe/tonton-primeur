package com.tontonprimeur;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class SupplierManagerPanel extends JPanel {

    private static final Color BG       = new Color(241, 245, 249);
    private static final Color CARD     = Color.WHITE;
    private static final Color TEXT     = new Color(17, 24, 39);
    private static final Color MUTED    = new Color(107, 114, 128);
    private static final Color ACCENT   = new Color(34, 197, 94);
    private static final Color ACCENT_H = new Color(21, 163, 74);
    private static final Color DANGER   = new Color(239, 68, 68);
    private static final Color DANGER_H = new Color(200, 45, 45);
    private static final Color DARK_BTN   = new Color(45, 55, 72);
    private static final Color DARK_BTN_H = new Color(26, 32, 44);
    private static final Color BORDER_C = new Color(218, 225, 232);
    private static final Color ROW_SEL  = new Color(187, 247, 208);
    private static final Color ROW_ODD  = new Color(249, 250, 251);

    private final SupplierRepository repository;
    private final List<Supplier> suppliers;
    private final SupplierTableModel tableModel;
    private final JTable table;
    private final FadeStatusLabel statusLabel = new FadeStatusLabel();

    public SupplierManagerPanel(SupplierRepository repository, List<Supplier> suppliers) {
        super(new BorderLayout(0, 12));
        this.repository = repository;
        this.suppliers = suppliers;
        this.tableModel = new SupplierTableModel(suppliers);
        this.table = new JTable(tableModel);

        setBackground(BG);
        setBorder(new EmptyBorder(16, 20, 10, 20));

        configureTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD);
        tableCard.setBorder(BorderFactory.createLineBorder(BORDER_C, 1, true));
        tableCard.add(scroll);

        add(buildToolbar(), BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 4, 0));

        AnimatedButton addBtn  = new AnimatedButton("+ Ajouter",    ACCENT,    ACCENT_H,   Color.WHITE);
        AnimatedButton editBtn = new AnimatedButton("✎ Modifier",   DARK_BTN,  DARK_BTN_H, Color.WHITE);
        AnimatedButton delBtn  = new AnimatedButton("✕ Supprimer",  DANGER,    DANGER_H,   Color.WHITE);

        addBtn.addActionListener(e -> addSupplier());
        editBtn.addActionListener(e -> editSupplier());
        delBtn.addActionListener(e -> deleteSupplier());

        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(delBtn);
        return panel;
    }

    private void configureTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(48);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setBackground(CARD);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(ROW_SEL);
        table.setSelectionForeground(TEXT);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C));
        header.setPreferredSize(new Dimension(0, 44));

        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(280);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                setBorder(new EmptyBorder(0, 16, 0, 16));
                setForeground(TEXT);
                setBackground(sel ? ROW_SEL : row % 2 == 0 ? CARD : ROW_ODD);
                setHorizontalAlignment(LEFT);
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
    }

    private void addSupplier() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        SupplierDialog d = new SupplierDialog(owner, null);
        d.setVisible(true);
        Supplier s = d.getResult();
        if (s != null) {
            try {
                repository.addSupplier(s);
                suppliers.add(s);
                tableModel.refresh();
                statusLabel.show("Fournisseur ajouté.", ACCENT_H);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSupplier() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionne un fournisseur à modifier.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Supplier sel = tableModel.getSupplier(row);
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        SupplierDialog d = new SupplierDialog(owner, sel);
        d.setVisible(true);
        Supplier updated = d.getResult();
        if (updated != null) {
            try {
                repository.updateSupplier(updated);
                sel.setNom(updated.getNom());
                sel.setTelephone(updated.getTelephone());
                sel.setEmail(updated.getEmail());
                sel.setAdresse(updated.getAdresse());
                tableModel.refresh();
                statusLabel.show("Fournisseur modifié.", ACCENT_H);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSupplier() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionne un fournisseur à supprimer.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Supplier sel = tableModel.getSupplier(row);
        if (repository.hasArticles(sel.getId())) {
            JOptionPane.showMessageDialog(this,
                "Impossible de supprimer \"" + sel.getNom() + "\" : des articles lui sont associés.",
                "Contrainte d'intégrité", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Supprimer \"" + sel.getNom() + "\" ?", "Confirmer", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                repository.deleteSupplier(sel.getId());
                suppliers.remove(row);
                tableModel.refresh();
                statusLabel.show("Fournisseur supprimé.", DANGER);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ─── FadeStatusLabel ──────────────────────────────────────────────────────

    static final class FadeStatusLabel extends JLabel {
        private float alpha = 0f;
        private Timer fadeIn, fadeOut, hold;

        FadeStatusLabel() {
            super(" ");
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(6, 2, 6, 0));
        }

        void show(String text, Color color) {
            if (hold    != null) hold.stop();
            if (fadeIn  != null) fadeIn.stop();
            if (fadeOut != null) fadeOut.stop();
            setText(text); setForeground(color); alpha = 0f;
            fadeIn = new Timer(16, e -> {
                alpha = Math.min(alpha + 0.1f, 1f); repaint();
                if (alpha >= 1f) ((Timer) e.getSource()).stop();
            });
            fadeIn.start();
            hold = new Timer(3000, e -> {
                fadeOut = new Timer(30, ev -> {
                    alpha = Math.max(alpha - 0.05f, 0f); repaint();
                    if (alpha <= 0f) { ((Timer) ev.getSource()).stop(); setText(" "); }
                });
                fadeOut.start();
            });
            hold.setRepeats(false); hold.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

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
            setPreferredSize(new Dimension(138, 42));
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
