import java.util.ArrayList;

public class Scheduler {
    static ArrayList <pcb> readyQueue = new ArrayList<>();
    ArrayList<pcb> waitQueue = new ArrayList<>();
    static int readyQueueProcesses = 0;
    static int quantum = 4; //processes will execute for 4 milliseconds and then be preeempted

    //Long-term/job Scheduler will add processes to readyQueue
    public void addToReadyQueue(pcb process){
        readyQueue.add(process);
        process.state = ProcessState.READY;
        readyQueueProcesses++;
    }

    //when processes are 1) executing 2)moved to waiting queue
    public void removeFromReadyQueue(pcb process){
        readyQueue.remove(process);
        readyQueueProcesses--;
    }
}
