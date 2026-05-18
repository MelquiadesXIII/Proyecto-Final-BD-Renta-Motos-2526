package org.proyectobdmotos.utils;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TermsWindow {

    private static final String TERMS_TEXT =
        "TÉRMINOS Y CONDICIONES DE USO\n\n" +
        "1. ACEPTACIÓN DE LOS TÉRMINOS\n" +
        "El acceso y la utilización de la presente aplicación informática (en lo sucesivo, «el Software») " +
        "conllevan la aceptación plena, irrevocable e incondicional de los presentes Términos y Condiciones. " +
        "Si el Usuario no estuviere conforme con cualquiera de las estipulaciones aquí contenidas, " +
        "deberá abstenerse de emplear el Software de manera inmediata e irrevocable, " +
        "quedando vedada cualquier forma de acceso o utilización ulterior.\n\n" +

        "2. NATURALEZA DEL SERVICIO\n" +
        "El Software se suministra exclusivamente como una herramienta de gestión administrativa " +
        "destinada al control y seguimiento de operaciones de alquiler de motocicletas. " +
        "Su funcionalidad se circunscribe a la administración de clientes, vehículos y contratos asociados. " +
        "El desarrollador no interviene, en modo alguno, en las transacciones comerciales subyacentes " +
        "ni actúa como intermediario, mandatario o representante contractual entre las partes involucradas.\n\n" +

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
        "idoneidad para un propósito particular y no infracción de derechos de propiedad intelectual. " +
        "El desarrollador no garantiza la exactitud, integridad, actualidad o fiabilidad " +
        "de los datos almacenados, ni la disponibilidad ininterrumpida o libre de errores del servicio.\n\n" +

        "5. PROPIEDAD INTELECTUAL\n" +
        "Todos los derechos de propiedad intelectual e industrial sobre el código fuente, " +
        "diseño, interfaz gráfica, logotipos, bases de datos y demás elementos del Software " +
        "pertenecen en exclusiva al desarrollador. Queda terminantemente prohibida " +
        "la reproducción, distribución, comunicación pública, transformación o ingeniería inversa " +
        "del Software, total o parcialmente, sin autorización previa, expresa y por escrito del titular.\n\n" +

        "6. MODIFICACIONES\n" +
        "El desarrollador se reserva el derecho de modificar unilateralmente " +
        "los presentes Términos y Condiciones en cualquier momento y sin previo aviso. " +
        "Las modificaciones entrarán en vigor desde el instante mismo de su publicación en el Software. " +
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
        "y aceptado de manera irrevocable la totalidad de los presentes Términos y Condiciones.";

    public static void show(Stage owner) 
    {
        // Texto con area de desplazamiento
        TextArea textArea = new TextArea(TERMS_TEXT);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-size: 14px; -fx-control-inner-background: rgba(0,0,0,0.7); -fx-text-fill: white;");

        // Imagen de fondo
        ImageView background = new ImageView(new Image(TermsWindow.class.getResourceAsStream("/Utiles/terminos.jpg")));
        background.setFitWidth(1920);
        background.setFitHeight(1080);
        background.setPreserveRatio(false);

        // StackPane para superponer el texto sobre la imagen
        StackPane root = new StackPane(background, textArea);
        Scene scene = new Scene(root, 700, 600);

        Stage stage = new Stage();
        stage.setTitle("Términos y Condiciones");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.show();
    }
}