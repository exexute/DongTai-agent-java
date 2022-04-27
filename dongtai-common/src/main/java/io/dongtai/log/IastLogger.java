package io.dongtai.log;

import io.dongtai.IastProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.regex.Matcher;

public class IastLogger {
    IastLogger instance;
    boolean supportLogToFile = false;
    File logFile;
    boolean supportConsoleColor;
    static IastProperties properties = IastProperties.getInstance();
    public static java.util.logging.Level LEVEL = java.util.logging.Level.CONFIG;

    private static final String RESET = "\033[0m";
    private static final int RED = 31;
    private static final int GREEN = 32;
    private static final int YELLOW = 33;
    private static final int BLUE = 34;

    private static final String TITTLE = "[io.dongtai.iast.agent] ";
    private static final String TITTLE_COLOR_PREFIX = "[" + colorStr("io.dongtai.iast.agent", BLUE) + "] ";

    private static final String TRACE_PREFIX = "[TRACE] ";
    private static final String TRACE_COLOR_PREFIX = "[" + colorStr("TRACE", GREEN) + "] ";

    private static final String DEBUG_PREFIX = "[DEBUG] ";
    private static final String DEBUG_COLOR_PREFIX = "[" + colorStr("DEBUG", GREEN) + "] ";

    private static final String INFO_PREFIX = "[INFO] ";
    private static final String INFO_COLOR_PREFIX = "[" + colorStr("INFO", GREEN) + "] ";

    private static final String WARN_PREFIX = "[WARN] ";
    private static final String WARN_COLOR_PREFIX = "[" + colorStr("WARN", YELLOW) + "] ";

    private static final String ERROR_PREFIX = "[ERROR] ";
    private static final String ERROR_COLOR_PREFIX = "[" + colorStr("ERROR", RED) + "] ";

    public IastLogger getInstance() {
        if (instance == null) {
            instance = new IastLogger();
        }
        return instance;
    }

    public IastLogger() {
        supportConsoleColor = (System.console() != null && !System.getProperty("os.name").toLowerCase().contains("windows"));
        logFile = new File(properties.getLogPath());
        if (logFile.exists()) {
            return;
        }
        if (logFile.getParentFile().exists()) {
            return;
        }
        if (!logFile.getParentFile().mkdirs()) {
            return;
        }
        try {
            logFile.createNewFile();
        } catch (Exception e) {
            e.toString();
        }
    }

    /**
     * set logger Level
     *
     * @param level
     * @return
     * @see java.util.logging.Level
     */
    public static Level level(Level level) {
        Level old = LEVEL;
        LEVEL = level;
        return old;
    }

    private static String colorStr(String msg, int colorCode) {
        return "\033[" + colorCode + "m" + msg + RESET;
    }

    public void trace(String msg) {
        if (canLog(Level.FINEST)) {
            if (supportConsoleColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + TRACE_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + TRACE_PREFIX + msg);
            }
            msg = getTime() + TITTLE + TRACE_PREFIX + msg;
            writeLogToFile(msg);
        }
    }

    public void trace(String format, Object... arguments) {
        if (canLog(Level.FINEST)) {
            trace(format(format, arguments));
        }
    }

    public void trace(Throwable t) {
        if (canLog(Level.FINEST)) {
            t.printStackTrace(System.out);
        }
    }

    public void debug(String msg) {
        if (canLog(Level.FINER)) {
            if (supportConsoleColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + DEBUG_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + DEBUG_PREFIX + msg);
            }
            msg = getTime() + TITTLE + DEBUG_PREFIX + msg;
            if (supportLogToFile) {
                writeLogToFile(msg);
            }
        }
    }

    public void debug(String format, Object... arguments) {
        if (canLog(Level.FINER)) {
            debug(format(format, arguments));
        }
    }

    public void debug(Throwable t) {
        if (canLog(Level.FINER)) {
            t.printStackTrace(System.out);
        }
    }

    public void info(String msg) {
        if (canLog(Level.CONFIG)) {
            if (supportConsoleColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + INFO_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + INFO_PREFIX + msg);
            }
            msg = getTime() + TITTLE + INFO_PREFIX + msg;
            if (supportLogToFile) {
                writeLogToFile(msg);
            }
        }
    }

    public void info(String format, Object... arguments) {
        if (canLog(Level.CONFIG)) {
            info(format(format, arguments));
        }
    }

    public void info(Throwable t) {
        if (canLog(Level.CONFIG)) {
            t.printStackTrace(System.out);
        }
    }

    public void warn(String msg) {
        if (canLog(Level.WARNING)) {
            if (supportConsoleColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + WARN_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + WARN_PREFIX + msg);
            }
            msg = getTime() + TITTLE + WARN_PREFIX + msg;
            if (supportLogToFile) {
                writeLogToFile(msg);
            }
        }
    }

    public void warn(String format, Object... arguments) {
        if (canLog(Level.WARNING)) {
            warn(format(format, arguments));
        }
    }

    public void warn(Throwable t) {
        if (canLog(Level.WARNING)) {
            t.printStackTrace(System.out);
        }
    }

    public void error(String msg) {
        if (canLog(Level.SEVERE)) {
            if (supportConsoleColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + ERROR_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + ERROR_PREFIX + msg);
            }
            msg = getTime() + TITTLE + ERROR_PREFIX + msg;
            if (supportLogToFile) {
                writeLogToFile(msg);
            }
        }
    }

    public void error(String format, Object... arguments) {
        if (canLog(Level.SEVERE)) {
            error(format(format, arguments));
        }
    }

    public void error(Throwable t) {
        if (canLog(Level.SEVERE)) {
            t.printStackTrace(System.out);
        }
    }

    private String format(String from, Object... arguments) {
        if (from != null) {
            String computed = from;
            if (arguments != null && arguments.length != 0) {
                for (Object argument : arguments) {
                    computed = computed.replaceFirst("\\{\\}", argument == null ? "NULL" : Matcher.quoteReplacement(argument.toString()));
                }
            }
            return computed;
        }
        return null;
    }

    private boolean canLog(Level level) {
        return level.intValue() >= LEVEL.intValue();
    }

    public boolean isDebugEnabled() {
        if ("debug".equals(properties.getLogLevel())) {
            level(Level.ALL);
            return true;
        } else {
            return false;
        }
    }

    private String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return simpleDateFormat.format(new Date()) + " ";
    }

    private void writeLogToFile(String msg) {
        FileOutputStream o = null;
        try {
            o = new FileOutputStream(logFile, true);
            o.write(msg.getBytes());
            o.write(System.getProperty("line.separator").getBytes());
            o.flush();
            o.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
