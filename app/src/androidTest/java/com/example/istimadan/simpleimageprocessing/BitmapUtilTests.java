package com.example.istimadan.simpleimageprocessing;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class BitmapUtilTests {
    @Test
    public void bitmapMaintainsAspectRatio(){
        //scale a 1440p 21:9 image to 1080 21:9
        Bitmap input = Bitmap.createBitmap(3440, 1440, Bitmap.Config.ARGB_8888);

        Bitmap result = BitmapUtils.scaleBitmapDownAndKeepAspectRatio(input, 2580, 1200, true);

        assertEquals(2580, result.getWidth());
        assertEquals(1080, result.getHeight());
        assert input.isRecycled();
    }
}
