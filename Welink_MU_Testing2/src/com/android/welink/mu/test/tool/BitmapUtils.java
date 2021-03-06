package com.android.welink.mu.test.tool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtils {
	
	public static int calculateInSampleSize(BitmapFactory.Options options,  
            int reqWidth, int reqHeight) {  
        // Raw height and width of image  
        final int height = options.outHeight;  
        final int width = options.outWidth;  
        int inSampleSize = 1;  
  
        if (height > reqHeight || width > reqWidth) {  
            if (width > height) {  
                inSampleSize = Math.round((float) height / (float) reqHeight);  
            } else {  
                inSampleSize = Math.round((float) width / (float) reqWidth);  
            }  
        }
        return inSampleSize;  
    }  

        
        
        
        public static Bitmap decodeSampledBitmapFromFile(String filename,  
                int reqWidth, int reqHeight) {  
      
            // First decode with inJustDecodeBounds=true to check dimensions  
            final BitmapFactory.Options options = new BitmapFactory.Options();  
            options.inJustDecodeBounds = true;  
            BitmapFactory.decodeFile(filename, options);  
      
            // Calculate inSampleSize  
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);  
      
            // Decode bitmap with inSampleSize set  
            options.inJustDecodeBounds = false;  
            return BitmapFactory.decodeFile(filename, options);  
        } 
}
