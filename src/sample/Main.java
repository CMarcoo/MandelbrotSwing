package sample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends JComponent implements MouseWheelListener, KeyListener {

    private static int WIDTH = 1920 / 2, HEIGHT = 1080 / 2;
    private static int ITERATIONS = 125;
    private static int RESIZE = 225;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2;
    private static final int THREAD_DIMENSION_INCREASE = (WIDTH / THREAD_COUNT);
    private static double MOVE_X = 2d;
    private static double MOVE_Y = 2d;
    private BufferedImage bufferedImage;
    private JFrame jFrame;

    public Main() {
        jFrame = new JFrame("Mandelbrot");
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setResizable(true);
        jFrame.getContentPane().add(this);
        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.addMouseWheelListener(this);
        //jFrame.addMouseMotionListener(this);
        jFrame.addKeyListener(this);

        generateMandelbrotSet();
        repaint();

        jFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Component c = e.getComponent();
                WIDTH = c.getWidth();
                HEIGHT = c.getHeight();
                bufferedImage = new BufferedImage(WIDTH, HEIGHT, 1);
                generateMandelbrotSet();
                paint(c.getGraphics());
            }
        });
    }

    @Override
    public void addNotify() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.drawImage(bufferedImage, 0, 0, null);
    }

    public static int generateColor(double x, double y) {
        double colorX = x;
        double colorY = y;
        int iterations = 0;
        while (iterations < ITERATIONS) {
            double xSquared = Math.pow(x, 2d);
            double ySquared = Math.pow(y, 2d);
            double nX = xSquared - ySquared + colorX;
            double nY = 2d * x * y + colorY;
            x = nX;
            y = nY;
            if (xSquared + ySquared > 4d) break;
            iterations += 1;
        }
        if (iterations == ITERATIONS) return Color.BLACK.getRGB();

        return Color.HSBtoRGB((((float) iterations / ITERATIONS) - 0.645f), 0.55f, 0.95f);
    }

    public static final class ThreadRenderer extends Thread {
        private final Map<Integer, Boolean> threadStatuses;
        private final int startX, endX;
        private final BufferedImage image;
        private final int NUMBER;
        private final JFrame jFrame;

        public ThreadRenderer(int startX,
                              int endX,
                              BufferedImage image,
                              int NUMBER,
                              JFrame jFrame,
                              Map<Integer, Boolean> statusesMap) {
            this.startX = startX;
            this.endX = endX;
            this.image = image;
            this.NUMBER = NUMBER;
            this.jFrame = jFrame;
            this.threadStatuses = statusesMap;
        }

        public int getNUMBER() {
            return NUMBER;
        }

        @Override
        public void run() {
            threadStatuses.put(NUMBER, false);
            for (int x = startX; x < endX; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    int color = generateColor((x - (double) WIDTH / MOVE_X) / RESIZE, (y - (double) HEIGHT / MOVE_Y) / RESIZE);
                    image.setRGB(x, y, color);
                }
            }
            threadStatuses.put(NUMBER, true);
            if (threadStatuses.values().stream().allMatch(Boolean::booleanValue) && threadStatuses.values().size() == THREAD_COUNT) {
                jFrame.getGraphics().drawImage(image, 0, 0, null);
                threadStatuses.clear();
            }
        }
    }

    private final Map<Integer, Boolean> threadStatuses = new ConcurrentHashMap<>();

    public void generateMandelbrotSet() {
        BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        int threadStarted = 1;
        int space = 0;
        int increaseFactor = WIDTH / THREAD_COUNT;
        do {
            ThreadRenderer rendered = new ThreadRenderer(space, (space + increaseFactor), bufferedImage, threadStarted, jFrame, threadStatuses);
            rendered.start();
            space += increaseFactor;
            threadStarted++;
        } while (threadStarted - 1 != THREAD_COUNT);
    }

    public static void main(String[] args) {
        Main main = new Main();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotations = e.getWheelRotation() * -1;
        if (rotations > 0) {
            RESIZE += 50;
        } else if (RESIZE > 5) {
            RESIZE -= 50;
        } else {
            JOptionPane.showMessageDialog(jFrame, "You cannot zoom-out any further.");
        }
        generateMandelbrotSet();
        paint(jFrame.getGraphics());
    }

    private enum Move {
        X_AXIS, Y_AXIS
    }

    private final EnumMap<Move, Integer> moveMap = new EnumMap<>(Move.class);

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                MOVE_Y += 0.50;
                generateMandelbrotSet();
                break;
            case KeyEvent.VK_DOWN:
                MOVE_Y -= 0.50;
                generateMandelbrotSet();
                break;
            case KeyEvent.VK_RIGHT:
                MOVE_X += 0.50;
                generateMandelbrotSet();
                break;
            case KeyEvent.VK_LEFT:
                MOVE_X -= 0.50;
                generateMandelbrotSet();
                break;
            default:
                break;
        }
    }
}
