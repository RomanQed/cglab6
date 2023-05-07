package com.github.romanqed.cglab6;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class DrawBuffer {
    private final Map<Point, Color> pixels;

    public DrawBuffer() {
        this.pixels = new HashMap<>();
    }

    private DrawBuffer(Map<Point, Color> pixels) {
        this.pixels = pixels;
    }

    private static int round(double n) {
        if (n - (int) n < 0.5) {
            return (int) n;
        }
        return (int) (n + 1);
    }

    public Color getPixel(Point point, Color def) {
        return this.pixels.putIfAbsent(point, def);
    }

    public Color getPixel(double x, double y) {
        return getPixel(new Point(x, y));
    }

    public Color getPixel(Point point) {
        return getPixel(point, Color.WHITE);
    }

    public void setPixel(Point point, Color color) {
        this.pixels.put(point, color);
    }

    public void setPixel(double x, double y, Color color) {
        this.setPixel(new Point(x, y), color);
    }

    public DrawBuffer copy() {
        return new DrawBuffer(new HashMap<>(pixels));
    }

    public void drawLine(Point start, Point end, Color color) {
        // Calculate dx and dy
        double dx = end.x - start.x;
        double dy = end.y - start.y;

        // If dx > dy we will take step as dx
        // else we will take step as dy to draw the complete
        // line
        double step = Math.max(Math.abs(dx), Math.abs(dy));

        // Calculate x-increment and y-increment for each step
        double x_incr = dx / step;
        double y_incr = dy / step;

        // Take the initial points as x and y
        double x = start.x;
        double y = start.y;

        for (int i = 0; i < step; ++i) {
            setPixel(new Point(round(x), round(y)), color);
            x += x_incr;
            y += y_incr;
        }
    }

    public void flush(GraphicsContext context) {
        for (Map.Entry<Point, Color> pixel : pixels.entrySet()) {
            context.setStroke(pixel.getValue());
            Point point = pixel.getKey();
            context.strokeLine(point.x, point.y, point.x, point.y);
        }
        pixels.clear();
    }
}
