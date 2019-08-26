package com.android.landicorp.f8face.IMI;

import com.hjimi.api.iminect.ImiDevice;
import com.hjimi.api.iminect.ImiFrameMode;
import com.hjimi.api.iminect.ImiFrameType;
import com.hjimi.api.iminect.Utils;

import java.nio.ByteBuffer;


public class SimpleViewer extends Thread {

    private boolean mShouldRun = false;

    private ImiFrameType mFrameType;
    private GLPanel mGLPanel;
    private DecodePanel mDecodePanel;
    private ImiDevice mDevice;
    private ImiFrameMode mCurrentMode;

    public OnGetFrameResult getOnGetFrameResult() {
        return onGetFrameResult;
    }

    public void setOnGetFrameResult(OnGetFrameResult onGetFrameResult) {
        this.onGetFrameResult = onGetFrameResult;
    }

    private OnGetFrameResult onGetFrameResult;

    public interface OnGetFrameResult{
        public void onFrame(ByteBuffer frame);
        public void getFrameFail();
    }

    public SimpleViewer(ImiDevice device, ImiFrameType frameType) {
        mDevice = device;
        mFrameType = frameType;

        mCurrentMode = mDevice.getCurrentFrameMode(ImiDevice.ImiStreamType.COLOR);
    }

    public void setGLPanel(GLPanel GLPanel) {
        this.mGLPanel = GLPanel;
    }

    public void setDecodePanel(DecodePanel decodePanel) {
        this.mDecodePanel = decodePanel;
    }

    @Override
    public void run() {
        super.run();

        //start read frame.
        while (mShouldRun) {
            ImiDevice.ImiFrame nextFrame = mDevice.readNextFrame(ImiDevice.ImiStreamType.COLOR, 25);
            //frame maybe null, if null, continue.
            if(nextFrame == null){
                continue;
            }
//            mDevice.saveFrameToPicture(nextFrame);
            if (onGetFrameResult!=null){
                onGetFrameResult.onFrame(nextFrame.getData());
            }
            //draw color.
            drawColor(nextFrame);
        }
    }

    /**
     *
     *
     *  File f;
     int c = 0;c++;
     if(c==300 && f == null){
     f = new File("/sdcard/test.jpg");
     try {
     f.createNewFile();
     } catch (IOException e) {
     e.printStackTrace();
     }
     byte[] data = new byte[460800];
     frameData.get(data);
     YuvImage yuv = new YuvImage(data, ImageFormat.NV21, 640, 480, null);
     FileOutputStream out = null;
     try {
     out = new FileOutputStream(f);
     } catch (FileNotFoundException e) {
     e.printStackTrace();
     }
     yuv.compressToJpeg(new Rect(0, 0, 640, 480), 100, out);//相片质量
     }

     */

    private void drawColor(ImiDevice.ImiFrame nextFrame) {
        ByteBuffer frameData = nextFrame.getData();
        int width = nextFrame.getWidth();
        int height = nextFrame.getHeight();
        //draw color image.
        switch (mCurrentMode.getFormat())
        {
            case IMI_PIXEL_FORMAT_IMAGE_H264:
                if(mDecodePanel != null){
                    mDecodePanel.paint(frameData, nextFrame.getTimeStamp());
                }
                break;
            case IMI_PIXEL_FORMAT_IMAGE_YUV420SP:
                frameData = Utils.yuv420sp2RGB(nextFrame);
                if(mGLPanel != null){
                    mGLPanel.paint(null, frameData, width, height);
                }
                break;
            case IMI_PIXEL_FORMAT_IMAGE_RGB24:
                if(mGLPanel != null){
                    mGLPanel.paint(null, frameData, width, height);
                }
                break;
            default:
                break;
        }
    }

    public void onPause(){
        if(mGLPanel != null){
            mGLPanel.onPause();
        }
    }

    public void onResume(){
        if(mGLPanel != null){
            mGLPanel.onResume();
        }
    }

    public void onStart(){
        if(!mShouldRun){
            mShouldRun = true;

            //start read thread
            this.start();
        }
    }
    public void onRun(){
        mShouldRun = false;
    }
    public void onStop(){
        mShouldRun = false;
    }
    public void onDestroy(){
        mShouldRun = false;
    }
}
