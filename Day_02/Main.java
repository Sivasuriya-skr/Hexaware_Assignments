import java.time.LocalDate;
import java.util.*;

/**
 * Set 2 - Insurance Claims Processing
 * Java implementation of BST, duplicate detection, directed-graph BFS/DFS,
 * consecutive-day sliding window, and two-pointer merge.
 */
public class Main {

    // -------------------- 1. BST of claim IDs --------------------
    static class ClaimBST {
        private static class Node {
            int id;
            Node left, right;
            Node(int id) { this.id = id; }
        }
        private Node root;

        /** @return false if the ID already exists. */
        public boolean insert(int id) {
            if (root == null) {
                root = new Node(id);
                return true;
            }
            Node current = root;
            while (true) {
                if (id == current.id) return false;
                if (id < current.id) {
                    if (current.left == null) {
                        current.left = new Node(id);
                        return true;
                    }
                    current = current.left;
                } else {
                    if (current.right == null) {
                        current.right = new Node(id);
                        return true;
                    }
                    current = current.right;
                }
            }
        }

        public boolean search(int id) {
            Node current = root;
            while (current != null) {
                if (id == current.id) return true;
                current = id < current.id ? current.left : current.right;
            }
            return false;
        }

        public List<Integer> inorder() {
            List<Integer> result = new ArrayList<>();
            inorder(root, result);
            return result;
        }
        private void inorder(Node node, List<Integer> out) {
            if (node == null) return;
            inorder(node.left, out);
            out.add(node.id);
            inorder(node.right, out);
        }

        public List<Integer> preorder() {
            List<Integer> result = new ArrayList<>();
            preorder(root, result);
            return result;
        }
        private void preorder(Node node, List<Integer> out) {
            if (node == null) return;
            out.add(node.id);
            preorder(node.left, out);
            preorder(node.right, out);
        }

        public List<Integer> postorder() {
            List<Integer> result = new ArrayList<>();
            postorder(root, result);
            return result;
        }
        private void postorder(Node node, List<Integer> out) {
            if (node == null) return;
            postorder(node.left, out);
            postorder(node.right, out);
            out.add(node.id);
        }
    }

    // -------------------- 2. Hash-based duplicate detection --------------------
    static class ClaimKey {
        private final String policy;
        private final LocalDate incidentDate;

        ClaimKey(String policy, LocalDate incidentDate) {
            this.policy = normalize(policy);
            this.incidentDate = Objects.requireNonNull(incidentDate);
        }

        private static String normalize(String value) {
            if (value == null) throw new IllegalArgumentException("Policy number cannot be null");
            // Remove all whitespace and compare without regard to letter case.
            return value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ClaimKey)) return false;
            ClaimKey other = (ClaimKey) obj;
            return policy.equals(other.policy) && incidentDate.equals(other.incidentDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(policy, incidentDate);
        }

        @Override
        public String toString() {
            return policy + " | " + incidentDate;
        }
    }

    static class DuplicateDetector {
        private final Map<ClaimKey, String> firstClaimByKey = new HashMap<>();

        /** Returns true when this claim matches an earlier policy/date key. */
        public boolean isDuplicate(String claimId, String policy, LocalDate incidentDate) {
            ClaimKey key = new ClaimKey(policy, incidentDate);
            String previous = firstClaimByKey.putIfAbsent(key, claimId);
            if (previous != null) {
                System.out.println("Duplicate: " + claimId + " matches " + previous + " (" + key + ")");
                return true;
            }
            return false;
        }
    }

    // -------------------- 3. Directed approval-stage graph --------------------
    static class ApprovalGraph {
        private final Map<String, List<String>> adjacency = new LinkedHashMap<>();

        public void addStage(String stage) {
            adjacency.putIfAbsent(stage, new ArrayList<>());
        }

        public void addTransition(String from, String to) {
            addStage(from);
            addStage(to);
            adjacency.get(from).add(to);
        }

        /** BFS gives a shortest path in number of directed edges. */
        public List<String> shortestPathBFS(String start, String target) {
            if (!adjacency.containsKey(start) || !adjacency.containsKey(target)) {
                return Collections.emptyList();
            }
            Queue<String> queue = new ArrayDeque<>();
            Map<String, String> parent = new HashMap<>();
            Set<String> visited = new HashSet<>();
            queue.add(start);
            visited.add(start);

            while (!queue.isEmpty()) {
                String current = queue.remove();
                if (current.equals(target)) break;
                for (String next : adjacency.get(current)) {
                    if (visited.add(next)) {
                        parent.put(next, current);
                        queue.add(next);
                    }
                }
            }

            if (!visited.contains(target)) return Collections.emptyList();
            LinkedList<String> path = new LinkedList<>();
            for (String at = target; at != null; at = parent.get(at)) {
                path.addFirst(at);
                if (at.equals(start)) break;
            }
            return path;
        }

        /** DFS cycle detection for a directed graph using 0=unseen, 1=active, 2=done. */
        public boolean hasCycleDFS() {
            Map<String, Integer> state = new HashMap<>();
            for (String stage : adjacency.keySet()) {
                if (state.getOrDefault(stage, 0) == 0 && dfsCycle(stage, state)) return true;
            }
            return false;
        }

        private boolean dfsCycle(String node, Map<String, Integer> state) {
            state.put(node, 1);
            for (String next : adjacency.get(node)) {
                int nextState = state.getOrDefault(next, 0);
                if (nextState == 1) return true;       // back edge to active node
                if (nextState == 0 && dfsCycle(next, state)) return true;
            }
            state.put(node, 2);
            return false;
        }
    }

    // -------------------- 4. Sliding window: consecutive high-claim days --------------------
    /**
     * Returns the longest consecutive run of days with more than threshold claims.
     * The input must contain one count for each consecutive calendar day.
     */
    public static int longestHighClaimStreak(int[] dailyClaims, int threshold) {
        int left = 0, best = 0;
        for (int right = 0; right < dailyClaims.length; right++) {
            if (dailyClaims[right] <= threshold) {
                left = right + 1; // invalid day breaks the current window
            } else {
                best = Math.max(best, right - left + 1);
            }
        }
        return best;
    }

    // -------------------- 5. Merge sorted regional claim lists --------------------
    /** Merges two already-sorted arrays. No sorting operation is performed. */
    public static int[] mergeSortedClaims(int[] north, int[] south) {
        int[] merged = new int[north.length + south.length];
        int i = 0, j = 0, k = 0;
        while (i < north.length && j < south.length) {
            if (north[i] <= south[j]) merged[k++] = north[i++];
            else merged[k++] = south[j++];
        }
        while (i < north.length) merged[k++] = north[i++];
        while (j < south.length) merged[k++] = south[j++];
        return merged;
    }

    // -------------------- Tests and demonstration --------------------
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("FAILED: " + message);
        System.out.println("PASS: " + message);
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Claim ID BST ===");
        ClaimBST bst = new ClaimBST();
        check(!bst.search(100), "search in empty BST returns false");
        check(bst.inorder().isEmpty(), "empty BST traversal returns empty list");
        int[] ids = {50, 30, 70, 20, 40, 60, 80};
        for (int id : ids) bst.insert(id);
        check(!bst.insert(50), "duplicate claim ID is not inserted");
        check(bst.search(60) && !bst.search(999), "BST search finds present and rejects absent ID");
        System.out.println("Inorder   (sorted): " + bst.inorder());
        System.out.println("Preorder         : " + bst.preorder());
        System.out.println("Postorder        : " + bst.postorder());

        System.out.println("\n=== 2. Duplicate claim detection ===");
        DuplicateDetector detector = new DuplicateDetector();
        LocalDate date = LocalDate.of(2026, 10, 20);
        check(!detector.isDuplicate("CLM-001", "POL 123 ab", date), "first claim is unique");
        check(detector.isDuplicate("CLM-002", "pol123AB", date), "case/spacing variation is detected");
        check(!detector.isDuplicate("CLM-003", "POL123AB", date.plusDays(1)), "different incident date is unique");
        check(!detector.isDuplicate("CLM-004", "POL-123AB", date), "different punctuation remains distinct");

        System.out.println("\n=== 3. Approval-stage graph ===");
        ApprovalGraph graph = new ApprovalGraph();
        graph.addTransition("Registration", "Assessment");
        graph.addTransition("Assessment", "Surveyor");
        graph.addTransition("Surveyor", "Settlement");
        graph.addTransition("Assessment", "Settlement"); // optional direct route
        System.out.println("Shortest path: " + graph.shortestPathBFS("Registration", "Settlement"));
        check(!graph.hasCycleDFS(), "normal approval flow has no directed cycle");
        graph.addTransition("Settlement", "Assessment");
        check(graph.hasCycleDFS(), "DFS detects a directed cycle");

        System.out.println("\n=== 4. Consecutive days above 100 claims ===");
        int[] daily = {95, 110, 125, 130, 80, 105, 115, 118, 121, 90};
        check(longestHighClaimStreak(new int[0], 100) == 0, "empty daily input returns zero");
        check(longestHighClaimStreak(daily, 100) == 4, "longest high-claim streak is four days");
        check(longestHighClaimStreak(new int[]{100, 100}, 100) == 0, "exactly 100 does not exceed threshold");

        System.out.println("\n=== 5. Merge regional sorted claim lists ===");
        check(Arrays.equals(mergeSortedClaims(new int[0], new int[0]), new int[0]),
                "two empty regional lists merge to empty");
        check(Arrays.equals(mergeSortedClaims(new int[]{1, 3, 5}, new int[]{2, 4, 6}),
                new int[]{1, 2, 3, 4, 5, 6}), "two sorted lists merge correctly");
        check(Arrays.equals(mergeSortedClaims(new int[]{1, 2, 2}, new int[]{2, 3}),
                new int[]{1, 2, 2, 2, 3}), "merge preserves duplicate IDs");
        check(Arrays.equals(mergeSortedClaims(new int[0], new int[]{4, 7}),
                new int[]{4, 7}), "one empty regional list is handled");

        System.out.println("\nAll test cases passed.");
    }
}
