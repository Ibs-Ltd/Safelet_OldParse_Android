package com.safelet.android.utils.log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.squareup.tape.QueueFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothLog {

    private static final String FOLDER_NAME = "/Bluetooth";

    private static final int LOG_SIZE = 1000;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private static QueueFile sQueueFile = null;
    private static File sLogFile = null;

    public static byte[] getLogs() {
        if (sQueueFile == null) {
            return new byte[0];
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            sQueueFile.forEach(new LogFileElementReader(byteArrayOutputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static void writeLog(final String tag, final String message) {
        EXECUTOR_SERVICE.submit(new Runnable() {
            @Override
            public void run() {
                writeLogInternal(tag, message);
            }
        });
    }

    public static void writeThrowable(final String tag, final Throwable throwable) {
        if (throwable == null) {
            return;
        }
        EXECUTOR_SERVICE.submit(new Runnable() {
            @Override
            public void run() {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                throwable.printStackTrace(printWriter);
                writeLogInternal(tag, stringWriter.toString());
            }
        });
    }

    private static void writeLogInternal(String tag, String message) {
        if (sQueueFile == null || sLogFile == null) {
            try {
                sLogFile = LogUtils.getLogFile(FOLDER_NAME);
                sQueueFile = new QueueFile(sLogFile);
            } catch (IOException exception) {
                exception.printStackTrace();
                FirebaseCrashlytics.getInstance().log(exception.getMessage());
            }
        }
        if (sQueueFile == null) {
            return;
        }
        while (sQueueFile.size() >= LOG_SIZE) {
            try {
                sQueueFile.remove();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LogUtils.writeLog(tag, message, sQueueFile);
    }

    public static void clearLogFiles() {
        if (sLogFile != null) {
            sLogFile.delete();
            sLogFile = null;
        }
        sQueueFile = null;
    }

    private static class LogFileElementReader implements QueueFile.ElementReader {

        private ByteArrayOutputStream mByteArrayOutputStream;

        LogFileElementReader(ByteArrayOutputStream byteArrayOutputStream) {
            mByteArrayOutputStream = byteArrayOutputStream;
        }

        @Override
        public void read(InputStream in, int length) throws IOException {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) != -1) {
                mByteArrayOutputStream.write(buffer, 0, count);
            }
        }
    }
}
