import java.util.List;
import java.util.Stack;

public class Oval extends AbstractGraphicalObject {
    public Oval() {
        super(new Point[]{new Point(150, 100), new Point(100, 150)});
    }

    public Oval(Point right, Point bottom) {
        super(new Point[]{right, bottom});
    }

    @Override
    public Rectangle getBoundingBox() {
        Point right = getHotPoint(0);
        Point bottom = getHotPoint(1);
        int centerX = bottom.getX();
        int centerY = right.getY();
        int a = Math.abs(right.getX() - centerX);
        int b = Math.abs(bottom.getY() - centerY);

        return new Rectangle(centerX - a, centerY - b, 2*a, 2*b);
    }

    @Override
    public double selectionDistance(Point mousePoint) {
        Point right = getHotPoint(0);
        Point bottom = getHotPoint(1);
        int centerX = bottom.getX();
        int centerY = right.getY();
        int a = Math.abs(right.getX() - centerX);
        int b = Math.abs(bottom.getY() - centerY);

        double dx = mousePoint.getX() - centerX;
        double dy = mousePoint.getY() - centerY;
        double normalized = (dx*dx)/(a*a) + (dy*dy)/(b*b);

        if (normalized <= 1.0) {
            return 0;
        } else {
            return Math.sqrt(dx*dx + dy*dy) - Math.sqrt(a*a + b*b);
        }
    }

    @Override
    public String getShapeName() {
        return "Oval";
    }

    @Override
    public String getShapeID() {
        return "@OVAL";
    }

    @Override
    public void save(List<String> rows) {
        Point right = getHotPoint(0);
        Point bottom = getHotPoint(1);
        rows.add(String.format("%s %d %d %d %d",
                getShapeID(), right.getX(), right.getY(), bottom.getX(), bottom.getY()));
    }

    @Override
    public void load(Stack<GraphicalObject> stack, String data) {
        String[] parts = data.trim().split("\\s+");
        int x1 = Integer.parseInt(parts[0]);
        int y1 = Integer.parseInt(parts[1]);
        int x2 = Integer.parseInt(parts[2]);
        int y2 = Integer.parseInt(parts[3]);

        Oval newOval = new Oval(new Point(x1, y1), new Point(x2, y2));
        stack.push(newOval);
    }

    @Override
    public GraphicalObject duplicate() {
        return new Oval(
                new Point(getHotPoint(0).getX(), getHotPoint(0).getY()),
                new Point(getHotPoint(1).getX(), getHotPoint(1).getY())
        );
    }

    @Override
    public void render(Renderer r) {
        Point right = getHotPoint(0);
        Point bottom = getHotPoint(1);
        int centerX = bottom.getX();
        int centerY = right.getY();
        int a = Math.abs(right.getX() - centerX);
        int b = Math.abs(bottom.getY() - centerY);

        int points = 64;
        Point[] ovalPoints = new Point[points];
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            int x = centerX + (int)(a * Math.cos(angle));
            int y = centerY + (int)(b * Math.sin(angle));
            ovalPoints[i] = new Point(x, y);
        }

        r.fillPolygon(ovalPoints);
    }
}