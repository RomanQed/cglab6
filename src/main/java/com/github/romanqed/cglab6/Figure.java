package com.github.romanqed.cglab6;

import java.util.Collections;
import java.util.List;

public class Figure {
    final List<Point> points;

    public Figure(List<Point> points) {
        this.points = Collections.unmodifiableList(points);
    }

    public List<Point> getPoints() {
        return points;
    }
}
