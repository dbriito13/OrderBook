import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

public class Utils {
    private static final GarbageCollectorMXBean gcBean = ManagementFactory.getGarbageCollectorMXBeans().get(0);
    private static long lastCollectionCount = gcBean.getCollectionCount();

    public static void checkGC(String message) {
        long currentCollectionCount = gcBean.getCollectionCount();
        if (currentCollectionCount > lastCollectionCount) {
            System.out.println(message + " - GC occurred! New GC count: " + currentCollectionCount);
            lastCollectionCount = currentCollectionCount;
        } else {
            System.out.println(message + " - No GC");
        }
    }
}
