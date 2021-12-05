//new scheduler
import java.util.ArrayDeque;
import java.util.ArrayList;

public class Scheduler extends Thread{
    static ArrayDeque<pcb> readyQueue;
    static ArrayList<pcb> waitQueue;
    static ArrayList<pcb> terminatedProcesses;
    static ArrayList<pcb> childProcesses;
    static int cycles = 0; //tracks elapsed cycles of simulator
    static int quantum = 5; //round robin scheduler time quantum
    static boolean simulating = true;
    //static pcb simP; // the current simulating process
    static CSHandler cs; // handles critical section in each process
    static pcb [] simProcess =  new pcb [4];
    static ArrayDeque<pcb> newQueue;
    static int remMemory = 0;

    //Round Robin scheduling algorithm
    public synchronized void roundRobinScheduler(pcb simProcess) throws InterruptedException {
        readyQueue = new ArrayDeque<>();
        waitQueue = new ArrayList<>();
        terminatedProcesses = new ArrayList<>();
        this.childProcesses = new ArrayList<>();
        cs = new CSHandler();
        IODevice ioDevice = new IODevice();
        this.newQueue = OS.newQueue;
        this.remMemory = OS.remMemory;
        pcb simP = simProcess;

        while (simulating) {
            cycles += 1;
            //PROCESSING NEW QUEUE
            //admitting processes to ready queue by arriving time
            if (!newQueue.isEmpty()) {
                for (pcb process : newQueue) {
                    if (process.arrivalTime <= cycles ) {
                        //if there's enough memory for process, then it'll move to ready queue
                        if (remMemory > process.memorySize) {
                            //remMemory = remMemory - process.memorySize; //updating main memory size
                            process.state = ProcessState.READY;
                            readyQueue.add(process);
                            newQueue.remove(process);
                            log("Process " + process.PID + " successfully loaded into main memory.\n");
                        }
                        // or process will stay in new queue and enter wait queue to re-enter the ready queue
                        else{
                            log("Process " + process.PID + " exceeds OS memory limit and will stay in new queue.\n");
                        }

                    }
                }
            }

            //PROCESSING WAIT QUEUE
            //waiting processes will be in waiting state for its number of cycles then moved to ready state
            for (int i = 0; i < waitQueue.size(); ) {
                pcb process = waitQueue.get(i);
                int index = process.programCounter; //for tracking cycles
                process.cycles[index]--; //decrease cycle amount
                //if cycles is up for instruction move on to next instruction
                if (process.cycles[index] <= 0) {
                    process.programCounter++;
                    process.state = ProcessState.READY;
                    readyQueue.add(process);
                    waitQueue.remove(i);
                    continue;
                }
                i++;
            }

            //CHOOSING RUNNING PROCESS
            //Grab a process from ready queue if there's not one being currently simulated
            if (simP == null) {
                if (readyQueue.isEmpty()) {
                    simP = null;
                } else {
                    simP = readyQueue.pop(); //removes process from the front of ready queue
                    log("Process " + simP.PID + " is running\n");
                }
            }

            // if there is one, evaluate its instructions, if there are even instructions left
            //I/O Interrupts may occur during any instruction and are handled accordingly
            else {
                if (simP.programCounter < simP.instructions.length) {
                    switch (simP.instructions[simP.programCounter]){

                        //CALCULATE - process will run on CPU for a number of cycles
                        case "CALCULATE":
                            if(ioDevice.shouldInterrupt()){
                                log(ioDevice.name + " INTERRUPT\n");
                                simP.state =  ProcessState.WAIT;
                                //waitQueue.add(simP);
                                log("Process " + simP.PID + " added to wait queue due to interrupt \n");
                                int suspendProcess = ioDevice.cycle;
                                //Thread.sleep(suspendProcess);
                                //waitQueue.remove(simP);
                                log("\n" + ioDevice.name + " interrupt"+ " handled\n");
                            }
                            else {
                                simP.cycles[simP.programCounter]--;
                                if (simP.cycles[simP.programCounter] <= 0) {
                                    simP.programCounter++;
                                }
                                log("Process " + simP.PID + " is running (CALCULATE)\n");
                            }
                            //continue;

                            //I/O - process will enter waiting state for a number of cycles
                        case "I/O":
                            if(ioDevice.shouldInterrupt()){
                                log(ioDevice.name + " INTERRUPT\n");
                                simP.state =  ProcessState.WAIT;
                                //waitQueue.add(simP);
                                log("Process " + simP.PID + " added to wait queue due to interrupt \n");
                                int suspendProcess = ioDevice.cycle;
                                //Thread.sleep(suspendProcess);
                                //waitQueue.remove(simP);
                                log("\n" + ioDevice.name + " interrupt"+ " handled\n");
                            }
                            else {
                                simP.state = ProcessState.WAIT;
                                waitQueue.add(simP);
                                log("Process " + simP.PID + " is waiting (I/O)\n");
                                if (readyQueue.isEmpty()) {
                                    simP = null;
                                } else {
                                    simP = readyQueue.pop();
                                    simP.state = ProcessState.RUN;
                                    log("Process " + simP.PID + " is running\n");
                                }
                            }
                            //continue;

                            //FORK - child process will be created for simulated process
                            //Single level parent child relationship - every process will include a FORK instruction
                        case "FORK":
                            if(ioDevice.shouldInterrupt()){
                                log(ioDevice.name + " INTERRUPT\n\n");
                                simP.state =  ProcessState.WAIT;
                                //waitQueue.add(simP);
                                log("Process " + simP.PID + " added to wait queue due to interrupt \n");
                                int suspendProcess = ioDevice.cycle;
                                //Thread.sleep(suspendProcess);
                                //waitQueue.remove(simP);
                                log( ioDevice.name + " interrupt"+ " handled\n");
                            }
                            else {
                                pcb childP = simP;
                                childP.state = ProcessState.NEW;
                                childProcesses.add(childP); //child processes are created and added to new queue
                                log("Child process " + childP.childPID + " created for Process " + simP.PID + " (FORK)\n");
                            }
                            //continue;

                            //CRIT_START - Tag denotes when a process is running during its critical section
                        case "CRIT_START":
                            log("Process " + simP.PID + " has entered its critical section\n");
                            cs.waitSem(cs.sem, simP, 1);
                            simP.programCounter++;
                            //continue;

                            //CRIT_END - Tag denotes when a process has completed its critical section
                        case "CRIT_END":
                            cs.signalSem(cs.sem,simP, 1);
                            simP.programCounter++;
                            CSHandler.Semaphore.value--; //keeps track of permit so processes can execute their cs one at a time
                            Thread.sleep(500);
                            log("Process " + simP.PID + " has exited its critical section\n");
                            //break;

                        default:
                    }
                }

                //TERMINATED PROCESSES
                //all instructions for process have been evaluated, so it should be terminated
                else{
                    simP.state = ProcessState.TERMINATE;
                    terminatedProcesses.add(simP);
                    remMemory = remMemory + simP.memorySize; //freeing main memory for other processes
                    log("Process " + simP.PID + " has terminated\n");
                    log("Freed " + simP.memorySize + " MB of main memory from Process " + simP.PID + "\n");
                    if (readyQueue.isEmpty()){
                        simP = null;
                    }
                    else{
                        simP = readyQueue.pop();
                        simP.state = ProcessState.RUN;
                        log("Process " + simP.PID + " is running\n");
                    }
                }

                //ROUND ROBIN SCHEDULING - process will run its amount cycles in relation to the time quantum
                if(cycles % quantum == 0 && simP != null) {
                    //time is up so process will go back to ready queue
                    simP.state = ProcessState.READY;
                    readyQueue.add(simP);
                    log("Process " + simP.PID + " is ready \n");
                    //choose next process
                    if (readyQueue.isEmpty()){
                        simP = null;
                    }
                    else{
                        simP = readyQueue.pop();
                        simP.state = ProcessState.RUN;
                        log("Process " + simP.PID + " is running\n");
                    }
                }

                //once terminated processes equal the process total, simulation is completed
                if (terminatedProcesses.size() == newQueue.size()){
                    simulating = false;
                }

            }
        }
        //adding child processes to new queue, will be processed and terminated which are displayed to console
        //will be checked against memory size, ran, and terminated as soon when its parent terminates.
        //CASCADING TERMINATION - terminated child processes will be displayed to user.
        for (pcb c : childProcesses){
            newQueue.add(c);
            log("Child Process " + c.childPID + " added to new queue\n");
        }
    }

    //records scheduler activities
    public static void log(String activity) {
        StringBuilder sb = new StringBuilder();
        sb.append(activity);
        System.out.print(sb);
    }

    @Override
    public void run(){
        int threadNum = Integer.parseInt(Thread.currentThread().getName());
        for (int i = 0; i < simProcess.length; i++){
            simProcess[i] = null;
        }
        try {
            roundRobinScheduler(simProcess[threadNum]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}