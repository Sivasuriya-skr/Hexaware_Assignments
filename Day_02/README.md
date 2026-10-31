# Set 2 – Insurance Claims Processing (Java)

## Requirements
- Java 8 or later
- No external libraries

## Files
- `Main.java` – all five algorithm implementations and executable test cases
- `complexity_table.txt` – time and space complexity summary
- `test_cases.txt` – test scenarios and expected outcomes

## Compile and run
Open a terminal in this folder and run:

```bash
javac Main.java
java Main
```

The program runs its test cases and prints `All test cases passed.` if every assertion succeeds.

## Implementation notes
1. **BST:** Insert and search claim IDs. Inorder traversal (left, root, right) returns IDs in ascending order. Preorder and postorder are also printed.
2. **Hashing:** A claim key is the normalized policy number plus incident date. Normalization removes whitespace and converts letters to uppercase. `HashMap` uses `hashCode()` to locate a bucket and `equals()` to distinguish keys that land in the same bucket; collisions do not automatically mean duplicate claims.
3. **Graph:** Approval stages are vertices and allowed transitions are directed edges. BFS finds a minimum-edge path. DFS uses three states (unvisited, active, completed); an edge to an active vertex indicates a directed cycle.
4. **Sliding window:** The input array must represent claim counts for consecutive calendar days in chronological order. A day with 100 or fewer claims breaks the current window. The result is the longest run with strictly more than 100 claims.
5. **Two pointers:** Both regional lists must already be sorted in ascending order. The merge walks each list once and does not sort again.
