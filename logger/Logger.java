package logger;

public class Logger {

    private static LogLevel logLevel = LogLevel.DEBUG;
    private static boolean enabled = true;

    public static void log(LogLevel messageLogLevel, String logMessage) {

        if (logLevel.ordinal() >= messageLogLevel.ordinal() && enabled) {
            for (int i = 0; i < messageLogLevel.ordinal(); i++) {
                System.out.print("\t");
            }
            System.out.printf("[<%s> \t-> message: %s] \n", messageLogLevel.name(), logMessage);
        }
    }

    public static synchronized void init(LogLevel newLogLevel) {
        logLevel = newLogLevel;
    }

    public static synchronized void enable() {
        enabled = true;
    }

    public static synchronized void disable() {
        enabled = true;
    }

}
