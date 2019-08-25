
import java.io.File;
import org.alicebot.ab.Bot;
import org.alicebot.ab.MagicBooleans;

/**
 * This class generates CSV according to AIML.
 *
 * @author Team 12
 */
public class GenerateAIML {

    private static final boolean TRACE_MODE = false;
    static String botName = "Computer";

    /**
     * Main method.
     *
     * @param args Line arguments.
     */
    public static void main(String[] args) {
        try {

            String resourcesPath = getResourcesPath();
            System.out.println(resourcesPath);
            MagicBooleans.trace_mode = TRACE_MODE;
            Bot bot = new Bot("Computer", resourcesPath);

            bot.writeAIMLFiles();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//End of main.

    /**
     * Get the path of AIML.
     *
     * @return Resource Path.
     */
    private static String getResourcesPath() {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        path = path.substring(0, path.length() - 2);
        System.out.println(path);
        String resourcePath = path + File.separator + "src/mybot";
        return resourcePath;
    }
}
