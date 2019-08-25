
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.History;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.geometry.*;
import javafx.scene.input.KeyCode;

import java.io.File;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;
import java.util.Locale;
import javax.speech.*;
import javax.speech.synthesis.*;
// Import.

/**
 * This class represents a chatbot to interact with users on computer
 * recommendation.
 *
 * @author Team 12
 */
public class MychatbotLive extends Application {

    private static final boolean TRACE_MODE = false;
    static String botName = "Computer";
    private static String robotText = " ";
    // Private data fields.

    // Method to get the path of aiml.
    private static String getResourcePath() {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        path = path.substring(0, path.length() - 2);
        System.out.println(path);
        String resourcePath = path + File.separator + "src/mybot";
        return resourcePath;
    }

    /**
     *
     * @param args Line arguments.
     */
    public static void main(String args[]) {
        // launch the application 
        launch(args);

    }//End of main.

    /**
     * This method transfers text to speech.
     *
     * @param robotText The text for the robot to read.
     * @param voiceName Name of the voice.
     */
    public void dospeak(String robotText, String voiceName) {

        try {
            System.setProperty("FreeTTSSynthEngineCentral", "com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            // Set property.
            Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
            // Register an engine.
            SynthesizerModeDesc desc = new SynthesizerModeDesc(null, "general", Locale.US, null, null);
            Synthesizer synthesizer = Central.createSynthesizer(desc);
            // Define synthesizer

            EngineList engineList = Central.availableSynthesizers(desc);
            EngineCreate creator = (EngineCreate) engineList.get(0);
            List<Synthesizer> bagOfSynthesizers = new LinkedList<Synthesizer>();
            //Engine and synthesizer list.

            int numberSynthesizers = 1;
            for (int i = 0; i < numberSynthesizers; i++) {
                synthesizer = (Synthesizer) creator.createEngine();
                bagOfSynthesizers.add(synthesizer);
                synthesizer.allocate();
                synthesizer.resume();
                desc = (SynthesizerModeDesc) synthesizer.getEngineModeDesc();
            }
            //Allocate synthesizer.

            Voice[] voices = desc.getVoices();
            Voice voice = null;
            for (int i = 0; i < voices.length; i++) {
                if (voices[i].getName().equals(voiceName)) {
                    voice = voices[i];
                    break;
                }
            }
            //Get the voice.

            synthesizer.getSynthesizerProperties().setVoice(voice);
            synthesizer.speakPlainText(robotText, null);
            synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
            synthesizer.deallocate();
            //Get the text and speak out.

        } catch (PropertyVetoException | IllegalArgumentException | InterruptedException | SecurityException | AudioException | EngineException e) {
            String message = " missing speech.properties in " + System.getProperty("user.home") + "\n";
            System.out.println("" + e);
            System.out.println(message);
        }//Catch exceptions.
    }

    /**
     * This method build a GUI and have event handling.
     *
     * @param s Stage
     */
    @Override
    public void start(Stage s) {
        try {
            String resourcePath = getResourcePath();
            System.out.println(resourcePath);
            MagicBooleans.trace_mode = TRACE_MODE;
            Bot bot = new Bot(botName, resourcePath);
            Chat chatSession = new Chat(bot);
            bot.brain.nodeStats();
            //Set the bot.

            BorderPane pane = new BorderPane();
            Button btStart = new Button("Recording");
            Button btSpeech = new Button("Robot Speak");
            pane.setLeft(btStart);
            pane.setRight(btSpeech);
            BorderPane.setMargin(btStart, new Insets(10, 20, 20, 50));
            BorderPane.setMargin(btSpeech, new Insets(10, 20, 20, 50));
            TextField b = new TextField();
            TextArea rr = new TextArea();
            rr.setPrefSize(150, 375);
            //Create the pane and adds the elements.

            b.setAlignment(Pos.CENTER);// set alignment of text 

            rr.appendText("Robot : Hello, I am your personal computer recommender! May I know your name?"
                    + "\n" + "(press ENTER to send or type 'q' to exit.)");

            // action event 
            EventHandler<ActionEvent> addQuestion = new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    String textLine = "";
                    textLine = b.getText();
                    if ((textLine == null) || (textLine.length() < 1)) {
                        textLine = MagicStrings.null_input;
                    }
                    if (textLine.equals("q")) {
                        System.exit(0);//Exit if uesers input q.
                    } else if (textLine.equals("wq")) {
                        bot.writeQuit();
                    } else {
                        String request = textLine;
                        if (MagicBooleans.trace_mode) {
                            System.out.println("STATE=" + request + ":THAT" + ((History) chatSession.thatHistory.get(0)).get(0) + ": Topic" + chatSession.predicates.get("topic"));
                        }
                        String response = chatSession.multisentenceRespond(request);
                        while (response.contains("&lt;")) {
                            response = response.replace("&lt;", "<");
                        }
                        while (response.contains("&gt")) {
                            response = response.replace("&gt;", ">");
                        }
                        rr.appendText("\n\n" + "Human : " + b.getText() + "\n\n" + "Robot : " + response);
                        //Append the text to the textfield.
                        robotText = response;
                    }
                }
            };

            btSpeech.setOnAction(e -> dospeak(robotText, "kevin16"));
            // when enter is pressed 

            b.setOnAction(addQuestion);
            // clear the textfield every time release ENTER   

            b.setOnKeyReleased(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    b.clear();
                }
            });

            pane.setBottom(b);
            BorderPane.setMargin(b, new Insets(0, 20, 10, 20));
            pane.setTop(rr);
            BorderPane.setMargin(rr, new Insets(10, 20, 20, 20));
            //Set the layout.

            Configuration configuration = new Configuration();
            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
            LiveSpeechRecognizer recognizer1 = new LiveSpeechRecognizer(configuration);

            EventHandler<ActionEvent> beginRecord = new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    long t = System.currentTimeMillis();
                    long stopt = t + 5000;
                    while (System.currentTimeMillis() < stopt) {
                        recognizer1.startRecognition(true);
                        SpeechResult result1 = recognizer1.getResult();
                        String x = "";
                        x = x + result1.getHypothesis();
                        b.setText(x);
                    }
                    recognizer1.stopRecognition();//Recognise users' voice.
                }
            };

            btStart.setOnAction(beginRecord);

            Scene scene = new Scene(pane, 700, 500);
            scene.getStylesheets().add(getClass().getResource("JMetroDarkTheme.css").toExternalForm());
            //Set the theme of the GUI.
            s.setScene(scene);// set the scene 

            s.setTitle("ComputerRobot");
            s.show();// set title for the stage

        } catch (IOException e) {
            e.printStackTrace();
        }//Catch exceptions/
    }
}//End of class
