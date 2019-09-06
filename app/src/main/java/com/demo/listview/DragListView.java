package com.demo.listview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * item可拖拽的ListView
 * Created by xiangkezhu on 19-09-03.
 */
public class DragListView extends ListView {

    private int mLastPosition;
    private int mCurrentPosition;

    private int mAutoScrollUpY;
    private int mAutoScrollDownY;

    private int mLastX, mLastY;
    private int mDownX, mDownY;

    private int mDragViewOffset;

    private DragItemListener mDragItemListener;

    private boolean mHasStart = false;

    private Bitmap mBitmap;

    private View mItemView;

    private int mTouchSlop;
    private long mLastScrollTime;
    private boolean mScrolling = false;
    private Runnable mScrollRunnable;
    private Vibrator mVibrator;
    private Handler mHandler = new Handler();
    private Runnable mLongClickRunnable = new Runnable() {
        @Override
        public void run() {
            mVibrator.vibrate(100);
            mDragViewOffset = mDownY - mItemView.getTop();
            mItemView.setDrawingCacheEnabled(true);
            mBitmap = Bitmap.createBitmap(mItemView.getDrawingCache());
            mItemView.setDrawingCacheEnabled(false);
        mHasStart = false;
        mLastY = mDownY;
        mLastX = mDownX;
        invalidate();
    }
    };

    public DragListView(Context context) {
        this(context, null);
    }

    public DragListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public DragListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        mScrollRunnable = new Runnable() {
            @Override
            public void run() {
                mScrolling = false;
                if (mBitmap != null) {
                    mLastScrollTime = System.currentTimeMillis();
                    onMove((int) mMoveY);
                    invalidate();
                }
            }
        };
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
                if(mDragItemListener != null) {
                    mHandler.removeCallbacks(mLongClickRunnable);
                    if (mBitmap != null) {
                        mLastX = (int) ev.getX();
                        mLastY = (int) ev.getY();
                        stopDrag();
                        invalidate();
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mDragItemListener != null) {
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    if (!isTouchInItem(mItemView, x, y)) {
                        mHandler.removeCallbacks(mLongClickRunnable);
                    }
                    if (mBitmap != null) {
                        if (!mHasStart) {
                            mDragItemListener.startDrag(mCurrentPosition, mItemView);
                            mHasStart = true;
                        }
                        int moveY = (int) ev.getY();
                        if (moveY < 0) {
                            moveY = 0;
                        } else if (moveY > getHeight()) {
                            moveY = getHeight();
                        }
                        mMoveY = moveY;
                        onMove(moveY);
                        mLastY = moveY;
                        mLastX = (int) ev.getX();
                        invalidate();
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                if(mDragItemListener != null) {
                    stopDrag();
                    mDownX = (int) ev.getX();
                    mDownY = (int) ev.getY();
                    int temp = pointToPosition(mDownX, mDownY);
                    if (temp == AdapterView.INVALID_POSITION) {
                        return super.dispatchTouchEvent(ev);
                    }
                    mLastPosition = mCurrentPosition = temp;
                    ViewGroup itemView = (ViewGroup) getChildAt(mCurrentPosition - getFirstVisiblePosition());
                    if (itemView != null && mDragItemListener.canDrag(itemView, mDownX, mDownY)) {
                        mItemView = itemView;
                        mHandler.postDelayed(mLongClickRunnable, 1000);
                        return true;
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private float mMoveY;

    private boolean isTouchInItem(View dragView, int x, int y) {
        if (dragView == null) {
            return false;
        }
        int leftOffset = dragView.getLeft();
        int topOffset = dragView.getTop();
        if (x < leftOffset || x > leftOffset + dragView.getWidth()) {
            return false;
        }
        if (y < topOffset || y > topOffset + dragView.getHeight()) {
            return false;
        }
        return true;
    }
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mBitmap != null && !mBitmap.isRecycled()) {
            canvas.drawBitmap(mBitmap, 0, mLastY - mDragViewOffset, null);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mAutoScrollUpY = dp2px(getContext(), 80);
        mAutoScrollDownY = h - mAutoScrollUpY;
    }

    public void onMove(int moveY) {
        int endPos = pointToPosition(getWidth() / 2, moveY);
        if (endPos == INVALID_POSITION) {
            checkScroller(moveY);
            return;
        }
        int mask = mLastPosition > endPos ? -1 : 1;
        for (int i = mLastPosition; mask > 0 ? i <= endPos : i >= endPos; i += mask) {
            int index = i - getFirstVisiblePosition();
            if (index >= getChildCount() || index < 0) {
                continue;
            }
            int y = getChildAt(index).getTop();
            int tempPosition = pointToPosition(0, y);
            if (tempPosition != INVALID_POSITION) {
                mCurrentPosition = tempPosition;
            }
            if (y < getChildAt(0).getTop()) {
                mCurrentPosition = 0;
            } else if (y > getChildAt(getChildCount() - 1).getBottom()) {
                mCurrentPosition = getAdapter().getCount() - 1;
            }
            checkExchange(y);
        }
        checkScroller(moveY);
    }

    public void checkScroller(final int y) {

        int offset = 0;
        if (y < mAutoScrollUpY) {
            if (y <= mDownY - mTouchSlop) {
                offset = dp2px(getContext(), 6);
            }
        } else if (y > mAutoScrollDownY) {
            if (y >= mDownY + mTouchSlop) {
                offset = -dp2px(getContext(), 6);
            }
        }

        if (offset != 0) {
            View view = getChildAt(mCurrentPosition - getFirstVisiblePosition());
            if (view != null) {
                // 滚动列表
                setSelectionFromTop(mCurrentPosition, view.getTop() + offset);
                if (!mScrolling) {
                    mScrolling = true;
                    long passed = System.currentTimeMillis() - mLastScrollTime;
                    postDelayed(mScrollRunnable, passed > 15 ? 15 : 15 - passed);
                }
            }
        }
    }

    public void stopDrag() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
            if (mDragItemListener != null) {
                mDragItemListener.onRelease(mCurrentPosition, mItemView, mLastY - mDragViewOffset, mLastX, mLastY);
            }
        }
        if (mItemView != null) {
            mItemView = null;
        }
        mScrolling = false;
        removeCallbacks(mScrollRunnable);
    }

    private void checkExchange(int y) {
        if (mCurrentPosition != mLastPosition) {
            if (mDragItemListener != null) {
                if (mDragItemListener.canExchange(mLastPosition, mCurrentPosition)) {
                    View lastView = mItemView;
                    mItemView = getChildAt(mCurrentPosition - getFirstVisiblePosition());
                    mDragItemListener.onExchange(mLastPosition, mCurrentPosition, lastView, mItemView);
                    mLastPosition = mCurrentPosition;
                }
            }
        }
    }


    public void setDragItemListener(DragItemListener listener) {
        mDragItemListener = listener;
    }

    public DragItemListener getDragListener() {
        return mDragItemListener;
    }

    public interface DragItemListener {

        boolean canExchange(int srcPosition, int position);
        void onExchange(int srcPosition, int position, View srcItemView, View itemView);
        void onRelease(int position, View itemView, int itemViewY, int releaseX, int releaseY);
        boolean canDrag(View itemView, int x, int y);
        void startDrag(int position, View itemView);
        void beforeDrawingCache(View itemView);
        Bitmap afterDrawingCache(View itemView, Bitmap bitmap);
    }

    public static abstract class SimpleAnimationDragItemListener implements DragItemListener {
        @Override
        public void onRelease(int positon, View itemView, int itemViewY, int releaseX, int releaseY) {
            itemView.setVisibility(View.VISIBLE);
            if (itemView != null && Math.abs(itemViewY - itemView.getTop()) > itemView.getHeight() / 5) {
                AlphaAnimation animation = new AlphaAnimation(0.5f, 1);
                animation.setDuration(150);
                itemView.clearAnimation();
                itemView.startAnimation(animation);
            }

        }

        @Override
        public void startDrag(int position, View itemView) {
            if (itemView != null) { // 隐藏view
                itemView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onExchange(int srcPosition, int position, View srcItemView, View itemView) {
            if (srcItemView != null) {
                int height = srcPosition > position ? -srcItemView.getHeight() : srcItemView.getHeight();
                TranslateAnimation animation = new TranslateAnimation(0, 0, height, 0);
                animation.setDuration(200);
                srcItemView.clearAnimation();
                srcItemView.startAnimation(animation);
                srcItemView.setVisibility(View.VISIBLE);
            }
            if (itemView != null) {
                itemView.setVisibility(View.INVISIBLE);
            }
        }
    }

    public int dp2px(Context context, float dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
    }
}