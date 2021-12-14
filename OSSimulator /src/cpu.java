//OS will have two CPUS
//8 threads in totals for OS, 4 per CPU
//Each CPU will have its own scheduler and I/O handlers
//CPUs can process interrupts concurrently

public class cpu extends Thread {
    Scheduler rr;
    PriorityScheduler p;
    String schedulerType;

    public cpu (String schedulerType){
        this.schedulerType = schedulerType;
        this.rr = new Scheduler();
        this.p = new PriorityScheduler();
    }

    @Override
    public synchronized void run(){

        if (this.schedulerType == "rr"){
            System.out.print("\nROUND ROBIN SCHEDULER ACTIVITY LOG\n*********************************\n");
            Scheduler [] threads = new Scheduler[4]; //4 threads
            for (int i = 0; i < 4; i++) {
                threads[i] = new Scheduler();
                threads[i].setName(Integer.toString(i));
                threads[i].start();
                System.out.print("CHILD PROCESS " + Scheduler.terminatedProcesses.get(i).childPID + "\n PID: " + Scheduler.terminatedProcesses.get(i).childPID + Scheduler.terminatedProcesses.get(i).toString().replace("PID: ", "PPID: ") + "\n\n");
            }
            /*System.out.print("******************* TERMINATED CHILD PROCESSES (ROUND ROBIN) *******************\n");*/
           /* for (int i= 0; i < Scheduler.childProcesses.size(); i++){
                System.out.print("CHILD PROCESS " + Scheduler.childProcesses.get(i).childPID + "\n PID: " + Scheduler.childProcesses.get(i).childPID + Scheduler.childProcesses.get(i).toString().replace("PID: ", "PPID: ") + "\n\n");
            }*/
        }

        else{
            System.out.print("\nPRIORITY ACTIVITY LOG\n*********************************\n");
            PriorityScheduler [] threads = new PriorityScheduler[4]; //4 threads
            for (int i = 0; i < 4; i++) {
                threads[i] = new PriorityScheduler();
                threads[i].setName(Integer.toString(i));
                threads[i].start();
                System.out.print("CHILD PROCESS " + PriorityScheduler.terminatedProcesses.get(i).childPID + "\n PID: " + PriorityScheduler.terminatedProcesses.get(i).childPID + PriorityScheduler.terminatedProcesses.get(i).toString().replace("PID: ", "PPID: ") + "\n\n");
            }
        }
    }
}