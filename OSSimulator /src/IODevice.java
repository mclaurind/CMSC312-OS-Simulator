import java.util.Random;

//simulate IO Devices
public class IODevice {
    String name;
    int cycle;

    public IODevice(){
        //choosing I/O device
        String type1 = "Keyboard";
        String type2 = "Mouse";
        String type3 = "Monitor";
        int i = new Random().nextInt((3 - 1)+ 1) + 1;
        if (i == 1){
            name = type1;
        }
        else if (i == 2){
            name = type2;
        }
        else{
            name = type3;
        }

        //generating cycle length for interrupt
        cycle = new Random().nextInt((100 - 1)+ 1) + 1;
    }

    //interrupts running process for I/O device completion
    //Each instruction will have the chance for an I/O interrupt
    public boolean shouldInterrupt(){
        Random r = new Random();
        return r.nextBoolean();
    }
}
