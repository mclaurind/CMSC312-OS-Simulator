//used to track schedulers for comparison

public class clock {

    static int clock;

    public clock(){
        this.clock = 0;
    }

    public void tiktok(){
        clock++;
    }

    public int getTime() {
        return clock;
    }
}
