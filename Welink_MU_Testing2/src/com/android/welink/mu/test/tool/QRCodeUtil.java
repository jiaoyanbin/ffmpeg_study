package com.android.welink.mu.test.tool;

import java.util.HashMap;
import java.util.Hashtable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by jiao on 2016/12/14.
 */

public class QRCodeUtil {
	
	private static  int imageWH = 0;

	
	public static int realHeight = 0;
	public static int realWidth = 0;
    /**
     * 生成二维码Bitmap
     *
     * @param content   内容
     * @param widthPix  图片宽度
     * @param heightPix 图片高度
     * @param logoBm    二维码中心的Logo图标（可以为null）
     * @param filePath  用于存储二维码图片的文件路径
     * @return 生成二维码及保存文件是否成功
     */
    public static Bitmap createQRImage(String content, int widthPix, int heightPix, Context context) {
    	
    	widthPix = dip2px(context, widthPix);
    	heightPix = widthPix;
    	imageWH = widthPix;
        try {
            if (content == null || "".equals(content)) {
                return null;
            }

            //配置参数
            HashMap<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //容错级别
//            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            //设置空白边距的宽度
//            hints.put(EncodeHintType.MARGIN, 2); //default is 4

            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = null;

            bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);

            int[] pixels = new int[widthPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;
                    } else {
                        pixels[y * widthPix + x] = 0xbcd2ee;
                    }
                }
            }

            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);

            

            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            return bitmap; //&& bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath));
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }


    //解析二维码图片,返回结果封装在Result对象中
    public static com.google.zxing.Result  parseQRcodeBitmap(String bitmapPath){
        //解析转换类型UTF-8
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        //获取到待解析的图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        //如果我们把inJustDecodeBounds设为true，那么BitmapFactory.decodeFile(String path, Options opt)
        //并不会真的返回一个Bitmap给你，它仅仅会把它的宽，高取回来给你
        options.inJustDecodeBounds = true;
        //此时的bitmap是null，这段代码之后，options.outWidth 和 options.outHeight就是我们想要的宽和高了
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath,options);
        //我们现在想取出来的图片的边长（二维码图片是正方形的）设置为400像素
        /**
         options.outHeight = 400;
         options.outWidth = 400;
         options.inJustDecodeBounds = false;
         bitmap = BitmapFactory.decodeFile(bitmapPath, options);
         */
        //以上这种做法，虽然把bitmap限定到了我们要的大小，但是并没有节约内存，如果要节约内存，我们还需要使用inSimpleSize这个属性
        options.inSampleSize = options.outHeight / 400;
        if(options.inSampleSize <= 0){
            options.inSampleSize = 1; //防止其值小于或等于0
        }
        /**
         * 辅助节约内存设置
         *
         * options.inPreferredConfig = Bitmap.Config.ARGB_4444;    // 默认是Bitmap.Config.ARGB_8888
         * options.inPurgeable = true;
         * options.inInputShareable = true;
         */
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        //新建一个RGBLuminanceSource对象，将bitmap图片传给此对象
        LuminanceSource rgbLuminanceSource = new MyRGBLuminanceSource(bitmap);
        //将图片转换成二进制图片
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        //初始化解析对象
        QRCodeReader reader = new QRCodeReader();
        //开始解析
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (Exception e) {
            // TODO: handle exception
        }

        return result;
    }


    //解析二维码图片,返回结果封装在Result对象中
    public static com.google.zxing.Result  parseQRcodeBitmap(Bitmap bitmap){
        //解析转换类型UTF-8
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");

        LuminanceSource rgbLuminanceSource = new MyRGBLuminanceSource(bitmap);
        //将图片转换成二进制图片
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        //初始化解析对象
        QRCodeReader reader = new QRCodeReader();
        //开始解析
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (Exception e) {

        }

        return result;
    }
    
    /** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
    
    

    /**
     * @param bitmap
     * @param scale  二维码宽比手机的宽
     * @return 左上右下的二维码是否都识别成功
     */
    public static boolean discernBitmap(Context context,Bitmap bitmap) {

        boolean lt = false;
        boolean lb = false;
        boolean rt = false;
        boolean rb = false;
        int remain = 0;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        
       
        
        width = width > height ? width:height;
        
        if(realWidth!=0&&realWidth!=width){
        	remain = realWidth-width;
        }
        int widthBm = bitmap.getWidth();
        int heightBm = bitmap.getHeight();
        
        int qrCodeWidth = (imageWH)*widthBm/(realWidth == 0 ? width : realWidth);
        int remainSacle = remain*widthBm/(realWidth == 0 ? width : realWidth);
        Log.e("2222", "phone width = "+width+"  widthBm = "+widthBm+"  heightBm = "+heightBm+"  qrCodeWidth = "+qrCodeWidth+"  realWidth = "+realWidth);
        Paint paint = new Paint();

        Bitmap qrBitmapLT = Bitmap.createBitmap(qrCodeWidth, qrCodeWidth, Bitmap.Config.ARGB_8888);
        Canvas canvasLT = new Canvas(qrBitmapLT);//左上角二维码的canvas
        canvasLT.drawBitmap(bitmap, 0, 0, paint);
        canvasLT.save();
        canvasLT.restore();
        lt = parseBitmap(context,qrBitmapLT);
        canvasLT = null;
        qrBitmapLT = null;

        Bitmap qrBitmapRT = Bitmap.createBitmap(qrCodeWidth, qrCodeWidth, Bitmap.Config.ARGB_8888);
        Canvas canvasRT = new Canvas(qrBitmapRT);//右上角二维码的canvas
        canvasRT.drawBitmap(bitmap, qrCodeWidth - widthBm+remainSacle, 0, paint);
        canvasRT.save();
        canvasRT.restore();
        rt = parseBitmap(context,qrBitmapRT);
        canvasRT = null;
        qrBitmapRT = null;

        Bitmap qrBitmapLD = Bitmap.createBitmap(qrCodeWidth, qrCodeWidth, Bitmap.Config.ARGB_8888);
        Canvas canvasLD = new Canvas(qrBitmapLD);//左下角二维码的canvas
        canvasLD.drawBitmap(bitmap, 0, qrCodeWidth - heightBm, paint);
        canvasLD.save();
        canvasLD.restore();
        lb = parseBitmap(context,qrBitmapLD);
        canvasLD = null;
        qrBitmapLD = null;

        Bitmap qrBitmapRD = Bitmap.createBitmap(qrCodeWidth, qrCodeWidth, Bitmap.Config.ARGB_8888);
        Canvas canvasRD = new Canvas(qrBitmapRD);//右下角二维码的canvas
        canvasRD.drawBitmap(bitmap, qrCodeWidth - widthBm+remainSacle, qrCodeWidth - heightBm, paint);
        canvasRD.save();
        canvasRD.restore();
        rb = parseBitmap(context,qrBitmapRD);
        canvasRD = null;
        qrBitmapRD = null;
        
        
        Bitmap qrBitmapCT = Bitmap.createBitmap(qrCodeWidth, qrCodeWidth, Bitmap.Config.ARGB_8888);
        Canvas canvasCT = new Canvas(qrBitmapCT);//中间二维码的canvas
        canvasCT.drawBitmap(bitmap, (qrCodeWidth - widthBm+remainSacle)/2, (qrCodeWidth - heightBm)/2, paint);
        canvasCT.save();
        canvasCT.restore();
        rb = parseBitmap(context,qrBitmapCT);
        canvasCT = null;
        qrBitmapCT = null;
        i= 0;
        return (lt&&lb&&rt&&rb);

    }
    

    static int i = 0;
	//视频流
    
    /**
     * 解析二维码图片
     * @param qrBitmapLT
     * @return
     */
    private static boolean parseBitmap(Context context,Bitmap qrBitmapLT) {

        Result result = QRCodeUtil.parseQRcodeBitmap(qrBitmapLT);
        i++;
        if (result == null) {
            return false;
        }
     
        String text = result.toString();
        Log.e("2222","1111 di "+i+" ge er wei ma  text = "+text);
//        Toast.makeText(context, "识别成功"+i, Toast.LENGTH_SHORT).show();
        
        if (TextUtils.isEmpty(text)) {
            return false;
        }

        return true;

    }
}
