import java.util.*;

class Usuario {
    String nombre;
    Set<String> amigos;
    Map<String, Integer> interacciones; //c(u,v)

    public Usuario(String nombre) {
        this.nombre = nombre;
        this.amigos = new HashSet<>();
        this.interacciones = new HashMap<>();
    }

    public void agregarAmigo(String nombre) {
        amigos.add(nombre);
    }

    // Registra 1 interacción dirigida: this.nombre -> nombreAmigo
    public void registrarInteraccion(String nombreAmigo) {
        interacciones.put(nombreAmigo, interacciones.getOrDefault(nombreAmigo, 0) + 1); // c(u,v)++
    }

    public int obtenerInteraccionesCon(String nombreAmigo) {
        return interacciones.getOrDefault(nombreAmigo, 0);
    }
}

public class RedSocial {

    static Map<String, Usuario> usuarios = new HashMap<>(); //almacena usuarios por nombre

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Ingrese el índice de imprecisión (0 a 1): ");
        double indiceImprecision = sc.nextDouble(); //lee token numerico

        crearUsuarios();
        registrarInteraccionesVariadas();
        sugerirAmigos(indiceImprecision);

        sc.close();
    }

    public static void crearUsuarios() {
        usuarios.put("rene", new Usuario("rene"));
        usuarios.put("adrian", new Usuario("adrian"));
        usuarios.put("raul", new Usuario("raul"));
        usuarios.put("eduardo", new Usuario("eduardo"));
        usuarios.put("enrique", new Usuario("enrique"));
        usuarios.put("juan", new Usuario("juan"));
        usuarios.put("jose", new Usuario("jose"));
        usuarios.put("daniel", new Usuario("daniel"));
        usuarios.put("clement", new Usuario("clement"));

        // 3 amistades por usuario
        usuarios.get("rene").agregarAmigo("adrian");
        usuarios.get("rene").agregarAmigo("raul");
        usuarios.get("rene").agregarAmigo("eduardo");

        usuarios.get("adrian").agregarAmigo("rene");
        usuarios.get("adrian").agregarAmigo("raul");
        usuarios.get("adrian").agregarAmigo("enrique");

        usuarios.get("raul").agregarAmigo("rene");
        usuarios.get("raul").agregarAmigo("adrian");
        usuarios.get("raul").agregarAmigo("juan");

        usuarios.get("eduardo").agregarAmigo("rene");
        usuarios.get("eduardo").agregarAmigo("enrique");
        usuarios.get("eduardo").agregarAmigo("jose");

        usuarios.get("enrique").agregarAmigo("adrian");
        usuarios.get("enrique").agregarAmigo("eduardo");
        usuarios.get("enrique").agregarAmigo("daniel");

        usuarios.get("juan").agregarAmigo("raul");
        usuarios.get("juan").agregarAmigo("jose");
        usuarios.get("juan").agregarAmigo("clement");

        usuarios.get("jose").agregarAmigo("eduardo");
        usuarios.get("jose").agregarAmigo("juan");
        usuarios.get("jose").agregarAmigo("daniel");

        usuarios.get("daniel").agregarAmigo("enrique");
        usuarios.get("daniel").agregarAmigo("jose");
        usuarios.get("daniel").agregarAmigo("clement");

        usuarios.get("clement").agregarAmigo("juan");
        usuarios.get("clement").agregarAmigo("daniel");
        usuarios.get("clement").agregarAmigo("rene");
    }

    // Helper: registra "veces" interacciones dirigidas u -> v
    static void registrarVarias(String u, String v, int veces) {
        Usuario origen = usuarios.get(u);
        if (origen == null) return;
        for (int i = 0; i < veces; i++) origen.registrarInteraccion(v);
    }

    // Interacciones dirigidas (visitas/comentarios)
    public static void registrarInteraccionesVariadas() {
        // rene -> {adrian, raul, eduardo}
        registrarVarias("rene", "adrian", 20);
        registrarVarias("rene", "raul", 8);
        registrarVarias("rene", "eduardo", 2);

        // adrian -> {rene, raul, enrique}
        registrarVarias("adrian", "rene", 15);
        registrarVarias("adrian", "raul", 5);
        registrarVarias("adrian", "enrique", 1);

        // raul -> {rene, adrian, juan}
        registrarVarias("raul", "rene", 12);
        registrarVarias("raul", "adrian", 3);
        registrarVarias("raul", "juan", 6);

        // eduardo -> {rene, enrique, jose}
        registrarVarias("eduardo", "rene", 4);
        registrarVarias("eduardo", "enrique", 10);
        registrarVarias("eduardo", "jose", 1);

        // enrique -> {adrian, eduardo, daniel}
        registrarVarias("enrique", "adrian", 2);
        registrarVarias("enrique", "eduardo", 7);
        registrarVarias("enrique", "daniel", 18);

        // juan -> {raul, jose, clement}
        registrarVarias("juan", "raul", 5);
        registrarVarias("juan", "jose", 14);
        registrarVarias("juan", "clement", 1);

        // jose -> {eduardo, juan, daniel}
        registrarVarias("jose", "eduardo", 9);
        registrarVarias("jose", "juan", 4);
        registrarVarias("jose", "daniel", 2);

        // daniel -> {enrique, jose, clement}
        registrarVarias("daniel", "enrique", 6);
        registrarVarias("daniel", "jose", 2);
        registrarVarias("daniel", "clement", 16);

        // clement -> {juan, daniel, rene}
        registrarVarias("clement", "juan", 1);
        registrarVarias("clement", "daniel", 5);
        registrarVarias("clement", "rene", 19);
    }

    public static void sugerirAmigos(double indiceImprecision) {
        StringBuilder dot = new StringBuilder("digraph G {\n");

        for (Usuario u1 : usuarios.values()) {
            for (Usuario u2 : usuarios.values()) {
                if (!u1.nombre.equals(u2.nombre)) {
                    double rs = calcularSimilitud(u1, u2);

                    if (rs > indiceImprecision) {
                        dot.append("  \"")
                           .append(u1.nombre)
                           .append("\" -> \"")
                           .append(u2.nombre)
                           .append("\" [label=\"")
                           .append(String.format("%.2f", rs))
                           .append("\"];\n");
                    }
                }
            }
        }

        dot.append("}\n");
        System.out.println(dot.toString());
    }

    // RS(u,v) usa SOLO c(u,v) (visitas/comentarios) y queda en (0,1)
    public static double calcularSimilitud(Usuario u1, Usuario u2) {
        int c = u1.obtenerInteraccionesCon(u2.nombre);
        int k = 3;
        if (c == 0) return 0.0;
        return c / (double) (c + k);
    }
}