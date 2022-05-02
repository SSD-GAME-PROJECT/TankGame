import java.util.Random;

public abstract class Block {
    private int x;
    private int y;


    Block(int x, int y){
        this.x = x;
        this.y = y;
    }

    public boolean isBulletHit(Bullet bullet) {
        return (this.x == bullet.getX()) && (this.y == bullet.getY());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
