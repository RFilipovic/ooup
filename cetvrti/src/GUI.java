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

        // Toolbar
        JToolBar toolBar = new JToolBar();

        // Dodaj gumb "Selektiraj"
        JButton selectButton = new JButton("Selektiraj");
        selectButton.addActionListener(e -> setState(new SelectShapeState(model)));
        toolBar.add(selectButton);

        // Dodaj separator
        toolBar.addSeparator();

        // Postojeći gumbovi za objekte
        for (GraphicalObject obj : objects) {
            JButton button = new JButton(obj.getShapeName());
            button.addActionListener(e -> {
                setState(new AddShapeState(model, obj));
            });
            toolBar.add(button);
        }
        cp.add(toolBar, BorderLayout.NORTH);

        // Canvas
        canvas = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                Renderer r = new G2DRendererImpl(g2d);

                for (GraphicalObject obj : model.list()) {
                    obj.render(r);

                    // Pozovi afterDraw za svaki objekt (State će crtati selekciju)
                    currentState.afterDraw(r, obj);
                }

                // Pozovi afterDraw nakon crtanja čitavog crteža
                currentState.afterDraw(r);
            }
        };
        canvas.setBackground(Color.WHITE);
        canvas.setFocusable(true);
        cp.add(canvas, BorderLayout.CENTER);

        // Add model listener to repaint on changes
        model.addDocumentModelListener(() -> canvas.repaint());

        // Mouse listeners koji pozivaju metode trenutnog stanja
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

        // Key listener koji poziva metode trenutnog stanja
        canvas.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                // ESC vraća u IdleState
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setState(new IdleState());
                } else {
                    currentState.keyPressed(e.getKeyCode());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Nije potrebno za specifikaciju
            }

            @Override
            public void keyTyped(KeyEvent e) {
                // Nije potrebno za specifikaciju
            }
        });
    }

    // Metoda za promjenu stanja
    public void setState(State newState) {
        if (currentState != null) {
            currentState.onLeaving();
        }
        currentState = newState;
        canvas.repaint();
    }

    // Getter za trenutno stanje (može biti koristan kasnije)
    public State getCurrentState() {
        return currentState;
    }

    // Getter za model (može biti koristan za implementaciju stanja)
    public DocumentModel getModel() {
        return model;
    }
}