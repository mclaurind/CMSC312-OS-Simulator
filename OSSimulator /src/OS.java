import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class OS {
    static ArrayDeque<pcb> newQueue; //all processes in system
    static int mainMemorySize = 1024; //OS simulator will have a main memory size of 1024 MB
    static int remMemory = 0; //remaining memory
    static int processTotal;
    static Scheduler [] threads = new Scheduler[4]; //4 threads

    public static void main(String [] args) throws InterruptedException {
        Scanner in = new Scanner(System.in);
        newQueue = new ArrayDeque<>();

        System.out.println("***** OS SIMULATOR *****");
        System.out.print("******* PROGRAMS *******\n\t  Email Client\n\t Word Processor\n\t   Calculator \n\tInternet Browser\n\t Printer Driver \n\t  Spreadsheet\n************************\n");
        System.out.println("Enter the name of the program you would like to run:\n");

        //user chooses a program and its number of processes
        String program = in.nextLine();
        System.out.println("Enter the number of processes for this program:\n");
        processTotal = in.nextInt();
        System.out.println("\n\n\t\tPROCESSES\n**************************");


        //all processes in system will reside in the new queue
        for (int i = 0; i < processTotal; i++) {
            pcb process = (generateProcess(program));
            process.PID = i+1;
            process.arrivalTime = i;
            newQueue.add(process);
            System.out.print(" PROCESS " + process.PID);
            System.out.print(process.toString()+"\n");
            System.out.print("\n");
            remMemory = mainMemorySize - process.memorySize; //tracking memory size
        }

        //cpu1 - round robin scheduler with named pipe for inter-process communication
        //cpu2 - priority scheduler with ordinal pipe for inter-process communication
        System.out.println("Enter the number of the scheduling algorithm you'd like: \n");
        System.out.println("        1) Round Robin            2) Priority");
        int choice = in.nextInt();

        if (choice == 1){
            cpu cpu1 = new cpu("rr");
            cpu1.start();
        }
        else {
            cpu cpu2 = new cpu("p");
            cpu2.start();
        }
    }

    //reads program files and create a process
    public static pcb generateProcess(String programName){
        //formatting file path
        String filePath = System.getProperty("user.dir") + "/Programs/" + programName;
        ArrayList<String> instructions =new ArrayList<>();
        ArrayList<Integer>  minCycles = new ArrayList<>();
        ArrayList<Integer>  maxCycles = new ArrayList<>();
        ArrayList<Integer>  cycles = new ArrayList<>();
        String memorySize = "";

        //parsing file into process instruction and min/max cycle lengths
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            String line;
            line = br.readLine(); //reading first line - memory size of process
            memorySize = line.replace(" MB", "");
            //now to read the instructions and cycles
            while ((line = br.readLine()) != null){
                String [] arr = line.split("\\s+");
                instructions.add(arr[0]);
                minCycles.add(Integer.parseInt(arr[1]));
                maxCycles.add(Integer.parseInt(arr[2]));
            }
            br.close();
        }
        catch (Exception e){
            e.getMessage();
        }
        //randomizing cycle length for each instruction in process
        for (int i = 0; i < instructions.size(); i++){
            cycles.add(new Random().nextInt((maxCycles.get(i) - minCycles.get(i)) + 1 + minCycles.get(i)));
        }
        return new pcb(instructions,cycles,memorySize);
    }
}