package com.example.myapplication.ui.home;

import androidx.fragment.app.Fragment;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
//import androidx.lifecycle.ViewModelProviders;


import com.example.myapplication.GlobalData;
import com.example.myapplication.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HomeFragment extends Fragment implements TextureView.SurfaceTextureListener {

    private Camera mCamera;
    private TextureView alert;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
//                alert.setVisibility(View.VISIBLE);
                alert_alaram();
            }else {
//                alert.setVisibility(View.INVISIBLE);
            }
        }
    };

    @SuppressLint("CutPasteId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        HomeViewModel homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        TextureView textureView = root.findViewById(R.id.texture_view);
        alert = root.findViewById(R.id.texture_view);
        textureView.setRotation(90);
        textureView.setSurfaceTextureListener(this);
        return root;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // 打开相机 0后置 1前置
        mCamera = Camera.open(1);
        if (mCamera != null) {
            // 设置相机预览宽高获取最低要求
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> size_List = params.getSupportedPreviewSizes();
            Camera.Size picture = MyCamPara.getInstance().getPreviewSize(size_List, 200);
            params.setPreviewSize(picture.width, picture.height);
            mCamera.setParameters(params);
            // 设置自动对焦模式
            List<String> focus_Modes = params.getSupportedFocusModes();
            if (focus_Modes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
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
        if (mCamera != null) {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    int width;
                    int height = size.height;
                    width = size.width;
                    //初始化宽高
                    try {
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, width, height, null);
                        byte[] temp = rotateYUVDegree270AndMirror(image.getYuvData(), width, height);
                        YuvImage new_image = new YuvImage(temp, ImageFormat.NV21, height, width, null);
                        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        new_image.compressToJpeg(new Rect(0, 0, height, width), 60, byteArrayOutputStream);
                        new Thread() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void run() {
                                response(byteArrayOutputStream);
                            }
                        }.start();
                        byteArrayOutputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void response(final ByteArrayOutputStream byteArrayOutputStream) {
        try {
            //发送
            String host = "192.168.1.14";
            int port = 9999;
            final Socket socket = new Socket(host, port);
            final OutputStream outputStream = socket.getOutputStream();

            //upload
            InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            int size = 0;
            GlobalData globalData = new GlobalData();
            String phoneNumber = globalData.getPhoneNumber();
            try {

                //头文件处理

                //电话唯一识别码
                StringBuilder number_string = new StringBuilder(phoneNumber);
                while (number_string.length() < 15) {
                    number_string.append(" ");
                }
                byte[] number_string_bytes = number_string.toString().getBytes();
                outputStream.write(number_string_bytes);
                outputStream.flush();

                //flag传递
                String flag_str = "0";
                byte[] flag_string_bytes = flag_str.getBytes();
                outputStream.write(flag_string_bytes);
                outputStream.flush();

                //文件大小
                size = inputStream.available();
                StringBuilder s = new StringBuilder(String.valueOf(size));
                while (s.length() < 10) {
                    s.append(" ");
                }
                byte[] bytes = s.toString().getBytes();
                outputStream.write(bytes);
                outputStream.flush();

                //图片传输
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf);
                    outputStream.flush();
                }
                int a = 0;
                System.out.println(a);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //response
            InputStream result;
            try {
                result = socket.getInputStream();
                byte[] temp = new byte[1024];
                int temp_size = result.read(temp);
                String response = utfToString(temp, temp_size);
                response.trim();
                System.out.println("LooK" + response);
                if (response.equals("true")) {
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
                result.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void alert_alaram() {
        MediaPlayer mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.alert);
        mMediaPlayer.start();
        int time;
        time = 2;
        while (0 < time) {
            time--;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mMediaPlayer.stop();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String utfToString(byte[] data, int size) {
        return new String(data, 0, size, StandardCharsets.UTF_8);

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
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

}

