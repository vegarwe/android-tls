package no.raiom.tls;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import java.io.IOException;

public class DropboxAppender {
    private String            filename;
    private DbxFile           sample_file;
    private AppConfig app;

    public DropboxAppender(Context context, String filename) {
        this.filename = filename;
        app = AppConfig.getInstance(context);
        sample_file = null;
    }

    public static boolean hasLinkedAccount(Context context) {
        AppConfig app = AppConfig.getInstance(context);
        DbxAccountManager dbxAcctMgr = DbxAccountManager.getInstance(app.context, app.APP_KEY, app.APP_SECRET);
        return dbxAcctMgr.hasLinkedAccount();
    }

    public static void startLink(Activity activity, Context context, int callbackRequestCode) {
        // TODO: Cast activity to Context rather than ask for parmater?
        AppConfig app = AppConfig.getInstance(context);
        DbxAccountManager dbxAcctMgr = DbxAccountManager.getInstance(app.context, app.APP_KEY, app.APP_SECRET);
        dbxAcctMgr.startLink(activity, callbackRequestCode);
    }

    public void appendString(String sample) {
        openFileIfNeeded();
        try {
            sample_file.appendString(sample);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (sample_file != null) {
            sample_file.close();
            sample_file = null;
        }
    }

    private void openFileIfNeeded() {
        if (sample_file != null) return;

        try {
            DbxAccountManager dbxAcctMgr = DbxAccountManager.getInstance(app.context, app.APP_KEY, app.APP_SECRET);
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(dbxAcctMgr.getLinkedAccount());
            if (dbxFs.exists(new DbxPath(filename))) {
                Log.i("Fisken", "dbxFs.open: " + filename);
                sample_file = dbxFs.open(new DbxPath(filename));
            } else {
                Log.i("Fisken", "dbxFs.create: " + filename);
                sample_file = dbxFs.create(new DbxPath(filename));
            }
        } catch (DbxException.Unauthorized e) {
            e.printStackTrace();
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }
}
