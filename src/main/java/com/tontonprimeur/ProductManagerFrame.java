package com.tontonprimeur;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class ProductManagerFrame extends JFrame {

    private static final Color BG         = new Color(241, 245, 249);
    private static final Color HEADER_BG  = new Color(22, 63, 45);
    private static final Color TAB_BG     = new Color(30, 75, 55);
    private static final Color CARD       = Color.WHITE;
    private static final Color TEXT       = new Color(17, 24, 39);
    private static final Color MUTED      = new Color(107, 114, 128);
    private static final Color ACCENT     = new Color(34, 197, 94);
    private static final Color ACCENT_H   = new Color(21, 163, 74);
    private static final Color DANGER     = new Color(239, 68, 68);
    private static final Color DANGER_H   = new Color(200, 45, 45);
    private static final Color DARK_BTN   = new Color(45, 55, 72);
    private static final Color DARK_BTN_H = new Color(26, 32, 44);
    private static final Color BORDER_C   = new Color(218, 225, 232);
    private static final Color ROW_FRUIT  = new Color(255, 251, 235);
    private static final Color ROW_LEGUME = new Color(240, 253, 244);
    private static final Color ROW_ODD    = new Color(249, 250, 251);
    private static final Color ROW_SEL    = new Color(187, 247, 208);
    private static final Color FLASH_COL  = new Color(254, 249, 195);

    private final ProductRepository repository;
    private final List<Product>  products;
    private final List<Supplier> suppliers;
    private StatsPanel statsPanel;
    private final ProductTableModel tableModel;
    private final JTable productTable;
    private final TableRowSorter<ProductTableModel> rowSorter;
    private final PlaceholderTextField searchField = new PlaceholderTextField("Rechercher un article...", 22);
    private final FadeStatusLabel statusLabel = new FadeStatusLabel();
    private final JLabel statsLabel = new JLabel();
    private int flashRow = -1;
    private Timer flashTimer;

    public ProductManagerFrame(ProductRepository repository, List<Product> products,
                               SupplierRepository supplierRepository, List<Supplier> suppliers) {
        super("Gestion de stock Primeur");
        this.repository = repository;
        this.products = products;
        this.suppliers = suppliers;
        this.tableModel = new ProductTableModel(products);
        this.productTable = new JTable(tableModel);
        this.rowSorter = new TableRowSorter<>(tableModel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        configureTable();

        statsPanel = new StatsPanel(products, suppliers);

        CardLayout cards = new CardLayout();
        JPanel cardPanel = new JPanel(cards);
        cardPanel.setBackground(BG);
        cardPanel.add(buildArticlePanel(), "articles");
        cardPanel.add(new SupplierManagerPanel(supplierRepository, suppliers), "fournisseurs");
        cardPanel.add(statsPanel, "stats");

        JPanel north = new JPanel(new BorderLayout());
        north.add(buildHeader(), BorderLayout.NORTH);
        north.add(buildTabBar(cards, cardPanel), BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { saveProducts(); }
        });

        updateStats();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HEADER_BG);
        panel.setBorder(new EmptyBorder(18, 28, 16, 28));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("Gestion de stock Primeur");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Dashboard pour gérer les produits en magasin.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(134, 239, 172));
        subtitle.setBorder(new EmptyBorder(3, 0, 0, 0));

        left.add(title);
        left.add(subtitle);

        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLabel.setForeground(new Color(134, 239, 172));
        statsLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(left, BorderLayout.WEST);
        panel.add(statsLabel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildTabBar(CardLayout cards, JPanel cardPanel) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(TAB_BG);
        bar.setBorder(new EmptyBorder(0, 20, 0, 0));

        TabButton[] tabs = {
            new TabButton("Articles",       true),
            new TabButton("Fournisseurs",   false),
            new TabButton("Statistiques",   false)
        };
        String[] keys = {"articles", "fournisseurs", "stats"};

        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            tabs[i].addActionListener(e -> {
                cards.show(cardPanel, keys[idx]);
                for (int j = 0; j < tabs.length; j++) tabs[j].setActive(j == idx);
                if (keys[idx].equals("stats")) statsPanel.refresh();
            });
            bar.add(tabs[i]);
        }
        return bar;
    }

    private JPanel buildArticlePanel() {
        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD);
        tableCard.setBorder(BorderFactory.createLineBorder(BORDER_C, 1, true));
        tableCard.add(scroll);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 20, 10, 20));
        panel.add(buildToolbar(), BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 4, 0));

        searchField.setPreferredSize(new Dimension(280, 42));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.setBackground(CARD);
        searchField.setForeground(TEXT);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT, 2, true),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            public void focusLost(FocusEvent e) {
                searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_C, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchWrap.setOpaque(false);
        searchWrap.add(searchField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        AnimatedButton addBtn  = new AnimatedButton("+ Ajouter",      ACCENT,                  ACCENT_H,               Color.WHITE);
        AnimatedButton editBtn = new AnimatedButton("✎ Modifier",     DARK_BTN,                DARK_BTN_H,             Color.WHITE);
        AnimatedButton delBtn  = new AnimatedButton("✕ Supprimer",    DANGER,                  DANGER_H,               Color.WHITE);
        AnimatedButton saveBtn = new AnimatedButton("↓ Enregistrer",  new Color(22, 163, 74),  new Color(15, 130, 60), Color.WHITE);

        addBtn.addActionListener(e -> addProduct());
        editBtn.addActionListener(e -> editProduct());
        delBtn.addActionListener(e -> deleteProduct());
        saveBtn.addActionListener(e -> saveProducts());

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(saveBtn);

        panel.add(searchWrap, BorderLayout.WEST);
        panel.add(btnPanel, BorderLayout.EAST);
        return panel;
    }

    private void configureTable() {
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.setRowHeight(48);
        productTable.setShowGrid(false);
        productTable.setIntercellSpacing(new Dimension(0, 0));
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowSorter(rowSorter);
        productTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        productTable.setBackground(CARD);
        productTable.setFillsViewportHeight(true);
        productTable.setSelectionBackground(ROW_SEL);
        productTable.setSelectionForeground(TEXT);

        JTableHeader header = productTable.getTableHeader();
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C));
        header.setPreferredSize(new Dimension(0, 44));

        productTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(160);

        productTable.getColumnModel().getColumn(1).setCellRenderer(new TypeBadgeRenderer());

        DefaultTableCellRenderer baseRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                int mr = t.convertRowIndexToModel(row);
                Product p = tableModel.getProduct(mr);
                String type = p.getDescription();
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                setBorder(new EmptyBorder(0, 16, 0, 16));
                setForeground(TEXT);
                if (sel) {
                    setBackground(ROW_SEL);
                } else if (mr == flashRow) {
                    setBackground(FLASH_COL);
                } else if ("fruit".equalsIgnoreCase(type)) {
                    setBackground(ROW_FRUIT);
                } else if ("legume".equalsIgnoreCase(type) || "légume".equalsIgnoreCase(type)) {
                    setBackground(ROW_LEGUME);
                } else {
                    setBackground(row % 2 == 0 ? CARD : ROW_ODD);
                }
                if (col == 2) {
                    setHorizontalAlignment(CENTER);
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                } else if (col == 3) {
                    setHorizontalAlignment(RIGHT);
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                    setForeground(new Color(22, 101, 52));
                } else if (col == 4) {
                    setHorizontalAlignment(LEFT);
                    setFont(new Font("Segoe UI", Font.ITALIC, 13));
                    setForeground(MUTED);
                } else {
                    setHorizontalAlignment(LEFT);
                }
                return this;
            }
        };
        productTable.setDefaultRenderer(Object.class, baseRenderer);
        productTable.setDefaultRenderer(Integer.class, baseRenderer);
    }

    private void filter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) rowSorter.setRowFilter(null);
        else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }

    private void addProduct() {
        ProductDialog d = new ProductDialog(this, null, suppliers);
        d.setVisible(true);
        Product p = d.getProductResult();
        if (p != null) {
            products.add(p);
            tableModel.refresh();
            triggerFlash(products.size() - 1);
            updateStats();
            statusLabel.show("Article ajouté.", ACCENT_H);
        }
    }

    private void editProduct() {
        int view = productTable.getSelectedRow();
        if (view < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionne un article à modifier.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int model = productTable.convertRowIndexToModel(view);
        Product sel = tableModel.getProduct(model);
        ProductDialog d = new ProductDialog(this, sel, suppliers);
        d.setVisible(true);
        Product u = d.getProductResult();
        if (u != null) {
            sel.setName(u.getName());
            sel.setDescription(u.getDescription());
            sel.setQuantity(u.getQuantity());
            sel.setPrice(u.getPrice());
            sel.setSupplierId(u.getSupplierId());
            sel.setSupplierName(u.getSupplierName());
            tableModel.refresh();
            triggerFlash(model);
            updateStats();
            statusLabel.show("Article modifié.", ACCENT_H);
        }
    }

    private void deleteProduct() {
        int view = productTable.getSelectedRow();
        if (view < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionne un article à supprimer.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Confirmer la suppression ?", "Supprimer", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            products.remove(productTable.convertRowIndexToModel(view));
            tableModel.refresh();
            updateStats();
            statusLabel.show("Article supprimé.", DANGER);
        }
    }

    private void saveProducts() {
        try {
            repository.saveProducts(products);
            statusLabel.show("Données enregistrées dans MySQL.", ACCENT_H);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void triggerFlash(int modelRow) {
        flashRow = modelRow;
        if (flashTimer != null) flashTimer.stop();
        int[] n = {0};
        flashTimer = new Timer(100, e -> {
            if (++n[0] >= 8) { flashRow = -1; ((Timer) e.getSource()).stop(); }
            productTable.repaint();
        });
        flashTimer.start();
    }

    private void updateStats() {
        long fruits  = products.stream().filter(p -> "fruit".equalsIgnoreCase(p.getDescription())).count();
        long legumes = products.stream().filter(p -> "legume".equalsIgnoreCase(p.getDescription()) || "légume".equalsIgnoreCase(p.getDescription())).count();
        double total = products.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
        statsLabel.setText(String.format(
            "<html><span style='color:#86EFAC;'>%d articles &nbsp;&bull;&nbsp; %d fruits &nbsp;&bull;&nbsp; %d légumes &nbsp;&bull;&nbsp; <b>%.2f €</b></span></html>",
            products.size(), fruits, legumes, total
        ));
    }

    // ─── TabButton ────────────────────────────────────────────────────────────

    private static final class TabButton extends JButton {
        private boolean active;

        TabButton(String text, boolean active) {
            super(text);
            this.active = active;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setPreferredSize(new Dimension(140, 38));
            updateStyle();
        }

        void setActive(boolean a) { this.active = a; updateStyle(); repaint(); }

        private void updateStyle() {
            setForeground(active ? new Color(22, 63, 45) : new Color(134, 239, 172));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 2, 8, 8);
            }
            g2.dispose();
            super.paintComponent(g);
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
            setPreferredSize(new Dimension(142, 42));
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

    // ─── TypeBadgeRenderer ────────────────────────────────────────────────────

    private final class TypeBadgeRenderer extends JPanel implements TableCellRenderer {
        private String type = "";
        private boolean sel = false;
        private int mr = -1;

        TypeBadgeRenderer() { setOpaque(true); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean selected, boolean foc, int row, int col) {
            type = v == null ? "" : v.toString();
            sel = selected;
            mr = t.convertRowIndexToModel(row);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg;
            if (sel) bg = ROW_SEL;
            else if (mr == flashRow) bg = FLASH_COL;
            else if ("fruit".equalsIgnoreCase(type)) bg = ROW_FRUIT;
            else if ("legume".equalsIgnoreCase(type) || "légume".equalsIgnoreCase(type)) bg = ROW_LEGUME;
            else bg = CARD;
            g2.setColor(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            String label;
            Color badgeBg, badgeFg;
            if ("fruit".equalsIgnoreCase(type)) {
                label = "fruit"; badgeBg = new Color(254, 234, 199); badgeFg = new Color(180, 83, 9);
            } else if ("legume".equalsIgnoreCase(type) || "légume".equalsIgnoreCase(type)) {
                label = "légume"; badgeBg = new Color(220, 252, 231); badgeFg = new Color(22, 101, 52);
            } else {
                label = type; badgeBg = new Color(243, 244, 246); badgeFg = new Color(75, 85, 99);
            }

            Font f = new Font("Segoe UI", Font.BOLD, 12);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(label);
            int bw = tw + 22, bh = 24;
            int bx = 14, by = (getHeight() - bh) / 2;

            g2.setColor(badgeBg);
            g2.fillRoundRect(bx, by, bw, bh, bh, bh);
            g2.setColor(badgeFg);
            g2.drawString(label, bx + 11, by + (bh - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // ─── FadeStatusLabel ──────────────────────────────────────────────────────

    private static final class FadeStatusLabel extends JLabel {
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

    // ─── PlaceholderTextField ─────────────────────────────────────────────────

    private static final class PlaceholderTextField extends JTextField {
        private final String ph;

        PlaceholderTextField(String ph, int cols) { super(cols); this.ph = ph; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(156, 163, 175));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                Insets ins = getInsets();
                g2.drawString(ph, ins.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }
}
