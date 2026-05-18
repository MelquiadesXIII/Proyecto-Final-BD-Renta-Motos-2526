package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.proyectobdmotos.ui.navigation.NavigationHistory;
import org.proyectobdmotos.ui.navigation.ScreenLoader;
import java.io.IOException;

public class TerminosController {

    @FXML
    private TextArea areaTexto;
    private final ScreenLoader screenLoader;

    public TerminosController(ScreenLoader screenLoader) {
        this.screenLoader = screenLoader;
    }

    @FXML
    private void initialize() {
        areaTexto.setText(
                "TÉRMINOS Y CONDICIONES DE USO\n\n" +
                        "1. ACEPTACIÓN DE LOS TÉRMINOS\n" +
                        "El acceso y la utilización de la presente aplicación informática (en lo sucesivo, «el Software») "
                        +
                        "conllevan la aceptación plena, irrevocable e incondicional de los presentes Términos y Condiciones. "
                        +
                        "Si el Usuario no estuviere conforme con cualquiera de las estipulaciones aquí contenidas, " +
                        "deberá abstenerse de emplear el Software de manera inmediata e irrevocable, " +
                        "quedando vedada cualquier forma de acceso o utilización ulterior.\n\n" +

                        "2. NATURALEZA DEL SERVICIO\n" +
                        "El Software se suministra exclusivamente como una herramienta de gestión administrativa " +
                        "destinada al control y seguimiento de operaciones de alquiler de motocicletas. " +
                        "Su funcionalidad se circunscribe a la administración de clientes, vehículos y contratos asociados. "
                        +
                        "El desarrollador no interviene, en modo alguno, en las transacciones comerciales subyacentes "
                        +
                        "ni actúa como intermediario, mandatario o representante contractual entre las partes involucradas.\n\n"
                        +

                        "3. EXENCIÓN DE RESPONSABILIDAD\n" +
                        "El desarrollador declina y excluye de manera expresa toda responsabilidad por daños, " +
                        "pérdidas o perjuicios de cualquier naturaleza —ya sean directos, indirectos, incidentales, " +
                        "consecuenciales, punitivos o ejemplarizantes— que pudieran derivarse de la utilización, " +
                        "correcta o incorrecta, lícita o ilícita, del Software. Lo anterior comprende, " +
                        "a título enunciativo pero no limitativo:\n" +
                        "- Fallos en el sistema informático, errores de programación, interrupciones del servicio, " +
                        "pérdida o corrupción de datos.\n" +
                        "- Decisiones comerciales, financieras, legales o de cualquier otra índole adoptadas " +
                        "con base en la información proporcionada por el Software.\n" +
                        "- Accidentes, lesiones corporales, daños materiales o fallecimientos relacionados " +
                        "con el uso de las motocicletas gestionadas mediante el Software.\n" +
                        "- Cualesquiera actos u omisiones imputables a los usuarios, empleados, " +
                        "contratistas o terceros vinculados a la operación de la aplicación.\n\n" +

                        "4. AUSENCIA DE GARANTÍAS\n" +
                        "El Software se proporciona «tal cual» y «según disponibilidad», " +
                        "sin garantías de ninguna clase, ya sean explícitas o implícitas, " +
                        "incluyendo, entre otras, las garantías implícitas de comerciabilidad, " +
                        "idoneidad para un propósito particular y no infracción de derechos de propiedad intelectual. "
                        +
                        "El desarrollador no garantiza la exactitud, integridad, actualidad o fiabilidad " +
                        "de los datos almacenados, ni la disponibilidad ininterrumpida o libre de errores del servicio.\n\n"
                        +

                        "5. PROPIEDAD INTELECTUAL\n" +
                        "Todos los derechos de propiedad intelectual e industrial sobre el código fuente, " +
                        "diseño, interfaz gráfica, logotipos, bases de datos y demás elementos del Software " +
                        "pertenecen en exclusiva al desarrollador. Queda terminantemente prohibida " +
                        "la reproducción, distribución, comunicación pública, transformación o ingeniería inversa " +
                        "del Software, total o parcialmente, sin autorización previa, expresa y por escrito del titular.\n\n"
                        +

                        "6. MODIFICACIONES\n" +
                        "El desarrollador se reserva el derecho de modificar unilateralmente " +
                        "los presentes Términos y Condiciones en cualquier momento y sin previo aviso. " +
                        "Las modificaciones entrarán en vigor desde el instante mismo de su publicación en el Software. "
                        +
                        "Se recomienda encarecidamente al Usuario revisar de forma periódica este apartado, " +
                        "ya que el uso continuado del Software tras la publicación de cambios " +
                        "implica la aceptación tácita de los mismos.\n\n" +

                        "7. LEGISLACIÓN APLICABLE Y JURISDICCIÓN\n" +
                        "La interpretación, validez y cumplimiento de estos Términos se regirán " +
                        "por las leyes de la República de Cuba. Cualquier controversia, litigio o reclamación " +
                        "que pudiera surgir en relación con el Software o los presentes Términos " +
                        "será sometida a la jurisdicción exclusiva de los tribunales ordinarios competentes " +
                        "del domicilio del desarrollador, con renuncia expresa e irrevocable " +
                        "a cualquier otro fuero o jurisdicción que pudiera corresponder.\n\n" +

                        "Al hacer uso del Software, el Usuario manifiesta haber leído, comprendido " +
                        "y aceptado de manera irrevocable la totalidad de los presentes Términos y Condiciones.");
    }

    @FXML
    private void cerrarVentana() {
        Parent anterior = NavigationHistory.goBack(screenLoader);
        Stage stage = (Stage) areaTexto.getScene().getWindow();
        if (anterior != null) {
            stage.setScene(new Scene(anterior));
        } else {
            stage.close();
        }
    }
}