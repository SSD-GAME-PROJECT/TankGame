public class Bullet {
    private Direction direction;
    private int x;
    private int y;

    public Bullet(int x, int y, Direction direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void move(){
        this.setPosition(getX() + direction.getX(), getY() + direction.getY());
    }
}


