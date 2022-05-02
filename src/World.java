import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.stream.IntStream;

public class World extends Observable {

    private int tick;
    private int size;

    private Player player;
    private Thread thread;
    private boolean notOver;
    private long delayed = 250;
    private int enemyCount = 10;
    private List<Enemy> enemies = new ArrayList<Enemy>();
    private List<Enemy> enemiesStart = new ArrayList<Enemy>();

    public World(int size) {
        this.size = size;
        tick = 0;
        player = new Player(size/2, size/2);
        //  enemies = new Enemy[enemyCount];
        //  enemiesStart = new Enemy[enemyCount];
        Random random = new Random();
        for(int i = 0; i < enemyCount; i++) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            enemies.add(new Enemy(x, y));
            enemiesStart.add(new Enemy(x, y));
        }
        // enemies[enemies.length] = new Enemy((size/2), (size/2)+2);
    }

    public void start() {
        player.reset();
        player.setPosition(size/2, size/2);
        for(int i = 0; i < enemies.size(); i++) {
            enemies.get(i).setPosition(enemiesStart.get(i).getX(), enemiesStart.get(i).getY());
        }
        tick = 0;
        notOver = true;
        thread = new Thread() {
            @Override
            public void run() {
                while(notOver) {
                    tick++;
                    player.move();
                    for(int b = 0; b < player.getBullets().size(); b++) {
                        player.getBullets().get(b).move();
                    }
                    for(int i =0; i < enemies.size(); i++) {
                        for(int b = 0; b < player.getBullets().size(); b++){
                            if (enemies.get(i).collision(player.getBullets().get(b))){
                                player.getBullets().remove(player.getBullets().get(b));
                                enemies.remove(enemies.get(i));
                            }
                        }
                    }

                    for(Enemy enemy: enemies) {
                        enemy.moveTankEnermy(player.getX(), player.getY(), tick);
                        for (Bullet bullet : enemy.getBullets()){
                            bullet.move();
                        }
                    }

                    checkCollisions();
                    setChanged();
                    notifyObservers();
                    waitFor(delayed);
                }
            }
        };
        thread.start();
    }

    private void checkCollisions() {
        for(Enemy e : enemies) {
            if(e.hit(player)) {
                notOver = false;
            }
        }
    }

    private void waitFor(long delayed) {
        try {
            Thread.sleep(delayed);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getTick() {
        return tick;
    }

    public int getSize() {
        return size;
    }

    public Player getPlayer() {
        return player;
    }

    public void turnPlayerNorth() {
        player.turnNorth();
    }

    public void turnPlayerSouth() {
        player.turnSouth();
    }

    public void turnPlayerWest() {
        player.turnWest();
    }

    public void turnPlayerEast() {
        player.turnEast();
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public boolean isGameOver() {
        return !notOver;
    }
}
