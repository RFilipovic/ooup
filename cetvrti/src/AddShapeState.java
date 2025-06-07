public class AddShapeState implements State {

    private GraphicalObject prototype;
    private DocumentModel model;

    public AddShapeState(DocumentModel model, GraphicalObject prototype) {
        this.model = model;
        this.prototype = prototype;
    }

    @Override
    public void mouseDown(Point mousePoint, boolean shiftDown, boolean ctrlDown) {
        // dupliciraj zapamćeni prototip, pomakni ga na poziciju miša i dodaj u model
        GraphicalObject newObject = prototype.duplicate();

        // Izračunaj pomak potreban da se objekt postavi na poziciju miša
        Rectangle bbox = newObject.getBoundingBox();
        Point currentCenter = new Point(
                bbox.getX() + bbox.getWidth() / 2,
                bbox.getY() + bbox.getHeight() / 2
        );

        Point delta = new Point(
                mousePoint.getX() - currentCenter.getX(),
                mousePoint.getY() - currentCenter.getY()
        );

        // Pomakni objekt na poziciju miša
        newObject.translate(delta);

        // Dodaj objekt u model
        model.addGraphicalObject(newObject);
    }

    @Override
    public void mouseUp(Point mousePoint, boolean shiftDown, boolean ctrlDown) {
        // Prazna implementacija
    }

    @Override
    public void mouseDragged(Point mousePoint) {
        // Prazna implementacija
    }

    @Override
    public void keyPressed(int keyCode) {
        // Prazna implementacija
    }

    @Override
    public void afterDraw(Renderer r, GraphicalObject go) {
        // Prazna implementacija
    }

    @Override
    public void afterDraw(Renderer r) {
        // Prazna implementacija
    }

    @Override
    public void onLeaving() {
        // Prazna implementacija
    }
}