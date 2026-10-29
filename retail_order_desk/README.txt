RETAIL ORDER DESK - COMPLEXITY AND DESIGN NOTE

1. ORDER LOOKUP
Linear search checks orders sequentially and takes O(n) time, O(1) extra space. It works on unsorted data but can be slow for 50,000 rows. Binary search halves the search range each step, taking O(log n) time and O(1) extra space, but requires the list to be sorted by orderId first. The benchmark excludes the one-time sorting cost and repeats the same lookup to make the difference visible. For frequently changing data, consider a HashMap keyed by orderId for expected O(1) lookups.

2. SORTING BY AMOUNT DESCENDING
Bubble sort swaps adjacent out-of-order elements. It is easy to trace but takes O(n^2) time in average/worst cases and O(1) extra space. Merge sort divides the input into runs and merges them in descending order; it takes O(n log n) time and O(n) extra space. Java List.sort is the practical default and uses a stable adaptive object sort. Benchmark times depend on CPU, JVM warm-up, and input; the output contains measured timings from this run. The trace uses five values so each pass can be checked manually.

3. DUPLICATE SUBMISSIONS
For each order, create a key from customerId, amount normalized to two decimal places, and createdAt truncated to the minute. A HashSet detects whether that key has appeared. Expected total time is O(n), and the set uses O(n) auxiliary space. This flags matching submissions in the same minute; it does not prove fraud, so production systems should also use idempotency keys and payment-state checks.

4. UNDO STACK AND DISPATCH QUEUE
A stack is LIFO, so the most recent edit is undone first. A queue is FIFO, so the earliest order enqueued is dispatched first. The custom stack uses an ArrayList and removes from its end; the custom queue uses a LinkedList. The built-in ArrayDeque supports stack and queue operations efficiently and is generally preferred in production. Push/pop and enqueue/dequeue are O(1) amortized/typical operations; storage is O(n).

5. REFUND CHANGE
Greedy repeatedly chooses the largest denomination that does not exceed the remaining amount. For common Indian denominations it works for the example, but greedy is not correct for every denomination system. Counterexample: denominations [4, 3, 1], amount 6. Greedy chooses 4+1+1 (3 coins), while the optimum is 3+3 (2 coins). Dynamic programming computes dp[x] = 1 + min(dp[x - coin]) over usable denominations, with dp[0]=0. Complexity is O(A*d) time and O(A) space, where A is the integer amount in smallest units and d is the number of denominations.

RUN
Use JDK 8 or newer. Put Main.java and orders.csv in the same directory, then run:
  javac Main.java
  java Main
Or provide another CSV path:
  java Main /path/to/orders.csv
CSV timestamps must use ISO local date-time format, e.g. 2026-10-29T10:15:12. The included CSV is a small demonstration fixture. Benchmarks automatically use generated 50,000-row and 10,000-row data sets when the supplied CSV is smaller than the required benchmark size.
