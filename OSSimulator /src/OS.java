import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class OS {
    public static void main(String [] args) {
        boolean simulating = true;
        Scanner in = new Scanner(System.in);
        Scheduler scheduler = new Scheduler();
        ArrayList<pcb> newQueue = new ArrayList<>();
        ArrayList<pcb>  terminatedProcesses = new ArrayList<>();
        ArrayList<pcb>  runningProcess = new ArrayList<>();

        Clock clock = new Clock();

        while (simulating) {
            System.out.println("OS SIMULATOR\n");
            System.out.print("Programs: Email Client | Word Processor | Calculator | Internet Browser | Printer Driver | Spreadsheet\n");
            System.out.println("Enter the name of the program you would like to run:\n");

            //user chooses a program and its number of processes
            String program = in.nextLine();
            System.out.println("Enter the number of processes for this program:\n");
            int processTotal = in.nextInt();
            System.out.println("Creating " + processTotal + " processes...");

            //new processes will be added to the newQueue aka job queue.
            //each process will be in the new state
            for (int i = 0; i < processTotal; i++) {
                pcb process = (generateProcess());
                process.PID = i;
                process.state = ProcessState.NEW;
                newQueue.add(process);
            }

            //adding processes into ready queue
            for (int i = 0; i < newQueue.size(); i++) {
                scheduler.addToReadyQueue(newQueue.get(i));
                i++;
            }

            //evaluating queues for CALCULATE
            for (int i = 0; i < scheduler.readyQueue.size(); i++){
                clock.resetClock(); //for quantum
                int newRunningTime = 0;
                int timeLeft = 0;
                scheduler.readyQueue.get(i).programCounter = i;
                if (scheduler.readyQueue.get(i).instructions[i] == "CALCULATE"){
                    scheduler.readyQueue.get(i).state = ProcessState.RUN;
                    runningProcess.add(scheduler.readyQueue.get(i));
                    scheduler.readyQueue.get(i).arrivalTime = i;//setting arrival time to queue
                    while (runningProcess.get(i).state == ProcessState.RUN && runningProcess.get(i).cycles[i] != 0){
                        newRunningTime = clock.addTime();
                        if (newRunningTime > scheduler.quantum){ //time is up for current running process so back to readyQueue
                            timeLeft += runningProcess.get(i).burstTime - scheduler.quantum;
                            runningProcess.get(i).state = ProcessState.READY;
                            scheduler.addToReadyQueue(runningProcess.get(i));

                        }
                        //process is finished so add to terminated list
                        else {
                            timeLeft = 0;
                            runningProcess.get(i).state = ProcessState.TERMINATE;
                            terminatedProcesses.add( runningProcess.remove(i)); //added to terminated process list

                        }
                    }
                }

                // now for I/O instruction
                else if (scheduler.readyQueue.get(i).instructions[i] == "I/O"){
                    scheduler.readyQueue.get(i).state = ProcessState.WAIT;
                    scheduler.removeFromReadyQueue(scheduler.readyQueue.get(i)); //removing from ready queue and adding to waiting queue
                    scheduler.waitQueue.add(scheduler.readyQueue.get(i));

                    while (scheduler.waitQueue.get(i).cycles[i] > 0){
                        scheduler.waitQueue.get(i).state = ProcessState.WAIT;
                    }

                    //after cycle length is reach, process is added back to ready queue
                    scheduler.readyQueue.add(scheduler.waitQueue.get(i));
                }

                else {
                    //for critical sections - moves processes between wait and ready queue
                    if (scheduler.readyQueue.get(i).instructions[i] != "CALCULATE" && (scheduler.readyQueue.get(i).instructions[i] != "I/O")) {
                        if (scheduler.readyQueue.get(i).instructions[i] == "CRIT_START") {
                            for (int j = 0; i < scheduler.readyQueue.size(); j++) {
                                if (scheduler.readyQueue.get(j) != scheduler.readyQueue.get(i)) {
                                    //any other processes will be added to waiting queue to wait for process
                                    scheduler.waitQueue.add(scheduler.readyQueue.get(j));
                                    if (scheduler.readyQueue.get(i).instructions[i] == "CRIT_END") {
                                        scheduler.waitQueue.remove(scheduler.waitQueue.get(j));
                                        scheduler.waitQueue.add(scheduler.waitQueue.get(j));//add other processes back into ready queue
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //outputting new, ready, waiting, terminated lists for user
            System.out.print("PROCESSES IN NEW QUEUE\n");
            System.out.print("____________________________________________________\n");
            for (int i = 0; i < newQueue.size(); i++){
                System.out.print("Process " + newQueue.get(i).PID + " | ");
            }
            System.out.print("\n\n\n");

            System.out.print("\n\nPROCESSES IN WAITING QUEUE\n");
            System.out.print("____________________________________________________\n");
            for (int i = 0; i < scheduler.waitQueue.size(); i++){
                System.out.print("Process " + scheduler.waitQueue.get(i).PID + " | ");
            }
            System.out.print("\n\n\n");

            System.out.print("\n\nPROCESSES IN READY QUEUE\n");
            System.out.print("____________________________________________________\n");
            for (int i = 0; i < scheduler.readyQueue.size(); i++){
                System.out.print("Process: " + scheduler.readyQueue.get(i).PID + " ");
            }
            System.out.print("\n\n\n");

            System.out.print("\n\nRunning Processes\n");
            System.out.print("____________________________________________________\n");
            for (int i = 0; i < runningProcess.size(); i++){
                System.out.print("Process: " + runningProcess.get(i).PID + " ");
            }
            System.out.print("\n\n\n");

            System.out.print("\n\nTERMINATED PROCESSES\n");
            System.out.print("____________________________________________________\n");
            for (int i = 0; i < terminatedProcesses.size(); i++){
                System.out.print("Process: " + terminatedProcesses.get(i).PID);
            }
            System.out.print("\n\n\n");
        }
    }

    //reads program file to create a process
    public static pcb generateProcess(){
        //formatting file path
        //programName = "/Users/darrielmclaurin/IdeaProjects/OS Simulator/Programs/" + programName;
        ArrayList<String> instructions =new ArrayList<>();
        ArrayList<Integer>  minCycles = new ArrayList<>();
        ArrayList<Integer>  maxCycles = new ArrayList<>();
        ArrayList<Integer>  cycles = new ArrayList<>();

        //intervals for critical sections
        //ArrayList<String>  critStart = new ArrayList<>();
        //ArrayList<String>  critEnd = new ArrayList<>();



        //parsing file into process instruction and min/max cycle lengths
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("/Users/darrielmclaurin/IdeaProjects/OS Simulator/Programs/Calculator")));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null){
                String [] arr = line.split("\\s+");
                //if (arr[0] == "CRIT_START"){
                //critStart.add(arr[0]);
                //}
                //if(arr[0] == "CRIT_END"){
                //critEnd.add(arr[0]);
                //}

                instructions.add(arr[0]);
                minCycles.add(Integer.parseInt(arr[1]));
                maxCycles.add(Integer.parseInt(arr[2]));

            }
        }
        catch (Exception e){
            e.getMessage();
        }

        //randomizing cycle length for each instruction in process
        for (int i = 0; i < instructions.size(); i++){
            cycles.add(new Random().nextInt((maxCycles.get(i) - minCycles.get(i)) + 1 + minCycles.get(i)));
        }
        return new pcb(instructions,cycles);
    }
}