package results;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class ChartGenerator {

    public static void main(String[] args) throws Exception {
        Path resultsDir = Paths.get("D:/SWT301_TEST/Results");
        System.out.println("Generating PNG figures using Java AWT Graphics2D...");

        generateFig1(resultsDir.resolve("fig1_detection_rates.png"));
        generateFig2(resultsDir.resolve("fig2_defect_type_breakdown.png"));
        generateFig3(resultsDir.resolve("fig3_hybrid_synergy.png"));
        generateFig4(resultsDir.resolve("fig4_llm_prompt_factors.png"));
        generateFig5(resultsDir.resolve("fig5_cost_vs_detection.png"));

        System.out.println("All 5 PNG figures generated successfully!");
    }

    private static void setupGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    // FIG 1: Detection Rates across 4 Apps
    private static void generateFig1(Path out) throws Exception {
        int width = 900, height = 500;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        setupGraphics(g);

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Title
        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString("Figure 1: Defect Detection Rate by Method Across 4 Applications", 50, 45);

        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.setColor(new Color(100, 116, 139));
        g.drawString("Benchmark of Human TFD, Pairwise PICT (Plain), and Best Gemini 3.1 Pro LLM Condition", 50, 68);

        // Chart Area
        int x0 = 80, y0 = 100, w = 740, h = 320;
        g.setColor(new Color(241, 245, 249));
        g.fillRect(x0, y0, w, h);

        g.setColor(new Color(203, 213, 225));
        g.drawRect(x0, y0, w, h);

        // Y-axis gridlines (0% to 100%)
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (int p = 0; p <= 100; p += 20) {
            int y = y0 + h - (p * h / 100);
            g.setColor(new Color(226, 232, 240));
            g.drawLine(x0, y, x0 + w, y);
            g.setColor(new Color(71, 85, 105));
            g.drawString(p + "%", x0 - 40, y + 5);
        }

        String[] apps = {"BasicPackage", "BasicPayroll", "EnhancedPackage", "EnhancedPayroll"};
        // Rates: TFD, PICT_plain, Best_LLM
        double[][] rates = {
                {1.00, 0.80, 1.00}, // BasicPackage
                {1.00, 0.80, 1.00}, // BasicPayroll
                {1.00, 0.65, 0.90}, // EnhancedPackage
                {1.00, 0.80, 0.85}  // EnhancedPayroll
        };

        Color[] colors = {
                new Color(37, 99, 235),  // Blue for TFD
                new Color(234, 88, 12),  // Orange for PICT
                new Color(16, 185, 129)  // Green for LLM
        };

        int groupWidth = w / apps.length;
        int barWidth = 35;
        int gap = 12;

        for (int i = 0; i < apps.length; i++) {
            int cx = x0 + i * groupWidth + groupWidth / 2;
            int startX = cx - (3 * barWidth + 2 * gap) / 2;

            for (int m = 0; m < 3; m++) {
                int bx = startX + m * (barWidth + gap);
                int barH = (int) (rates[i][m] * h);
                int by = y0 + h - barH;

                g.setColor(colors[m]);
                g.fillRoundRect(bx, by, barWidth, barH, 4, 4);

                // Label on top
                g.setColor(new Color(30, 41, 59));
                g.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String pctStr = String.format("%.0f%%", rates[i][m] * 100);
                int strW = g.getFontMetrics().stringWidth(pctStr);
                g.drawString(pctStr, bx + (barWidth - strW) / 2, by - 5);
            }

            // X-axis label
            g.setColor(new Color(30, 41, 59));
            g.setFont(new Font("Segoe UI", Font.BOLD, 13));
            int labelW = g.getFontMetrics().stringWidth(apps[i]);
            g.drawString(apps[i], cx - labelW / 2, y0 + h + 25);
        }

        // Legend
        int lx = 280, ly = 465;
        String[] legLabels = {"Human TFD", "Pairwise PICT (Plain)", "Gemini 3.1 Pro (Best)"};
        for (int m = 0; m < 3; m++) {
            g.setColor(colors[m]);
            g.fillRect(lx + m * 160, ly, 16, 16);
            g.setColor(new Color(51, 65, 85));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString(legLabels[m], lx + m * 160 + 24, ly + 13);
        }

        g.dispose();
        ImageIO.write(img, "png", out.toFile());
    }

    // FIG 2: Defect Type Breakdown
    private static void generateFig2(Path out) throws Exception {
        int width = 900, height = 500;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        setupGraphics(g);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString("Figure 2: Pooled Defect Detection Rate by Defect Category (N=60 Defects)", 50, 45);

        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.setColor(new Color(100, 116, 139));
        g.drawString("Breakdown across Boundary (13), Arithmetic (15), Logic (18), and Conditional (14) Seeded Faults", 50, 68);

        int x0 = 80, y0 = 100, w = 740, h = 320;
        g.setColor(new Color(241, 245, 249));
        g.fillRect(x0, y0, w, h);
        g.setColor(new Color(203, 213, 225));
        g.drawRect(x0, y0, w, h);

        for (int p = 0; p <= 100; p += 20) {
            int y = y0 + h - (p * h / 100);
            g.setColor(new Color(226, 232, 240));
            g.drawLine(x0, y, x0 + w, y);
            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString(p + "%", x0 - 40, y + 5);
        }

        String[] categories = {"Boundary (n=13)", "Arithmetic (n=15)", "Logic (n=18)", "Conditional (n=14)"};
        // Detection rates: TFD, PICT_plain, LLM_union
        // Boundary: TFD=13/13(100%), PICT=1/13(7.7%), LLM=13/13(100%)
        // Arithmetic: TFD=15/15(100%), PICT=14/15(93.3%), LLM=15/15(100%)
        // Logic: TFD=18/18(100%), PICT=17/18(94.4%), LLM=18/18(100%)
        // Conditional: TFD=14/14(100%), PICT=13/14(92.9%), LLM=13/14(92.9%)
        double[][] rates = {
                {1.00, 1.0 / 13.0, 1.00},
                {1.00, 14.0 / 15.0, 1.00},
                {1.00, 17.0 / 18.0, 1.00},
                {1.00, 13.0 / 14.0, 13.0 / 14.0}
        };

        Color[] colors = {
                new Color(37, 99, 235),  // TFD Blue
                new Color(234, 88, 12),  // PICT Orange
                new Color(139, 92, 246)  // LLM Purple (Union)
        };

        int groupWidth = w / categories.length;
        int barWidth = 35;
        int gap = 12;

        for (int i = 0; i < categories.length; i++) {
            int cx = x0 + i * groupWidth + groupWidth / 2;
            int startX = cx - (3 * barWidth + 2 * gap) / 2;

            for (int m = 0; m < 3; m++) {
                int bx = startX + m * (barWidth + gap);
                int barH = (int) (rates[i][m] * h);
                int by = y0 + h - barH;

                g.setColor(colors[m]);
                g.fillRoundRect(bx, by, barWidth, barH, 4, 4);

                g.setColor(new Color(30, 41, 59));
                g.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String pctStr = String.format("%.1f%%", rates[i][m] * 100);
                int strW = g.getFontMetrics().stringWidth(pctStr);
                g.drawString(pctStr, bx + (barWidth - strW) / 2, by - 5);
            }

            g.setColor(new Color(30, 41, 59));
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            int labelW = g.getFontMetrics().stringWidth(categories[i]);
            g.drawString(categories[i], cx - labelW / 2, y0 + h + 25);
        }

        // Legend
        int lx = 260, ly = 465;
        String[] legLabels = {"Human TFD (100%)", "Pairwise PICT (Plain)", "Gemini LLM (Union C1-C6)"};
        for (int m = 0; m < 3; m++) {
            g.setColor(colors[m]);
            g.fillRect(lx + m * 170, ly, 16, 16);
            g.setColor(new Color(51, 65, 85));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString(legLabels[m], lx + m * 170 + 24, ly + 13);
        }

        g.dispose();
        ImageIO.write(img, "png", out.toFile());
    }

    // FIG 3: Hybrid Synergy (Enhanced Apps)
    private static void generateFig3(Path out) throws Exception {
        int width = 900, height = 500;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        setupGraphics(g);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString("Figure 3: Hybrid Method Synergy in Enhanced Applications", 50, 45);

        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.setColor(new Color(100, 116, 139));
        g.drawString("Defect Counts and Synergy Gains for Standalone Suites vs Combined Hybrid Unions", 50, 68);

        int x0 = 80, y0 = 100, w = 740, h = 320;
        g.setColor(new Color(241, 245, 249));
        g.fillRect(x0, y0, w, h);
        g.setColor(new Color(203, 213, 225));
        g.drawRect(x0, y0, w, h);

        for (int d = 0; d <= 20; d += 4) {
            int y = y0 + h - (d * h / 20);
            g.setColor(new Color(226, 232, 240));
            g.drawLine(x0, y, x0 + w, y);
            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString(d + " / 20", x0 - 45, y + 5);
        }

        String[] series = {"EnhancedPackage (20)", "EnhancedPayroll (20)"};
        String[] bars = {"PICT_plain", "Best LLM", "LLM_Union", "LLM+PICT", "TFD"};

        double[][] counts = {
                {13, 18, 20, 20, 20}, // EnhancedPackage
                {16, 17, 19, 20, 20}  // EnhancedPayroll
        };

        Color[] barColors = {
                new Color(234, 88, 12),  // PICT
                new Color(16, 185, 129),  // Best LLM
                new Color(139, 92, 246), // LLM Union
                new Color(236, 72, 153), // LLM+PICT Hybrid
                new Color(37, 99, 235)   // TFD
        };

        int groupWidth = w / series.length;
        int barW = 45;
        int gap = 12;

        for (int s = 0; s < series.length; s++) {
            int cx = x0 + s * groupWidth + groupWidth / 2;
            int startX = cx - (5 * barW + 4 * gap) / 2;

            for (int b = 0; b < 5; b++) {
                int bx = startX + b * (barW + gap);
                int barH = (int) (counts[s][b] * h / 20.0);
                int by = y0 + h - barH;

                g.setColor(barColors[b]);
                g.fillRoundRect(bx, by, barW, barH, 4, 4);

                g.setColor(new Color(30, 41, 59));
                g.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String cStr = String.format("%.0f", counts[s][b]);
                int strW = g.getFontMetrics().stringWidth(cStr);
                g.drawString(cStr, bx + (barW - strW) / 2, by - 6);
            }

            g.setColor(new Color(30, 41, 59));
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            int labelW = g.getFontMetrics().stringWidth(series[s]);
            g.drawString(series[s], cx - labelW / 2, y0 + h + 25);
        }

        // Legend
        int lx = 140, ly = 465;
        for (int b = 0; b < 5; b++) {
            g.setColor(barColors[b]);
            g.fillRect(lx + b * 130, ly, 14, 14);
            g.setColor(new Color(51, 65, 85));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString(bars[b], lx + b * 130 + 20, ly + 12);
        }

        g.dispose();
        ImageIO.write(img, "png", out.toFile());
    }

    // FIG 4: LLM Prompt Factors (Detail & Shots)
    private static void generateFig4(Path out) throws Exception {
        int width = 900, height = 500;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        setupGraphics(g);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString("Figure 4: LLM Prompt Factors Effect on Defect Detection Rate", 50, 45);

        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.setColor(new Color(100, 116, 139));
        g.drawString("Descriptive Means Across All 4 Applications (24 Independent Gemini 3.1 Pro Suites)", 50, 68);

        int x0 = 80, y0 = 100, w = 740, h = 320;
        g.setColor(new Color(241, 245, 249));
        g.fillRect(x0, y0, w, h);
        g.setColor(new Color(203, 213, 225));
        g.drawRect(x0, y0, w, h);

        // Zoomed in Y-axis from 80% to 100% to clearly show subtle factor differences
        int minY = 80, maxY = 100;
        for (int p = minY; p <= maxY; p += 5) {
            int y = y0 + h - ((p - minY) * h / (maxY - minY));
            g.setColor(new Color(226, 232, 240));
            g.drawLine(x0, y, x0 + w, y);
            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString(p + "%", x0 - 40, y + 5);
        }

        String[] groups = {"Detail Level (N=12 each)", "Example Shots (N=8 each)"};
        String[][] labels = {
                {"Low Detail (C1-C3)", "High Detail (C4-C6)"},
                {"0-shot (C1,C4)", "1-shot (C2,C5)", "3-shot (C3,C6)"}
        };
        double[][] values = {
                {93.75, 92.08},
                {93.13, 93.13, 92.50}
        };

        Color[] col1 = {new Color(14, 165, 233), new Color(2, 132, 199)};
        Color[] col2 = {new Color(168, 85, 247), new Color(147, 51, 234), new Color(126, 34, 206)};

        // Draw left group (Detail Level)
        int cx1 = x0 + w / 4;
        int bW = 60, gap = 20;
        int sx1 = cx1 - (2 * bW + gap) / 2;
        for (int i = 0; i < 2; i++) {
            int bx = sx1 + i * (bW + gap);
            int barH = (int) ((values[0][i] - minY) * h / (maxY - minY));
            int by = y0 + h - barH;
            g.setColor(col1[i]);
            g.fillRoundRect(bx, by, bW, barH, 4, 4);

            g.setColor(new Color(30, 41, 59));
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String valStr = String.format("%.2f%%", values[0][i]);
            int strW = g.getFontMetrics().stringWidth(valStr);
            g.drawString(valStr, bx + (bW - strW) / 2, by - 6);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            int lW = g.getFontMetrics().stringWidth(labels[0][i]);
            g.drawString(labels[0][i], bx + (bW - lW) / 2, y0 + h + 18);
        }
        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        int g1W = g.getFontMetrics().stringWidth(groups[0]);
        g.drawString(groups[0], cx1 - g1W / 2, y0 + h + 40);

        // Draw right group (Shots)
        int cx2 = x0 + 3 * w / 4;
        int sx2 = cx2 - (3 * bW + 2 * gap) / 2;
        for (int i = 0; i < 3; i++) {
            int bx = sx2 + i * (bW + gap);
            int barH = (int) ((values[1][i] - minY) * h / (maxY - minY));
            int by = y0 + h - barH;
            g.setColor(col2[i]);
            g.fillRoundRect(bx, by, bW, barH, 4, 4);

            g.setColor(new Color(30, 41, 59));
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String valStr = String.format("%.2f%%", values[1][i]);
            int strW = g.getFontMetrics().stringWidth(valStr);
            g.drawString(valStr, bx + (bW - strW) / 2, by - 6);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            int lW = g.getFontMetrics().stringWidth(labels[1][i]);
            g.drawString(labels[1][i], bx + (bW - lW) / 2, y0 + h + 18);
        }
        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        int g2W = g.getFontMetrics().stringWidth(groups[1]);
        g.drawString(groups[1], cx2 - g2W / 2, y0 + h + 40);

        g.dispose();
        ImageIO.write(img, "png", out.toFile());
    }

    // FIG 5: Cost (Time) vs Defect Detection
    private static void generateFig5(Path out) throws Exception {
        int width = 900, height = 500;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        setupGraphics(g);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString("Figure 5: Total Creation Time vs Seeded Defect Detection", 50, 45);

        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.setColor(new Color(100, 116, 139));
        g.drawString("Cost Efficiency Comparison: Human TFD (Self-Reported) vs Automated LLM / PICT", 50, 68);

        int x0 = 100, y0 = 100, w = 700, h = 320;
        g.setColor(new Color(241, 245, 249));
        g.fillRect(x0, y0, w, h);
        g.setColor(new Color(203, 213, 225));
        g.drawRect(x0, y0, w, h);

        // X-axis: Time (minutes) from 0 to 300
        // Y-axis: Defect Detection Rate from 50% to 100%
        int minX = 0, maxX = 300;
        int minY = 60, maxY = 105;

        // X grid
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int xVal = 0; xVal <= 300; xVal += 50) {
            int x = x0 + (xVal - minX) * w / (maxX - minX);
            g.setColor(new Color(226, 232, 240));
            g.drawLine(x, y0, x, y0 + h);
            g.setColor(new Color(71, 85, 105));
            g.drawString(xVal + " min", x - 18, y0 + h + 18);
        }

        // Y grid
        for (int yVal = 60; yVal <= 100; yVal += 10) {
            int y = y0 + h - (yVal - minY) * h / (maxY - minY);
            g.setColor(new Color(226, 232, 240));
            g.drawLine(x0, y, x0 + w, y);
            g.setColor(new Color(71, 85, 105));
            g.drawString(yVal + "%", x0 - 40, y + 5);
        }

        // Data points (Total Time Across 4 Apps, Overall Pooled Detection Rate)
        // 1. TFD: 280 minutes, 60/60 (100.0%)
        // 2. PICT_plain: <0.1 minutes, 45/60 (75.0%)
        // 3. Best LLM (single run per app): ~3.3 minutes total, 55/60 (91.7%)
        // 4. LLM Union (all 6 runs per app): 19.69 minutes total, 59/60 (98.3%)

        class Point {
            String name;
            double timeMin;
            double ratePct;
            Color col;
            String note;
            int alignX, alignY;

            Point(String name, double timeMin, double ratePct, Color col, String note, int alignX, int alignY) {
                this.name = name;
                this.timeMin = timeMin;
                this.ratePct = ratePct;
                this.col = col;
                this.note = note;
                this.alignX = alignX;
                this.alignY = alignY;
            }
        }

        List<Point> pts = new ArrayList<>();
        pts.add(new Point("Pairwise PICT", 0.1, 75.0, new Color(234, 88, 12), "<0.1 min (Automated), 75.0% (45/60)", 15, 5));
        pts.add(new Point("Gemini Single Suite", 3.3, 91.7, new Color(16, 185, 129), "3.3 min, 91.7% (55/60)", 15, -10));
        pts.add(new Point("Gemini LLM Union", 19.69, 98.3, new Color(139, 92, 246), "19.7 min (All 6 conditions), 98.3% (59/60)", 15, -12));
        pts.add(new Point("Human TFD", 280.0, 100.0, new Color(37, 99, 235), "280 min (Self-reported), 100% (60/60)", -230, -10));

        for (Point p : pts) {
            int px = x0 + (int) ((p.timeMin - minX) * w / (maxX - minX));
            int py = y0 + h - (int) ((p.ratePct - minY) * h / (maxY - minY));

            g.setColor(p.col);
            g.fillOval(px - 8, py - 8, 16, 16);
            g.setColor(Color.WHITE);
            g.fillOval(px - 4, py - 4, 8, 8);

            // Text
            g.setColor(new Color(30, 41, 59));
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString(p.name, px + p.alignX, py + p.alignY);

            g.setColor(new Color(100, 116, 139));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString(p.note, px + p.alignX, py + p.alignY + 15);
        }

        // Axis Titles
        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.drawString("Creation Time (Minutes)", x0 + w / 2 - 60, y0 + h + 42);

        g.dispose();
        ImageIO.write(img, "png", out.toFile());
    }
}

