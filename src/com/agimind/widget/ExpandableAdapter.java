package com.agimind.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

public class ExpandableAdapter implements ListAdapter {
	
	private Context mContext;
	
	private ListAdapter mWrappedAdapter;
	
	private int mSwitchId;
	private int mHolderId;
	
	private int mCheckedPosition;
	
	private List<ExpandableView> mViews = new ArrayList<ExpandableView>();
	
	public ExpandableAdapter(Context context, ListAdapter wrapped, int switchId, int holderId) {
		mContext = context;
		mWrappedAdapter = wrapped;
		
		mSwitchId = switchId;
		mHolderId = holderId;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return mWrappedAdapter.areAllItemsEnabled();
	}

	@Override
	public boolean isEnabled(int i) {
		return mWrappedAdapter.isEnabled(i);
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver dataSetObserver) {
		mWrappedAdapter.registerDataSetObserver(dataSetObserver);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
		mWrappedAdapter.unregisterDataSetObserver(dataSetObserver);
	}
	
	@Override
	public boolean hasStableIds() {
		return mWrappedAdapter.hasStableIds();
	}
	
	@Override
	public int getCount() {
		return mWrappedAdapter.getCount();
	}

	@Override
	public Object getItem(int position) {
		return mWrappedAdapter.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return mWrappedAdapter.getItemId(position);
	}
	
	@Override
	public int getItemViewType(int position) {
		return mWrappedAdapter.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return mWrappedAdapter.getViewTypeCount();
	}

	@Override
	public boolean isEmpty() {
		return mWrappedAdapter.isEmpty();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ExpandableView holder;
		
		if(convertView == null) {
			holder = new ExpandableView(mContext);
			
			View v = mWrappedAdapter.getView(position, null, parent);
			
			holder.addView(v);
			holder.init();
			
			mViews.add(holder);
		} else {
			holder = (ExpandableView) convertView;
			mWrappedAdapter.getView(position, holder.getChildAt(0), parent);
		}
		
		holder.setPosition(position);
		
		holder.setChecked(position == mCheckedPosition);
		
		return holder;
	}
	
	void closeNotChecked() {
		for(final ExpandableView v : mViews) {
			if(v.mPosition != mCheckedPosition && v.isChecked()) {
				if(v.mAnimating && v.mVisible) {
					v.closeWithoutAnimation();
					
					continue;
				}
				
				v.close();
			}
		}
	}
	
	private class ExpandableView extends FrameLayout implements Checkable {
		
		private int mPosition = -1;
		
		private boolean mVisible = false;
		private boolean mAnimating = false;
		private Bitmap mCachedBitmap;
		
		private View mHolder;
		private View mSwitch;
		
		private int mHeightOffset = 0;
		
		public ExpandableView(Context context) {
			super(context);
		}
		
		private void init() {
			mHolder = findViewById(mHolderId);
			mSwitch = findViewById(mSwitchId);
			
			if(mHolder == null || mSwitch == null) {
				mVisible = true;
				return;
			}
			
			mSwitch.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					toggle();
				}
			});
			
			mVisible = false;
		}
		
		private void setPosition(int pos) {
			mPosition = pos;
			
			clearAnimation();
			mAnimating = false;
			
			clearCache();
		}
		
		private void clearCache() {
			if(mCachedBitmap != null && !mCachedBitmap.isRecycled()) {
				mCachedBitmap.recycle();
			}
			
			mCachedBitmap = null;
		}
		
		public void closeWithoutAnimation() {
			clearAnimation();
			clearCache();
			
			mHeightOffset = 0;
			mVisible = false;
			mAnimating = false;
			requestLayout();
		}
		
		public void close() {
			mVisible = false;
			mAnimating = true;
			requestLayout();
		}
		
		@Override
		public void toggle() {
			mVisible = !mVisible;
			
			mCheckedPosition = mVisible ? mPosition : -1;
			closeNotChecked();
			
			requestLayout();
			
			mAnimating = true;
		}
		
		@Override
		public boolean isChecked() {
			return mVisible;
		}
		
		@Override
		public void setChecked(final boolean checked) {
			if(checked == mVisible) {
				return;
			}
			
			if(!checked) {
				mHeightOffset = 0;
			} else {
				closeNotChecked();
			}
			
			mVisible = checked;
			requestLayout();
		}
		
		private void buildCache() {
			clearCache();
			
			mCachedBitmap = Bitmap.createBitmap(getMeasuredWidth(),
					getMeasuredHeight() + mHolder.getMeasuredHeight(),
					Bitmap.Config.ARGB_8888);
			
			final View v = getChildAt(0);
			
			Canvas canvas = new Canvas(mCachedBitmap);
			v.draw(canvas);
			
			invalidate();
			
			float start = !mVisible ? mHolder.getMeasuredHeight() : 0;
			float offset = mVisible ? mHolder.getMeasuredHeight() : 0;
			
			startAnimation(new ExpandAnimation(start, offset));
		}
		
		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			return mAnimating ? true : super.onInterceptTouchEvent(ev);
		}
		
		@Override
		protected void onMeasure(int wS, int hS) {
			super.onMeasure(wS, hS);
			
			if(mHolder == null || (mVisible && !mAnimating)) {
				if(mHolder != null) {
					mHeightOffset = mHolder.getMeasuredHeight();
				}
				
				return;
			}
			
			int width = MeasureSpec.getSize(wS);
			
			int mH = getMeasuredHeight();
			int holderH = mHolder.getMeasuredHeight();
			
			setMeasuredDimension(width, mH - holderH + mHeightOffset);
			
			if(mAnimating && mCachedBitmap == null) {
				buildCache();
			}
		}
		
		@Override
		protected void dispatchDraw(Canvas canvas) {
			if(!mAnimating || mCachedBitmap == null || mCachedBitmap.isRecycled()) {
				super.dispatchDraw(canvas);
			} else {
				canvas.drawBitmap(mCachedBitmap, 0, 0, null);
			}
		}
		
		@Override
		protected void onAnimationEnd() {
			super.onAnimationEnd();
			
			mAnimating = false;
			clearCache();
		}
		
		private class ExpandAnimation extends Animation {
			
			private static final float SPEED = 0.2f;
			
			private float mStart;
			private float mEnd;
			
			public ExpandAnimation(float from, float to) {
				mStart = from;
				mEnd = to;
				
				float duration = Math.abs(to - from) / SPEED;
				setDuration((long) duration);
				setInterpolator(new DecelerateInterpolator());
			}
			
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				super.applyTransformation(interpolatedTime, t);
				
				float offset = (mEnd - mStart) * interpolatedTime + mStart;
				mHeightOffset = (int) offset;
				
				requestLayout();
			}
			
		}
		
	}

}
