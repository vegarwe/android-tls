package no.raiom.tls;

import android.content.Context;
import android.util.Log;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import java.io.IOException;

public class DropboxAppender {
    private final String            filename;
    private       DbxAccountManager dbxAcctMgr;
    private       DbxFile           sample_file;

    public DropboxAppender(Context context, String appKey, String appSecret, String filename) {
        this.filename = filename;
        dbxAcctMgr = DbxAccountManager.getInstance(context, appKey, appSecret);
        sample_file = null;
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
