import java.util.ArrayList;
import java.util.List;

public class EraserState implements State {

    private DocumentModel model;
    private List<Point> eraserPath;
    private boolean isErasing;

    public EraserState(DocumentModel model) {
        this.model = model;
        this.eraserPath = new ArrayList<>();
        this.isErasing = false;
    }

    @Override
    public void mouseDown(Point mousePoint, boolean shiftDown, boolean ctrlDown) {
        eraserPath.clear();
        eraserPath.add(mousePoint);
        isErasing = true;
    }

    @Override
    public void mouseUp(Point mousePoint, boolean shiftDown, boolean ctrlDown) {
        if (isErasing && eraserPath.size() > 1) {
            performErasure();
        }

        eraserPath.clear();
        isErasing = false;
        model.notifyListeners();
    }

    @Override
    public void mouseDragged(Point mousePoint) {
        if (isErasing) {
            eraserPath.add(mousePoint);
            model.notifyListeners();
        }
    }

    private void performErasure() {
        List<GraphicalObject> objectsToRemove = new ArrayList<>();

        for (GraphicalObject obj : model.list()) {
            if (doesEraserPathIntersectObject(obj)) {
                objectsToRemove.add(obj);
            }
        }

        for (GraphicalObject obj : objectsToRemove) {
            model.removeGraphicalObject(obj);
        }
    }

    private boolean doesEraserPathIntersectObject(GraphicalObject obj) {
        Rectangle bbox = obj.getBoundingBox();

        for (int i = 0; i < eraserPath.size() - 1; i++) {
            Point p1 = eraserPath.get(i);
            Point p2 = eraserPath.get(i + 1);

            if (lineIntersectsRectangle(p1, p2, bbox)) {
                return true;
            }
        }

        for (Point point : eraserPath) {
            if (obj.selectionDistance(point) <= 5) {
                return true;
            }
        }

        return false;
    }

    private boolean lineIntersectsRectangle(Point p1, Point p2, Rectangle rect) {
        Point topLeft = new Point(rect.getX(), rect.getY());
        Point topRight = new Point(rect.getX() + rect.getWidth(), rect.getY());
        Point bottomLeft = new Point(rect.getX(), rect.getY() + rect.getHeight());
        Point bottomRight = new Point(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());

        return lineSegmentsIntersect(p1, p2, topLeft, topRight) ||
                lineSegmentsIntersect(p1, p2, topRight, bottomRight) ||
                lineSegmentsIntersect(p1, p2, bottomRight, bottomLeft) ||
                lineSegmentsIntersect(p1, p2, bottomLeft, topLeft);
    }

    private boolean lineSegmentsIntersect(Point p1, Point q1, Point p2, Point q2) {
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        if (o1 != o2 && o3 != o4) return true;

        if (o1 == 0 && onSegment(p1, p2, q1)) return true;
        if (o2 == 0 && onSegment(p1, q2, q1)) return true;
        if (o3 == 0 && onSegment(p2, p1, q2)) return true;
        if (o4 == 0 && onSegment(p2, q1, q2)) return true;

        return false;
    }

    private int orientation(Point p, Point q, Point r) {
        int val = (q.getY() - p.getY()) * (r.getX() - q.getX()) -
                (q.getX() - p.getX()) * (r.getY() - q.getY());
        if (val == 0) return 0;
        return (val > 0) ? 1 : 2;
    }

    private boolean onSegment(Point p, Point q, Point r) {
        return q.getX() <= Math.max(p.getX(), r.getX()) &&
                q.getX() >= Math.min(p.getX(), r.getX()) &&
                q.getY() <= Math.max(p.getY(), r.getY()) &&
                q.getY() >= Math.min(p.getY(), r.getY());
    }

    @Override
    public void keyPressed(int keyCode) {
    }

    @Override
    public void afterDraw(Renderer r, GraphicalObject go) {
    }

    @Override
    public void afterDraw(Renderer r) {
        if (isErasing && eraserPath.size() > 1) {
            for (int i = 0; i < eraserPath.size() - 1; i++) {
                r.drawLine(eraserPath.get(i), eraserPath.get(i + 1));
            }
        }
    }

    @Override
    public void onLeaving() {
        eraserPath.clear();
        isErasing = false;
    }
}