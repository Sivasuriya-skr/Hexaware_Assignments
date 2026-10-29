SET 3 – HOSPITAL APPOINTMENT DESK

Files:
- Main.java: all five tasks and runnable test data.
- test_data.csv: illustrative patient, appointment, and slot records.
- complexity_table.txt: complexity analysis and data-structure choices.
- sorting_trace.txt: typed selection-sort trace.
- console_output.txt: run instructions and output notes.

Run with JDK 8 or later:
  javac Main.java
  java Main

Task coverage:
1. Linear and binary search across 20,000 sorted patient IDs for 1,000 lookups.
2. Selection sort and quick sort by urgency ascending (1 is most urgent), then arrival time ascending; reports comparisons and swaps.
3. HashSet composite key (patient ID + slot) for expected O(1) duplicate detection.
4. Custom circular array FIFO queue and array-backed LIFO stack.
5. Weighted interval scheduling DP compared with a greedy highest-value-first baseline.

Modeling note: Task 5 treats different appointment values as weights and maximizes total value. If the assignment instead means maximum COUNT only, the classic earliest-finish-time greedy algorithm is optimal.
