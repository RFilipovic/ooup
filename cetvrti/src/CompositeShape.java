import java.util.ArrayList;
import java.util.List;

public class CompositeShape implements GraphicalObject {

    private List<GraphicalObject> children;
    private boolean selected;
    private List<GraphicalObjectListener> listeners = new ArrayList<>();

    public CompositeShape(List<GraphicalObject> children) {
        this.children = new ArrayList<>(children);
        this.selected = false;

        // Dodaj listener na svu djecu
        for (GraphicalObject child : this.children) {
            child.addGraphicalObjectListener(new GraphicalObjectListener() {
                @Override
                public void graphicalObjectChanged(GraphicalObject go) {
                    notifyListeners();
                }

                @Override
                public void graphicalObjectSelectionChanged(GraphicalObject go) {
                    // Djeca kompozita ne mogu biti selektirana individualno
                    // Deselekcija djeteta se može dogoditi kad se kompozit deselektira
                }
            });
        }
    }

    public List<GraphicalObject> getChildren() {
        return new ArrayList<>(children);
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
        // Kad se kompozit deselektira, deselektiraj i svu djecu
        if (!selected) {
            for (GraphicalObject child : children) {
                child.setSelected(false);
            }
        }
        notifySelectionListeners();
    }

    @Override
    public int getNumberOfHotPoints() {
        return 0; // Kompozit nema hot-pointove
    }

    @Override
    public Point getHotPoint(int index) {
        throw new UnsupportedOperationException("CompositeShape has no hot points");
    }

    @Override
    public void setHotPoint(int index, Point point) {
        throw new UnsupportedOperationException("CompositeShape has no hot points");
    }

    @Override
    public boolean isHotPointSelected(int index) {
        return false; // Nema hot-pointova
    }

    @Override
    public void setHotPointSelected(int index, boolean selected) {
        throw new UnsupportedOperationException("CompositeShape has no hot points");
    }

    @Override
    public double getHotPointDistance(int index, Point mousePoint) {
        return Double.MAX_VALUE; // Nema hot-pointova
    }

    @Override
    public void translate(Point delta) {
        // Delegiranje - pomakni svu djecu
        for (GraphicalObject child : children) {
            child.translate(delta);
        }
        notifyListeners();
    }

    @Override
    public Rectangle getBoundingBox() {
        if (children.isEmpty()) {
            return new Rectangle(0, 0, 0, 0);
        }

        // Unija svih bounding boxova djece
        Rectangle firstBbox = children.get(0).getBoundingBox();
        int minX = firstBbox.getX();
        int minY = firstBbox.getY();
        int maxX = minX + firstBbox.getWidth();
        int maxY = minY + firstBbox.getHeight();

        for (int i = 1; i < children.size(); i++) {
            Rectangle bbox = children.get(i).getBoundingBox();
            minX = Math.min(minX, bbox.getX());
            minY = Math.min(minY, bbox.getY());
            maxX = Math.max(maxX, bbox.getX() + bbox.getWidth());
            maxY = Math.max(maxY, bbox.getY() + bbox.getHeight());
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public double selectionDistance(Point mousePoint) {
        // Delegiranje - vrati minimalnu udaljenost od bilo kojeg djeteta
        double minDistance = Double.MAX_VALUE;
        for (GraphicalObject child : children) {
            double distance = child.selectionDistance(mousePoint);
            minDistance = Math.min(minDistance, distance);
        }
        return minDistance;
    }

    @Override
    public void render(Renderer r) {
        // Delegiranje - renderuj svu djecu
        for (GraphicalObject child : children) {
            child.render(r);
        }
    }

    @Override
    public void addGraphicalObjectListener(GraphicalObjectListener l) {
        listeners.add(l);
    }

    @Override
    public void removeGraphicalObjectListener(GraphicalObjectListener l) {
        listeners.remove(l);
    }

    @Override
    public String getShapeName() {
        return "Composite (" + children.size() + " objects)";
    }

    @Override
    public GraphicalObject duplicate() {
        List<GraphicalObject> duplicatedChildren = new ArrayList<>();
        for (GraphicalObject child : children) {
            duplicatedChildren.add(child.duplicate());
        }
        return new CompositeShape(duplicatedChildren);
    }

    private void notifyListeners() {
        for (GraphicalObjectListener l : listeners) {
            l.graphicalObjectChanged(this);
        }
    }

    private void notifySelectionListeners() {
        for (GraphicalObjectListener l : listeners) {
            l.graphicalObjectSelectionChanged(this);
        }
    }
}