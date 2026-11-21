package gestionrpg;

import gestionrpg.controlador.PersonajeControlador;
import gestionrpg.repositorio.PersonajeRepository;
import gestionrpg.repositorio.RepositoryFactory;
import gestionrpg.servicio.PersonajeService;
import io.javalin.Javalin;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Punto de arranque del RPG Manager (Javalin).
 *
 * 1. Lee config.properties (copia config.example.properties y pon tus datos).
 * 2. La fábrica crea el repositorio que diga la clave "repositorio".
 * 3. El Service recibe ese repositorio por el constructor.
 * 4. El controlador recibe el Service y registra las rutas.
 */
public class Main {

    public static Properties cargarConfiguracion() {
        Path directorio = Path.of("").toAbsolutePath();
        Path proyecto = directorio.resolve("rpg_manager_multinivel_javalin");
        Path ruta = directorio.resolve("config.properties");
        if (!Files.exists(ruta) && Files.exists(proyecto.resolve("config.properties"))) {
            ruta = proyecto.resolve("config.properties");
        }
        if (!Files.exists(ruta)) {
            Path ejemplo = directorio.resolve("config.example.properties");
            if (!Files.exists(ejemplo)) {
                ejemplo = proyecto.resolve("config.example.properties");
            }
            if (!Files.exists(ejemplo)) {
                throw new IllegalStateException("No se encontró config.properties ni config.example.properties "
                        + "en " + directorio + " o " + proyecto);
            }
            ruta = ejemplo;
            System.err.println("Aviso: no existe config.properties; se usará la configuración de ejemplo "
                    + "(repositorio en memoria).");
        }
        Properties cfg = new Properties();
        try (Reader lector = new InputStreamReader(Files.newInputStream(ruta), StandardCharsets.UTF_8)) {
            cfg.load(lector);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer config.properties", e);
        }
        return cfg;
    }

    public static void main(String[] args) {
        // >>> SALA 4.1 · El arranque arma la cadena
        // Arma este bloque en el Edificio de la Persistencia (pestaña Java),
        // pulsa «Copiar bloque» y pégalo entre estas dos marcas.
                Properties cfg = cargarConfiguracion();
        PersonajeRepository repositorio = RepositoryFactory.crear(cfg);     // aquí se elige el motor
        PersonajeService service = new PersonajeService(repositorio);       // inyección por constructor
        PersonajeControlador controlador = new PersonajeControlador(service, cfg.getProperty("repositorio"));

        Javalin app = Javalin.create();
        controlador.registrarRutas(app);
        app.start(7070);
        // <<< FIN SALA 4.1
        System.out.println("RPG Manager en http://localhost:7070/personajes · repositorio: "
                + cfg.getProperty("repositorio"));
    }
}
