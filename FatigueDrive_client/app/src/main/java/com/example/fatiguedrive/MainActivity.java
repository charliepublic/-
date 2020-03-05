package com.example.fatiguedrive;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private ImageView ivPic;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    private void initView() {
        TextureView textureView = findViewById(R.id.texture_view);
        textureView.setRotation(90); // // 设置预览角度，并不改变获取到的原始数据方向(与Camera.setDisplayOrientation(0)相同)
        ivPic = findViewById(R.id.iv_pic);
        ivPic.setRotation(-90);
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
        if (mCamera != null) {
            // 设置相机预览宽高，此处设置为TextureView宽高
            Camera.Parameters params = mCamera.getParameters();
//            params.setPreviewSize(width, height);
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
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    try{
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                        Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                        ivPic.setImageBitmap(bmp);
                        //传输压缩后的东西
                        new Thread(){
                            @Override
                            public  void run(){
                                try {
                                    //发送
                                    String host = "127.0.0.1";
                                    int port = 9999;
                                    DatagramSocket socket = new DatagramSocket();
                                    ByteArrayInputStream in = new ByteArrayInputStream(stream.toByteArray());
                                    byte[] data = new byte[1024];
                                    while (in.read(data) != -1){
                                        DatagramPacket packet = new DatagramPacket(
                                                data,data.length, InetAddress.getByName(host),port);
                                        socket.send(packet);
                                    }
                                    //接收服务器反馈数据
                                    byte[] back_buf = new byte[1024];
                                    DatagramPacket backPacket = new DatagramPacket(back_buf, back_buf.length);
                                    socket.receive(backPacket);  //接收返回数据
                                    String backMsg = new String(back_buf, 0, backPacket.getLength());
                                    System.out.println("服务器返回的数据为:" + backMsg);
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        stream.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
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
