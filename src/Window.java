import javax.swing.*;
import javax.swing.border.BevelBorder;
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
        setSize(size, size+120);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Tank Game");
        setResizable(false);
    }

    @Override
    public void update(Observable o, Object arg) {
        renderer.repaint();
        gui.updateScorePlayer1(world.getplayer1HitEnemy());
        gui.updateScorePlayer2(world.getplayer2HitEnemy());

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
            if (!world.isSingleMode()) {
                paintPlayer2(g);
            }
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
            if(world.getPlayer().getDirection() == Direction.UP) {
                g.drawImage(new ImageIcon("img/Player1/Up.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }else if(world.getPlayer().getDirection() == Direction.DOWN){
                g.drawImage(new ImageIcon("img/Player1/Down.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }else if(world.getPlayer().getDirection() == Direction.LEFT){
                g.drawImage(new ImageIcon("img/Player1/Left.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }else if(world.getPlayer().getDirection() == Direction.RIGHT){
                g.drawImage(new ImageIcon("img/Player1/Right.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }
        }

        private void paintPlayer2(Graphics g) {
            int perCell = size/world.getSize();
            int x = world.getPlayer2().getX();
            int y = world.getPlayer2().getY();
            if(world.getPlayer2().getDirection() == Direction.UP) {
                g.drawImage(new ImageIcon("img/Player2/Up.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }else if(world.getPlayer2().getDirection() == Direction.DOWN){
                g.drawImage(new ImageIcon("img/Player2/Down.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }else if(world.getPlayer2().getDirection() == Direction.LEFT){
                g.drawImage(new ImageIcon("img/Player2/Left.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }else if(world.getPlayer2().getDirection() == Direction.RIGHT){
                g.drawImage(new ImageIcon("img/Player2/Right.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
            }
        }

        private void paintEnemies(Graphics g) {
            int perCell = size/world.getSize();
            for(Enemy e : world.getEnemies()) {
                int x = e.getX();
                int y = e.getY();
                if(e.getDirection() == Direction.UP) {
                    g.drawImage(new ImageIcon("img/Enemy/Up.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
                }else if(e.getDirection() == Direction.DOWN){
                    g.drawImage(new ImageIcon("img/Enemy/Down.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
                }else if(e.getDirection() == Direction.LEFT){
                    g.drawImage(new ImageIcon("img/Enemy/Left.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
                }else if(e.getDirection() == Direction.RIGHT){
                    g.drawImage(new ImageIcon("img/Enemy/Right.png").getImage(), x * perCell, y * perCell, perCell, perCell, null, null);
                }
            }
        }

        private void paintBullets(Graphics g){
            int perCell = size/world.getSize();
            g.setColor(Color.black);
            for (Bullet bullet: world.getPlayer().getBullets()) {
                int x = bullet.getX();
                int y = bullet.getY();
                g.fillOval(x * perCell + 6, y * perCell + 6, 8, 8);
            }
            g.setColor(Color.DARK_GRAY);
            if (!world.isSingleMode()) {
                for (Bullet bullet : world.getPlayer2().getBullets()) {
                    int x = bullet.getX();
                    int y = bullet.getY();
                    g.fillOval(x * perCell + 6, y * perCell + 6, 8, 8);
                }
            }
            g.setColor(Color.cyan);
            for (Enemy enemy: world.getEnemies()) {
                for (Bullet bullet : enemy.getBullets()) {
                    int x = bullet.getX();
                    int y = bullet.getY();
                    g.fillOval(x * perCell + 6, y * perCell + 6, 8, 8);
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

        private JLabel player1ScoreLabel;
        private JLabel player2ScoreLabel;
        private JButton startMultiButton;
        private JButton  startSingleButton;
        private JButton replayButton;
        private JLabel gameOverLabel;
        private JLabel winningLabel;

        public Gui() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            player1ScoreLabel = new JLabel("Player1's Score: 0  ");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 0;
            add(player1ScoreLabel, c);
            player1ScoreLabel.setVisible(false);
            player2ScoreLabel = new JLabel("Player2's Score: 0  ");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 2;
            c.gridy = 0;
            add(player2ScoreLabel, c);
            player2ScoreLabel.setVisible(false);
            startSingleButton = new JButton("SinglePlayer");
            startSingleButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    world.startSinglePlayer();
                    player1ScoreLabel.setVisible(true);
                    startSingleButton.setEnabled(false);
                    startMultiButton.setEnabled(false);
                    Window.this.requestFocus();
                }
            });
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 1;
            add(startSingleButton, c);
            startMultiButton = new JButton("MultiPlayer");
            startMultiButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    world.start();
                    player1ScoreLabel.setVisible(true);
                    player2ScoreLabel.setVisible(true);
                    startSingleButton.setEnabled(false);
                    startMultiButton.setEnabled(false);
                    Window.this.requestFocus();
                }
            });
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 2;
            c.gridy = 1;
            add(startMultiButton, c);
            replayButton = new JButton("Replay");
            replayButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(player2ScoreLabel.isVisible()) {
                        world.start();
                    } else{
                        world.startSinglePlayer();
                    }
                    world.getPlayer().getBullets().clear();
                    world.getPlayer2().getBullets().clear();
                    replayButton.setEnabled(false);
                }
            });
            replayButton.setEnabled(false);
            c.fill = GridBagConstraints.CENTER;
            c.gridx = 1;
            c.gridy = 2;
            add(replayButton, c);
            gameOverLabel = new JLabel("GAME OVER");
            gameOverLabel.setForeground(Color.red);
            gameOverLabel.setVisible(false);
            c.fill = GridBagConstraints.CENTER;
            c.gridx = 1;
            c.gridy = 1;
            add(gameOverLabel, c);
            winningLabel = new JLabel("GREAT JOB");
            winningLabel.setForeground(Color.GREEN);
            winningLabel.setVisible(false);
            c.fill = GridBagConstraints.CENTER;
            c.gridx = 1;
            c.gridy = 1;
            add(winningLabel, c);
        }

        public void updateScorePlayer1(int score) {
            player1ScoreLabel.setText("Player1's Score: " + score + "  ");
        }

        public void updateScorePlayer2(int score) {
            player2ScoreLabel.setText("Player2's Score: " + score + "  ");
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
