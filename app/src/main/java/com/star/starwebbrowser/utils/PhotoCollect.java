package com.star.starwebbrowser.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import android.graphics.Bitmap;
import android.os.Environment;

public class PhotoCollect {

	public static String path = Environment.getExternalStorageDirectory()
			+ "/vecimage/";// 定义图片的路径
	public Map<String, String> allFiles = new HashMap<String, String>();
	public Map<String, String> allState = new HashMap<String, String>();

	/**
	 * 添加图片的方法
	 */
	public void AddPhotoItem(String paramString) {
		this.allFiles.put(paramString, "");
	}

	/**
	 * 删除图片的方法
	 */
	public void clearAll() {
		this.allFiles.clear();
	}

	/**
	 * 获取图片
	 */
	public void execCommand(String paramString) {
		System.out.println("execCommand........");
		File localFile = new File(path);
		if (!localFile.exists())
			localFile.mkdir();
		String strTemp = "rm " + path + paramString + "/";

		@SuppressWarnings("unused")
		Process proc = null;
		try {
			Runtime runtime = Runtime.getRuntime();
			proc = runtime.exec(strTemp);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public boolean CheckCaches() {
		try {
			File localFile = new File(path);
			if (!localFile.exists())
				localFile.mkdir();
			if (localFile.isDirectory()) {
				File[] files = localFile.listFiles();
				if (files.length > 0)
					return true;
			}
			return false;
		} catch (Exception ex) {
		}
		return false;
	}

	public boolean DeleteDirectory(String paramString) {
		try {
			File localFile = new File(path);
			if (!localFile.exists())
				localFile.mkdir();
			String strTemp = path + paramString + "/";
			File fl = new File(strTemp);
			if (fl.isDirectory()) {
				File[] files = fl.listFiles();
				for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
					files[i].delete();
				}
				fl.delete();
			}
			return true;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return false;
	}

	public boolean deleteFile(String paramString) {
		try {
			File localFile = new File(path);
			if (!localFile.exists())
				localFile.mkdir();
			String strTemp = path + paramString + "/";
			new File(strTemp).delete();
			return true;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return false;
	}

	/**
	 *
	 * @return
	 */
	public boolean getAllPhotoUpload() {
		Iterator<String> localIterator = allState.keySet().iterator();
		while (localIterator.hasNext()) {
			String str1 = localIterator.next().toString();
			if (allState.get(str1) == null || allState.get(str1).equals("0")) {
				return false;
			}
		}
		return true;
	}

	/*
	 * 检测所有拍照项是否已经拍照
	 */
	public boolean getAllPhotoCollectedState() {
		Iterator<String> localIterator = allFiles.keySet().iterator();
		while (localIterator.hasNext()) {
			String str1 = localIterator.next().toString();
			if (allFiles.get(str1) == null || allFiles.get(str1).equals("")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 通过文件夹获取图片
	 */
	public byte[] getPhotoByField(String paramString) {
		// ((byte[])null);
		try {
			File localFile = new File((String) this.allFiles.get(paramString));
			@SuppressWarnings("resource")
			BufferedInputStream localBufferedInputStream = new BufferedInputStream(
					new FileInputStream(localFile));
			byte[] arrayOfByte = new byte[(int) localFile.length()];
			int i = 0;
			int j = (int) localFile.length();
			while (true) {
				int k;
				if (i < j) {
					k = localBufferedInputStream.read(arrayOfByte, i, j - i);
					if (k >= 0)
						;
				} else {
					return arrayOfByte;
				}
				i += k;
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取图片路径
	 */
	public String getPhotoPath(String paramString1, String paramString2) {
		return path + paramString1 + "/" + paramString2 + ".jpg";
	}

	/**
	 * 保存图片
	 */
	public boolean saveFile(Bitmap paramBitmap, String paramString1,String paramString2) {
		try {
			File localFile1 = new File(path);
			if (!localFile1.exists())
				localFile1.mkdir();
			File localFile2 = new File(path + paramString1 + "/");
			if (!localFile2.exists())
				localFile2.mkdir();
			BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(
					new FileOutputStream(new File(path + paramString1 + "/"
							+ paramString2 + ".jpg")));
			paramBitmap.compress(Bitmap.CompressFormat.JPEG, 80,
					localBufferedOutputStream);
			localBufferedOutputStream.flush();
			localBufferedOutputStream.close();
			this.allFiles.put(paramString2, path + paramString1 + "/"
					+ paramString2 + ".jpg");
			return true;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return false;
	}
}
