import java.text.SimpleDateFormat;
import java.util.Date;

public class Helper {
    /**
     * Log messages from specific address and port with timestamp
     * @param message  request message from the client
     */
    public void logMessage(String message) {
        String timestamp = getFormattedTimestamp();
        System.out.println(timestamp + " [INFO] " + message);
    }

    /**
     * Log error messages with timestamp.
     * @param errorMessage error occurred
     */
    public void logError(String errorMessage) {
        String timestamp = getFormattedTimestamp();
        System.err.println(timestamp + " [ERROR] " + errorMessage);
    }


    /**
     * Utils.Helper function get current system time in milliseconds.
     * @return time in millis
     */
    private String getFormattedTimestamp() {
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        return sdf.format(new Date(currentTimeMillis));
    }
}
