import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class WObject {

    private int x;
    private int y;

    private int dx;
    private int dy;
    private Direction direction;
    private List<Bullet> bullets = new ArrayList<Bullet>();
    private final long PERIOD = 250L; // Adjust to suit timing
    private long lastTime = System.currentTimeMillis() - PERIOD;

    public WObject(int x, int y, Direction direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
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

    public void move(List nearBlock, WObject obj) {
        if (((nearBlock.contains("up") || y == 0 || (obj.getY() == y - 1 && obj.getX() == x)) && dy == -1) || ((nearBlock.contains("down") || y == 24 || (obj.getY() == y + 1 && obj.getX() == x)) && dy == 1)) {
            dy = 0;
        } else if (((nearBlock.contains("right") || x == 24 || (obj.getX() == x + 1 && obj.getY() == y)) && dx == 1) || ((nearBlock.contains("left") || x == 0 || (obj.getX() == x - 1 && obj.getY() == y)) && dx == -1)) {
            dx = 0;
        }
        this.x += dx;
        this.y += dy;

    }

    public void moveTankEnemy(int disX, int disY, int tick, List nearBlock) {
        if (this.x == disX && this.y > disY) {
            if (!nearBlock.contains("up")) {
                direction = Direction.UP;
            }
            this.fire();
        } else if (this.x == disX && this.y < disY) {
            if (!nearBlock.contains("down")) {
                direction = Direction.DOWN;
            }
            this.fire();
        } else if (this.x > disX && this.y == disY) {
            if (!nearBlock.contains("left")) {
                direction = Direction.LEFT;
            }
            this.fire();
        } else if (this.x < disX && this.y == disY) {
            if (!nearBlock.contains("right")) {
                direction = Direction.RIGHT;
            }
            this.fire();
        }
        if (tick % 4 == 0) {
            if (direction != Direction.RIGHT && !nearBlock.contains("right") && x <= 23) {
                this.x += 1;
                direction = Direction.RIGHT;
            }
        }
        if (tick % 4 == 1) {
            if (direction != Direction.UP && !nearBlock.contains("up") && y >= 1) {
                this.y -= 1;
                direction = Direction.UP;
            }
        }
        if (tick % 5 == 2) {
            if (direction != Direction.LEFT && !nearBlock.contains("left") && x >= 1) {
                this.x += -1;
                direction = Direction.LEFT;
            }
        }
        if (tick % 4 == 3){
            if (direction != Direction.DOWN && !nearBlock.contains("down") && y <= 23) {
                this.y += 1;
                direction = Direction.DOWN;
            }
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

    public void setPosition(int x, int y, Direction direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
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

    public Direction getDirection() {
        return direction;
    }
}
