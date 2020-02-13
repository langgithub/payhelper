package com.lang.payhelper.feature.migrate.db;

import android.content.Context;

import com.lang.payhelper.feature.migrate.ITransition;
import com.lang.payhelper.ui.record.CodeRecordRestoreManager;

import java.io.File;

public class DBTransition implements ITransition {

    private Context mContext;

    public DBTransition(Context context) {
        mContext = context;
    }

    @Override
    public boolean shouldTransit() {
        File[] recordFiles = CodeRecordRestoreManager.getRecordFiles();
        return recordFiles != null && recordFiles.length > 0;
    }

    @Override
    public boolean doTransition() {
        return CodeRecordRestoreManager.importToDatabase(mContext);
    }
}
