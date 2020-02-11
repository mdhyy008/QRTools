package com.dabai.qrtools.utils;

import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * 对view实现拖拽移动、双指缩放效果（默认全开启）
 * 使用方法：1创建DragTouchListener实例 ；2设置监听 view.setOnTouchListener(DragTouchListener);
 * Created by alan on 2019/1/3 0007.
 */
public class DragTouchListener implements View.OnTouchListener {

    private DisplayMetrics dm;
    private int maxWidth;
    private int maxHeight;
    private int lastX;
    private int lastY;
    //刚触摸时的view坐标（用来获取按下时view的大小）
    private int oriLeft;
    private int oriRight;
    private int oriTop;
    private int oriBottom;
    private float baseValue;
    private DragListener dragListener;
    float originalScale;
    private static final int TOUCH_NONE = 0x00;
    private static final int TOUCH_ONE = 0x20;
    private static final int TOUCH_TWO = 0x21;
    /**
     * 当前触摸模式：
     * 无触摸；
     * 单指触摸；
     * 双指触摸；
     */
    private int currentTouchMode = TOUCH_NONE ;
    /**
     * 是否开启：双指触摸缩放
     */
    private boolean touchTwoZoomEnable = true;
    /**
     * 是否取消：触摸移动
     */
    private boolean isCancleTouchDrag = false;

    /**
     * 产生效果的view（缩放、拖拽效果）
     */
    private View mEffectView ;

    /**
     * 控制是否开启两指触摸缩放
     * @param touchTwoZoomEnable
     */
    public DragTouchListener setTouchTwoZoomEnable(boolean touchTwoZoomEnable) {
        this.touchTwoZoomEnable = touchTwoZoomEnable;
        return this;
    }

    /**
     * 设置：是否取消拖拽移动
     * @param cancleTouchDrag
     */
    public DragTouchListener setCancleTouchDrag(boolean cancleTouchDrag) {
        isCancleTouchDrag = cancleTouchDrag;
        return this;
    }

    public interface DragListener {
        void actionDown(View v);

        void actionUp(View v);

        void dragging(View listenerView, int left, int top, int right, int bottom);

        void zooming(float scale);
    }

//    public void setDragListener(DragListener dragListener) {
//        this.dragListener = dragListener;
//    }

    //    public DragTouchListener(DisplayMetrics dm) {
//        this.dm = dm;
//        maxWidth = dm.widthPixels;
//        maxHeight = dm.heightPixels - 50;
//    }

    public DragTouchListener(final ViewGroup limitParent,DragListener dragListener) {
//        maxHeight = viewGroup.getHeight();
//        maxWidth = viewGroup.getWidth();
        this(limitParent);
        this.dragListener = dragListener;
    }

    public DragTouchListener(){
        this(null);
    }

    /**
     *
     * @param limitParent 拖动限制区域，防止移出屏幕(null:拖动无限制)
     */
    public DragTouchListener(final ViewGroup limitParent) {
        if (limitParent !=null) {
            ViewTreeObserver vto = limitParent.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    maxHeight = limitParent.getMeasuredHeight();
                    maxWidth = limitParent.getMeasuredWidth();
                    //                Log.i("TAG", "maxHeight: "+maxHeight+", maxWidth"+maxWidth);
                    return true;
                }

            });
        }
        dragListener = new DragListener() {
            @Override
            public void actionDown(View v) {

            }
            @Override
            public void actionUp(View v) {
            }
            @Override
            public void dragging(View listenerView, int left, int top, int right, int bottom) {

            }
            @Override
            public void zooming(float scale) {
            }
        };
    }

    private boolean moveFlag ;
    @Override
    public boolean onTouch(View v, MotionEvent event) {

//        int action = event.getAction();
        int action = event.getAction()& MotionEvent.ACTION_MASK;
//        Log.i("TAG", "Touch:"+action);
        //屏蔽父控件拦截onTouch事件
        v.getParent().requestDisallowInterceptTouchEvent(true);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                dragListener.actionDown(v);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                oriLeft = v.getLeft();
                oriRight = v.getRight();
                oriTop = v.getTop();
                oriBottom = v.getBottom();
                currentTouchMode = TOUCH_ONE;
                baseValue = 0;
                lastScale = 1;
//                originalScale =  v.getScaleX();
                break;
            case MotionEvent.ACTION_POINTER_DOWN://多指触摸
                oriLeft = v.getLeft();
                oriRight = v.getRight();
                oriTop = v.getTop();
                oriBottom = v.getBottom();
                currentTouchMode = TOUCH_TWO;
                baseValue = 0;
                lastScale = 1 ;
                break;
            /**
             * layout(l,t,r,b)
             * l  Left position, relative to parent
             t  Top position, relative to parent
             r  Right position, relative to parent
             b  Bottom position, relative to parent
             * */
            case MotionEvent.ACTION_MOVE:

                moveFlag = !moveFlag;
                if (event.getPointerCount() == 2) {
                    //                if (currentTouchMode == TOUCH_TWO ) {
                    if (touchTwoZoomEnable) {
                        float x = event.getX(0) - event.getX(1);
                        float y = event.getY(0) - event.getY(1);

//                            Log.i("TAG", "---y:点1: "+event.getY(0)+"  点2: "+event.getY(1));
                        float value = (float) Math.sqrt(x * x + y * y);// 计算两点的距离
                        if (baseValue == 0) {
                            baseValue = value;
//                                Log.i("TAG","  value: "+value+"  ; baseValue: "+baseValue);
                        } else {
                            if ((value - baseValue) >= 10 || value - baseValue <= -10) {
                                // 当前两点间的距离 除以 手指落下时两点间的距离就是需要缩放的比例。
                                float scale = value / baseValue;
                                //                                Log.i("TAG", "onTouch-scale: "+scale+"  value: "+value+"  ; baseValue: "+baseValue);
                                //缩放view(不能用当前touch方法里的view，会造成频闪效果)（只能在其他view调用）
//                                    mEffectView.setScaleX(scale);
//                                    mEffectView.setScaleY(scale);

                                //改变大小进行缩放（只能缩放当前view的大小，如果是父布局，则里面的子控件无法缩小）
                                touchZoom(v,scale);

                                this.dragListener.zooming(scale);

                            }
                        }
                    }
                } else if (currentTouchMode == TOUCH_ONE) {//1个手指
                    //如果取消拖拽，触摸就交给系统处理
                    if(isCancleTouchDrag){
                        return false;
                    }
                    //移动图片位置
                    touchDrag(v, event);
                }

                break;
            case MotionEvent.ACTION_UP:
                baseValue = 0;
                dragListener.actionUp(v);
                break;
            default:
                currentTouchMode = TOUCH_NONE;
                break;
        }
        return true;
    }


    private float lastScale = 1;
    /**
     * 缩放view
     * @param v
     * @param scale  当前距离按下时的比例  (0.8：缩小到0.8倍)
     */
    private void touchZoom(View v,float scale){
        int oriWidth = Math.abs(oriRight - oriLeft);
        int oriHeight = Math.abs(oriBottom - oriTop);

//        if(lastScale == 0)lastScale = scale;
        //需要缩放的比例（1-0.9=0.1，需要缩小0.1倍；-0.1：放大0.1倍）
        float  zoomScale = (lastScale - scale);

        int dx = (int) (oriWidth*zoomScale/2f);
        int dy = (int) (oriHeight*zoomScale/2f);

//        Log.i("TAG", "---------------zoomScale: "+zoomScale);
//        Log.i("TAG", "--------------------------dx: "+dx);

        int left = v.getLeft() + dx;
        int top = v.getTop() + dy;
        int right = v.getRight() - dx;
        int bottom = v.getBottom() - dy;

        v.layout(left, top, right, bottom);

        lastScale = scale;

    }

    private void touchDrag(View v, MotionEvent event) {

        int dx = (int) event.getRawX() - lastX;
        int dy = (int) event.getRawY() - lastY;

        int left = v.getLeft() + dx;
        int top = v.getTop() + dy;
        int right = v.getRight() + dx;
        int bottom = v.getBottom() + dy;

        if (maxWidth !=0 && maxHeight!=0) {
            //防止移出屏幕
            if(left < 0){
                left = 0;
                right = left + v.getWidth();
            }
            if(right > maxWidth){
                right = maxWidth;
                left = right - v.getWidth();
            }
            if(top < 0){
                top = 0;
                bottom = top + v.getHeight();
            }
            if(bottom > maxHeight){
                bottom = maxHeight;
                top = bottom - v.getHeight();
            }
        }

        v.layout(left, top, right, bottom);

        dragListener.dragging(v, left, top, right, bottom);
//                Log.i("TAG", "position" + left +", " + top + ", " + right + ", " + bottom);
        lastX = (int) event.getRawX();
        lastY = (int) event.getRawY();
    }

}