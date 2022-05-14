import java.util.*;
import java.util.stream.IntStream;

public class World extends Observable {

    private int tick;
    private int size;

    private Player player;
    private Player player2;
    private Thread thread;
    private boolean notOver;
    private long delayed = 250;
    private int enemyCount = 3;
    private List<Enemy> enemies = new ArrayList<Enemy>();
    private List<Enemy> enemiesStart = new ArrayList<Enemy>();

    private List<BlockTree> treeBlocks = new ArrayList<BlockTree>();
    private List<BlockBrick> brickBlocks = new ArrayList<BlockBrick>();
    private List<BlockSteel> steelBlocks = new ArrayList<BlockSteel>();
    private List<BlockStream> streamBlocks = new ArrayList<BlockStream>();
    private int hitEnemy;
    private boolean win;

    public World(int size) {
        this.size = size;
        tick = 0;
        hitEnemy = 0;
        setEnemies();
        player = new Player(13, 24);
        player2 = new Player(size/2 + 3, size/2 + 3);
        setTreeBlocks();
        setSteelBlocks();
        setBrickBlocks();
        setStreamBlocks();
    }

    public void start() {
        player.reset();
        player.setPosition(13, 24);
        player2.reset();
        player2.setPosition(size/2 + 3, size/2 + 3);
        for(int i = 0; i < enemies.size(); i++) {
            enemies.get(i).setPosition(enemiesStart.get(i).getX(), enemiesStart.get(i).getY());
        }
        tick = 0;
        notOver = true;
        win = false;
        hitEnemy = 0;
        thread = new Thread() {
            @Override
            public void run() {
                while(notOver&&!win) {
                    tick++;
                    player.move(where(player));
                    player2.move(where(player2));
                    if (!player.getBullets().isEmpty()) {
                        for (int b = 0; b < player.getBullets().size(); b++) {
                            player.getBullets().get(b).move();
                        }
                    }
                    if (!player2.getBullets().isEmpty()) {
                        for (int b = 0; b < player2.getBullets().size(); b++) {
                            player2.getBullets().get(b).move();
                        }
                    }
                    for(int i =0; i < enemies.size(); i++) {
                        for(int b = 0; b < player.getBullets().size(); b++){
                            if (enemies.get(i).collision(player.getBullets().get(b))){
                                player.getBullets().remove(player.getBullets().get(b));
                                enemies.remove(enemies.get(i));
                                hitEnemy++;
                                if(hitEnemy == enemiesStart.size()){
                                    win = true;
                                }
                            }
                        }
                    }

                    for(int i =0; i < enemies.size(); i++) {
                        for(int b = 0; b < player2.getBullets().size(); b++){
                            if (enemies.get(i).collision(player2.getBullets().get(b))){
                                player2.getBullets().remove(player2.getBullets().get(b));
                                enemies.remove(enemies.get(i));
                            }
                        }
                    }

                    // enemy move and shoot the bullet
                    for(Enemy enemy: enemies) {
                        enemy.moveTankEnermy(player.getX(), player.getY(), tick, where(enemy));
                        enemy.moveTankEnermy(player2.getX(), player2.getY(), tick, where(enemy));
                        for (Bullet bullet : enemy.getBullets()){
                            bullet.move();
                        }
                    }
                    // player collision
                    playerBulletCollision();
                    // check bullet collision
//                    if (!enemies.isEmpty()) {
//                        for (int e = 0; e < enemies.size(); e++){
//                            for (int b = 0; b < enemies.get(e).getBullets().size(); b++){
//                                for (int bp = 0; bp < player.getBullets().size(); bp++) {
//                                    if (enemies.get(e).getBullets().get(b).bulletCollision(player.getBullets().get(bp))) {
//                                        player.getBullets().remove(player.getBullets().get(bp));
//                                        enemies.get(e).getBullets().remove(enemies.get(e).getBullets().get(bp));
//                                    }
//                                }
//                            }
//                        }
//                        for (Enemy e : enemies) {
//                            for (Bullet enemyBullet : e.getBullets()) {
//                                player.getBullets().removeIf(enemyBullet::bulletCollision);
//                                for (Bullet bp : player.getBullets()) {
//                                    e.getBullets().removeIf(bp::bulletCollision);
//                                }
//                            }
//                        }
//                        for (Enemy e : enemies) {
//                            for (Bullet enemyBullet : e.getBullets()) {
//                                player2.getBullets().removeIf(enemyBullet::bulletCollision);
//                            }
//                        }
//                    }

                    playerBulletCollision();
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

    private void playerBulletCollision(){
        for (Bullet bp1 : player.getBullets()) {
            if (player2.collision(bp1)) {
                notOver = false;
            }
        }
        for (Bullet bp2 : player2.getBullets()) {
            if (player.collision(bp2)) {
                    notOver = false;
            }
        }
    }

    private void checkCollisions() {
        for(Enemy e : enemies) {
            if(e.hit(player)) {
                notOver = false;
            } else if (e.hit(player2)) {
                notOver = false;
            }
            for (Bullet b : e.getBullets())
                if(player.collision(b)){
                    notOver = false;
                } else if(player2.collision(b)){
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
                        break;
                    }
                }
            }
        }
        if (!player.getBullets().isEmpty()) {
            for (int b = 0; b < player.getBullets().size(); b++) {
                for (int brick = 0; brick < brickBlocks.size(); brick++) {
                    if (brickBlocks.get(brick).isBulletHit(player.getBullets().get(b))) {
                        player.getBullets().remove(player.getBullets().get(b));
                        brickBlocks.remove(brickBlocks.get(brick));
                      break;
                    }
                }
            }
        }
        if (!player2.getBullets().isEmpty()) {
            for (int b = 0; b < player2.getBullets().size(); b++) {
                for (int brick = 0; brick < brickBlocks.size(); brick++) {
                    if (brickBlocks.get(brick).isBulletHit(player2.getBullets().get(b))) {
                        player2.getBullets().remove(player2.getBullets().get(b));
                        brickBlocks.remove(brickBlocks.get(brick));
                      break
                    }
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
        for(int b = 0; b < player2.getBullets().size(); b++){
            for (int steel=0; steel<steelBlocks.size(); steel++) {
                if (steelBlocks.get(steel).isBulletHit(player2.getBullets().get(b))) {
                    player2.getBullets().remove(player2.getBullets().get(b));
                    break;
                }
            }
        }
    }

    private String where(WObject obj) {
        for (BlockBrick brick : brickBlocks) {
            if (brick.getX() == obj.getX() && brick.getY() + 1 == obj.getY()) {
                return "up";
            }
            if (brick.getX() == obj.getX() && brick.getY() - 1 == obj.getY()) {
                return "down";
            }
            if (brick.getX() + 1 == obj.getX() && brick.getY() == obj.getY()) {
                return "left";
            }
            if (brick.getX() - 1 == obj.getX() && brick.getY() == obj.getY()) {
                return "right";
            }
        }
        for (BlockSteel steel : steelBlocks) {
            if (steel.getX() == obj.getX() && steel.getY() + 1 == obj.getY()) {
                return "up";
            }
            if (steel.getX() == obj.getX() && steel.getY() - 1 == obj.getY()) {
                return "down";
            }
            if (steel.getX() + 1 == obj.getX() && steel.getY() == obj.getY()) {
                return "left";
            }
            if (steel.getX() - 1 == obj.getX() && steel.getY() == obj.getY()) {
                return "right";
            }
        }
        for (BlockStream stream : streamBlocks) {
            if (stream.getX() == obj.getX() && stream.getY() + 1 == obj.getY()) {
                return "up";
            }
            if (stream.getX() == obj.getX() && stream.getY() - 1 == obj.getY()) {
                return "down";
            }
            if (stream.getX() + 1 == obj.getX() && stream.getY() == obj.getY()) {
                return "left";
            }
            if (stream.getX() - 1 == obj.getX() && stream.getY() == obj.getY()) {
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
        for(BlockStream stream: streamBlocks){
            if(stream.getX()==p.getX()&&stream.getY()+1==p.getY()){
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
        for(BlockStream stream: streamBlocks){
            if(stream.getX()==p.getX()&&stream.getY()-1==p.getY()){
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
        for(BlockStream stream: streamBlocks){
            if(stream.getX()+1==p.getX()&&stream.getY()==p.getY()){
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
        for(BlockStream stream: streamBlocks){
            if(stream.getX()-1==p.getX()&&stream.getY()==p.getY()){
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

    public Player getPlayer2(){
        return player2;
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

    private void setEnemies(){
        for(int i = 0; i < enemyCount/2; i++) {
            enemies.add(new Enemy(i*5+3, 0));
            enemiesStart.add(new Enemy(i*5+3, 0));
        }
        enemies.add(new Enemy(3, 24));
        enemiesStart.add(new Enemy(3, 24));
        enemies.add(new Enemy(0, 18));
        enemiesStart.add(new Enemy(0, 18));
        enemies.add(new Enemy(0, 22));
        enemiesStart.add(new Enemy(0, 22));
        enemies.add(new Enemy(22, 24));
        enemiesStart.add(new Enemy(22, 24));
        enemies.add(new Enemy(24, 18));
        enemiesStart.add(new Enemy(24, 18));
        enemies.add(new Enemy(24, 22));
        enemiesStart.add(new Enemy(24, 22));
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public boolean isGameOver() {
        return !notOver;
    }

    private void setTreeBlocks(){
        for(int j=0;j<25;j++){
            if((3<=j&&j<=6)||(10<=j&&j<=12)||(18<=j&&j<=21)){
                continue;
            }
            treeBlocks.add(new BlockTree(j, 16));
            treeBlocks.add(new BlockTree(j, 17));
        }
    }

    public List<BlockTree> getTreeBlocks() {
        return treeBlocks;
    }

    private void setBrickBlocks(){
        for(int j=17; j<25; j++) {
            brickBlocks.add(new BlockBrick(j, 5));
            brickBlocks.add(new BlockBrick(j, 7));
        }
        for(int j=6;j<17;j++){
            brickBlocks.add(new BlockBrick(j, 13));
            brickBlocks.add(new BlockBrick(j, 14));
        }
        for(int j=18;j<25;j++){
            brickBlocks.add(new BlockBrick(6, j));
            brickBlocks.add(new BlockBrick(7, j));
            brickBlocks.add(new BlockBrick(15, j));
            brickBlocks.add(new BlockBrick(16, j));
        }
    }

    public List<BlockBrick> getBrickBlocks() {
        return brickBlocks;
    }

    private void setSteelBlocks(){
        for(int j=5; j<14; j++) {
            steelBlocks.add(new BlockSteel(j, 5));
            steelBlocks.add(new BlockSteel(j, 6));
            steelBlocks.add(new BlockSteel(j, 7));
        }
        for(int j=18; j<25; j++) {
            steelBlocks.add(new BlockSteel(19, j));
            steelBlocks.add(new BlockSteel(20, j));
        }
    }

    private void setStreamBlocks(){
        for(int j=2; j<12; j++) {
            streamBlocks.add(new BlockStream(3, j));
            streamBlocks.add(new BlockStream(4, j));
        }
    }

    public List<BlockStream> getStreamBlocks() {
        return streamBlocks;
    }

    public List<BlockSteel> getSteelBlocks() {
        return steelBlocks;
    }

    public int getHitEnemy(){
        return hitEnemy;
    }
    public boolean isWinning(){
        return win;
    }
}
