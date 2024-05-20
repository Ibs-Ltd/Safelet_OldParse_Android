package com.safelet.android.interactors;

import android.os.Build;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.safelet.android.interactors.utils.ParseConstants;
import com.safelet.android.utils.log.BluetoothLog;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;


public class LoggerManager {
    private static final String TAG = LoggerManager.class.getSimpleName();

    public static void uploadBluetoothFailedLogFile(String firmwareRevision) {
        BluetoothLog.writeLog(TAG, Build.MANUFACTURER + " " + Build.MODEL +
                " aos v" + Build.VERSION.RELEASE + " lvl" + Build.VERSION.SDK_INT +
                " firmware " + firmwareRevision);
        // upload connection failed log file to parse cloud
        final ParseFile parseFile = new ParseFile(BluetoothLog.getLogs());
        try {
            parseFile.save();
            logUploadLogsFile("upload log file on parse succeeded");
            saveFileOnParse(parseFile);
            // delete bt connection log file
            BluetoothLog.clearLogFiles();
        } catch (ParseException e) {
            logUploadLogsFile("upload log file on parse failed with " + e.getMessage());
        }
    }

    private static void saveFileOnParse(ParseFile parseFile) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.LOG_FILE_URL_PARAM_KEY, parseFile.getUrl());
        logUploadLogsFile("File url = " + parseFile.getUrl());
        try {
            ParseCloud.callFunction(ParseConstants.BLUETOOTH_CONNECTION_FAILED, parameters);
            logUploadLogsFile("save log file on parse succeeded");
        } catch (ParseException e) {
            logUploadLogsFile("save log file on parse failed with: " + e.getMessage());
        }
    }

    private static void logUploadLogsFile(String message) {
        Timber.tag(TAG);
        Timber.d(message);
    }
}
