package com.github.romanqed.cglab6;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


public class MainController implements Initializable {
    private final ActionPool pool = new ActionPool();

    @FXML
    protected Canvas canvas;

    @FXML
    protected ColorPicker borderPicker;

    @FXML
    protected ColorPicker fillPicker;

    @FXML
    protected TextField x;

    @FXML
    protected TextField y;
    private Point point;
    private Drawer drawer;
    private GraphicsContext context;
    private Set<Figure> figures;
    private List<Point> points;

    private void commit(Figure figure) {
        this.pool.add(new ActionImpl(figure));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.borderPicker.setValue(Color.BLACK);
        this.fillPicker.setValue(Color.RED);
        this.context = canvas.getGraphicsContext2D();
        this.figures = new HashSet<>();
        this.points = new ArrayList<>();
        this.drawer = new Drawer(canvas.getWidth(), canvas.getHeight());
        this.refresh();
    }

    private void clear() {
        context.setFill(Color.WHITE);
        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void refresh() {
        DrawBuffer buffer = new DrawBuffer();
        this.refresh(buffer);
        buffer.flush(context);
    }

    private void refresh(DrawBuffer buffer) {
        clear();
        context.setLineWidth(4);
        if (point != null) {
            drawPoint(point, fillPicker.getValue());
        }
        Color border = borderPicker.getValue();
        points.forEach(p -> drawPoint(p, border));
        context.setLineWidth(1);
        drawFigures(buffer, border);
    }

    private <T> T parse(TextField textField, Function<String, T> parser) {
        try {
            return parser.apply(textField.getText());
        } catch (Throwable e) {
            return null;
        }
    }

    private Point getPoint(TextField xField, TextField yField) {
        Double x = parse(xField, Double::parseDouble);
        Double y = parse(yField, Double::parseDouble);
        if (x == null || y == null) {
            return null;
        }
        return new Point(x, y);
    }

    private void drawFigures(DrawBuffer buffer, Color color) {
        for (Figure figure : figures) {
            List<Point> points = figure.points;
            int size = points.size();
            for (int i = 0; i < size - 1; ++i) {
                buffer.drawLine(points.get(i), points.get(i + 1), color);
            }
            buffer.drawLine(points.get(size - 1), points.get(0), color);
        }
    }

    @FXML
    protected void onFigureAdd() {
        int size = points.size();
        if (size < 3) {
            Util.showError("Ошибка", "Для замыкания фигуры необходимо не менее 3 точек");
            return;
        }
        commit(new Figure(points));
        this.points = new ArrayList<>();
    }

    @FXML
    protected void onSetDrawPoint() {
        Point toSet = getPoint(x, y);
        if (toSet == null) {
            Util.showError("Ошибка", "Введенные координаты не являются числом");
            return;
        }
        this.point = toSet;
        refresh();
    }

    private DrawBuffer prepareDraw() {
        if (figures.isEmpty()) {
            Util.showInfo("Пустой холст", "Добавьте сначала фигуры");
            return null;
        }
        if (point == null) {
            Util.showInfo("Нет точки", "Не задана точка затравки");
            return null;
        }
        DrawBuffer buffer = new DrawBuffer();
        this.refresh(buffer);
        drawer.setFill(fillPicker.getValue());
        drawer.setBorder(borderPicker.getValue());
        return buffer;
    }

    @FXML
    protected void onDelayedFill() {
        DrawBuffer buffer = prepareDraw();
        if (buffer == null) {
            return;
        }
        List<DrawBuffer> stages = new LinkedList<>();
        drawer.draw(buffer, point, () -> stages.add(buffer.copy()));
        Timeline timeline = new Timeline();

        AtomicInteger count = new AtomicInteger(0);
        KeyFrame keyFrame = new KeyFrame(
                Duration.millis(50),
                event -> Platform.runLater(() -> {
                    if (count.get() == stages.size()) {
                        timeline.stop();
                        return;
                    }
                    clear();
                    DrawBuffer toDraw = stages.get(count.getAndIncrement());
                    toDraw.flush(context);
                }));
        timeline.setCycleCount(Integer.MAX_VALUE);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    @FXML
    protected void onFill() {
        DrawBuffer buffer = prepareDraw();
        if (buffer == null) {
            return;
        }
        long time = Util.calculateTime(() -> {
            drawer.draw(buffer, point);
        });
        buffer.flush(context);
        Util.showInfo("Время работы", "Время работы алгоритма составило: " + time + " ns");
    }

    @FXML
    protected void onExitAction() {
        System.exit(0);
    }

    @FXML
    protected void onAboutAction() throws IOException {
        Util.showInfo("О программе", Util.readResourceFile("about.txt"));
    }

    @FXML
    protected void onAuthorAction() {
        Util.showInfo("Автор", "Бакалдин Роман ИУ7-45Б");
    }

    @FXML
    protected void onCancelAction() {
        this.pool.undo();
    }

    @FXML
    protected void onRedoAction() {
        this.pool.redo();
    }

    @FXML
    protected void onResetAction() {
        this.pool.clear();
        this.points.clear();
        this.figures.clear();
        this.point = null;
        this.refresh();
    }

    private void drawPoint(Point point, Color color) {
        context.setStroke(color);
        context.strokeLine(point.x, point.y, point.x, point.y);
    }

    @FXML
    protected void onAdd() {
        Point toAdd = getPoint(x, y);
        if (toAdd == null) {
            Util.showError("Ошибка", "Введенные координаты точки не являются числом");
            return;
        }
        points.add(toAdd);
        refresh();
    }

    @FXML
    protected void onRemove() {
        int size = points.size();
        if (size == 0) {
            return;
        }
        points.remove(size - 1);
        refresh();
    }

    @FXML
    protected void onCanvasMouseClicked(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        MouseButton button = event.getButton();
        if (button == MouseButton.PRIMARY) {
            points.add(new Point(x, y));
            refresh();
        } else if (button == MouseButton.MIDDLE) {
            point = new Point(x, y);
            refresh();
        } else {
            this.x.setText(Double.toString(x));
            this.y.setText(Double.toString(y));
        }
    }

    private final class ActionImpl implements Action {
        private final Figure backup;

        public ActionImpl(Figure backup) {
            this.backup = backup;
        }

        @Override
        public void perform() {
            figures.add(backup);
            refresh();
        }

        @Override
        public void undo() {
            figures.remove(backup);
            refresh();
        }
    }
}
