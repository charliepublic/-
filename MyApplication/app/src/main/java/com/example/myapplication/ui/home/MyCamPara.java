package com.example.myapplication.ui.home;

import android.hardware.Camera.Size;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class MyCamPara {
    private static final String tag = "size";
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static MyCamPara myCamPara = null;

    private MyCamPara() {

    }

    static MyCamPara getInstance() {
        if (myCamPara == null) {
            myCamPara = new MyCamPara();
            return myCamPara;
        } else {
            return myCamPara;
        }
    }

    Size getPreviewSize(List<Size> list, int th) {
        Collections.sort(list, sizeComparator);

        int i = 0;
        for (Size s : list) {
            if ((s.width > th) && equalRate(s, 1.33f)) {
                Log.i(tag, "最终设置预览尺寸:w = " + s.width + "h = " + s.height);
                break;
            }
            i++;
        }

        return list.get(i);
    }


    private boolean equalRate(@NotNull Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate) <= 0.2;
    }

    public class CameraSizeComparator implements Comparator<Size> {
        //按升序排列
        public int compare(@NotNull Size lhs, @NotNull Size rhs) {

            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }
}
