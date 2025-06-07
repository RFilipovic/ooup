import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

public class GUI extends JFrame {
    private DocumentModel model;
    private JComponent canvas;
    private State currentState;

    public GUI(List<GraphicalObject> objects) {
        setTitle("Vector Graphics Editor");
        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        model = new DocumentModel();
        currentState = new IdleState();

        initGUI(objects);
    }

    private void initGUI(List<GraphicalObject> objects) {
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();

        JButton selectButton = new JButton("Selektiraj");
        selectButton.addActionListener(e -> setState(new SelectShapeState(model)));
        toolBar.add(selectButton);

        JButton eraserButton = new JButton("Brisač");
        eraserButton.addActionListener(e -> setState(new EraserState(model)));
        toolBar.add(eraserButton);

        toolBar.addSeparator();

        for (GraphicalObject obj : objects) {
            JButton button = new JButton(obj.getShapeName());
            button.addActionListener(e -> {
                setState(new AddShapeState(model, obj));
            });
            toolBar.add(button);
        }
        cp.add(toolBar, BorderLayout.NORTH);

        canvas = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                Renderer r = new G2DRendererImpl(g2d);

                for (GraphicalObject obj : model.list()) {
                    obj.render(r);
                    currentState.afterDraw(r, obj);
                }

                currentState.afterDraw(r);
            }
        };
        canvas.setBackground(Color.WHITE);
        canvas.setFocusable(true);
        cp.add(canvas, BorderLayout.CENTER);

        model.addDocumentModelListener(() -> canvas.repaint());

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                Point mousePoint = new Point(evt.getX(), evt.getY());
                boolean shiftDown = evt.isShiftDown();
                boolean ctrlDown = evt.isControlDown();

                currentState.mouseDown(mousePoint, shiftDown, ctrlDown);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                Point mousePoint = new Point(evt.getX(), evt.getY());
                boolean shiftDown = evt.isShiftDown();
                boolean ctrlDown = evt.isControlDown();

                currentState.mouseUp(mousePoint, shiftDown, ctrlDown);
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                Point mousePoint = new Point(evt.getX(), evt.getY());
                currentState.mouseDragged(mousePoint);
            }
        });

        canvas.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setState(new IdleState());
                } else {
                    currentState.keyPressed(e.getKeyCode());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });
    }

    public void setState(State newState) {
        if (currentState != null) {
            currentState.onLeaving();
        }
        currentState = newState;
        canvas.repaint();
    }

    public State getCurrentState() {
        return currentState;
    }

    public DocumentModel getModel() {
        return model;
    }
}