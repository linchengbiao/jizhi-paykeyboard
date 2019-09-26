package com.android.landicorp.f8face.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2019/6/6.
 */

public class BitmapUtil {
    /**
     * 生产二维码图像
     * @param content 内容
     * @param widthPix 框
     * @param heightPix 高
     * @param logoBm 二维码logo
     * @return
     */
    public static Bitmap createQRImage(String content, int widthPix, int heightPix,Bitmap logoBm) {
//        try {
//            //配置参数
//            Map<EncodeHintType, Object> hints = new HashMap<>();
//            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//            //容错级别
//            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
//            //设置空白边距的宽度
//            hints.put(EncodeHintType.MARGIN, 1); //default is 4
//
//            // 图像数据转换，使用了矩阵转换
//            BitMatrix bitMatrix = null;
//            try {
//                bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix,
//                        heightPix, hints);
//            } catch (WriterException e) {
//                e.printStackTrace();
//            }
//            int[] pixels = new int[widthPix * heightPix];
//            // 下面这里按照二维码的算法，逐个生成二维码的图片，
//            // 两个for循环是图片横列扫描的结果
//            for (int y = 0; y < heightPix; y++) {
//                for (int x = 0; x < widthPix; x++) {
//                    if (bitMatrix.get(x, y)) {
//                        pixels[y * widthPix + x] = 0xff000000;
//                    } else {
//                        pixels[y * widthPix + x] = 0xffffffff;
//                    }
//                }
//            }
//
//            // 生成二维码图片的格式，使用ARGB_8888
//            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
//            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);
//
//            if (logoBm != null) {
//                bitmap = addLogo(bitmap, logoBm);
//            }
//
//            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，
//            // 内存消耗巨大！
//            return bitmap;
////            return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

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

    public static List<Uri> convertResIdToUrl(Context mContext, Integer[] list){
        Resources r =mContext.getResources();
        List<Uri> uris = new ArrayList<>();

        if (list!=null){
            for (int i=0;i<list.length;i++){
                Uri uri =  Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+mContext.getPackageName()+"/"+list[i]);
                uris.add(uri);
            }
        }
        return uris;
    }

    //绝对路径转uri
    public static Uri getImageContentUri(Context context,String filePath) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            File imageFile = new File(filePath);
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}
