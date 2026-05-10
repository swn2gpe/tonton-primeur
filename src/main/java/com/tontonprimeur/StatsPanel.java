package com.tontonprimeur;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

public class StatsPanel extends JPanel {

    private static final Color BG       = new Color(241, 245, 249);
    private static final Color CARD     = Color.WHITE;
    private static final Color TEXT     = new Color(17, 24, 39);
    private static final Color MUTED    = new Color(107, 114, 128);
    private static final Color BORDER_C = new Color(218, 225, 232);

    private final List<Product> products;

    private final StatCard[] cards;
    private final DonutChartPanel donutPanel;
    private final BarChartPanel   barPanel;

    public StatsPanel(List<Product> products, List<Supplier> suppliers) {
        super(new BorderLayout(0, 16));
        this.products = products;
        setBackground(BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // ── Cartes résumé ────────────────────────────────────────────────────
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setBackground(BG);
        cardsRow.setPreferredSize(new Dimension(0, 110));

        cards = new StatCard[]{
            new StatCard("Valeur du stock",      new Color(34,  197,  94)),
            new StatCard("Articles en stock",    new Color(59,  130, 246)),
            new StatCard("Fournisseurs actifs",  new Color(139,  92, 246)),
            new StatCard("Prix moyen",           new Color(251, 146,  60))
        };
        for (StatCard c : cards) cardsRow.add(c);
        add(cardsRow, BorderLayout.NORTH);

        // ── Graphiques ───────────────────────────────────────────────────────
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setBackground(BG);

        donutPanel = new DonutChartPanel(products);
        barPanel   = new BarChartPanel(products);

        chartsRow.add(wrapCard("Répartition par type",    donutPanel));
        chartsRow.add(wrapCard("Top articles en stock",   barPanel));
        add(chartsRow, BorderLayout.CENTER);
    }

    /** Appelé à chaque affichage de l'onglet ou après une modification. */
    public void refresh() {
        double totalVal  = products.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
        double avgPrice  = products.isEmpty() ? 0 : products.stream().mapToDouble(Product::getPrice).average().orElse(0);
        long activeSupp  = products.stream().mapToInt(Product::getSupplierId).filter(id -> id > 0).distinct().count();

        cards[0].animateTo(totalVal,       "%.2f €");
        cards[1].animateTo(products.size(), "%.0f");
        cards[2].animateTo(activeSupp,      "%.0f");
        cards[3].animateTo(avgPrice,        "%.2f €");

        donutPanel.refresh();
        barPanel.refresh();
    }

    private JPanel wrapCard(String title, JPanel content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createLineBorder(BORDER_C, 1, true));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(TEXT);
        titleLbl.setBorder(new EmptyBorder(14, 18, 8, 18));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(content,  BorderLayout.CENTER);
        return card;
    }

    // ─── StatCard ─────────────────────────────────────────────────────────────

    private static final class StatCard extends JPanel {
        private final JLabel valueLabel;
        private double current = 0, target = 0;
        private String fmt = "%.0f";
        private Timer anim;

        StatCard(String title, Color accent) {
            super(new BorderLayout());
            setBackground(CARD);
            setBorder(BorderFactory.createLineBorder(new Color(218, 225, 232), 1, true));

            JPanel accentBar = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(accent);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.dispose();
                }
            };
            accentBar.setOpaque(false);
            accentBar.setPreferredSize(new Dimension(0, 5));

            JPanel content = new JPanel(new BorderLayout(0, 4));
            content.setOpaque(false);
            content.setBorder(new EmptyBorder(12, 16, 14, 16));

            valueLabel = new JLabel("—");
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
            valueLabel.setForeground(new Color(17, 24, 39));

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            titleLbl.setForeground(new Color(107, 114, 128));

            content.add(valueLabel, BorderLayout.CENTER);
            content.add(titleLbl,   BorderLayout.SOUTH);
            add(accentBar, BorderLayout.NORTH);
            add(content,   BorderLayout.CENTER);
        }

        void animateTo(double value, String format) {
            this.target = value; this.fmt = format;
            if (anim != null) anim.stop();
            anim = new Timer(16, e -> {
                current += (target - current) * 0.16;
                if (Math.abs(current - target) < 0.005) { current = target; ((Timer)e.getSource()).stop(); }
                valueLabel.setText(String.format(fmt, current));
            });
            anim.start();
        }
    }

    // ─── DonutChartPanel ──────────────────────────────────────────────────────

    private static final class DonutChartPanel extends JPanel {
        private static final class Slice {
            final String label; final int count; final Color color;
            Slice(String l, int c, Color col) { label=l; count=c; color=col; }
        }

        private final List<Product> products;
        private List<Slice> slices = new ArrayList<>();
        private float anim = 1f;
        private Timer animTimer;

        DonutChartPanel(List<Product> products) {
            this.products = products;
            setBackground(CARD);
        }

        void refresh() {
            long fruits  = products.stream().filter(p -> "fruit".equalsIgnoreCase(p.getDescription())).count();
            long legumes = products.stream().filter(p -> "legume".equalsIgnoreCase(p.getDescription()) || "légume".equalsIgnoreCase(p.getDescription())).count();
            long autres  = products.size() - fruits - legumes;

            slices = new ArrayList<>();
            if (fruits  > 0) slices.add(new Slice("Fruits",  (int)fruits,  new Color(251, 146, 60)));
            if (legumes > 0) slices.add(new Slice("Légumes", (int)legumes, new Color(34, 197, 94)));
            if (autres  > 0) slices.add(new Slice("Autres",  (int)autres,  new Color(148, 163, 184)));

            anim = 0f;
            if (animTimer != null) animTimer.stop();
            animTimer = new Timer(16, e -> {
                anim = Math.min(anim + 0.035f, 1f);
                repaint();
                if (anim >= 1f) ((Timer)e.getSource()).stop();
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (slices.isEmpty()) {
                g2.setColor(MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.drawString("Aucune donnée", 20, getHeight() / 2);
                g2.dispose(); return;
            }

            int total = slices.stream().mapToInt(s -> s.count).sum();
            int legH  = 24 * slices.size() + 8;
            int avail = getHeight() - legH - 20;
            int diam  = Math.min(getWidth() - 60, Math.max(avail, 40));
            int cx    = getWidth() / 2;
            int cy    = 10 + diam / 2;
            int outerR = diam / 2;
            int innerR = outerR * 55 / 100;

            float t = ease(anim);
            float startAngle = -90f;
            for (Slice slice : slices) {
                float sweep = (float)slice.count / total * 360f * t;
                g2.setColor(slice.color);
                g2.fill(new Arc2D.Float(cx - outerR, cy - outerR, diam, diam, startAngle, sweep, Arc2D.PIE));
                startAngle += sweep;
            }

            // Trou central
            g2.setColor(CARD);
            g2.fillOval(cx - innerR, cy - innerR, 2*innerR, 2*innerR);

            // Texte centre
            if (anim > 0.85f) {
                float alpha = Math.min((anim - 0.85f) / 0.15f, 1f);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g2.setColor(TEXT);
                String tot = String.valueOf(total);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(tot, cx - fm.stringWidth(tot)/2, cy + fm.getAscent()/2 - 3);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(MUTED);
                fm = g2.getFontMetrics();
                g2.drawString("articles", cx - fm.stringWidth("articles")/2, cy + fm.getAscent()/2 + 14);
                g2.setComposite(AlphaComposite.SrcOver);
            }

            // Légende
            int ly = cy + outerR + 18;
            for (Slice slice : slices) {
                double pct = (double)slice.count / total * 100;
                g2.setColor(slice.color);
                g2.fillRoundRect(20, ly, 14, 14, 4, 4);
                g2.setColor(TEXT);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.drawString(String.format("%s — %d  (%.0f%%)", slice.label, slice.count, pct), 42, ly + 12);
                ly += 24;
            }

            g2.dispose();
        }

        private float ease(float t) { return 1 - (1-t)*(1-t)*(1-t); }
    }

    // ─── BarChartPanel ────────────────────────────────────────────────────────

    private static final class BarChartPanel extends JPanel {
        private static final class Bar {
            final String name; final int value; final Color color;
            Bar(String n, int v, Color c) { name=n; value=v; color=c; }
        }

        private final List<Product> products;
        private List<Bar> bars = new ArrayList<>();
        private float anim = 1f;
        private Timer animTimer;

        BarChartPanel(List<Product> products) {
            this.products = products;
            setBackground(CARD);
        }

        void refresh() {
            bars = products.stream()
                .sorted(Comparator.comparingInt(Product::getQuantity).reversed())
                .limit(7)
                .map(p -> {
                    String t = p.getDescription();
                    Color c = "fruit".equalsIgnoreCase(t)  ? new Color(251, 146, 60)
                            : "legume".equalsIgnoreCase(t) ? new Color(34, 197, 94)
                            : "légume".equalsIgnoreCase(t) ? new Color(34, 197, 94)
                            : new Color(148, 163, 184);
                    return new Bar(p.getName(), p.getQuantity(), c);
                })
                .collect(Collectors.toList());

            anim = 0f;
            if (animTimer != null) animTimer.stop();
            animTimer = new Timer(16, e -> {
                anim = Math.min(anim + 0.035f, 1f);
                repaint();
                if (anim >= 1f) ((Timer)e.getSource()).stop();
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bars.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int padL   = 134;
            int padR   = 52;
            int padTop = 14;
            int barH   = 30;
            int gap    = 12;
            int chartW = getWidth() - padL - padR;
            int maxVal = bars.stream().mapToInt(b -> b.value).max().orElse(1);

            float t = ease(anim);

            for (int i = 0; i < bars.size(); i++) {
                Bar bar = bars.get(i);
                int y  = padTop + i * (barH + gap);
                int bw = (int)(chartW * bar.value / (double)maxVal * t);

                // Nom (tronqué, aligné à droite)
                String name = bar.name.length() > 13 ? bar.name.substring(0, 12) + "…" : bar.name;
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(new Color(55, 65, 81));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(name, padL - fm.stringWidth(name) - 10, y + barH/2 + fm.getAscent()/2 - 2);

                // Piste de fond
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(padL, y, chartW, barH, 6, 6);

                // Barre colorée
                if (bw > 0) {
                    g2.setColor(bar.color);
                    g2.fillRoundRect(padL, y, Math.max(bw, 10), barH, 6, 6);
                }

                // Valeur à droite
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(new Color(55, 65, 81));
                String val = String.valueOf(bar.value);
                g2.drawString(val, padL + chartW + 8, y + barH/2 + g2.getFontMetrics().getAscent()/2 - 2);
            }

            g2.dispose();
        }

        private float ease(float t) { return 1 - (1-t)*(1-t)*(1-t); }
    }
}
