import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class SelectShapeState implements State {

    private DocumentModel model;
    private Point lastMousePoint;
    private GraphicalObject selectedObject;
    private int selectedHotPointIndex = -1;
    private boolean isDragging = false;

    private Point selectionStart = null;
    private Point selectionEnd = null;
    private boolean isRectangleSelection = false;

    public SelectShapeState(DocumentModel model) {
        this.model = model;
    }


    @Override
    public void mouseDown(Point mousePoint, boolean shiftDown, boolean ctrlDown) {
        lastMousePoint = mousePoint;
        isDragging = false;

        GraphicalObject clickedObject = null;
        List<GraphicalObject> objects = model.list();
        for (int i = objects.size() - 1; i >= 0; i--) {
            GraphicalObject obj = objects.get(i);
            if (obj.selectionDistance(mousePoint) <= 3) {
                clickedObject = obj;
                break;
            }
        }

        if (clickedObject != null) {
            if (ctrlDown) {
                clickedObject.setSelected(!clickedObject.isSelected());
                selectedObject = clickedObject.isSelected() ? clickedObject : null;
            } else if (shiftDown) {
                clickedObject.setSelected(true);
                selectedObject = clickedObject;
            } else {
                if (clickedObject.isSelected()) {
                    selectedObject = clickedObject;

                    for (int i = 0; i < clickedObject.getNumberOfHotPoints(); i++) {
                        Point hotPoint = clickedObject.getHotPoint(i);
                        if (GeometryUtil.distanceFromPoint(hotPoint, mousePoint) <= 3) {
                            selectedHotPointIndex = i;
                            return;
                        }
                    }
                } else {
                    for (GraphicalObject obj : model.list()) {
                        obj.setSelected(false);
                    }
                    clickedObject.setSelected(true);
                    selectedObject = clickedObject;
                }
            }
        } else {
            if (!ctrlDown && !shiftDown) {
                selectionStart = mousePoint;
                isRectangleSelection = true;

                for (GraphicalObject obj : model.list()) {
                    obj.setSelected(false);
                }
            }
        }
    }

    @Override
    public void mouseUp(Point mousePoint, boolean shiftDown, boolean ctrlDown) {
        if (isRectangleSelection && selectionStart != null && selectionEnd != null) {
            performRectangleSelection(ctrlDown);
        }

        // Reset sve
        isDragging = false;
        selectedObject = null;
        selectedHotPointIndex = -1;
        lastMousePoint = null;
        selectionStart = null;
        selectionEnd = null;
        isRectangleSelection = false;

        // Triggeriraj repaint da se ukloni selection rectangle
        model.notifyListeners();
    }

    @Override
    public void mouseDragged(Point mousePoint) {
        if (lastMousePoint == null) return;

        isDragging = true;
        Point delta = new Point(
                mousePoint.getX() - lastMousePoint.getX(),
                mousePoint.getY() - lastMousePoint.getY()
        );

        if (selectedHotPointIndex != -1 && selectedObject != null) {
            // Pomicanje hot-pointa
            Point currentHotPoint = selectedObject.getHotPoint(selectedHotPointIndex);
            Point newHotPoint = new Point(
                    currentHotPoint.getX() + delta.getX(),
                    currentHotPoint.getY() + delta.getY()
            );
            selectedObject.setHotPoint(selectedHotPointIndex, newHotPoint);
        } else if (selectedObject != null && model.getSelectedObjects().contains(selectedObject)) {
            // Pomicanje selektiranih objekata
            for (GraphicalObject obj : model.getSelectedObjects()) {
                obj.translate(delta);
            }
        } else if (selectionStart != null) {
            // Rectangle selection
            selectionEnd = mousePoint;
            isRectangleSelection = true;

            // KLJUČNO: Triggeriraj repaint da se vidi selection rectangle
            model.notifyListeners();
        }

        lastMousePoint = mousePoint;
    }

    private void performRectangleSelection(boolean ctrlDown) {
        if (selectionStart == null || selectionEnd == null) return;

        // Izračunaj selection rectangle
        int minX = Math.min(selectionStart.getX(), selectionEnd.getX());
        int minY = Math.min(selectionStart.getY(), selectionEnd.getY());
        int maxX = Math.max(selectionStart.getX(), selectionEnd.getX());
        int maxY = Math.max(selectionStart.getY(), selectionEnd.getY());

        Rectangle selectionRect = new Rectangle(minX, minY, maxX - minX, maxY - minY);

        // Selektiraj objekte koji se nalaze unutar selection rectangle-a
        for (GraphicalObject obj : model.list()) {
            Rectangle objBbox = obj.getBoundingBox();

            // Provjeri da li je objekt potpuno unutar selection rectangle-a
            boolean isInside = (objBbox.getX() >= selectionRect.getX() &&
                    objBbox.getY() >= selectionRect.getY() &&
                    objBbox.getX() + objBbox.getWidth() <= selectionRect.getX() + selectionRect.getWidth() &&
                    objBbox.getY() + objBbox.getHeight() <= selectionRect.getY() + selectionRect.getHeight());

            if (isInside) {
                if (ctrlDown) {
                    // S CTRL - toggle selekciju
                    obj.setSelected(!obj.isSelected());
                } else {
                    // Bez CTRL - selektiraj objekt
                    obj.setSelected(true);
                }
            }
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        Point delta = null;

        // Kursorske tipke - pomicanje za 1 piksel
        switch (keyCode) {
            case KeyEvent.VK_UP:
                delta = new Point(0, -1);
                break;
            case KeyEvent.VK_DOWN:
                delta = new Point(0, 1);
                break;
            case KeyEvent.VK_LEFT:
                delta = new Point(-1, 0);
                break;
            case KeyEvent.VK_RIGHT:
                delta = new Point(1, 0);
                break;
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_EQUALS: // Za tipkovnice gdje je + na istoj tipki kao =
                // Povećaj Z-order (bliže promatraču)
                for (GraphicalObject obj : model.getSelectedObjects()) {
                    model.increaseZ(obj);
                }
                break;
            case KeyEvent.VK_MINUS:
                // Smanji Z-order (dalje od promatrača)
                for (GraphicalObject obj : model.getSelectedObjects()) {
                    model.decreaseZ(obj);
                }
                break;
            case KeyEvent.VK_G:
                // Grupiranje - stvori kompozit od selektiranih objekata
                groupSelectedObjects();
                break;
            case KeyEvent.VK_U:
                // Ungrouping - razdijeli kompozit
                ungroupSelectedObject();
                break;
        }

        // Pomakni selektirane objekte ako je delta definiran
        if (delta != null) {
            for (GraphicalObject obj : model.getSelectedObjects()) {
                obj.translate(delta);
            }
        }
    }

    private void groupSelectedObjects() {
        List<GraphicalObject> selectedObjects = new ArrayList<>(model.getSelectedObjects());

        if (selectedObjects.size() < 2) {
            return; // Treba barem 2 objekta za grupiranje
        }

        // Ukloni selektirane objekte iz modela
        for (GraphicalObject obj : selectedObjects) {
            obj.setSelected(false); // Deselektiraj objekt
            model.removeGraphicalObject(obj);
        }

        // Stvori kompozit i dodaj ga u model
        CompositeShape composite = new CompositeShape(selectedObjects);
        composite.setSelected(true); // Selektiraj novi kompozit
        model.addGraphicalObject(composite);
    }

    private void ungroupSelectedObject() {
        List<GraphicalObject> selectedObjects = model.getSelectedObjects();

        if (selectedObjects.size() != 1) {
            return; // Mora biti selektiran točno jedan objekt
        }

        GraphicalObject selectedObj = selectedObjects.get(0);
        if (!(selectedObj instanceof CompositeShape)) {
            return; // Mora biti kompozit
        }

        CompositeShape composite = (CompositeShape) selectedObj;
        List<GraphicalObject> children = composite.getChildren();

        // Ukloni kompozit iz modela
        composite.setSelected(false);
        model.removeGraphicalObject(composite);

        // Dodaj djecu kompozita u model i selektiraj ih
        for (GraphicalObject child : children) {
            child.setSelected(true);
            model.addGraphicalObject(child);
        }
    }

    @Override
    public void afterDraw(Renderer r, GraphicalObject go) {
        // Crtanje bounding boxa za selektirane objekte
        if (go.isSelected()) {
            Rectangle bbox = go.getBoundingBox();

            // Crtaj bounding box
            Point[] bboxPoints = {
                    new Point(bbox.getX(), bbox.getY()),
                    new Point(bbox.getX() + bbox.getWidth(), bbox.getY()),
                    new Point(bbox.getX() + bbox.getWidth(), bbox.getY() + bbox.getHeight()),
                    new Point(bbox.getX(), bbox.getY() + bbox.getHeight()),
                    new Point(bbox.getX(), bbox.getY()) // Zatvaranje
            };

            for (int i = 0; i < bboxPoints.length - 1; i++) {
                r.drawLine(bboxPoints[i], bboxPoints[i + 1]);
            }

            // Ako je selektiran samo jedan objekt I nije kompozit, prikaži hot-pointove
            if (model.getSelectedObjects().size() == 1 && !(go instanceof CompositeShape)) {
                for (int i = 0; i < go.getNumberOfHotPoints(); i++) {
                    Point hp = go.getHotPoint(i);

                    // Crtaj hot-point kao mali kvadratić (5x5 piksela)
                    Point[] hotPointSquare = {
                            new Point(hp.getX() - 2, hp.getY() - 2),
                            new Point(hp.getX() + 2, hp.getY() - 2),
                            new Point(hp.getX() + 2, hp.getY() + 2),
                            new Point(hp.getX() - 2, hp.getY() + 2)
                    };

                    r.fillPolygon(hotPointSquare);
                }
            }
        }
    }

    @Override
    public void afterDraw(Renderer r) {
        // Crtanje selection rectangle-a dok ga korisnik povlači
        if (isRectangleSelection && selectionStart != null && selectionEnd != null) {
            int minX = Math.min(selectionStart.getX(), selectionEnd.getX());
            int minY = Math.min(selectionStart.getY(), selectionEnd.getY());
            int maxX = Math.max(selectionStart.getX(), selectionEnd.getX());
            int maxY = Math.max(selectionStart.getY(), selectionEnd.getY());

            // Crtaj selection rectangle - koristimo drawLine za jasniji prikaz
            Point topLeft = new Point(minX, minY);
            Point topRight = new Point(maxX, minY);
            Point bottomRight = new Point(maxX, maxY);
            Point bottomLeft = new Point(minX, maxY);

            // Crtaj sve četiri stranice selection rectangle-a
            r.drawLine(topLeft, topRight);      // Gornja stranica
            r.drawLine(topRight, bottomRight);  // Desna stranica
            r.drawLine(bottomRight, bottomLeft); // Donja stranica
            r.drawLine(bottomLeft, topLeft);    // Lijeva stranica
        }
    }

    @Override
    public void onLeaving() {
        // Deselektiraj sve objekte kada napuštamo ovo stanje
        for (GraphicalObject obj : model.list()) {
            if (obj.isSelected()) {
                obj.setSelected(false);
            }
        }

        // Reset rectangle selection
        selectionStart = null;
        selectionEnd = null;
        isRectangleSelection = false;
    }
}