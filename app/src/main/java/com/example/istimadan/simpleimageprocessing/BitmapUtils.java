package com.example.istimadan.simpleimageprocessing;

import android.graphics.Bitmap;

public class BitmapUtils {

    public static Bitmap scaleBitmapDownAndKeepAspectRatio(Bitmap bmp, int maxWidth, int maxHeight, boolean disposeOriginalBitmap){
        if(bmp == null || bmp.isRecycled())
            return bmp;

        float ratio = (float)bmp.getWidth() / bmp.getHeight();
        float maxRatio = (float)maxWidth / maxHeight;

        int scaledWidth, scaledHeight;
        if(maxRatio > ratio){
            //The height is the constraining value
            scaledHeight = maxHeight;
            scaledWidth = (int)(ratio * scaledHeight);
        }
        else {
            //The width is the constraining value
            scaledWidth = maxWidth;
            scaledHeight = (int)(scaledWidth / ratio);
        }
        return scaleBitmapDown(bmp, scaledWidth, scaledHeight, disposeOriginalBitmap);
    }

    @SuppressWarnings("UnusedAssignment")
    public static Bitmap scaleBitmapDown(Bitmap bmp, int width, int height, boolean disposeOriginalBitmap){
        if(bmp == null || bmp.isRecycled())
            return bmp;

        if(bmp.getWidth() < width && bmp.getHeight() < height)
            return bmp;

        Bitmap scaled = Bitmap.createScaledBitmap(bmp, width, height, false);
        if(disposeOriginalBitmap){
            bmp.recycle();
            bmp = null;
        }

        return scaled;
    }
}
