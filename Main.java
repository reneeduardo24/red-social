import java.util.*;

public class Main {

    static final class User {
        final int id;
        final String name;

        User(int id, String name) {
            this.id = id;
            this.name = Objects.requireNonNull(name);
        }

        @Override public String toString() { return id + ":" + name; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof User)) return false;
            return id == ((User) o).id;
        }

        @Override public int hashCode() { return Integer.hashCode(id); }
    }

    static final class SocialGraph {
        private final Map<Integer, User> users = new HashMap<>();
        private final Map<Integer, Set<Integer>> friends = new HashMap<>();
        private final Map<Integer, Map<Integer, Integer>> visits = new HashMap<>();

        public User addUser(int id, String name) {
            User u = new User(id, name);
            users.put(id, u);
            friends.computeIfAbsent(id, k -> new HashSet<>());
            visits.computeIfAbsent(id, k -> new HashMap<>());
            return u;
        }

        public void addFriendship(int a, int b) {
            if (a == b) return;
            friends.get(a).add(b);
            friends.get(b).add(a);
        }

        public void recordVisit(int from, int to) {
            if (from == to) return;
            Map<Integer, Integer> m = visits.get(from);
            m.put(to, m.getOrDefault(to, 0) + 1);
        }

        public double similarityRS(int u, int v) {
            Map<Integer, Integer> m = visits.getOrDefault(u, Collections.emptyMap());
            if (m.isEmpty()) return 0.0;

            int max = 0;
            for (int c : m.values()) max = Math.max(max, c);
            if (max == 0) return 0.0;

            int uv = m.getOrDefault(v, 0);
            return (double) uv / max;
        }

        public List<Suggestion> suggestFromFriendsOf(int u, int baseUser, double threshold) {
            Set<Integer> baseFriends = friends.get(baseUser);
            Set<Integer> uFriends = friends.get(u);

            List<Suggestion> out = new ArrayList<>();
            for (int c : baseFriends) {
                if (c == u) continue;
                if (uFriends.contains(c)) continue;

                double rs = similarityRS(u, c);
                if (rs >= threshold) {
                    out.add(new Suggestion(c, rs));
                }
            }

            out.sort((a, b) -> Double.compare(b.rs, a.rs));
            return out;
        }

        public User getUser(int id) { return users.get(id); }

        // Método para generar el grafo en formato DOT
        public String toDot(int u, int baseUser, List<Suggestion> candidates) {
            Set<Integer> nodes = new HashSet<>();
            nodes.add(u);
            nodes.add(baseUser);
            for (Suggestion s : candidates) nodes.add(s.userId);

            StringBuilder sb = new StringBuilder();
            sb.append("digraph Social {\n");
            sb.append("  rankdir=LR;\n");
            sb.append("  node [shape=circle];\n");

            // nodos
            for (int id : nodes) {
                User user = users.get(id);
                sb.append("  ").append(id)
                  .append(" [label=\"").append(escape(user.name)).append("\"];\n");
            }

            // amistades (solo entre nodos incluidos)
            Set<String> seenUndir = new HashSet<>();
            for (int a : nodes) {
                for (int b : friends.getOrDefault(a, Collections.emptySet())) {
                    if (!nodes.contains(b)) continue;
                    int x = Math.min(a, b), y = Math.max(a, b);
                    String key = x + "-" + y;
                    if (seenUndir.add(key)) {
                        sb.append("  ").append(x).append(" -> ").append(y)
                          .append(" [dir=none, style=solid];\n");
                    }
                }
            }

            // sugerencias: u -> candidato (dirigido) con etiqueta RS
            for (Suggestion s : candidates) {
                sb.append("  ").append(u).append(" -> ").append(s.userId)
                  .append(" [style=dashed, label=\"RS=").append(String.format(Locale.US, "%.3f", s.rs)).append("\"];\n");
            }

            sb.append("}\n");
            return sb.toString();
        }

        private static String escape(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    static final class Suggestion {
        final int userId;
        final double rs;

        Suggestion(int userId, double rs) {
            this.userId = userId;
            this.rs = rs;
        }
    }

    public static void main(String[] args) {

        SocialGraph g = new SocialGraph();

        // Usuarios solicitados
        g.addUser(1, "Rene");
        g.addUser(2, "Raul");
        g.addUser(3, "Adrian");
        g.addUser(4, "Eduardo");
        g.addUser(5, "Enrique");
        g.addUser(6, "Clement");
        g.addUser(7, "Daniel");
        g.addUser(8, "Juan");
        g.addUser(9, "Jose");

        // Amistades (ejemplo mínimo)
        g.addFriendship(1, 2); // Rene - Raul
        g.addFriendship(2, 3); // Raul - Adrian
        g.addFriendship(2, 4); // Raul - Eduardo
        g.addFriendship(3, 5); // Adrian - Enrique
        g.addFriendship(4, 6); // Eduardo - Clement
        g.addFriendship(5, 7); // Enrique - Daniel
        g.addFriendship(6, 8); // Clement - Juan
        g.addFriendship(7, 9); // Daniel - Jose

        // Visitas (direccional, asimétrico)
        g.recordVisit(1, 3); // Rene visita Adrian
        g.recordVisit(1, 3);
        g.recordVisit(1, 4); // Rene visita Eduardo
        g.recordVisit(1, 5); // Rene visita Enrique
        g.recordVisit(1, 5);
        g.recordVisit(1, 5);

        Scanner sc = new Scanner(System.in);

        System.out.print("ID usuario u: ");
        int u = sc.nextInt();

        System.out.print("ID usuario base: ");
        int base = sc.nextInt();

        System.out.print("Indice de imprecision (0..1): ");
        double threshold = sc.nextDouble();

        List<Suggestion> sug = g.suggestFromFriendsOf(u, base, threshold);

        System.out.println("\nSugerencias:");
        if (sug.isEmpty()) {
            System.out.println("Sin resultados.");
        } else {
            for (Suggestion s : sug) {
                System.out.printf(Locale.US,
                        "%s  RS=%.3f%n",
                        g.getUser(s.userId).name,
                        s.rs);
            }
        }

        // Aquí se imprime el grafo en formato DOT
        System.out.println("\nGrafo (DOT/Graphviz) de conexiones posibles:");
        System.out.println(g.toDot(u, base, sug));
    }
}