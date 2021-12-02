//OS will have two CPUS
//8 threads in totals for OS, 4 per CPU
//Each CPU will have its own scheduler and I/O handlers
//CPUs can process interrupts concurrently
//Each cpu will also share memory
public class cpu {
    Scheduler scheduler;

    public cpu(){
        this.scheduler = new Scheduler();
    }
}