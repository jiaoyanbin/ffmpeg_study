package com.android.welink.mu.test.tool;

import java.io.FileNotFoundException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.LuminanceSource;

/**
 * 
 * @author jiaoyb
 * 
 */
public final class MyRGBLuminanceSource extends LuminanceSource {
	private final byte[] luminances;

	public MyRGBLuminanceSource(String path) throws FileNotFoundException {
		this(loadBitmap(path));
	}

	public MyRGBLuminanceSource(Bitmap bitmap) {
		super(bitmap.getWidth(), bitmap.getHeight());
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		luminances = new byte[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				if (r == g && g == b) {
					luminances[offset + x] = (byte) r;
				} else {
					luminances[offset + x] = (byte) ((r + g + g + b) >> 2);
				}
			}
		}
	}

	@Override
	public byte[] getRow(int y, byte[] row) {
		if (y < 0 || y >= getHeight()) {
			throw new IllegalArgumentException(
					"Requested row is outside the image: " + y);
		}
		int width = getWidth();
		if (row == null || row.length < width) {
			row = new byte[width];
		}
		System.arraycopy(luminances, y * width, row, 0, width);
		return row;
	}

	@Override
	public byte[] getMatrix() {
		return luminances;
	}

	private static Bitmap loadBitmap(String path) throws FileNotFoundException {
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		if (bitmap == null) {
			throw new FileNotFoundException("Couldn't open " + path);
		}
		return bitmap;
	}
}