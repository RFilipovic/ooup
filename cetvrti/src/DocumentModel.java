import java.util.*;
import java.util.List;

public class DocumentModel {
    public final static double SELECTION_PROXIMITY = 10;

    private List<GraphicalObject> objects = new ArrayList<>();
    private List<GraphicalObject> roObjects = Collections.unmodifiableList(objects);
    private List<DocumentModelListener> listeners = new ArrayList<>();
    private List<GraphicalObject> selectedObjects = new ArrayList<>();
    private List<GraphicalObject> roSelectedObjects = Collections.unmodifiableList(selectedObjects);

    private final GraphicalObjectListener goListener = new GraphicalObjectListener() {
        @Override
        public void graphicalObjectChanged(GraphicalObject go) {
            notifyListeners();
        }

        @Override
        public void graphicalObjectSelectionChanged(GraphicalObject go) {
            if (go.isSelected() && !selectedObjects.contains(go)) {
                selectedObjects.add(go);
            } else if (!go.isSelected()) {
                selectedObjects.remove(go);
            }
            notifyListeners();
        }
    };

    public DocumentModel() {
    }

    public void clear() {
        for (GraphicalObject obj : objects) {
            obj.removeGraphicalObjectListener(goListener);
        }
        objects.clear();
        selectedObjects.clear();
        notifyListeners();
    }

    public void addGraphicalObject(GraphicalObject obj) {
        objects.add(obj);
        obj.addGraphicalObjectListener(goListener);
        if (obj.isSelected()) {
            selectedObjects.add(obj);
        }
        notifyListeners();
    }

    public void removeGraphicalObject(GraphicalObject obj) {
        obj.removeGraphicalObjectListener(goListener);
        objects.remove(obj);
        selectedObjects.remove(obj);
        notifyListeners();
    }

    public List<GraphicalObject> list() {
        return roObjects;
    }

    public void addDocumentModelListener(DocumentModelListener l) {
        listeners.add(l);
    }

    public void removeDocumentModelListener(DocumentModelListener l) {
        listeners.remove(l);
    }

    public void notifyListeners() {
        for (DocumentModelListener l : listeners) {
            l.documentChange();
        }
    }

    public List<GraphicalObject> getSelectedObjects() {
        return roSelectedObjects;
    }

    public void increaseZ(GraphicalObject go) {
        int index = objects.indexOf(go);
        if (index < objects.size() - 1) {
            Collections.swap(objects, index, index + 1);
            notifyListeners();
        }
    }

    public void decreaseZ(GraphicalObject go) {
        int index = objects.indexOf(go);
        if (index > 0) {
            Collections.swap(objects, index, index - 1);
            notifyListeners();
        }
    }

    public GraphicalObject findSelectedGraphicalObject(Point mousePoint) {
        GraphicalObject selected = null;
        double minDistance = SELECTION_PROXIMITY;

        for (GraphicalObject obj : objects) {
            double distance = obj.selectionDistance(mousePoint);
            if (distance < minDistance) {
                minDistance = distance;
                selected = obj;
            }
        }

        return selected;
    }

    public int findSelectedHotPoint(GraphicalObject object, Point mousePoint) {
        int selectedIndex = -1;
        double minDistance = SELECTION_PROXIMITY;

        for (int i = 0; i < object.getNumberOfHotPoints(); i++) {
            double distance = object.getHotPointDistance(i, mousePoint);
            if (distance < minDistance) {
                minDistance = distance;
                selectedIndex = i;
            }
        }

        return selectedIndex;
    }
}