import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Window extends JFrame implements Observer {

    private int size = 500;
    private World world;
    private Renderer renderer;
    private Gui gui;

    List<Command> replays = new ArrayList<Command>();

    public Window() {
        super();
        addKeyListener(new Controller());
        setLayout(new BorderLayout());
        renderer = new Renderer();
        add(renderer, BorderLayout.CENTER);
        gui = new Gui();
        add(gui, BorderLayout.SOUTH);
        world = new World(25);
        world.addObserver(this);
        setSize(size, size+70);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Tank Game");
        setResizable(false);
    }

    @Override
    public void update(Observable o, Object arg) {
        renderer.repaint();
        gui.updateTick(world.getTick());
        gui.updateScore(world.getHitEnemy());

        for (Command c: replays){
            if (c.getTick() == world.getTick()){
                c.execute();
            }
        }
        if(world.isGameOver()) {
            gui.showGameOverLabel();
            gui.enableReplayButton();
        }
        if(world.isWinning()) {
            gui.showWinningLabel();
            gui.enableReplayButton();
        }
    }

    class Renderer extends JPanel {

        public Renderer() {
            setDoubleBuffered(true);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            paintGrids(g);
            paintPlayer(g);
            paintPlayer2(g);
            paintEnemies(g);
            paintBullets(g);
            paintTreeBlock(g);
            paintSteelBlock(g);
            paintBrickBlock(g);
            paintStreamBlock(g);
        }

        private void paintGrids(Graphics g) {
            // Background
            g.setColor(Color.lightGray);
            g.fillRect(0, 0, size, size);

            // Lines
            g.setColor(Color.black);
            int perCell = size/world.getSize();
            for(int i = 0; i < world.getSize(); i++) {
                g.drawLine(i * perCell, 0, i * perCell, size);
                g.drawLine(0, i * perCell, size, i * perCell);
            }
        }

        private void paintPlayer(Graphics g) {
            int perCell = size/world.getSize();
            int x = world.getPlayer().getX();
            int y = world.getPlayer().getY();
            g.setColor(Color.green);
            g.fillRect(x * perCell,y * perCell,perCell, perCell);
        }

        private void paintPlayer2(Graphics g) {
            int perCell = size/world.getSize();
            int x = world.getPlayer2().getX();
            int y = world.getPlayer2().getY();
            g.setColor(Color.magenta);
            g.fillRect(x * perCell,y * perCell,perCell, perCell);
        }

        private void paintEnemies(Graphics g) {
            int perCell = size/world.getSize();
            g.setColor(Color.red);
            for(Enemy e : world.getEnemies()) {
                int x = e.getX();
                int y = e.getY();
                g.fillRect(x * perCell, y * perCell,perCell, perCell);
            }
        }

        private void paintBullets(Graphics g){
            int perCell = size/world.getSize();
            g.setColor(Color.black);
            for (Bullet bullet: world.getPlayer().getBullets()) {
                int x = bullet.getX();
                int y = bullet.getY();
                g.fillRect(x * perCell, y * perCell, perCell, perCell);
            }
            g.setColor(Color.DARK_GRAY);
            for (Bullet bullet: world.getPlayer2().getBullets()) {
                int x = bullet.getX();
                int y = bullet.getY();
                g.fillRect(x * perCell, y * perCell, perCell, perCell);
            }
            g.setColor(Color.cyan);
            for (Enemy enemy: world.getEnemies()) {
                for (Bullet bullet : enemy.getBullets()) {
                    int x = bullet.getX();
                    int y = bullet.getY();
                    g.fillRect(x * perCell, y * perCell, perCell, perCell);
                }
            }
        }
        public void paintTreeBlock(Graphics g) {
            int perCell = size/world.getSize();
            for(BlockTree tree: world.getTreeBlocks()){
                int x = tree.getX();
                int y = tree.getY();
                g.drawImage(new ImageIcon("img/Tree.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }
        }
        public void paintSteelBlock(Graphics g) {
            int perCell = size/world.getSize();
            for(BlockSteel steel: world.getSteelBlocks()){
                int x = steel.getX();
                int y = steel.getY();
                g.drawImage(new ImageIcon("img/Steel.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }
        }
        public void paintBrickBlock(Graphics g) {
            int perCell = size/world.getSize();
            for(BlockBrick brick: world.getBrickBlocks()){
                int x = brick.getX();
                int y = brick.getY();
                g.drawImage(new ImageIcon("img/Brick.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }
        }

        public void paintStreamBlock(Graphics g){
            int perCell = size/world.getSize();
            for(BlockStream stream: world.getStreamBlocks()){
                int x = stream.getX();
                int y = stream.getY();
                g.drawImage(new ImageIcon("img/Stream.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }
        }
    }

    class Gui extends JPanel {

        private JLabel tickLabel;
        private JLabel scoreLabel;
        private JButton startButton;
        private JButton replayButton;
        private JLabel gameOverLabel;
        private JLabel winningLabel;

        public Gui() {
            setLayout(new FlowLayout());
            tickLabel = new JLabel("Tick: 0");
            add(tickLabel);
            scoreLabel = new JLabel("Score: 0");
            add(scoreLabel);
            startButton = new JButton("Start");
            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    world.start();
                    startButton.setEnabled(false);
                    Window.this.requestFocus();
                }
            });
            add(startButton);
            replayButton = new JButton("Replay");
            replayButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    world.start();
                    world.getPlayer().getBullets().clear();
                    world.getPlayer2().getBullets().clear();
                    replayButton.setEnabled(false);
                    Window.this.requestFocus();
                }
            });
            replayButton.setEnabled(false);
            add(replayButton);
            gameOverLabel = new JLabel("GAME OVER");
            gameOverLabel.setForeground(Color.red);
            gameOverLabel.setVisible(false);
            add(gameOverLabel);
            winningLabel = new JLabel("GREAT JOB");
            winningLabel.setForeground(Color.GREEN);
            winningLabel.setVisible(false);
            add(winningLabel);
        }

        public void updateTick(int tick) {
            tickLabel.setText("Tick: " + tick);
        }

        public void updateScore(int score) {
            scoreLabel.setText("Score: " + score);
        }

        public void showGameOverLabel() {
            gameOverLabel.setVisible(true);
        }

        public void showWinningLabel() {
            winningLabel.setVisible(true);
        }

        public void enableReplayButton() {
            replayButton.setEnabled(true);
        }
    }

    class Controller extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_UP&& world.atUp(world.getPlayer())) {
                Command c = new CommandTurnNorth(world.getPlayer(), world.getTick());
                c.execute();
                replays.add(c);
            } else if(e.getKeyCode() == KeyEvent.VK_DOWN && world.atDown(world.getPlayer())) {
                Command c = new CommandTurnSouth(world.getPlayer(), world.getTick());
                c.execute();
                replays.add(c);
            } else if(e.getKeyCode() == KeyEvent.VK_LEFT && world.atLeft(world.getPlayer())) {
                Command c = new CommandTurnWest(world.getPlayer(), world.getTick());
                c.execute();
                replays.add(c);
            } else if(e.getKeyCode() == KeyEvent.VK_RIGHT && world.atRight(world.getPlayer())) {
                Command c = new CommandTurnEast(world.getPlayer(), world.getTick());
                c.execute();
                replays.add(c);
            } else if(e.getKeyCode() == KeyEvent.VK_L){
                Command c = new CommandFire(world.getPlayer(), world.getTick());
                c.execute();
                replays.add(c);
            } else if(e.getKeyCode() == KeyEvent.VK_W && world.atUp(world.getPlayer2())) {
                Command c = new CommandTurnNorth(world.getPlayer2(), world.getTick());
                c.execute();
                replays.add(c);
            } else if(e.getKeyCode() == KeyEvent.VK_S && world.atDown(world.getPlayer2())) {
                Command c = new CommandTurnSouth(world.getPlayer2(), world.getTick());
                c.execute();
                replays.add(c);
            } else if(e.getKeyCode() == KeyEvent.VK_A && world.atLeft(world.getPlayer2())) {
                Command c = new CommandTurnWest(world.getPlayer2(), world.getTick());
                c.execute();
                replays.add(c);
            } else if(e.getKeyCode() == KeyEvent.VK_D && world.atRight(world.getPlayer2())) {
                Command c = new CommandTurnEast(world.getPlayer2(), world.getTick());
                c.execute();
                replays.add(c);
            } else if(e.getKeyCode() == KeyEvent.VK_F){
                Command c = new CommandFire(world.getPlayer2(), world.getTick());
                c.execute();
                replays.add(c);
            }
        }
    }

    public static void main(String[] args) {
        Window window = new Window();
        window.setVisible(true);
    }

}
