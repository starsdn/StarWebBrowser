package com.star.starwebbrowser.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ToastEx
{
    public static int LENGTH_LONG = 1;
    public static int LENGTH_SHORT = 0;
    private static Toast toast = null;

    @SuppressLint("WrongConstant")
    public static void ImageToast(Context paramContext, int paramInt1, CharSequence paramCharSequence, int paramInt2)
    {
        toast = Toast.makeText(paramContext, paramCharSequence,2);
        toast.setGravity(17, 0, 0);
        View localView = toast.getView();
        ImageView localImageView = new ImageView(paramContext);
        localImageView.setImageResource(paramInt1);
        LinearLayout localLinearLayout = new LinearLayout(paramContext);
        localLinearLayout.addView(localImageView);
        localLinearLayout.addView(localView);
        toast.setView(localLinearLayout);
        toast.show();
    }

    public static void TextToast(Context paramContext, CharSequence paramCharSequence, int paramInt)
    {
        toast = Toast.makeText(paramContext, paramCharSequence, paramInt);
        toast.setGravity(17, 0, 0);
        toast.show();
    }
}