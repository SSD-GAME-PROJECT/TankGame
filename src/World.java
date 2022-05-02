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

    private List<BlockTree> treeBlocks = new ArrayList<BlockTree>();
    private List<BlockBrick> brickBlocks = new ArrayList<BlockBrick>();
    private List<BlockSteel> steelBlocks = new ArrayList<BlockSteel>();
    private String string;

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
        setTreeBlocks();
        setSteelBlocks();
        setBrickBlocks();
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
                        string = where(enemy);
                        enemy.moveTankEnermy(player.getX(), player.getY(), tick, string);
                        for (Bullet bullet : enemy.getBullets()){
                            bullet.move();
                        }
                    }
                    hitBrick();
                    hitSteel();

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

    private void hitBrick(){
        for(int i =0; i < enemies.size(); i++) {
            for(int b = 0; b < enemies.get(i).getBullets().size(); b++){
                for (int brick=0; brick<brickBlocks.size(); brick++) {
                    if (brickBlocks.get(brick).isBulletHit(enemies.get(i).getBullets().get(b))) {
                        enemies.get(i).getBullets().remove(enemies.get(i).getBullets().get(b));
                        brickBlocks.remove(brickBlocks.get(brick));
                    }
                }
            }
        }
        for(int b = 0; b < player.getBullets().size(); b++){
            for (int brick=0; brick<brickBlocks.size(); brick++) {
                if (brickBlocks.get(brick).isBulletHit(player.getBullets().get(b))) {
                    player.getBullets().remove(player.getBullets().get(b));
                    brickBlocks.remove(brickBlocks.get(brick));
                }
            }
        }
    }

    private void hitSteel(){
        for(int i =0; i < enemies.size(); i++) {
            for(int b = 0; b < enemies.get(i).getBullets().size(); b++){
                for (int steel=0; steel<steelBlocks.size(); steel++) {
                    if (steelBlocks.get(steel).isBulletHit(enemies.get(i).getBullets().get(b))) {
                        enemies.get(i).getBullets().remove(enemies.get(i).getBullets().get(b));
                        break;
                    }
                }
            }
        }
        for(int b = 0; b < player.getBullets().size(); b++){
            for (int steel=0; steel<steelBlocks.size(); steel++) {
                if (steelBlocks.get(steel).isBulletHit(player.getBullets().get(b))) {
                    player.getBullets().remove(player.getBullets().get(b));
                    break;
                }
            }
        }
    }

    private String where(WObject enemy) {
        for (BlockBrick brick : brickBlocks) {
            if (brick.getX() == enemy.getX() && brick.getY() + 1 == enemy.getY()) {
                return "up";
            }
            if (brick.getX() == enemy.getX() && brick.getY() - 1 == enemy.getY()) {
                return "down";
            }
            if (brick.getX() + 1 == enemy.getX() && brick.getY() == enemy.getY()) {
                return "left";
            }
            if (brick.getX() - 1 == enemy.getX() && brick.getY() == enemy.getY()) {
                return "right";
            }
        }
        for (BlockSteel steel : steelBlocks) {
            if (steel.getX() == enemy.getX() && steel.getY() + 1 == enemy.getY()) {
                return "up";
            }
            if (steel.getX() == enemy.getX() && steel.getY() - 1 == enemy.getY()) {
                return "down";
            }
            if (steel.getX() + 1 == enemy.getX() && steel.getY() == enemy.getY()) {
                return "left";
            }
            if (steel.getX() - 1 == enemy.getX() && steel.getY() == enemy.getY()) {
                return "right";
            }
        }
        return "";
    }

    public boolean atUp(WObject p){
        for(BlockBrick brick: brickBlocks){
            if(brick.getX()==p.getX()&&brick.getY()+1==p.getY()){
                return false;
            }
        }
        for(BlockSteel steel: steelBlocks){
            if(steel.getX()==p.getX()&&steel.getY()+1==p.getY()){
                return false;
            }
        }
        return true;
    }

    public boolean atDown(WObject p){
        for(BlockBrick brick: brickBlocks){
            if(brick.getX()==p.getX()&&brick.getY()-1==p.getY()){
                return false;
            }
        }
        for(BlockSteel steel: steelBlocks){
            if(steel.getX()==p.getX()&&steel.getY()-1==p.getY()){
                return false;
            }
        }
        return true;
    }

    public boolean atLeft(WObject p){
        for(BlockBrick brick: brickBlocks){
            if(brick.getX()+1==p.getX()&&brick.getY()==p.getY()){
                return false;
            }
        }
        for(BlockSteel steel: steelBlocks){
            if(steel.getX()+1==p.getX()&&steel.getY()==p.getY()){
                return false;
            }
        }
        return true;
    }

    public boolean atRight(WObject p){
        for(BlockBrick brick: brickBlocks){
            if(brick.getX()-1==p.getX()&&brick.getY()==p.getY()){
                return false;
            }
        }
        for(BlockSteel steel: steelBlocks){
            if(steel.getX()-1==p.getX()&&steel.getY()==p.getY()){
                return false;
            }
        }
        return true;
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

    private void setTreeBlocks(){
        BlockTree tree = new BlockTree(10, 10);
        BlockTree tree2 = new BlockTree(11, 10);
        BlockTree tree3 = new BlockTree(12, 10);
        BlockTree tree4 = new BlockTree(10, 11);
        BlockTree tree5 = new BlockTree(11, 11);
        BlockTree tree6 = new BlockTree(12, 11);
        treeBlocks.add(tree);
        treeBlocks.add(tree2);
        treeBlocks.add(tree3);
        treeBlocks.add(tree4);
        treeBlocks.add(tree5);
        treeBlocks.add(tree6);
    }

    public List<BlockTree> getTreeBlocks() {
        return treeBlocks;
    }

    private void setBrickBlocks(){
        BlockBrick brick = new BlockBrick(8, 4);
        BlockBrick brick2 = new BlockBrick(9, 4);
        BlockBrick brick3 = new BlockBrick(10, 4);
        brickBlocks.add(brick);
        brickBlocks.add(brick2);
        brickBlocks.add(brick3);
    }

    public List<BlockBrick> getBrickBlocks() {
        return brickBlocks;
    }

    private void setSteelBlocks(){
        BlockSteel steel = new BlockSteel(16, 18);
        BlockSteel steel2 = new BlockSteel(17, 18);
        BlockSteel steel3 = new BlockSteel(18, 18);
        steelBlocks.add(steel);
        steelBlocks.add(steel2);
        steelBlocks.add(steel3);
    }

    public List<BlockSteel> getSteelBlocks() {
        return steelBlocks;
    }
}
