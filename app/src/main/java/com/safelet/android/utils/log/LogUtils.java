package com.safelet.android.utils.log;

import com.safelet.android.global.ApplicationSafelet;
import com.squareup.tape.QueueFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

public class LogUtils {

    private static String APP_DIR_ABSOLUTE = null;

    static {
        File filesDir = ApplicationSafelet.getContext().getFilesDir();
        if (filesDir != null) {
            APP_DIR_ABSOLUTE = filesDir.getAbsolutePath();
        }
    }

    private static final String LOG_FOLDER = "/Log";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    // Constant to know if log should function or not
    private static boolean sLogsEnabled = true;

    public static void setLogsEnabled(boolean showLogs) {
        sLogsEnabled = showLogs;
    }

    static void writeLog(String tag, String message, QueueFile logFile) {
        if (!sLogsEnabled) {
            return;
        }
        TimberDecorator.d(tag, message);
        if (logFile == null) {
            return;
        }
        // BufferedWriter for performance, true to set append to file flag
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(new Date().toString()).append(" ");
        stringBuffer.append(tag).append(" ").append(message);
        stringBuffer.append(LINE_SEPARATOR);
        try {
            logFile.add(stringBuffer.toString().getBytes());
        } catch (IOException e) {
            Timber.e(e, "Couldn't write log message to file");
        }
    }

    private static String getLogFolder() {
        return APP_DIR_ABSOLUTE + LOG_FOLDER + File.separator;
    }

    /**
     * Returns the file where the logs will be written but the file is not actually created yet.
     *
     * @param folderName name of the folder where log file will be created
     * @return log file
     */
    static File getLogFile(String folderName) {
        deleteExistingLogFiles(folderName);

        String folderPath = getLogFolder() + folderName;
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileNameStr = DATE_FORMATTER.format(new Date()) + ".txt";
        return new File(folder, fileNameStr);
    }

    private static void deleteExistingLogFiles(String folderName) {
        String folderPath = APP_DIR_ABSOLUTE + LOG_FOLDER + File.separator + folderName;
        File folder = new File(folderPath);
        if (!folder.exists() || folder.listFiles() == null) {
            // do nothing because folder doesn't exist
            return;
        }
        File[] logFiles = folder.listFiles();
        for (File file : logFiles) {
            file.delete();
        }
    }
}
