import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static class Tx {
        String id, account, merchant;
        long cents;
        LocalDateTime time;
        int order;
        Tx(String id, String account, long cents, String merchant, String time, int order) {
            this.id=id; this.account=account; this.cents=cents; this.merchant=merchant;
            this.time=LocalDateTime.parse(time,F); this.order=order;
        }
        public String toString() {
            return id+" "+account+" $"+String.format(Locale.US,"%.2f",cents/100.0)+
                   " "+merchant+" "+time.format(F)+" (inputOrder="+order+")";
        }
    }
    static class Result { int index, comparisons; Result(int i,int c){index=i;comparisons=c;} }

    static Result binary(int[] a,int target) {
        int lo=0,hi=a.length-1,c=0;
        while(lo<=hi) {
            int mid=lo+(hi-lo)/2; c++;
            if(a[mid]==target)return new Result(mid,c);
            if(a[mid]<target)lo=mid+1; else hi=mid-1;
        }
        return new Result(-1,c);
    }
    static Result linear(int[] a,int target) {
        int c=0;
        for(int i=0;i<a.length;i++){c++;if(a[i]==target)return new Result(i,c);}
        return new Result(-1,c);
    }
    static void searchDemo() {
        int n=100_000; int[] ids=new int[n];
        for(int i=0;i<n;i++)ids[i]=1_000_000+i;
        long bt=0,lt=0; int bw=0,lw=0;
        for(int id:ids){Result b=binary(ids,id),l=linear(ids,id);bt+=b.comparisons;lt+=l.comparisons;
            bw=Math.max(bw,b.comparisons);lw=Math.max(lw,l.comparisons);}
        System.out.println("\nTASK 1 - SEARCH (100,000 IDs)");
        System.out.println("Binary best: 1 comparison (middle element)");
        System.out.printf(Locale.US,"Binary average comparisons: %.3f%n",bt/(double)n);
        System.out.println("Binary worst observed: "+bw+" comparisons");
        System.out.println("Linear best: 1 comparison (first element)");
        System.out.printf(Locale.US,"Linear average successful search: %.1f comparisons%n",lt/(double)n);
        System.out.println("Linear worst: "+lw+" comparisons");
    }

    static Tx[] sample() {
        return new Tx[]{
            new Tx("TX1001","AC101",2599,"Cafe","2026-10-29 09:15:00",0),
            new Tx("TX1002","AC101",4999,"BookShop","2026-10-29 09:14:00",1),
            new Tx("TX1003","AC101",1299,"Cafe","2026-10-29 09:15:00",2),
            new Tx("TX1004","AC202",2599,"Cafe","2026-10-29 09:15:40",3),
            new Tx("TX1005","AC101",2599,"Cafe","2026-10-29 09:15:45",4),
            new Tx("TX1006","AC101",2599,"Cafe","2026-10-29 09:17:30",5)
        };
    }
    static Tx[] insertion(Tx[] src) {
        Tx[] a=src.clone();
        for(int i=1;i<a.length;i++){Tx key=a[i];int j=i-1;
            while(j>=0 && a[j].time.isAfter(key.time)){a[j+1]=a[j];j--;}
            a[j+1]=key;}
        return a;
    }
    static Tx[] mergeSort(Tx[] src) {
        Tx[] a=src.clone(); merge(a,new Tx[a.length],0,a.length-1); return a;
    }
    static void merge(Tx[] a,Tx[] tmp,int lo,int hi) {
        if(lo>=hi)return; int mid=lo+(hi-lo)/2;
        merge(a,tmp,lo,mid);merge(a,tmp,mid+1,hi);
        int i=lo,j=mid+1,k=lo;
        while(i<=mid&&j<=hi) {
            // Taking the left item on equal timestamps preserves stability.
            if(!a[i].time.isAfter(a[j].time))tmp[k++]=a[i++];else tmp[k++]=a[j++];
        }
        while(i<=mid)tmp[k++]=a[i++]; while(j<=hi)tmp[k++]=a[j++];
        for(int p=lo;p<=hi;p++)a[p]=tmp[p];
    }
    static void sortDemo() {
        System.out.println("\nTASK 2 - SORT BY POSTING TIMESTAMP");
        System.out.println("Insertion sort:");
        for(Tx t:insertion(sample()))System.out.println("  "+t);
        System.out.println("Merge sort:");
        for(Tx t:mergeSort(sample()))System.out.println("  "+t);
        System.out.println("Stability: TX1001 remains before TX1003 because their timestamps tie.");
    }

    static class Key {
        String account,merchant; long cents;
        Key(Tx t){account=t.account;merchant=t.merchant;cents=t.cents;}
        public boolean equals(Object o){if(!(o instanceof Key))return false;Key k=(Key)o;
            return cents==k.cents&&account.equals(k.account)&&merchant.equals(k.merchant);}
        public int hashCode(){return Objects.hash(account,merchant,cents);}
    }
    static void duplicateDemo() {
        Tx[] a=sample(); Arrays.sort(a,Comparator.comparing(t->t.time));
        Map<Key,List<Tx>> groups=new HashMap<>();
        System.out.println("\nTASK 3 - DUPLICATE POSTING ALERTS");
        int found=0;
        for(Tx t:a){List<Tx> prior=groups.computeIfAbsent(new Key(t),k->new ArrayList<>());
            for(Tx p:prior){long sec=Math.abs(Duration.between(p.time,t.time).getSeconds());
                if(sec<=60){System.out.println("Flag: "+p.id+" and "+t.id+" ("+sec+" seconds apart)");found++;}}
            prior.add(t);}
        if(found==0)System.out.println("No duplicates found.");
        System.out.println("Hash key = account + amount in cents + merchant; time gap must be <= 60 seconds.");
    }

    // Fixed-array implementations: no Java collection classes inside stack or queue.
    static class StackArray {
        String[] a; int top=-1;
        StackArray(int cap){a=new String[cap];}
        boolean push(String s){if(top==a.length-1)return false;a[++top]=s;return true;}
        String pop(){if(top<0)return null;String s=a[top];a[top--]=null;return s;}
    }
    static class QueueArray {
        String[] a;int head=0,tail=0,size=0;
        QueueArray(int cap){a=new String[cap];}
        boolean enqueue(String s){if(size==a.length)return false;a[tail]=s;tail=(tail+1)%a.length;size++;return true;}
        String dequeue(){if(size==0)return null;String s=a[head];a[head]=null;head=(head+1)%a.length;size--;return s;}
    }
    static void stackQueueDemo() {
        System.out.println("\nTASK 4 - QUEUE AND STACK");
        QueueArray q=new QueueArray(5);q.enqueue("Case-101");q.enqueue("Case-102");q.enqueue("Case-103");
        System.out.println("FIFO review order: "+q.dequeue()+", then "+q.dequeue());
        StackArray s=new StackArray(5);s.push("Adjustment A +500");s.push("Adjustment B -200");s.push("Adjustment C +100");
        System.out.println("Reverse last adjustment: "+s.pop());
        System.out.println("Next to reverse: "+s.pop());
    }

    static class MM {long min,max,comparisons;MM(long n,long x,long c){min=n;max=x;comparisons=c;}}
    static MM divideMM(long[] a,int lo,int hi) {
        if(lo==hi)return new MM(a[lo],a[lo],0);
        if(hi==lo+1)return a[lo]<=a[hi]?new MM(a[lo],a[hi],1):new MM(a[hi],a[lo],1);
        int mid=lo+(hi-lo)/2;MM l=divideMM(a,lo,mid),r=divideMM(a,mid+1,hi);
        return new MM(Math.min(l.min,r.min),Math.max(l.max,r.max),l.comparisons+r.comparisons+2);
    }
    static MM bruteMM(long[] a) {
        long min=a[0],max=a[0],c=0;
        for(int i=1;i<a.length;i++){c++;if(a[i]<min)min=a[i];c++;if(a[i]>max)max=a[i];}
        return new MM(min,max,c);
    }
    static void minMaxDemo() {
        long[] amounts={2599,4999,1299,2599,8750,150,7200,3999};
        MM d=divideMM(amounts,0,amounts.length-1),b=bruteMM(amounts);
        System.out.println("\nTASK 5 - MINIMUM AND MAXIMUM");
        System.out.printf(Locale.US,"Divide-and-conquer: min=$%.2f max=$%.2f comparisons=%d%n",d.min/100.0,d.max/100.0,d.comparisons);
        System.out.printf(Locale.US,"Brute-force scan:   min=$%.2f max=$%.2f comparisons=%d%n",b.min/100.0,b.max/100.0,b.comparisons);
    }
    public static void main(String[] args) {
        System.out.println("SET 2 - RETAIL BANKING TRANSACTION REVIEW");
        searchDemo();sortDemo();duplicateDemo();stackQueueDemo();minMaxDemo();
        System.out.println("\nAll demonstrations completed.");
    }
}
