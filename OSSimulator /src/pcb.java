import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

//process control block (pcb) for a process
public class pcb{
    int PID;
    static int newPID = 1000;
    int memorySize;
    int childPID;
    ProcessState state;
    int arrivalTime;
    int burstTime;
    int programCounter;
    String [] instructions;
    Integer[] cycles;

    public pcb (ArrayList<String> instructions, ArrayList<Integer> cycles, String memorySize){
        this.memorySize = Integer.parseInt(memorySize);
        PID = 0;
        childPID = newPID++;
        state = ProcessState.NEW;
        burstTime = new Random().nextInt((10 - 0)+ 1) + 0;
        arrivalTime = 0;
        programCounter = 0;

        this.instructions = new String[instructions.size()];
        for(int i = 0; i < instructions.size(); i++){
            this.instructions[i] = instructions.get(i);
        }

        this.cycles = new Integer[cycles.size()];
        for(int i = 0; i < cycles.size(); i++){
            this.cycles[i] = cycles.get(i);
        }
    }

    @Override
    public String toString() {
        return "\n PID: " + PID +
                "\n Memory Size: " + memorySize + " MB" +
                "\n Current State:" + state +
                "\n Arrival Time: " + arrivalTime +
                "\n Burst Time: " +burstTime +
                "\n Current Program Counter: " + programCounter +
                "\n Instructions: " + Arrays.toString(instructions).replace("[","").replace("]","").replace(","," ") +
                "\n Cycles: " + Arrays.toString(cycles).replace("[","").replace("]","").replace(","," ");
    }
}