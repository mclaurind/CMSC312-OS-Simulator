import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

public class Scheduler extends Thread{
    static ArrayDeque<pcb> readyQueue;
    static ArrayList<pcb> waitQueue;
    static ArrayList<pcb> terminatedProcesses;
    static ArrayList <pcb> childProcesses;
    static int cycles = 0; //tracks elapsed cycles of simulator
    static int quantum = 4; //round robin scheduler time quantum
    static boolean simulating = true;
    static pcb simP; // the current simulating process
    static CSHandler cs; // handles critical section in each process
    static clock clock;
    static pcb[] simProcesses = new pcb[4]; //for threads of CPU
    ArrayList <pcb> tempReady;

    //Round Robin scheduling algorithm
    public synchronized void roundRobinScheduler(ArrayDeque<pcb> processes, int t) throws InterruptedException {
        readyQueue = new ArrayDeque<>();
        waitQueue = new ArrayList<>();
        terminatedProcesses = new ArrayList<>();
        childProcesses = new ArrayList<>();
        simP = null;
        cs = new CSHandler();
        IODevice ioDevice = new IODevice();
        clock = new clock();
        ArrayDeque<pcb> newQueue = processes;
        int remMemory = OS.remMemory;
        int processTotal = OS.processTotal;
        tempReady = new ArrayList<>();
        NamedPipe pipe = new NamedPipe();
        ResourceBank bank = new ResourceBank();

        while (simulating) {
            clock.tiktok();
            cycles += 1;
            //PROCESSING NEW QUEUE
            //admitting processes to ready queue by arriving time
            if (!newQueue.isEmpty()) {
                for (pcb process : newQueue) {
                    if (process.arrivalTime <= cycles ) {
                        //if there's enough memory for process, then it'll move to ready queue
                        if (remMemory > process.memorySize) {
                            remMemory = remMemory - process.memorySize; //updating main memory size
                            process.state = ProcessState.READY;
                            tempReady.add(process);
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

            if (t == 1){
                //assigning threads to 4 diff processes
                int thread = Integer.parseInt(Thread.currentThread().getName());
                for (int i = 0; i < simProcesses.length; i++){
                    simProcesses[thread] = tempReady.get(thread);
                }
            }

            //now adding processes to ready queue
            for (pcb p : tempReady) {
                readyQueue.add(p);
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
                                waitQueue.add(simP);
                                log("Process " + simP.PID + " added to wait queue due to interrupt \n");
                                int suspendProcess = ioDevice.cycle;
                                Thread.sleep(suspendProcess);
                                waitQueue.remove(simP);
                                clock.tiktok();
                                log("\n" + ioDevice.name + " interrupt"+ " handled\n");
                            }
                            else {
                                //process may send message
                                if (pipe.isSendMessage()){
                                    int ran = new Random().nextInt( new Random().nextInt((processTotal - 0)+ 0) + 1);
                                    pcb receiver = tempReady.get(ran);
                                    if (receiver.PID != simP.PID) {
                                        Message msg = new Message("Process " + simP.PID + " sent message to Process " + receiver.PID + "\n", receiver);
                                        pipe.messages.add(msg);
                                        log("Message for Process " + receiver.PID + " from Process " + simP.PID + "(NAMED PIPE MESSAGE) \n");
                                    }
                                }
                                //process may also request resources
                                //Banker's algorithm will be used to avoid deadlock
                                // at least one process should be able to acquire its maximum possible set of resources, and proceed to termination.
                                boolean resourceRequest = new Random().nextBoolean();
                                if (resourceRequest){
                                    int maxNeed = new Random().nextInt((10 - 1 +1)+1) ;
                                    int currNeed =new Random().nextInt((10 - 1 +1)+1);
                                    int res = new Random().nextInt((3 - 0)+ 0) + 0;
                                    log("Process " + simP.PID + " requests an amount of " + currNeed + " of " + bank.resources.get(res).toString() +" (RESOURCE REQUEST)\n");
                                    if (bank.resources.get(res).amount >= maxNeed){
                                        bank.resources.get(res).amount = bank.resources.get(res).amount - currNeed;
                                        int usedResources = bank.resources.get(res).amount;
                                        log("Process " + simP.PID + " resource request approved (RESOURCE REQUEST APPROVED)\n");
                                    }
                                    // unsafe state so resource request is denied
                                    else{
                                        log("Process " + simP.PID + " resource request denied (DEADLOCK AVOIDANCE)\n");
                                    }
                                    //
                                }
                                simP.cycles[simP.programCounter]--;
                                if (simP.cycles[simP.programCounter] <= 0) {
                                    simP.programCounter++;
                                }
                                log("Process " + simP.PID + " is running (CALCULATE)\n");
                            }
                            continue;

                        //I/O - process will enter waiting state for a number of cycles
                        case "I/O":
                            if(ioDevice.shouldInterrupt()){
                                log(ioDevice.name + " INTERRUPT\n");
                                simP.state =  ProcessState.WAIT;
                                waitQueue.add(simP);
                                log("Process " + simP.PID + " added to wait queue due to interrupt \n");
                                int suspendProcess = ioDevice.cycle;
                                Thread.sleep(suspendProcess);
                                waitQueue.remove(simP);
                                clock.tiktok();
                                log("\n" + ioDevice.name + " interrupt"+ " handled\n");
                            }
                            else {
                                simP.state = ProcessState.WAIT;
                                waitQueue.add(simP);
                                log("Process " + simP.PID + " is waiting (I/O)\n");

                                //process may send message
                                if (pipe.isSendMessage()){
                                    int ran = new Random().nextInt( new Random().nextInt((processTotal - 0)+ 0) + 1);
                                    pcb receiver = tempReady.get(ran);
                                    if (receiver.PID != simP.PID) {
                                        Message msg = new Message("Process " + simP.PID + " sent message to Process " + receiver.PID + "\n", receiver);
                                        pipe.messages.add(msg);
                                        log("Message for Process " + receiver.PID + " from Process " + simP.PID + "(NAMED PIPE MESSAGE) \n");
                                    }

                                }
                                if (readyQueue.isEmpty()) {
                                    simP = null;
                                } else {
                                    simP = readyQueue.pop();
                                    simP.state = ProcessState.RUN;
                                    log("Process " + simP.PID + " is running\n");
                                }
                            }
                            continue;

                            //FORK - child process will be created for simulated process
                            //Single level parent child relationship - every process will include a FORK instruction
                        case "FORK":
                            if(ioDevice.shouldInterrupt()){
                                log(ioDevice.name + " INTERRUPT\n\n");
                                simP.state =  ProcessState.WAIT;
                                waitQueue.add(simP);
                                log("Process " + simP.PID + " added to wait queue due to interrupt \n");
                                int suspendProcess = ioDevice.cycle;
                                Thread.sleep(suspendProcess);
                                waitQueue.remove(simP);
                                clock.tiktok();
                                log( ioDevice.name + " interrupt"+ " handled\n");
                            }
                            else {
                                pcb childP = simP;
                                childP.state = ProcessState.NEW;
                                childProcesses.add(childP); //child processes are created and added to new queue
                                if (simP.cycles[simP.programCounter] <= 0) {
                                    simP.programCounter++;//onto next instruction
                                }
                                log("Child process " + childP.childPID + " created for Process " + simP.PID + " (FORK)\n");

                                //multi-level parent-child relationship
                                //cascading termination- once parent terminates, children will, and children's children will also terminate.
                                for (int i = 0; i <childProcesses.size(); i++){
                                    for (int j = 0; j < childProcesses.size(); j++){
                                        if (childProcesses.get(i).childPID == childProcesses.get(j).childPID) {
                                            pcb forkedChild = childProcesses.get(j);
                                            //child process will be a  now parent
                                            forkedChild.PID = childProcesses.get(i).childPID;
                                            forkedChild.childPID++;
                                            log("Child process " + childProcesses.get(i).childPID + " has a child, Process " + forkedChild.childPID + " (FORK)\n");
                                        }
                                    }

                                }

                            }
                            break;

                            //CRIT_START - Tag denotes when a process is running during its critical section
                        case "CRIT_START":
                            log("Process " + simP.PID + " has entered its critical section\n");
                            cs.waitSem(cs.sem, simP, 1);
                            simP.programCounter++;
                            continue;

                            //CRIT_END - Tag denotes when a process has completed its critical section
                        case "CRIT_END":
                            cs.signalSem(cs.sem,simP, 1);
                            simP.programCounter++;
                            CSHandler.Semaphore.value--; //keeps track of permit so processes can execute their cs one at a time
                            Thread.sleep(500);
                            log("Process " + simP.PID + " has exited its critical section\n");
                            continue;
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
                if (terminatedProcesses.size() == processTotal){
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

        log("\nRound Robin Scheduling Total Time: " + clock.getTime()+ " \n");
        for (int i= 0; i < childProcesses.size(); i++){
            childProcesses.get(i).state = ProcessState.TERMINATE;
            System.out.print("\nCHILD PROCESS " + childProcesses.get(i).childPID + "\n PID: " + childProcesses.get(i).childPID + childProcesses.get(i).toString().replace("PID: ", "PPID: ") + "\n\n");
        }
    }

    //records scheduler activities
    public static void log(String activity) {
        StringBuilder sb = new StringBuilder();
        sb.append(activity);
        System.out.print(sb);
    }

    //for threads
    @Override
    public void run() {
        try {
            roundRobinScheduler(OS.newQueue,1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //1st inter-process method - doesn't require a child parent relationship
    //processes may send messages during I/O or CALCULATE instructions
    public class NamedPipe{
        ArrayList <Message> messages;

        public NamedPipe(){
            messages = new ArrayList<>();
        }

        public boolean isSendMessage(){
            return new Random().nextBoolean();
        }

    }
    public class Message{
        String message;
        pcb receiver;

        public Message(String message, pcb receiver){
            this.message = message;
            this.receiver = receiver;
        }
    }
}