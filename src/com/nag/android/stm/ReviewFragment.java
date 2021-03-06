package com.nag.android.stm;

import java.io.InputStream;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Toast;

public class ReviewFragment extends Fragment implements TranslationGestureDetector.TranslationGestureListener,MenuHandler.Listener{

	private ScaleGestureDetector sgd;
	private TranslationGestureDetector tgd;
	private MenuHandler menuhandler;
	private float scale = 0.0f;
	private float cx, cy;
	private float px, py;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		menuhandler = new MenuHandler(this);
		setHasOptionsMenu(true);
	}
 
	@Override
	public void onTranslationEnd(float x, float y) {
	}

	@Override
	public void onTranslationBegin(float x, float y) {
		px = x;
		py = y;
	}

	@Override
	public void onTranslation(float x, float y) {
		cx +=  x - px;
		cy += y - py;
		px = x;
		py = y;
	}

public static ReviewFragment newInstance(){
		final ReviewFragment instance = new ReviewFragment();
		return instance;
	}

	private Matrix matrix;

	ReviewFragment(){
		matrix = new Matrix();
	}

//	private Point size;
	public static final String ARG_FILENAME = "flename";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Context context = getActivity();
		InternalView rootView = new InternalView(context);
		sgd = new ScaleGestureDetector(context,
				new ScaleGestureDetector.SimpleOnScaleGestureListener() {
					@Override
					public boolean onScaleBegin(ScaleGestureDetector detector) {
						return super.onScaleBegin(detector);
					}
					@Override
					public void onScaleEnd(ScaleGestureDetector detector) {
						super.onScaleEnd(detector);
					}
					@Override
					public boolean onScale(ScaleGestureDetector detector) {
						scale *= detector.getScaleFactor();
						return true;
					}
		});
		tgd = new TranslationGestureDetector(this);

		try{
			InputStream in = context.openFileInput(getArguments().getString(ARG_FILENAME));
			Bitmap org = BitmapFactory.decodeStream(in);
			rootView.setBitmap(org);
			in.close();
		}catch(Exception e){
			rootView.setBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return rootView;
	}

	private class InternalView extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener{
		private final int MAX_MAGNIFY = 8;
		private SurfaceHolder holder;
		private float scale_max;
		private float scale_min;
		private int width;
		private int height;
		private Bitmap bitmap;

		public InternalView(Context context) {
			super(context);
			getHolder().addCallback(this);
			setOnTouchListener(this);
		}

		public void setBitmap(Bitmap bitmap){
			this.bitmap = bitmap;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			present(holder, width, height);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}
 
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			bitmap.recycle();
			if(camera!=null){
				camera.release();
				camera = null;
			}
		}

		private void present(){
			present(holder, width, height);
		}

		private void present(SurfaceHolder holder, int width, int height)
		{
			this.holder = holder;
			this.width = width;
			this.height = height;

			if(scale == 0.0f){
				float sw = (float)width /bitmap.getWidth();
				float sh = (float)height /bitmap.getHeight();
				if(sw>sh){
					scale = sw;
				}else{
					scale = sh;
				}
				scale_min = scale;
				scale_max = scale_min*MAX_MAGNIFY;
			}

			if(scale > scale_max){
				scale = scale_max;
			}else if(scale < scale_min){
				scale = scale_min;
			}
			if(cx > 0 ){
				cx = 0 ;
			}else if(cx<width-bitmap.getWidth()*scale){
				cx = width - bitmap.getWidth()*scale;
			}
			if(cy > 0 ){
				cy = 0 ;
			}else if(cy<height-bitmap.getHeight()*scale){
				cy = height - bitmap.getHeight()*scale;
			}
			matrix.reset();
			matrix.postScale(scale, scale);
			matrix.postTranslate(cx, cy);

			Canvas canvas = holder.lockCanvas();
			canvas.drawColor(Color.BLACK);
			canvas.drawBitmap(bitmap, matrix, null);

			holder.unlockCanvasAndPost(canvas);
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			super.onTouchEvent(event);
			sgd.onTouchEvent(event);
			tgd.onTouch(v, event);

			present();
			return true;
		}
		@Override
		public boolean performClick() {
			return super.performClick();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(menuhandler.onOptionsItemSelected(getActivity(), item)){
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onUpdateFlash(String mode) {
		// Do Nothing
	}

	@Override
	public void onResetCapacity(int size) {
		// Do Nothing
	}

	Camera camera = null;
	@Override
	public Camera getCamera() {
		if(camera==null){
			camera = Camera.open();
		}
		return camera;
	}

	@Override
	public void onUpdateThumbnailSide(int side) {
		// Do Nothing
	}

	
}
