import java.util.ArrayList;

//Semaphores will be used to resolve critical section
public class CSHandler {
    Semaphore sem = new Semaphore();

    //makes other processes wanting to execute their CS wait until semaphore permit is available
    public void waitSem(Semaphore semaphore, pcb process, int permit){
        sem = semaphore;
        permit = sem.value;
        permit--;
        if (permit < 0){
            semaphore.block(process);
        }
    }

    //signifies processes in wait queue that permit is available
    public void signalSem(Semaphore semaphore, pcb process,int permit){
        sem = semaphore;
        permit = sem.value;
        permit++;
        if (permit <= 0){
            //remove process from wait queue
            //allow it to process resource
            semaphore.wakeup(process);
        }
    }

    //simulates a semaphore
    public static class Semaphore{
        static int value;
        static ArrayList<pcb> waitQueue = new ArrayList<>();
        static ArrayList<pcb> readyQueue = new ArrayList<>();

        //process invoking operation placed in waiting queue
        public void block(pcb process){
            waitQueue.add(process);
            System.out.print("Process " + process.PID + " sent to semaphore wait queue\n");
        }

        //remove process from wait queue and into in ready queue for critical section execution
        public void wakeup(pcb process){
            readyQueue.add(process);
            waitQueue.remove(process);
            System.out.print("PROCESS " + process.PID + " IN CS \n");
        }
    }
}

