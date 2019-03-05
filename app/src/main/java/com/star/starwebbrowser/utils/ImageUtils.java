package com.star.starwebbrowser.utils;

import java.io.ByteArrayOutputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class ImageUtils {

    /**
     *  将字符串转换成Bitmap类型
     * @param string
     * @return
     */
    public static Bitmap stringtoBitmap(String string) {
        // 将字符串转换成Bitmap类型
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                    bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * 将Bitmap转换成字符串
     * @param bitmap
     * @return
     */
    public static String bitmaptoString(Bitmap bitmap) {
        // 将Bitmap转换成字符串
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, bStream);
        byte[] bytes = bStream.toByteArray();
        string = Base64.encodeToString(bytes, Base64.DEFAULT);
        return string;
    }

    /**
     * 将Bitmap对象转换为byte数组
     * @param bm bitmap对象
     * @return byte数组
     */
    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 90, baos);
        return baos.toByteArray();
    }

    /**
     * 将图片转换为base64字符串
     * @param bm 图片对象
     * @return base64字符串
     */
    public static String getBase64Str(Bitmap bm){
        byte[] data = bitmap2Bytes(bm);
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    /**
     * 获得图片高度
     * @param res 资源对象
     * @param resId 资源id
     * @return 包含2个元素的数组，下标0是宽度，下标1是高度
     */
    public static int[] getBitmapDimention(Resources res,int resId){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        int[] dimen = {options.outWidth, options.outHeight};
        return dimen;
    }

    /**
     * 缩放图片
     * @param bitmap 原始图片
     * @param w 缩放以后的宽度
     * @param h 缩放以后的高度
     * @return 缩放以后的图片
     */
    public static Bitmap scaleBitmap(Bitmap bitmap,int w,int h){
        if(bitmap == null){
            return null;
        }
        int bW = bitmap.getWidth();
        int bH = bitmap.getHeight();
        if(bW == w && bH == h){//如果图片大小一致，则不缩放
            return bitmap;
        }
        //计算缩放比例
        float scaleWidth = (float)w / bW; //水平缩放比例
        float scaleHeight = (float)h / bH; //垂直缩放比例
        Matrix  m = new Matrix();
        m.postScale(scaleWidth, scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bW, bH, m, true);
        return newBitmap;
    }

}
