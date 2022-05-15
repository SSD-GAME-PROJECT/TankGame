import java.util.*;

public class World extends Observable {

    private int tick;
    private int size;

    private Player player;
    private Player player2;
    private Thread thread;
    private boolean notOver;
    private long delayed = 250;
    private boolean singleMode;
    private List<Enemy> enemies = new ArrayList<Enemy>();

    private List<BlockTree> treeBlocks = new ArrayList<BlockTree>();
    private List<BlockBrick> brickBlocks = new ArrayList<BlockBrick>();
    private List<BlockSteel> steelBlocks = new ArrayList<BlockSteel>();
    private List<BlockStream> streamBlocks = new ArrayList<BlockStream>();
    private int player1HitEnemy;
    private int player2HitEnemy;
    private boolean win;

    public World(int size) {
        this.size = size;
        tick = 0;
        player1HitEnemy = 0;
        player2HitEnemy = 0;
        setEnemies();
        createPlayer();
        setTreeBlocks();
        setSteelBlocks();
        setBrickBlocks();
        setStreamBlocks();
    }

    private void createPlayer(){
        player = new Player(13, 24, Direction.UP);
        if (!singleMode) {
            player2 = new Player(100, 100, Direction.UP);
        }
    }

    public void start() {
        singleMode = false;
        player.reset();
        player.setPosition(13, 24, Direction.UP);
        player2.reset();
        player2.setPosition(5, 8, Direction.UP);
        setEnemies();
        setBrickBlocks();

        tick = 0;
        notOver = true;
        win = false;
        player1HitEnemy = 0;
        player2HitEnemy = 0;
        thread = new Thread() {
            @Override
            public void run() {
                while(notOver&&!win) {
                    tick++;
                    player.move(where(player), player2);
                    player2.move(where(player2), player);
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
                                player1HitEnemy++;
                            }
                        }
                    }

                    for(int i =0; i < enemies.size(); i++) {
                        for(int b = 0; b < player2.getBullets().size(); b++){
                            if (enemies.get(i).collision(player2.getBullets().get(b))){
                                player2.getBullets().remove(player2.getBullets().get(b));
                                enemies.remove(enemies.get(i));
                                player2HitEnemy++;
                            }
                        }
                    }
                    if(enemies.isEmpty()){
                        win = true;
                    }

                    // enemy move and shoot the bullet
                    for(Enemy enemy: enemies) {
                        enemy.moveTankEnemy(player.getX(), player.getY(), tick, where(enemy));
                        enemy.moveTankEnemy(player2.getX(), player2.getY(), tick, where(enemy));
                        for (Bullet bullet : enemy.getBullets()){
                            bullet.move();
                        }
                    }
                    // player collision
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

    public void startSinglePlayer(){
        singleMode = true;
        player.reset();
        player.setPosition(13, 24, Direction.UP);
        setEnemies();
        setBrickBlocks();

        tick = 0;
        notOver = true;
        win = false;
        player1HitEnemy = 0;
        player2HitEnemy = 0;
        thread = new Thread() {
            @Override
            public void run() {
                while(notOver&&!win) {
                    tick++;
                    player.move(where(player), player2);
                    if (!player.getBullets().isEmpty()) {
                        for (int b = 0; b < player.getBullets().size(); b++) {
                            player.getBullets().get(b).move();
                        }
                    }

                    for(int i =0; i < enemies.size(); i++) {
                        for(int b = 0; b < player.getBullets().size(); b++){
                            if (enemies.get(i).collision(player.getBullets().get(b))){
                                player.getBullets().remove(player.getBullets().get(b));
                                enemies.remove(enemies.get(i));
                                player1HitEnemy++;
                            }
                        }
                    }
                    if(enemies.isEmpty()){
                        win = true;
                    }

                    // enemy move and shoot the bullet
                    for(Enemy enemy: enemies) {
                        enemy.moveTankEnemy(player.getX(), player.getY(), tick, where(enemy));
                        for (Bullet bullet : enemy.getBullets()){
                            bullet.move();
                        }
                    }
                    // player collision
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
        if (!singleMode) {
            for (Bullet bp2 : player2.getBullets()) {
                if (player.collision(bp2)) {
                    notOver = false;
                }
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
                } else if(player2.collision(b) && !singleMode){
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
        if (!singleMode) {
            if (!player2.getBullets().isEmpty()) {
                for (int b = 0; b < player2.getBullets().size(); b++) {
                    for (int brick = 0; brick < brickBlocks.size(); brick++) {
                        if (brickBlocks.get(brick).isBulletHit(player2.getBullets().get(b))) {
                            player2.getBullets().remove(player2.getBullets().get(b));
                            brickBlocks.remove(brickBlocks.get(brick));
                            break;
                        }
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
        if (!singleMode) {
            for (int b = 0; b < player2.getBullets().size(); b++) {
                for (int steel = 0; steel < steelBlocks.size(); steel++) {
                    if (steelBlocks.get(steel).isBulletHit(player2.getBullets().get(b))) {
                        player2.getBullets().remove(player2.getBullets().get(b));
                        break;
                    }
                }
            }
        }
    }

    private List<String> where(WObject obj) {
        List<String> blockPosition = new ArrayList<>();
        blockPosition.add("");
        for (BlockBrick brick : brickBlocks) {
            if (brick.getX() == obj.getX() && brick.getY() + 1 == obj.getY()) {
                blockPosition.add("up");
            }
            if (brick.getX() == obj.getX() && brick.getY() - 1 == obj.getY()) {
                blockPosition.add("down");
            }
            if (brick.getX() + 1 == obj.getX() && brick.getY() == obj.getY()) {
                blockPosition.add("left");
            }
            if (brick.getX() - 1 == obj.getX() && brick.getY() == obj.getY()) {
                blockPosition.add("right");
            }
        }
        for (BlockSteel steel : steelBlocks) {
            if (steel.getX() == obj.getX() && steel.getY() + 1 == obj.getY()) {
                blockPosition.add("up");
            }
            if (steel.getX() == obj.getX() && steel.getY() - 1 == obj.getY()) {
                blockPosition.add("down");
            }
            if (steel.getX() + 1 == obj.getX() && steel.getY() == obj.getY()) {
                blockPosition.add("left");
            }
            if (steel.getX() - 1 == obj.getX() && steel.getY() == obj.getY()) {
                blockPosition.add("right");
            }
        }
        for (BlockStream stream : streamBlocks) {
            if (stream.getX() == obj.getX() && stream.getY() + 1 == obj.getY()) {
                blockPosition.add("up");
            }
            if (stream.getX() == obj.getX() && stream.getY() - 1 == obj.getY()) {
                blockPosition.add("down");
            }
            if (stream.getX() + 1 == obj.getX() && stream.getY() == obj.getY()) {
                blockPosition.add("left");
            }
            if (stream.getX() - 1 == obj.getX() && stream.getY() == obj.getY()) {
                blockPosition.add("right");
            }
        }
        return blockPosition;
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

    private void setEnemies(){
        enemies.clear();
        for(int i = 0; i < 5; i++) {
            enemies.add(new Enemy(i*5+3, 0, Direction.DOWN));
        }
        enemies.add(new Enemy(0, 18, Direction.RIGHT));
        enemies.add(new Enemy(0, 22, Direction.RIGHT));
        enemies.add(new Enemy(24, 18, Direction.LEFT));
        enemies.add(new Enemy(24, 22, Direction.LEFT));
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
        brickBlocks.clear();
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

    public int getPlayer1HitEnemy(){
        return player1HitEnemy;
    }

    public int getPlayer2HitEnemy(){
        return player2HitEnemy;
    }

    public boolean isWinning(){
        return win;
    }

    public boolean isSingleMode(){
        return singleMode;
    }
}
