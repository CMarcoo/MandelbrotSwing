package sample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

public class Main extends JComponent implements MouseWheelListener {

    private static int WIDTH = 1920 / 2, HEIGHT = 1080 / 2;
    private static int ITERATIONS = 125;
    private static int RESIZE = 222;
    private BufferedImage bufferedImage;

    public Main() {
        bufferedImage = new BufferedImage(WIDTH, HEIGHT, 1);
        generateMandelbrotSet();
        JFrame jFrame = new JFrame("Mandelbrot");
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setResizable(true);
        jFrame.getContentPane().add(this);
        jFrame.pack();
        jFrame.setVisible(true);
    }

    @Override
    public void addNotify() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.drawImage(bufferedImage, 0, 0, null);
    }

    public int generateColor(double x, double y) {
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
        if (iterations == ITERATIONS) return 0x00000000;
        return Color.HSBtoRGB((((float) iterations / ITERATIONS) + 0.5f) % 1f, 0.55f, 0.95f);
    }

    public void generateMandelbrotSet() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                int color = generateColor((x - (double) WIDTH / 2d) / RESIZE, (y - (double) HEIGHT / 2d) / RESIZE);
                bufferedImage.setRGB(x, y, color);
            }
        }
        repaint();
    }



    public static void main(String[] args) {
        Main main = new Main();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }
}
