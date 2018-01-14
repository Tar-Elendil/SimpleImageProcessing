package com.example.istimadan.simpleimageprocessing;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicColorMatrix;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public final class Laplacian {
    //Error tags
    public static final String OpenCVConvertBitmapToMatError = "OpenCVBitmapToMatFail";

    public static Bitmap openCV(Bitmap bmp){
        // Declare the variables we are going to use
        Mat src = new Mat(), src_gray = new Mat(), dst = new Mat();
        int kernel_size = 3;
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;

        //Convert bitmap to matrix
        Utils.bitmapToMat(bmp, src);
        // Check if image is loaded fine
        if( src.empty() ) {
            Log.e(OpenCVConvertBitmapToMatError , String.format("Error opening image with bytes %s", bmp.getByteCount()));
            return null;
        }

        // Reduce noise by blurring with a Gaussian filter ( kernel size = 3 )
        Imgproc.GaussianBlur( src, src, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT );
        // Convert the image to greyscale
        Imgproc.cvtColor( src, src_gray, Imgproc.COLOR_RGB2GRAY );
        Mat abs_dst = new Mat();
        Imgproc.Laplacian( src_gray, dst, ddepth, kernel_size, scale, delta, Core.BORDER_DEFAULT );
        // converting back to CV_8U
        Core.convertScaleAbs( dst, abs_dst );

        Bitmap retBmp = Bitmap.createBitmap(abs_dst.cols(), abs_dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(abs_dst, retBmp);

        return retBmp;
    }

    public static Bitmap renderscript(Bitmap bmp, RenderScript renderScript){
        Bitmap copy = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        copy.setHasAlpha(false);

        Allocation input = Allocation.createFromBitmap(renderScript, bmp);
        Allocation alloc1 = Allocation.createTyped(renderScript, input.getType());
        Allocation alloc2 = Allocation.createTyped(renderScript, input.getType());
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        scriptIntrinsicBlur.setRadius(1);
        scriptIntrinsicBlur.setInput(input);
        scriptIntrinsicBlur.forEach(alloc1);

        input.destroy();

        //convert to greyscale
        ScriptIntrinsicColorMatrix scriptGreyscale = ScriptIntrinsicColorMatrix.create(renderScript);
        scriptGreyscale.setGreyscale();
        scriptGreyscale.forEach(alloc1, alloc2);

        ScriptIntrinsicConvolve3x3 laplacianConvolve = ScriptIntrinsicConvolve3x3.create(renderScript, Element.U8_4(renderScript));

        laplacianConvolve.setCoefficients(new float[]{
                -1f, -1f, -1f,
                -1f, 8, -1f,
                -1f, -1f, -1f
        });
        laplacianConvolve.setInput(alloc2);
        laplacianConvolve.forEach(alloc1);
        alloc1.copyTo(copy);

        alloc1.destroy();
        alloc2.destroy();

        return copy;
    }
}
