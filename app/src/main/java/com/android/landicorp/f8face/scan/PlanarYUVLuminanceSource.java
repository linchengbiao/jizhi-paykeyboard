/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.landicorp.f8face.scan;

import android.graphics.Bitmap;

/**
 * 处理预览YUV数据
 * @author lizm
 *
 */
public final class PlanarYUVLuminanceSource {


	public PlanarYUVLuminanceSource() {

	}

	/**
	 * 获取预览时扫描区域的图片数据
	 * 
	 * @param yuvData
	 *            预览原始图像
	 * @param previewWidth
	 *            预览图片大小
	 * @param previewHeight
	 *            预览图片大小
	 * @param left
	 *            扫码区域left
	 * @param top
	 *            扫码区域top
	 * @param width 扫码区域width
	 * @param height  扫码区域高度
	 * @return 经过裁剪之后的数据
	 */
	public byte[] renderCroppedGreyscaleBitmapByte(byte[] yuvData,
			int previewWidth, int previewHeight, int left, int top, int width,
			int height) {
		int outWidth,outHeight;
		if (previewWidth < width){
			outWidth = previewWidth;
		}else{
			outWidth = width;
		}
		if (previewHeight < height){
			outHeight = previewHeight;
		}else{
			outHeight = height;
		}
		byte[] pixels = new byte[outWidth * outHeight];
		byte[] yuv = yuvData;
		int inputOffset;
		inputOffset = (top) * previewWidth + left;
		for (int y = 0; y < outHeight; y++) {
			int outputOffset = y * outWidth;
			for (int x = 0; x < outWidth; x++) {
				pixels[outputOffset + x] = yuv[inputOffset + x];
			}
			inputOffset += previewWidth;
		}
		
//		byte[] pixels = new byte[width * height];
//		byte[] yuv = yuvData;
//		int inputOffset;
//		inputOffset = (top) * previewWidth + left;
//
//		for (int y = 0; y < height; y++) {
//			int outputOffset = y * width;
//			for (int x = 0; x < width; x++) {
//				pixels[outputOffset + x] = yuv[inputOffset + x];
//			}
//			inputOffset += previewWidth;
//		}
		return pixels;
	}

	/**
	 * 裁剪扫码区域图片(横屏)
	 * @param yuvData
	 * @param previewWidth
	 * @param previewHeight
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 * @return
	 */
	private int[] renderCroppedGreyscaleBitmap(byte[] yuvData,
			int previewWidth, int previewHeight, int left, int top, int width,
			int height) {
		
		int outWidth,outHeight;
		if (previewWidth < width){
			outWidth = previewWidth;
		}else{
			outWidth = width;
		}
		if (previewHeight < height){
			outHeight = previewHeight;
		}else{
			outHeight = height;
		}
		
		int[] pixels = new int[outWidth * outHeight];
		byte[] yuv = yuvData;
		int inputOffset = top * previewWidth + left;
		for (int y = 0; y < outHeight; y++) {
			int outputOffset = y * outWidth;
			for (int x = 0; x < outWidth; x++) {
				int grey = yuv[inputOffset + x] & 0xff;
				pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
			}
			inputOffset += previewWidth;
		}
		
//		int[] pixels = new int[width * height];
//		byte[] yuv = yuvData;
//		int inputOffset = top * previewWidth + left;
//
//		for (int y = 0; y < height; y++) {
//			int outputOffset = y * width;
//			for (int x = 0; x < width; x++) {
//				int grey = yuv[inputOffset + x] & 0xff;
//				pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
//			}
//			inputOffset += previewWidth;
//		}
		return pixels;
	}
	
	/**
	 * 获得经过裁剪的扫码区域的Bitmap位图
	 * @param yuvCropData  已经裁剪号的原始YUV数据
	 * @param width 裁剪数据宽度
	 * @param height 裁剪数据高度
	 * @return 扫码区域的Bitmap
	 */
	public Bitmap toBitmapFromCropper(byte[] yuvCropData,int previewWidth, int previewHeight, int width,int height){
		
		int outWidth,outHeight;
		if (previewWidth < width){
			outWidth = previewWidth;
		}else{
			outWidth = width;
		}
		if (previewHeight < height){
			outHeight = previewHeight;
		}else{
			outHeight = height;
		}
		
		int[] pixels = new int[outWidth * outHeight];
		byte[] yuv = yuvCropData;
		int inputOffset = 0;
		for (int y = 0; y < outHeight; y++) {
			int outputOffset = y * outWidth;
			for (int x = 0; x < outWidth; x++) {
				int grey = yuv[inputOffset + x] & 0xff;
				pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
			}
			inputOffset += outWidth;
		}
		
		Bitmap bitmap = Bitmap.createBitmap(outWidth, outHeight,Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, outWidth, 0, 0, outWidth, outHeight);
		return bitmap;
	}

	
	/**
	 * 预览图像转化为bitmap
	 * @param yuvData
	 * @param previewWidth
	 * @param previewHeight
	 * @param left
	 * @param top
	 * @param scanWidth
	 * @param scanHeight
	 * @return
	 */
	public Bitmap toBitmap(byte[] yuvData, int previewWidth,
			int previewHeight, int left, int top, int scanWidth, int scanHeight) {
		int[] pixels = renderCroppedGreyscaleBitmap(yuvData, previewWidth,
				previewHeight, left, top, scanWidth, scanHeight);

		Bitmap bitmap = Bitmap.createBitmap(scanWidth, scanHeight,Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, scanWidth, 0, 0, scanWidth, scanHeight);
		return bitmap;
	}
}
