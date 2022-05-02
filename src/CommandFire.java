
public class CommandFire extends Command{


    public CommandFire(Player player, int tick) {
        super(player, tick);
    }

    @Override
    public void execute() {
        getPlayer().fire();
    }
}
