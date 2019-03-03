package com.star.starwebbrowser.activity;

import com.star.starwebbrowser.utils.PhotoCollect;

import android.app.Application;
import android.graphics.Bitmap;


/**
 * 项目应用类
 *
 * @author shandinan
 */
public class FTApplication extends Application {
	/** 软件版本 */
	public static final String VERSION = "1.9";
	public Bitmap currentPhoto;
	public  PhotoCollect photoCollect= new PhotoCollect();
	//public User user = null;
	public FTApplication(){}
}