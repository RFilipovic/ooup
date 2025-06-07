public class LineSegment extends AbstractGraphicalObject {
    public LineSegment() {
        super(new Point[]{new Point(100, 100), new Point(200, 150)});
    }

    public LineSegment(Point start, Point end) {
        super(new Point[]{start, end});
    }

    @Override
    public Rectangle getBoundingBox() {
        int x1 = getHotPoint(0).getX();
        int y1 = getHotPoint(0).getY();
        int x2 = getHotPoint(1).getX();
        int y2 = getHotPoint(1).getY();

        return new Rectangle(
                Math.min(x1, x2),
                Math.min(y1, y2),
                Math.abs(x1 - x2),
                Math.abs(y1 - y2)
        );
    }

    @Override
    public double selectionDistance(Point mousePoint) {
        return GeometryUtil.distanceFromLineSegment(
                getHotPoint(0), getHotPoint(1), mousePoint
        );
    }

    @Override
    public String getShapeName() {
        return "Linija";
    }

    @Override
    public GraphicalObject duplicate() {
        return new LineSegment(
                new Point(getHotPoint(0).getX(), getHotPoint(0).getY()),
                new Point(getHotPoint(1).getX(), getHotPoint(1).getY())
        );
    }

    @Override
    public void render(Renderer r) {
        r.drawLine(getHotPoint(0), getHotPoint(1));
    }
}