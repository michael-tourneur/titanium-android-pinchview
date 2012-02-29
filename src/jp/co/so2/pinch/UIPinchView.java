package jp.co.so2.pinch;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

public class UIPinchView extends TiUIView 
{
	public PinchView tiPinchView;
	private float maxZoom = 5.f;
	private float minZoom = 0.1f;
	private float curZoom = 1.f;
	
	public UIPinchView(TiViewProxy proxy)
	{
		super(proxy);
		
		setNativeView(tiPinchView = new PinchView(proxy.getActivity()));
	}
	
	@Override
	public void processProperties(KrollDict d) {
		super.processProperties(d);
		if(d.containsKeyAndNotNull("maxZoomValue"))
			maxZoom = d.getDouble("maxZoomValue").floatValue();
		if(d.containsKeyAndNotNull("minZoomValue"))
			minZoom = d.getDouble("minZoomValue").floatValue();
	}
	
	public void setMaxZoomValue(float maxZoom)
	{
		this.maxZoom = maxZoom;
	}
	
	public void setMinZoomValue(float minZoom)
	{
		this.minZoom = minZoom;
	}
	
	public void setCurZoomValue(float curZoom)
	{
		this.curZoom = curZoom;
	}
	
	public class PinchView extends View {
		private static final int INVALID_POINTER_ID = -1;
		private int activePointerId;
		private ScaleGestureDetector pinchDetector;
		
		private float lastX, lastY;
		
		public PinchView(Context c)
		{
			super(c);
			if(c.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH))
				pinchDetector = new ScaleGestureDetector(c, new ScaleListener());
			else
				Log.i("UIPinchView","Multitouch not supported on this device!");
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent e)
		{
			if(pinchDetector != null)
				pinchDetector.onTouchEvent(e);
			
			final int action = e.getAction();
			KrollDict eventData = new KrollDict();
			
			
			switch(action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN: {
				final float x = e.getX();
				final float y = e.getY();
				eventData.put("x", e.getX());
				eventData.put("y", e.getY());
				proxy.fireEvent("multiStart", eventData);
				activePointerId = e.getPointerId(0);
				lastX = x;
				lastY = y;
				break;
				}
			case MotionEvent.ACTION_MOVE: {
				final int pointerIndex = e.findPointerIndex(activePointerId);
				if(pointerIndex == INVALID_POINTER_ID)
					break;
				final float x = e.getX(pointerIndex);
				final float y = e.getY(pointerIndex);
				if(pinchDetector == null || !pinchDetector.isInProgress())
				{
					eventData.put("x", x - lastX);
					eventData.put("y", y - lastY);
					proxy.fireEvent("multiMove", eventData);
				}
				lastX = x;
				lastY = y;
				break;
				}
			case MotionEvent.ACTION_UP: {
				activePointerId = INVALID_POINTER_ID;
				eventData.put("x", e.getX());
				eventData.put("y", e.getY());
				proxy.fireEvent("multiEnd", eventData);
				break;
				}
			case MotionEvent.ACTION_POINTER_UP: {
				final int pointerIndex = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int pointerId = e.getPointerId(pointerIndex);
				if(pointerId == activePointerId) {
					final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
					activePointerId = e.getPointerId(newPointerIndex);
					lastX = e.getX(newPointerIndex);
					lastY = e.getY(newPointerIndex);
				}
				break;
				}
			}
			
			return true;
		}
		
		private class ScaleListener extends SimpleOnScaleGestureListener {
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				curZoom *= detector.getScaleFactor();
				
				curZoom = Math.max(minZoom, Math.min(curZoom, maxZoom));
				invalidate();

				KrollDict eventData = new KrollDict();
				eventData.put("scale", curZoom);				
				proxy.fireEvent("pinch", eventData);
				
				return true;
			}
		}
	}
}
