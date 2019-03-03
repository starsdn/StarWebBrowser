package com.star.starwebbrowser.tcp;

import android.content.Context;
import android.util.Log;

import com.star.starwebbrowser.save.SPUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpCleint implements Runnable {
    public String ip = "";
    public String msg = "";

    public static byte[] intToByte(int paramInt) {
        int i = paramInt;
        byte[] arrayOfByte = new byte[4];
        for (int j = 0;; j++) {
            if (j >= -1 + arrayOfByte.length)
                return arrayOfByte;
            arrayOfByte[j] = new Integer(i & 0xFF).byteValue();
            i >>= 8;
        }
    }

    public static String recvMessage(InputStream paramInputStream) {
        byte[] arrayOfByte1 = new byte[4];
        while (true) {
            int i;
            try {
                if (paramInputStream == null) {
                    Thread.sleep(200L);
                    continue;
                }
                try {
                    paramInputStream.read(arrayOfByte1, 0, 4);
                } catch (Exception ex) {
                    Thread.sleep(200L);
                    continue;
                }
                String strTemp = new String(arrayOfByte1).trim(); // 得到数据的长度
                i = Integer.parseInt(strTemp); // 得到数据的长度
                byte[] arrayOfByte2 = new byte[i];
                int j = 0;
                while (j < i) {
                    j += paramInputStream.read(arrayOfByte2, j, i - j);
                }
                return new String(arrayOfByte2, "GB2312");
            } catch (Exception localException) {
                return null;
            }
        }
    }


    public static String send(String paramString, Context paramContext)
            throws UnknownHostException, IOException, Exception {
        new TcpCleint();
        try {
            int iPort =8888;
            iPort = SPUtils.readInt(paramContext, "sdnvideoport");
            String strIp = SPUtils.readString(paramContext, "sdnvideoip");
            Socket localSocket = new Socket(InetAddress.getByName(strIp), iPort);
            localSocket.setSoTimeout(15000);
            InputStream localInputStream = localSocket.getInputStream();
            OutputStream localOutputStream = localSocket.getOutputStream();
            byte[] arrayOfByte = paramString.getBytes("GB2312");
            Object[] arrayOfObject = new Object[1];
            arrayOfObject[0] = Integer.valueOf(arrayOfByte.length);
            localOutputStream.write(String.format("%8d", arrayOfObject)
                    .getBytes("GB2312"));
            localOutputStream.write(arrayOfByte);
            localOutputStream.flush();
            String str = recvMessage(localInputStream);
            // Log.i("abc", str);
            try
            {
                localSocket.close();//关闭当前连接
            }
            catch(Exception ex){
                Log.i("aa", "------");
            }
            return str;
        } catch (Exception ex) {
            return "";
        }
    }

    public void run() {

    }
}
