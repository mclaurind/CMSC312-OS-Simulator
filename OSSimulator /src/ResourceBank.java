import java.util.ArrayList;
import java.util.Random;

public class ResourceBank {
    ArrayList <Resource> resources;

    public ResourceBank(){
        this.resources = new ArrayList<>();
        Resource r1 = new Resource();
        Resource r2 = new Resource();
        Resource r3 = new Resource();
        r1.name = "Resource 1";
        r2.name = "Resource 2";
        r3.name = "Resource 3";
        resources.add(r1);
        resources.add(r2);
        resources.add(r3);
    }

    public static class Resource{
        String name;
        int amount;

        public Resource(){
            this.name = "";
            this.amount = new Random().nextInt((10 - 1 +1)+1);
        }

        @Override
        public String toString() {
            return name + " (" +amount + ")";
        }
    }
}
