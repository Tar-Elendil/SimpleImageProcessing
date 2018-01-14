package com.example.istimadan.simpleimageprocessing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    //Constants
    public static final int LOADFROMGALLERYID = UUID.randomUUID().hashCode();
    private static final int TAKEACAMERAIMAGEID = UUID.randomUUID().hashCode();

    //Views
    Button _loadGallery;
    Button _loadTrees;
    ViewPager _imagePager;

    int _maxWidth;
    int _maxHeight;

    //Initialise open cv
    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    private RenderScript _renderScript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        _maxWidth = Math.min(width, height);
        _maxHeight = Math.max(width, height);

        _loadGallery = findViewById(R.id.load_image_button);
        _loadGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectFromGallery();
            }
        });

        _loadTrees = findViewById(R.id.load_trees_button);
        _loadTrees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ArrayList<ConstraintLayout> pagerViews = new ArrayList<>();
                    long loadTime, openCVTime, renderscriptTime;
                    loadTime = System.nanoTime();
                    Bitmap treesImage = BitmapFactory.decodeResource(getResources(), R.drawable.almond_bloom);
                    loadTime = System.nanoTime() - loadTime;
                    //cache trees Image here

                    openCVTime = System.nanoTime();
                    final Bitmap laplaceImage = Laplacian.openCV(treesImage);
                    openCVTime = System.nanoTime() - openCVTime;

                    renderscriptTime = System.nanoTime();
                    final Bitmap rLaplaceImage = Laplacian.renderscript(treesImage, _renderScript);
                    renderscriptTime = System.nanoTime() - renderscriptTime;
                    pagerViews.add(createViewForPager(BitmapUtils.scaleBitmapDownAndKeepAspectRatio(treesImage,_maxWidth, _maxHeight, true ), "Original image", TimeUnit.MILLISECONDS.convert(loadTime, TimeUnit.NANOSECONDS)));
                    pagerViews.add(createViewForPager(BitmapUtils.scaleBitmapDownAndKeepAspectRatio(laplaceImage,_maxWidth, _maxHeight, true ), "OpenCV image", TimeUnit.MILLISECONDS.convert(openCVTime, TimeUnit.NANOSECONDS)));
                    pagerViews.add(createViewForPager(BitmapUtils.scaleBitmapDownAndKeepAspectRatio(rLaplaceImage,_maxWidth, _maxHeight, true ), "Renderscript image", TimeUnit.MILLISECONDS.convert(renderscriptTime, TimeUnit.NANOSECONDS)));
                    _imagePager.setAdapter(new ImagePagerAdapter(pagerViews));
                }
                catch (Exception e){
                    Log.e("LoadResourceError", e.getMessage(), e);
                }

            }
        });

        _imagePager = findViewById(R.id.images_pager);

        _renderScript = RenderScript.create(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _renderScript.destroy();
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK && reqCode == LOADFROMGALLERYID) {
            try {
                ArrayList<ConstraintLayout> pagerViews = new ArrayList<>();
                long loadTime, openCVTime, renderscriptTime;
                loadTime = System.nanoTime();
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                loadTime = System.nanoTime() - loadTime;

                openCVTime = System.nanoTime();
                final Bitmap laplaceImage = Laplacian.openCV(selectedImage);
                openCVTime = System.nanoTime() - openCVTime;
                renderscriptTime = System.nanoTime();
                final Bitmap rLaplaceImage = Laplacian.renderscript(selectedImage, _renderScript);
                renderscriptTime = System.nanoTime() - renderscriptTime;

                pagerViews.add(createViewForPager(selectedImage, "Original image", TimeUnit.MILLISECONDS.convert(loadTime, TimeUnit.NANOSECONDS)));
                pagerViews.add(createViewForPager(laplaceImage, "OpenCV image", TimeUnit.MILLISECONDS.convert(openCVTime, TimeUnit.NANOSECONDS)));
                pagerViews.add(createViewForPager(rLaplaceImage, "Renderscript image", TimeUnit.MILLISECONDS.convert(renderscriptTime, TimeUnit.NANOSECONDS)));
                _imagePager.setAdapter(new ImagePagerAdapter(pagerViews));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }
    }

    public void SelectFromGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, LOADFROMGALLERYID);
    }

    public ConstraintLayout createViewForPager(Bitmap bmp, String name, long timeTaken){
        ConstraintLayout layout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.image_pager_view, null);

        ImageView imageView = layout.findViewById(R.id.viewpager_item_image);
        imageView.setImageBitmap(bmp);

        TextView nameView = layout.findViewById(R.id.viewpager_item_name);
        nameView.setText(name);

        TextView timeView = layout.findViewById(R.id.viewpager_item_time);
        timeView.setText(Long.toString(timeTaken));

        return layout;
    }
    public class ImagePagerAdapter extends PagerAdapter {

        private ArrayList<ConstraintLayout> _views;

        public ImagePagerAdapter(List<ConstraintLayout> views){
            _views = new ArrayList<>(getCount());
            _views.addAll(views);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ConstraintLayout view = _views.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ConstraintLayout)object);
        }
    }
}
