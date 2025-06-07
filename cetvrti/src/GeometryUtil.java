public class GeometryUtil {
    public static double distanceFromPoint(Point point1, Point point2) {
        int dx = point1.getX() - point2.getX();
        int dy = point1.getY() - point2.getY();
        return Math.sqrt(dx*dx + dy*dy);
    }

    public static double distanceFromLineSegment(Point s, Point e, Point p) {
        double lengthSquared = (e.getX()-s.getX())*(e.getX()-s.getX()) +
                (e.getY()-s.getY())*(e.getY()-s.getY());
        if (lengthSquared == 0) return distanceFromPoint(p, s);

        double t = ((p.getX()-s.getX())*(e.getX()-s.getX()) +
                (p.getY()-s.getY())*(e.getY()-s.getY())) / lengthSquared;

        t = Math.max(0, Math.min(1, t));

        Point projection = new Point(
                (int)(s.getX() + t*(e.getX()-s.getX())),
                (int)(s.getY() + t*(e.getY()-s.getY()))
        );

        return distanceFromPoint(p, projection);
    }
}