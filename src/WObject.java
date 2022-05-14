import java.util.ArrayList;
import java.util.List;

public abstract class WObject {

    private int x;
    private int y;

    private int dx;
    private int dy;
    private Direction direction;
    private List<Bullet> bullets = new ArrayList<Bullet>();
    private final long PERIOD = 250L; // Adjust to suit timing
    private long lastTime = System.currentTimeMillis() - PERIOD;

    public WObject() {
    }

    public WObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void turnNorth() {
        direction = Direction.UP;
        dx = 0;
        dy = -1;
    }

    public void turnSouth() {
        direction = Direction.DOWN;
        dx = 0;
        dy = 1;
    }

    public void turnWest() {
        direction = Direction.LEFT;
        dx = -1;
        dy = 0;
    }

    public void turnEast() {
        direction = Direction.RIGHT;
        dx = 1;
        dy = 0;
    }

    public void move(String nearBlock) {
        if(((nearBlock == "up" || y == 0) && dy == -1) || ((nearBlock == "down" || y == 24) && dy == 1 )) {
            dy = 0;
        }else if(((nearBlock == "right" || x == 24) && dx == 1) || ((nearBlock == "left" || x == 0) && dx == -1)) {
            dx = 0;
        }
        this.x += dx;
        this.y += dy;

    }

    public void moveTankEnermy(int disX, int disY, int tick, String nearBlock) {
        if (this.x == disX && this.y > disY) {
            if(nearBlock != "up") {
                this.y -= 1;
                direction = Direction.UP;
            }
            this.fire();
        } else if (this.x == disX && this.y < disY) {
            if(nearBlock!="down") {
                this.y += 1;
                direction = Direction.DOWN;
            }
            this.fire();
        } else if (this.x > disX && this.y == disY) {
            if(nearBlock!="left") {
                this.x -= 1;
                direction = Direction.LEFT;
            }
            this.fire();
        } else if (this.x < disX && this.y == disY) {
            if(nearBlock!="right") {
                this.x += 1;
                direction = Direction.RIGHT;
            }
            this.fire();
        }
    }

    public void fire() {
        long thisTime = System.currentTimeMillis();

        if ((thisTime - lastTime) >= PERIOD) {
            lastTime = thisTime;
            Bullet b = new Bullet(this.getX(), this.getY(), direction);
            b.setPosition(b.getX(), b.getY());
            b.move();
            bullets.add(b);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void reset() {
        dx = dy = 0;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean hit(WObject wObj) {
        return x == wObj.x && y == wObj.y;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public boolean collision(Bullet b) {
        return b.getX() == this.x && b.getY() == this.y;
    }
}
