package de.appplant.cordova.plugin.printer;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.core.content.FileProvider;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

public class Printer extends CordovaPlugin {

    private static final String TAG = "Printer";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("print".equals(action)) {
            Log.d("PrinterPlugin", "print() called "+content);
            JSONObject settings = args.optJSONObject(1);
            String content = args.getString(0);

            // Detect if content is base64
            if (content.startsWith("data:application/pdf;base64,")) {
                content = content.replaceFirst("data:application/pdf;base64,", "");
                printBase64(content, settings, callbackContext);
            } else {
                print(content, settings, callbackContext);
            }
            return true;
        }
        return false;
    }

    private void print(String url, JSONObject settings, CallbackContext callbackContext) {
        printJob(url, settings, callbackContext);
    }

    private void printBase64(String base64Data, JSONObject settings, CallbackContext callbackContext) {
        try {
            byte[] data = Base64.decode(base64Data, Base64.DEFAULT);
            File file = File.createTempFile("print", ".pdf", cordova.getActivity().getCacheDir());
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();

            Uri fileUri = FileProvider.getUriForFile(
                cordova.getActivity(),
                cordova.getActivity().getPackageName() + ".fileprovider",
                file
            );

            printJob(fileUri.toString(), settings, callbackContext);
        } catch (Exception e) {
            if (callbackContext != null) {
                callbackContext.error("Failed to handle base64 input: " + e.getMessage());
            } else {
                Log.e(TAG, "Failed to handle base64 input: " + e.getMessage());
            }
        }
    }

    private void printJob(String url, JSONObject settings, CallbackContext callbackContext) {
        try {
            Intent printIntent = new Intent(Intent.ACTION_VIEW);
            printIntent.setDataAndType(Uri.parse(url), "application/pdf");
            printIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            cordova.getActivity().startActivity(printIntent);

            if (callbackContext != null) {
                PluginResult result = new PluginResult(PluginResult.Status.OK);
                callbackContext.sendPluginResult(result);
            }
        } catch (Exception e) {
            if (callbackContext != null) {
                callbackContext.error("Failed to print document: " + e.getMessage());
            } else {
                Log.e(TAG, "Failed to print document: " + e.getMessage());
            }
        }
    }
}
