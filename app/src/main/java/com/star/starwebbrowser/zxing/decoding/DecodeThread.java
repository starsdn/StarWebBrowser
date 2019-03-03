package com.star.starwebbrowser.zxing.decoding;

import android.os.Handler;
import android.os.Looper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;
import com.star.starwebbrowser.activity.CaptureActivity;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

final class DecodeThread extends Thread {
	public static final String BARCODE_BITMAP = "barcode_bitmap";
	private final CaptureActivity activity;
	private Handler handler;
	private final CountDownLatch handlerInitLatch;
	private final Hashtable<DecodeHintType, Object> hints;

	DecodeThread(CaptureActivity paramCaptureActivity,
			Vector<BarcodeFormat> paramVector, String paramString,
			ResultPointCallback paramResultPointCallback) {
		this.activity = paramCaptureActivity;
		this.handlerInitLatch = new CountDownLatch(1);
		this.hints = new Hashtable(3);
		if ((paramVector == null) || (paramVector.isEmpty())) {
			paramVector = new Vector();
			paramVector.addAll(DecodeFormatManager.ONE_D_FORMATS);
			paramVector.addAll(DecodeFormatManager.QR_CODE_FORMATS);
			paramVector.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
		}
		this.hints.put(DecodeHintType.POSSIBLE_FORMATS, paramVector);
		if (paramString != null)
			this.hints.put(DecodeHintType.CHARACTER_SET, paramString);
		this.hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK,
				paramResultPointCallback);
	}

	Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		return handler;
	}

	public void run() {
		Looper.prepare();
		handler = new DecodeHandler(activity, hints);
		handlerInitLatch.countDown();
		Looper.loop();
	}
}
