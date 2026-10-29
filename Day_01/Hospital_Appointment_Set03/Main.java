import java.util.*;

public class Main {
    static class Patient { int id; String name; Patient(int id){this.id=id; this.name="Patient-"+id;} }
    static class Appointment {
        String id; int patientId, urgency, arrival;
        Appointment(String id,int p,int u,int a){this.id=id;patientId=p;urgency=u;arrival=a;}
        public String toString(){return id+"(patient="+patientId+", urgency="+urgency+", arrival="+arrival+")";}
    }
    static class Slot { String id; int start,end,value; Slot(String i,int s,int e,int v){id=i;start=s;end=e;value=v;} public String toString(){return id+"["+start+"-"+end+", value="+value+"]";} }
    static class Stats { long swaps, comparisons; }
    static class MyQueue<T> {
        Object[] a; int head=0,tail=0,size=0;
        MyQueue(int cap){a=new Object[cap];}
        void enqueue(T x){if(size==a.length)throw new IllegalStateException("Queue full");a[tail]=x;tail=(tail+1)%a.length;size++;}
        @SuppressWarnings("unchecked") T dequeue(){if(size==0)throw new NoSuchElementException("Queue empty");T x=(T)a[head];a[head]=null;head=(head+1)%a.length;size--;return x;}
        int size(){return size;}
    }
    static class MyStack<T> {
        Object[] a; int size=0; MyStack(int cap){a=new Object[cap];}
        void push(T x){if(size==a.length)throw new IllegalStateException("Stack full");a[size++]=x;}
        @SuppressWarnings("unchecked") T pop(){if(size==0)throw new NoSuchElementException("Stack empty");T x=(T)a[--size];a[size]=null;return x;}
    }
    public static void main(String[] args) {
        System.out.println("SET 3 - HOSPITAL APPOINTMENT DESK\n=================================");
        Patient[] patients=new Patient[20000]; for(int i=0;i<patients.length;i++)patients[i]=new Patient(100001+i);
        int[] ids=new int[1000];for(int i=0;i<ids.length;i++)ids[i]=patients[(i*19+37)%patients.length].id;
        long cmp=0,t=System.nanoTime();int found=0;
        for(int id:ids)for(Patient p:patients){cmp++;if(p.id==id){found++;break;}}
        long linearNs=System.nanoTime()-t;long linearCmp=cmp;
        cmp=0;found=0;t=System.nanoTime();
        for(int id:ids){int lo=0,hi=patients.length-1;while(lo<=hi){cmp++;int mid=lo+(hi-lo)/2;if(patients[mid].id==id){found++;break;}if(patients[mid].id<id)lo=mid+1;else hi=mid-1;}}
        long binaryNs=System.nanoTime()-t;
        System.out.println("\n1. SEARCH (20,000 records; 1,000 lookups)");
        System.out.printf("Linear: found=%d comparisons=%d time=%.3f ms%n",found,linearCmp,linearNs/1e6);
        System.out.printf("Binary: found=%d comparisons=%d time=%.3f ms%n",found,cmp,binaryNs/1e6);
        Appointment[] original={new Appointment("A01",101,3,540),new Appointment("A02",102,1,575),new Appointment("A03",103,2,550),new Appointment("A04",104,1,535),new Appointment("A05",105,3,530),new Appointment("A06",106,2,545),new Appointment("A07",107,1,560)};
        Appointment[] sel=original.clone(),quick=original.clone();Stats ss=new Stats(),qs=new Stats();selectionSort(sel,ss);quickSort(quick,0,quick.length-1,qs);
        System.out.println("\n2. SORT (urgency ascending, then arrival ascending)");System.out.println("Selection: "+Arrays.toString(sel));System.out.println("Selection comparisons="+ss.comparisons+" swaps="+ss.swaps);
        System.out.println("Quick:     "+Arrays.toString(quick));System.out.println("Quick comparisons="+qs.comparisons+" swaps="+qs.swaps);
        Appointment[] bookings={new Appointment("B01",2001,2,600),new Appointment("B02",2002,3,610),new Appointment("B03",2001,1,600),new Appointment("B04",2001,2,630)};
        Set<String> seen=new HashSet<>();List<String> duplicates=new ArrayList<>();for(Appointment a:bookings){String key=a.patientId+"#"+a.arrival;if(!seen.add(key))duplicates.add(a.id+" (patient "+a.patientId+", slot "+a.arrival+")");}
        System.out.println("\n3. DOUBLE BOOKING\nDuplicates: "+duplicates+"\nHashSet expected membership/add time: O(1); list scan per booking: O(n).");
        MyQueue<String> q=new MyQueue<>(10);q.enqueue("Patient-501");q.enqueue("Patient-502");q.enqueue("Patient-503");MyStack<String> st=new MyStack<>(10);st.push("Patient-101");st.push("Patient-205");st.push("Patient-309");
        System.out.println("\n4. CUSTOM QUEUE AND STACK\nQueue size="+q.size()+" next served="+q.dequeue()+"\nMost recently viewed="+st.pop());
        Slot[] slots={new Slot("S1",9,11,5),new Slot("S2",9,10,3),new Slot("S3",10,12,6),new Slot("S4",11,13,5),new Slot("S5",12,14,4),new Slot("S6",13,15,7)};
        List<Slot> dp=weightedSchedule(slots);int dpVal=dp.stream().mapToInt(s->s.value).sum();List<Slot> greedy=greedyByValue(slots);int grVal=greedy.stream().mapToInt(s->s.value).sum();
        System.out.println("\n5. DP VS GREEDY\nDP appointments="+dp+" total value="+dpVal+"\nGreedy appointments="+greedy+" total value="+grVal);
    }
    static int compare(Appointment a,Appointment b){int x=Integer.compare(a.urgency,b.urgency);return x!=0?x:Integer.compare(a.arrival,b.arrival);}
    static void selectionSort(Appointment[] a,Stats s){for(int i=0;i<a.length-1;i++){int m=i;for(int j=i+1;j<a.length;j++){s.comparisons++;if(compare(a[j],a[m])<0)m=j;}if(m!=i){Appointment x=a[i];a[i]=a[m];a[m]=x;s.swaps++;}}}
    static void quickSort(Appointment[] a,int lo,int hi,Stats s){if(lo>=hi)return;int p=partition(a,lo,hi,s);quickSort(a,lo,p-1,s);quickSort(a,p+1,hi,s);}
    static int partition(Appointment[] a,int lo,int hi,Stats s){Appointment pivot=a[hi];int i=lo-1;for(int j=lo;j<hi;j++){s.comparisons++;if(compare(a[j],pivot)<=0){i++;if(i!=j){Appointment x=a[i];a[i]=a[j];a[j]=x;s.swaps++;}}}if(i+1!=hi){Appointment x=a[i+1];a[i+1]=a[hi];a[hi]=x;s.swaps++;}return i+1;}
    static List<Slot> weightedSchedule(Slot[] input){Slot[] a=input.clone();Arrays.sort(a,Comparator.comparingInt((Slot x)->x.end).thenComparingInt(x->x.start));int n=a.length;int[] prev=new int[n];for(int i=0;i<n;i++){prev[i]=-1;for(int j=i-1;j>=0;j--)if(a[j].end<=a[i].start){prev[i]=j;break;}}int[] dp=new int[n+1];for(int i=1;i<=n;i++)dp[i]=Math.max(a[i-1].value+dp[prev[i-1]+1],dp[i-1]);List<Slot> out=new ArrayList<>();for(int i=n;i>0;){int include=a[i-1].value+dp[prev[i-1]+1];if(include>dp[i-1]){out.add(a[i-1]);i=prev[i-1]+1;}else i--;}Collections.reverse(out);return out;}
    static List<Slot> greedyByValue(Slot[] input){Slot[] a=input.clone();Arrays.sort(a,Comparator.comparingInt((Slot x)->x.value).reversed());List<Slot> out=new ArrayList<>();for(Slot c:a){boolean ok=true;for(Slot x:out)if(c.start<x.end&&x.start<c.end){ok=false;break;}if(ok)out.add(c);}out.sort(Comparator.comparingInt(x->x.start));return out;}
}
