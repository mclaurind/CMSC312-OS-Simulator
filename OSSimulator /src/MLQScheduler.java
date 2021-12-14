import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//multilevel queue scheduling algorithm
public class MLQScheduler {
    ArrayDeque<pcb> newQueue;
    ArrayList<pcb> temp;

    public synchronized void MLQScheduling() throws InterruptedException {
        this.temp = new ArrayList<>();
        this.newQueue = OS.newQueue;
        for (pcb p : newQueue){
            temp.add(p);
        }
        //sort new queue by priorities first
        Collections.sort(temp, new PriorityScheduler.pcbComparator());

        //partition processes into 3 queues by priority
        List <List<pcb>> queues= getBatches(temp,3);
        List<pcb> queue1 = queues.get(0);
        List<pcb> queue2 = queues.get(1);
        List<pcb> queue3 =  queues.get(2);


        ArrayDeque <pcb> systemProcesses = new ArrayDeque<>();
        ArrayDeque <pcb> interactiveProcesses = new ArrayDeque<>();
        ArrayDeque <pcb> batchProcesses = new ArrayDeque<>();

        for (pcb p : queue1){
            systemProcesses.add(p);
        }
        for (pcb p : queue2){
            interactiveProcesses.add(p);
        }
        for (pcb p : queue3){
            batchProcesses.add(p);
        }

        PriorityScheduler p = new PriorityScheduler();
        Scheduler s1 = new Scheduler();
        Scheduler s2 = new Scheduler();

        p.priorityScheduling(batchProcesses,0);
        s1.roundRobinScheduler(systemProcesses,0);
        s2.roundRobinScheduler(interactiveProcesses,0);
    }

    //breaks up new processes into three queues
    public static <T> List<List<T>> getBatches(List<T> collection, int batchSize) {
        return IntStream.iterate(0, i -> i < collection.size(), i -> i + batchSize)
                .mapToObj(i -> collection.subList(i, Math.min(i + batchSize, collection.size())))
                .collect(Collectors.toList());
    }
}