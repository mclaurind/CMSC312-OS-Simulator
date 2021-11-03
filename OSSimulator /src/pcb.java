import java.util.ArrayList;
import java.util.Random;

public class pcb{
    int PID;
    ProcessState state;
    int arrivalTime;
    int burstTime;
    int programCounter;
    String [] instructions;
    Integer[] cycles;
    //String [] critStart;
    //String [] critEnd;

    public pcb (ArrayList<String> instructions, ArrayList<Integer> cycles){
        PID = 0;
        state = ProcessState.NEW;
        arrivalTime = 0;
        burstTime = new Random().nextInt((10 - 0)+ 1) + 0;
        programCounter = 0;

        this.instructions = new String[instructions.size()];
        for(int i = 0; i < instructions.size(); i++){
            this.instructions[i] = instructions.get(i);
        }

        this.cycles = new Integer[cycles.size()];
        for(int i = 0; i < cycles.size(); i++){
            this.cycles[i] = cycles.get(i);
        }


        //length of both sets will be same size since one critical section per process
        //this.critStart = new String[critStart.size()];
        //for(int i = 0; i <  critStart.size(); i++){
            //this.critStart[i] = critStart.get(i);
        //}

        //this.critEnd = new String[critEnd.size()];
        //for(int i = 0; i < cycles.size(); i++){
            //this.critEnd[i] = critEnd.get(i);
        //}
    }
}