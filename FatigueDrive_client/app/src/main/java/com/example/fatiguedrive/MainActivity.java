package com.example.fatiguedrive;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    private void initView() {
        TextureView textureView = findViewById(R.id.texture_view);
        textureView.setRotation(90); // // 设置预览角度，并不改变获取到的原始数据方向
        int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
        if(numberOfCameras<1){
            Toast.makeText(this, "没有相机", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        textureView.setSurfaceTextureListener(this);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // 打开相机 0后置 1前置
        mCamera = Camera.open(1);
        int WIDTH = 200;
        int HEIGHT = 400;
        if (mCamera != null) {
            // 设置相机预览宽高，此处设置为TextureView宽高

            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizeList = params.getSupportedPreviewSizes();
            Camera.Size pictureS = MyCamPara.getInstance().getPreviewSize(sizeList, 300);
            params.setPreviewSize(pictureS.width, pictureS.height);
            mCamera.setParameters(params);
            // 设置自动对焦模式
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.setParameters(params);
            }
            try {
                // 绑定相机和预览的View
                mCamera.setPreviewTexture(surface);
                // 开始预览
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
            addCallBack();
        }

    }

    private void addCallBack() {
        if(mCamera!=null){
            System.out.println("helloWorld");
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    System.out.println(size);
                    try{
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        byte[] temp = rotateYUVDegree270AndMirror(image.getYuvData(),size.width,size.height);
                        YuvImage new_image = new YuvImage(temp,ImageFormat.NV21, size.height, size.width, null);
                        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        new_image.compressToJpeg(new Rect(0, 0, size.height, size.width), 60, byteArrayOutputStream);
                        new Thread(){
                            @Override
                            public  void run(){
                               response(byteArrayOutputStream);
                            }
                        }.start();
                        byteArrayOutputStream.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }



    private String utfToString(byte[] data,int size) {
        String str = null;
        try {
            str = new String(data, 0,size,"UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        return str;

    }

    private  void response(ByteArrayOutputStream byteArrayOutputStream ){
        try {
            //发送
            String host = "192.168.1.14";
            int port = 9999;
            Socket socket = new Socket(host,port);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

            int size = inputStream.available();
            StringBuilder s = new StringBuilder(String.valueOf(size));
            while(s.length()<10){
                s.append(" ");
            }
            byte[] bytes = s.toString().getBytes();
            outputStream.write(bytes);
            outputStream.flush();

            byte []buf = new byte[1024];
            int len;
            while((len = inputStream.read(buf)) > 0){
                outputStream.write(buf);
                outputStream.flush();
            }
            socket.shutdownOutput();



            InputStream result = socket.getInputStream();
            byte []temp = new byte[1024];
            int temp_size = result.read(temp);
            String response = utfToString(temp, temp_size);
            response.trim();
            System.out.println("LooK"+response);
            result.close();



            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] rotateYUVDegree270AndMirror(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate and mirror the Y luma
        int i = 0;
        int maxY = 0;
        for (int x = imageWidth - 1; x >= 0; x--) {
            maxY = imageWidth * (imageHeight - 1) + x * 2;
            for (int y = 0; y < imageHeight; y++) {
                yuv[i] = data[maxY - (y * imageWidth + x)];
                i++;
            }
        }
        // Rotate and mirror the U and V color components
        int uvSize = imageWidth * imageHeight;
        i = uvSize;
        int maxUV = 0;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            maxUV = imageWidth * (imageHeight / 2 - 1) + x * 2 + uvSize;
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[maxUV - 2 - (y * imageWidth + x - 1)];
                i++;
                yuv[i] = data[maxUV - (y * imageWidth + x)];
                i++;
            }
        }
        return yuv;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera!=null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        return false;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

}
