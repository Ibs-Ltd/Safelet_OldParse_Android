package com.safelet.android.global;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.widget.Button;

import androidx.core.content.ContextCompat;

import com.safelet.android.R;

/**
 * Boiler plate code for constructing dialogs
 */
public final class PopDialog {
    public static final int TYPE_OK = 1;
    public static final int TYPE_DISMISS = 2;

    private PopDialog() {
        throw new UnsupportedOperationException();
    }

    /**
     * Displays dialog
     *
     * @param context         Context to be used, please pass activity
     * @param title           Title of the dialog
     * @param message         Message of the dialog
     * @param type            Type of the dialog
     * @param onClickListener Callback for dialog actions
     */
    public static void showDialog(Context context, String title, String message, int type,
                                  OnClickListener onClickListener) {
        Builder builder = new Builder(context);
        builder.setMessage(message).setTitle(title);
        if (type == TYPE_OK) {
            builder.setNeutralButton(context.getString(R.string.ok), onClickListener);
        } else if (type == TYPE_DISMISS) {
            builder.setNeutralButton(context.getString(R.string.dismiss), onClickListener);
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Displays dialog
     *
     * @param context         Context to be used, please pass activity
     * @param title           Title of the dialog
     * @param message         Message of the dialog
     * @param onClickListener Callback for dialog actions
     */
    public static void showDialog(Context context, String title, String message,
                                  OnClickListener onClickListener) {
        Builder builder = new Builder(context);
        builder.setMessage(message).setTitle(title)
                .setNeutralButton(context.getString(R.string.ok), onClickListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Displays dialog
     *
     * @param context Context to be used, please pass activity
     * @param title   Title of the dialog
     * @param message Message of the dialog
     */
    public static void showDialog(Context context, String title, String message) {
        Builder builder = new Builder(context);
        builder.setMessage(message).setTitle(title)
                .setNeutralButton(context.getString(R.string.ok), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Displays dialog
     *
     * @param context                  Context to be used, please pass activity
     * @param message                  Message of the dialog
     * @param onClickListenerYesButton Callback for yes button
     * @param onClickListenerNoButton  Callback for no button
     */
    public static void showDialog(Context context, String message, OnClickListener onClickListenerYesButton,
                                  OnClickListener onClickListenerNoButton) {
        Builder builder = new Builder(context);
        builder.setMessage(message).setPositiveButton(context.getString(R.string.yes), onClickListenerYesButton)
                .setNegativeButton(context.getString(R.string.no), onClickListenerNoButton).setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Displays dialog
     *
     * @param context                  Context to be used, please pass activity
     * @param title                    Title of dialog
     * @param positiveMessage          Button positive action text
     * @param negativeMessage          Button negative action text
     * @param message                  Message of the dialog
     * @param onClickListenerYesButton Callback for yes button
     * @param onClickListenerNoButton  Callback for no button
     * @return the reference to the alert that is displayed
     */
    public static AlertDialog showDialog(Context context, String title, String message, String positiveMessage,
                                         String negativeMessage, OnClickListener onClickListenerYesButton,
                                         OnClickListener onClickListenerNoButton) {
        Builder builder = new Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveMessage, onClickListenerYesButton)
                .setNegativeButton(negativeMessage, onClickListenerNoButton)
                .setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static AlertDialog showDialogNew(Context context, String title, String message, String positiveMessage,
                                         String negativeMessage, String neutralMessage, OnClickListener onClickListenerYesButton,
                                         OnClickListener onClickListenerNoButton, OnClickListener onClickListenerNeutralButton) {
        Builder builder = new Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveMessage, onClickListenerYesButton)
                .setNegativeButton(negativeMessage, onClickListenerNoButton)
                .setNeutralButton(neutralMessage, onClickListenerNeutralButton)
                .setCancelable(true);
        AlertDialog dialog = builder.create();

        // Set custom color for the neutral button (API level 21 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.setOnShowListener(dialogInterface -> {
                Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                if (neutralButton != null) {
                    neutralButton.setTextSize(16);
                    neutralButton.setTextColor(ContextCompat.getColor(context, R.color.red_wrong));
                }
            });
        }

        dialog.show();
        return dialog;
    }


    /**
     * Displays dialog
     *
     * @param context                  Context to be used, please pass activity
     * @param positiveMessage          Button positive action text
     * @param negativeMessage          Button negative action text
     * @param message                  Message of the dialog
     * @param onClickListenerYesButton Callback for yes button
     * @param onClickListenerNoButton  Callback for no button
     */
    public static void showDialog(Context context, String message, String positiveMessage,
                                  String negativeMessage, OnClickListener onClickListenerYesButton,
                                  OnClickListener onClickListenerNoButton) {
        Builder builder = new Builder(context);
        builder.setMessage(message).setPositiveButton(positiveMessage, onClickListenerYesButton)
                .setNegativeButton(negativeMessage, onClickListenerNoButton).setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Displays dialog
     *
     * @param context Context to be used, please pass activity
     * @param message Message of the dialog
     */
    public static void showDialog(Context context, String message) {
        Builder builder = new Builder(context);
        builder.setMessage(message).setNeutralButton(context.getString(R.string.ok), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Displays dialog
     *
     * @param context         Context to be used, please pass activity
     * @param message         Message of the dialog
     * @param onClickListener Callback for ok button
     */
    public static void showDialog(Context context, String message, OnClickListener onClickListener) {
        Builder builder = new Builder(context);
        builder.setMessage(message).setNeutralButton(context.getString(R.string.ok), onClickListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Displays dialog
     *
     * @param context                 Context to be used, please pass activity
     * @param message                 Message of the dialog
     * @param onClickListenerOkButton Callback for ok button
     * @param onCancelListener        Callback when canceling the dialog
     */
    public static void showDialog(Context context, String message, OnClickListener onClickListenerOkButton,
                                  OnCancelListener onCancelListener) {
        Builder builder = new Builder(context);
        builder.setMessage(message).setNeutralButton(context.getString(R.string.ok), onClickListenerOkButton);
        builder.setOnCancelListener(onCancelListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Displays dialog
     *
     * @param context                  Context to be used, please pass activity
     * @param title                    Title of the dialog
     * @param message                  Message of the dialog
     * @param onClickListenerYesButton Callback for yes button
     * @param onClickListenerNoButton  Callback for no button
     */
    public static void showDialog(Context context, String title,
                                  String message, OnClickListener onClickListenerYesButton,
                                  OnClickListener onClickListenerNoButton) {
        Builder builder = new Builder(context);
        builder.setTitle(title);
        builder.setMessage(message)
                .setPositiveButton(context.getString(R.string.yes), onClickListenerYesButton)
                .setNegativeButton(context.getString(R.string.no), onClickListenerNoButton)
                .setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
