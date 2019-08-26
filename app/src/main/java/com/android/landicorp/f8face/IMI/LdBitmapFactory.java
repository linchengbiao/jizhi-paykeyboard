package com.android.landicorp.f8face.IMI;

import android.graphics.Bitmap;

/**
 * Created by admin on 2019/8/16.
 */

public class LdBitmapFactory {
    public static Bitmap createLdBitmap(byte[] data,int width,int height){

        int[] colors = convertByteToColor(data);
        if (colors==null||colors.length==0){
            return null;
        }
        colorconvertRGB_IYUV_I420(colors,width,height);
        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(colors,0,width,width,height,Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    public static byte[] createYUVData(byte[] data,int width,int height){
        int[] colors = convertByteToColor(data);
        if (colors==null||colors.length==0){
            return null;
        }
        return colorconvertRGB_IYUV_I420(colors,width,height);
    }

    /*
* 将RGB数组转化为像素数组
*/
    private static int[] convertByteToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        // 理论上data的长度应该是3的倍数，这里做个兼容
        int arg = 0;
        if (size % 3 != 0) {
            arg = 1;
        }

        int[] color = new int[size / 3 + arg];
        int red, green, blue;


        if (arg == 0) { //  正好是3的倍数
            for (int i = 0; i < color.length; ++i) {
                color[i] = (data[i * 3] << 16 & 0x00FF0000) |
                        (data[i * 3 + 1] << 8 & 0x0000FF00) |
                        (data[i * 3 + 2] & 0x000000FF) |
                        0xFF000000;
            }
        } else { // 不是3的倍数
            for (int i = 0; i < color.length - 1; ++i) {
                color[i] = (data[i * 3] << 16 & 0x00FF0000) |
                        (data[i * 3 + 1] << 8 & 0x0000FF00) |
                        (data[i * 3 + 2] & 0x000000FF) |
                        0xFF000000;
            }

            color[color.length - 1] = 0xFF000000; // 最后一个像素用黑色填充
        }

        return color;
    }

    public static byte[] colorconvertRGB_IYUV_I420(int[] aRGB, int width, int height) {
        final int frameSize = width * height;
        final int chromasize = frameSize / 4;

        int yIndex = 0;
        int uIndex = frameSize;
        int vIndex = frameSize + chromasize;
        byte[] yuv = new byte[width * height * 3 / 2];

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
//                if (index>=aRGB.length){
//                    return yuv;
//                }
                //a = (aRGB[index] & 0xff000000) >> 24; //not using it right now
                R = (aRGB[index] & 0xff0000) >> 16;
                G = (aRGB[index] & 0xff00) >> 8;
                B = (aRGB[index] & 0xff) >> 0;

                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                yuv[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));

                if (j % 2 == 0 && index % 2 == 0) {
                    yuv[vIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                    yuv[uIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                }
                index++;
            }
        }
        return yuv;
    }
}
