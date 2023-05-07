package com.github.romanqed.cglab6;

import javafx.scene.paint.Color;

import java.util.Stack;

public final class Drawer {
    private final double width;
    private final double height;
    private Color border;
    private Color fill;

    public Drawer(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public void setBorder(Color border) {
        this.border = border;
    }

    public void setFill(Color fill) {
        this.fill = fill;
    }

    private double fillTo(DrawBuffer buffer,
                          Stack<Point> points,
                          double x,
                          double y,
                          double border) {
        boolean flag = false;
        while (buffer.getPixel(x, y) != this.border &&
                buffer.getPixel(x, y) != this.fill &&
                x <= border) {
            flag = true;
            ++x;
        }
        if (flag) {
            points.push(new Point(x - 1, y));
        }
        return x;
    }

    public void draw(DrawBuffer buffer, Point target) {
        this.draw(buffer, target, () -> {
        });
    }

    public void draw(DrawBuffer buffer, Point target, Runnable onFrame) {
        Stack<Point> points = new Stack<>();
        points.push(target);

        while (!points.isEmpty()) {
            Point point = points.pop();
            double x = point.x, y = point.y;
            double source = x;

            buffer.setPixel(x, y, fill);

            while (buffer.getPixel(x, y) != border && x < width / 2) {
                buffer.setPixel(x++, y, fill);
            }
            double rightBorder = x - 1;

            x = source - 1;
            while (buffer.getPixel(x, y) != border && x > -(width / 2)) {
                buffer.setPixel(x--, y, fill);
            }
            double leftBorder = x + 1;

            onFrame.run();

            x = leftBorder;
            ++y;

            if (y > height / 2) {
                while (x <= rightBorder) {
                    x = fillTo(buffer, points, x, y, rightBorder);
                    do {
                        ++x;
                    } while ((buffer.getPixel(x, y) == border || buffer.getPixel(x, y) == fill) && x < rightBorder);
                }
            }

            x = leftBorder;
            y -= 2;
            if (y > -height / 2) {
                while (x <= rightBorder) {
                    x = fillTo(buffer, points, x, y, rightBorder);
                    do {
                        ++x;
                    } while ((buffer.getPixel(x, y) == border || buffer.getPixel(x, y) == fill) && x < rightBorder);
                }
            }
        }
    }
}
