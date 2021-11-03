//Clock - used to track process execution in relation to quantum
public class Clock{
    private static int tiktok = 0;

    public int getTime(){
        return tiktok;
    }

    public static int addTime(){
        return tiktok++;
    }

    public static void resetClock() {
        tiktok = 0;
    }

}