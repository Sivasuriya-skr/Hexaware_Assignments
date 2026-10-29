SET 2 - RETAIL BANKING TRANSACTION REVIEW
==========================================
Requirements: Java JDK 8 or newer.

Compile and run:
  javac Main.java
  java Main

Main.java is a self-contained console demonstration of all five tasks.
transactions.csv is a readable sample input reference; the demonstration currently
constructs the same sample records in code rather than parsing the CSV at runtime.

Task notes:
1. Search: creates 100,000 sorted IDs and searches all of them to calculate exact
   average successful-search comparison-operation counts.
2. Sorting: both insertion sort and merge sort are stable. Insertion sort shifts only
   strictly later timestamps; merge sort takes the left item when timestamps tie.
3. Duplicates: hashes account + amount in cents + merchant, then checks whether
   timestamps differ by 60 seconds or less.
4. Queue and stack: custom fixed-array implementations, no collection classes inside
   either structure. Queue is circular; queue is FIFO and stack is LIFO.
5. Min/max: recursive divide-and-conquer compared with a brute-force scan.
