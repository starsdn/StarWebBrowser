package com.star.starwebbrowser.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import com.star.starwebbrowser.R;

public class ProgressDialog
{
    static Dialog mDialog;

    public static void Hide()
    {
        if (mDialog != null)
            mDialog.hide();
    }

    public static void Show(Context paramContext)
    {
        mDialog = new AlertDialog.Builder(paramContext).create();
        mDialog.setTitle("数据传输中");
        mDialog.show();
        mDialog.setContentView(R.layout.loading_process_dialog_color);
    }
}