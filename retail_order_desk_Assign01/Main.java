import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Main {
    static class Order {
        String orderId, customerId;
        BigDecimal amount;
        LocalDateTime createdAt;
        Order(String orderId, String customerId, BigDecimal amount, LocalDateTime createdAt) {
            this.orderId = orderId; this.customerId = customerId; this.amount = amount; this.createdAt = createdAt;
        }
        public String toString() { return orderId + " | " + customerId + " | " + amount + " | " + createdAt; }
    }

    static List<Order> loadCsv(String filename) throws IOException {
        List<Order> orders = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", -1);
                if (p.length < 4) { System.err.println("Skipping invalid row: " + line); continue; }
                try {
                    orders.add(new Order(p[0].trim(), p[1].trim(), new BigDecimal(p[2].trim()),
                            LocalDateTime.parse(p[3].trim())));
                } catch (RuntimeException ex) { System.err.println("Skipping invalid row: " + line); }
            }
        }
        return orders;
    }

    static List<Order> syntheticOrders(int count) {
        Random r = new Random(42);
        List<Order> list = new ArrayList<>(count);
        LocalDateTime base = LocalDateTime.of(2026, 10, 29, 9, 0, 0);
        for (int i = 0; i < count; i++) {
            BigDecimal amount = BigDecimal.valueOf(100 + r.nextInt(99000), 2);
            list.add(new Order(String.format(Locale.ROOT, "%06d", i + 1),
                    "C" + (1 + r.nextInt(Math.max(1, count / 3))), amount,
                    base.plusSeconds(r.nextInt(86400))));
        }
        return list;
    }

    static Order linearSearch(List<Order> list, String id) {
        for (Order o : list) if (o.orderId.equals(id)) return o;
        return null;
    }
    static Order binarySearch(List<Order> sorted, String id) {
        int lo = 0, hi = sorted.size() - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int cmp = sorted.get(mid).orderId.compareTo(id);
            if (cmp == 0) return sorted.get(mid);
            if (cmp < 0) lo = mid + 1; else hi = mid - 1;
        }
        return null;
    }

    static void bubbleSort(List<Order> a) {
        for (int i = 0; i < a.size() - 1; i++) {
            boolean changed = false;
            for (int j = 0; j < a.size() - 1 - i; j++) {
                if (a.get(j).amount.compareTo(a.get(j + 1).amount) < 0) {
                    Collections.swap(a, j, j + 1); changed = true;
                }
            }
            if (!changed) break;
        }
    }
    static void mergeSort(List<Order> a) {
        if (a.size() < 2) return;
        Order[] src = a.toArray(new Order[0]), dst = new Order[src.length];
        for (int width = 1; width < src.length; width *= 2) {
            for (int left = 0; left < src.length; left += 2 * width) {
                int mid = Math.min(left + width, src.length), right = Math.min(left + 2 * width, src.length);
                int i = left, j = mid, k = left;
                while (i < mid && j < right)
                    dst[k++] = src[i].amount.compareTo(src[j].amount) >= 0 ? src[i++] : src[j++];
                while (i < mid) dst[k++] = src[i++];
                while (j < right) dst[k++] = src[j++];
            }
            Order[] temp = src; src = dst; dst = temp;
            if (width > src.length / 2) break; // prevents integer overflow on width *= 2
        }
        for (int i = 0; i < src.length; i++) a.set(i, src[i]);
    }
    static Comparator<Order> amountDescending = (a, b) -> b.amount.compareTo(a.amount);
    static void printAmounts(String label, List<Order> a, int limit) {
        System.out.print(label + ": ");
        for (int i = 0; i < Math.min(limit, a.size()); i++) System.out.print(a.get(i).amount + (i + 1 < Math.min(limit, a.size()) ? ", " : ""));
        System.out.println();
    }
    static void bubbleTrace() {
        int[] a = {5, 1, 4, 2, 8};
        System.out.println("Bubble sort trace (descending), start: " + Arrays.toString(a));
        for (int pass = 1; pass <= 3; pass++) {
            for (int j = 0; j < a.length - pass; j++) if (a[j] < a[j + 1]) { int t=a[j]; a[j]=a[j+1]; a[j+1]=t; }
            System.out.println("  Pass " + pass + ": " + Arrays.toString(a));
        }
    }
    static void mergeTrace() {
        int[] a = {5, 1, 4, 2, 8};
        System.out.println("Merge sort trace (descending), start: " + Arrays.toString(a));
        for (int width = 1, pass = 1; width < a.length && pass <= 3; width *= 2, pass++) {
            int[] out = a.clone();
            for (int left = 0; left < a.length; left += 2 * width) {
                int mid = Math.min(left + width, a.length), right = Math.min(left + 2 * width, a.length);
                int i=left, j=mid, k=left;
                while (i<mid && j<right) out[k++] = a[i] >= a[j] ? a[i++] : a[j++];
                while (i<mid) out[k++]=a[i++]; while (j<right) out[k++]=a[j++];
            }
            a=out; System.out.println("  Merge pass " + pass + " (run width " + width + "): " + Arrays.toString(a));
        }
    }

    static Set<String> findDuplicates(List<Order> orders) {
        Set<String> seen = new HashSet<>(), duplicates = new LinkedHashSet<>();
        for (Order o : orders) {
            String key = o.customerId + "|" + o.amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
                    + "|" + o.createdAt.truncatedTo(ChronoUnit.MINUTES);
            if (!seen.add(key)) duplicates.add(key);
        }
        return duplicates;
    }

    static class MyStack<T> {
        private final List<T> data = new ArrayList<>();
        void push(T x) { data.add(x); }
        T pop() { if (data.isEmpty()) throw new NoSuchElementException("Stack is empty"); return data.remove(data.size()-1); }
        boolean isEmpty() { return data.isEmpty(); }
    }
    static class MyQueue<T> {
        private final LinkedList<T> data = new LinkedList<>();
        void enqueue(T x) { data.addLast(x); }
        T dequeue() { if (data.isEmpty()) throw new NoSuchElementException("Queue is empty"); return data.removeFirst(); }
        boolean isEmpty() { return data.isEmpty(); }
    }
    static class Edit { String orderId, oldValue, newValue; Edit(String id, String oldV, String newV) { orderId=id; oldValue=oldV; newValue=newV; } }

    static int greedyChange(int amount, int[] denominations) {
        int count=0;
        for (int coin : denominations) { count += amount / coin; amount %= coin; }
        return amount == 0 ? count : -1;
    }
    static int dpChange(int amount, int[] denominations) {
        int[] dp = new int[amount+1]; Arrays.fill(dp, amount+1); dp[0]=0;
        for (int x=1; x<=amount; x++) for (int coin : denominations) if (coin<=x) dp[x]=Math.min(dp[x], dp[x-coin]+1);
        return dp[amount] > amount ? -1 : dp[amount];
    }

    static void benchmarkLookup(List<Order> data) {
        List<Order> sorted = new ArrayList<>(data); sorted.sort(Comparator.comparing(o -> o.orderId));
        String target = sorted.get(sorted.size()-1).orderId; // worst-case-ish target for linear search
        int repeats=2000; long start=System.nanoTime();
        for(int i=0;i<repeats;i++) linearSearch(data, target);
        long linear=System.nanoTime()-start; start=System.nanoTime();
        for(int i=0;i<repeats;i++) binarySearch(sorted, target);
        long binary=System.nanoTime()-start;
        System.out.printf(Locale.ROOT, "Lookup benchmark (%d records, %d repeats, target=%s): linear %.3f ms; binary %.3f ms%n",
                data.size(), repeats, target, linear/1e6, binary/1e6);
        System.out.println("  Note: binary search used a pre-sorted list; sorting cost is not included in its lookup timing.");
    }
    static void benchmarkSorts(List<Order> source) {
        List<Order> bubble = new ArrayList<>(source), merge = new ArrayList<>(source), builtin = new ArrayList<>(source);
        long t=System.nanoTime(); bubbleSort(bubble); long bubbleNs=System.nanoTime()-t;
        t=System.nanoTime(); mergeSort(merge); long mergeNs=System.nanoTime()-t;
        t=System.nanoTime(); builtin.sort(amountDescending); long builtinNs=System.nanoTime()-t;
        System.out.printf(Locale.ROOT, "Sort benchmark (%d records): bubble %.3f ms; merge %.3f ms; built-in List.sort %.3f ms%n",
                source.size(), bubbleNs/1e6, mergeNs/1e6, builtinNs/1e6);
        System.out.println("  Correctness check: bubble=" + sameOrder(bubble,builtin) + ", merge=" + sameOrder(merge,builtin));
        printAmounts("  Highest 5 amounts", builtin, 5);
    }
    static boolean sameOrder(List<Order> a, List<Order> b) {
        if(a.size()!=b.size()) return false;
        for(int i=0;i<a.size();i++) if(a.get(i).amount.compareTo(b.get(i).amount)!=0) return false;
        return true;
    }

    public static void main(String[] args) throws Exception {
        Path csv = Paths.get(args.length > 0 ? args[0] : "orders.csv");
        List<Order> loaded;
        if (Files.exists(csv)) { loaded=loadCsv(csv.toString()); System.out.println("Loaded " + loaded.size() + " rows from " + csv); }
        else { loaded=new ArrayList<>(); System.out.println("orders.csv not found; using generated sample data for demonstrations."); }
        if (loaded.isEmpty()) loaded = syntheticOrders(50000);

        System.out.println("\nTASK 1 - LOOKUP");
        Order example=loaded.get(0); System.out.println("Linear search for " + example.orderId + ": " + linearSearch(loaded, example.orderId));
        List<Order> byId=new ArrayList<>(loaded); byId.sort(Comparator.comparing(o -> o.orderId));
        System.out.println("Binary search for " + example.orderId + ": " + binarySearch(byId, example.orderId));
        List<Order> lookupData=loaded.size()>=50000 ? loaded : syntheticOrders(50000);
        benchmarkLookup(lookupData);

        System.out.println("\nTASK 2 - SORTING");
        bubbleTrace(); mergeTrace();
        List<Order> sortData=loaded.size()>=10000 ? loaded.subList(0,10000) : syntheticOrders(10000);
        benchmarkSorts(sortData);

        System.out.println("\nTASK 3 - DUPLICATE DETECTION");
        Set<String> duplicates=findDuplicates(loaded);
        System.out.println("Duplicate customer/amount/minute keys found: " + duplicates.size());
        for(String d : duplicates) System.out.println("  " + d);
        System.out.println("Expected time O(n), auxiliary space O(n), assuming well-distributed hash keys.");

        System.out.println("\nTASK 4 - STACK AND QUEUE");
        MyStack<Edit> customUndo=new MyStack<>(); customUndo.push(new Edit("1001","250.00","275.00"));
        Edit undone=customUndo.pop(); System.out.println("Custom stack undo: order="+undone.orderId+", restore amount="+undone.oldValue+" (revert from "+undone.newValue+")");
        Deque<Edit> builtInUndo=new ArrayDeque<>(); builtInUndo.push(new Edit("1002","300.00","325.00"));
        Edit builtinUndone=builtInUndo.pop(); System.out.println("ArrayDeque stack undo: order="+builtinUndone.orderId+", restore amount="+builtinUndone.oldValue);
        MyQueue<String> customDispatch=new MyQueue<>(); customDispatch.enqueue("1001"); customDispatch.enqueue("1002");
        System.out.println("Custom queue dispatch order: "+customDispatch.dequeue()+", "+customDispatch.dequeue());
        Deque<String> builtinDispatch=new ArrayDeque<>(); builtinDispatch.addLast("1001"); builtinDispatch.addLast("1002");
        System.out.println("ArrayDeque queue dispatch order: "+builtinDispatch.removeFirst()+", "+builtinDispatch.removeFirst());
        System.out.println("Both support O(1) push/pop or enqueue/dequeue; built-in ArrayDeque is generally preferred for production code.");

        System.out.println("\nTASK 5 - REFUND CHANGE");
        int amount=93; int[] indiaNotes={500,200,100,50,20,10,5,2,1};
        System.out.println("Greedy Indian denominations for Rs."+amount+": minimum pieces="+greedyChange(amount,indiaNotes));
        int[] tricky={4,3,1}; int trickyAmount=6;
        System.out.println("Greedy counterexample denominations "+Arrays.toString(tricky)+", amount="+trickyAmount+": greedy="+greedyChange(trickyAmount,tricky)+", DP="+dpChange(trickyAmount,tricky));
        System.out.println("DP tries each denomination for every amount up to A: O(A*d) time, O(A) space.");
    }
}
