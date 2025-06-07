public class AddShapeState implements State {

    private GraphicalObject prototype;
    private DocumentModel model;

    public AddShapeState(DocumentModel model, GraphicalObject prototype) {
        this.model = model;
        this.prototype = prototype;
    }

    @Override
    public void mouseDown(Point mousePoint, boolean shiftDown, boolean ctrlDown) {
        GraphicalObject newObject = prototype.duplicate();

        Rectangle bbox = newObject.getBoundingBox();
        Point currentCenter = new Point(
                bbox.getX() + bbox.getWidth() / 2,
                bbox.getY() + bbox.getHeight() / 2
        );

        Point delta = new Point(
                mousePoint.getX() - currentCenter.getX(),
                mousePoint.getY() - currentCenter.getY()
        );
        newObject.translate(delta);
        model.addGraphicalObject(newObject);
    }

    @Override
    public void mouseUp(Point mousePoint, boolean shiftDown, boolean ctrlDown) {
    }

    @Override
    public void mouseDragged(Point mousePoint) {
    }

    @Override
    public void keyPressed(int keyCode) {
    }

    @Override
    public void afterDraw(Renderer r, GraphicalObject go) {
    }

    @Override
    public void afterDraw(Renderer r) {
    }

    @Override
    public void onLeaving() {
    }
}