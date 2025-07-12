import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hnsw.*;

public class SimpleVisualizer extends JPanel {
    private final List<Point> points = new ArrayList<>();
    private final List<Line> lines = new ArrayList<>();
    private final int scale = 50;

    public void addPoint(int id, int x, int y) {
        while (points.size() <= id)
            points.add(null);
        points.set(id, new Point(x, y));
        repaint();
    }

    public void addLine(int from, int to) {
        lines.add(new Line(from, to));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);
        int w = getWidth();
        int h = getHeight();

        g.setColor(Color.WHITE);

        // Axes
        g.drawLine(0, h - scale * 0, w, h - scale * 0); // X-axis
        g.drawLine(scale * 0, 0, scale * 0, h); // Y-axis

        // Axis labels
        for (int i = 1; i < w / scale; i++) {
            g.drawString(String.valueOf(i), i * scale + 2, h - 2);
        }
        for (int i = 1; i < h / scale; i++) {
            g.drawString(String.valueOf(i), 2, h - i * scale - 2);
        }

        // Draw lines
        g.setColor(Color.WHITE);
        for (Line l : lines) {
            Point p1 = points.get(l.from);
            Point p2 = points.get(l.to);
            if (p1 != null && p2 != null) {
                int x1 = p1.x * scale;
                int y1 = h - p1.y * scale;
                int x2 = p2.x * scale;
                int y2 = h - p2.y * scale;
                g.drawLine(x1, y1, x2, y2);
            }
        }

        // Draw points
        g.setColor(Color.WHITE);
        for (Point p : points) {
            if (p != null) {
                int px = p.x * scale;
                int py = h - p.y * scale;
                g.fillOval(px - 5, py - 5, 10, 10);
            }
        }
    }

    private static class Line {
        int from, to;

        Line(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("HNSW Visualizer");
        SimpleVisualizer panel = new SimpleVisualizer();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.add(panel);
        frame.setVisible(true);

        HNSW obj = new HNSW();

        Vector[] vectors = {
                new Vector(1, 2), new Vector(2, 3), new Vector(3, 1), new Vector(4, 5), new Vector(5, 2),
                new Vector(6, 6), new Vector(7, 4), new Vector(8, 3), new Vector(9, 7), new Vector(2, 8),
                new Vector(3, 6), new Vector(4, 2), new Vector(5, 8), new Vector(6, 1), new Vector(7, 9),
                new Vector(8, 5), new Vector(9, 2), new Vector(3, 9), new Vector(1, 7), new Vector(6, 3)
        };

        // List of animation steps
        List<Runnable> steps = new ArrayList<>();

        for (Vector v : vectors) {
            obj.insertNode(v);

            // Capture current point
            steps.add(() -> panel.addPoint(v.index, (int) v.x, (int) v.y));

            // Capture connections from the graph at level 0
            Map<Integer, Set<Integer>> level0 = obj.graph.get(0);
            int from = v.index;
            Set<Integer> connections = level0.get(from);

            if (connections != null) {
                for (int to : connections) {
                    int f = from, t = to; // effectively final for lambda
                    steps.add(() -> panel.addLine(f, t));
                }
            }
        }

        obj.printGraphRelations();
        // Run steps one by one
        Timer timer = new Timer(500, null);
        final int[] i = { 0 };
        timer.addActionListener(e -> {
            if (i[0] < steps.size()) {
                steps.get(i[0]).run();
                i[0]++;
            } else {
                timer.stop();
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

}
