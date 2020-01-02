package com.shamo.shadow.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

import com.shamo.shadow.R;

public class ShadowFrameLayout extends FrameLayout {

    public static final int ALL = 0x1111;

    public static final int LEFT = 0x0001;

    public static final int TOP = 0x0010;

    public static final int RIGHT = 0x0100;

    public static final int BOTTOM = 0x1000;

    public static final int ALL_ROUND = 0x11111;

    public static final int NONE_ROUND = 0x00001;

    public static final int LEFT_TOP = 0x00010;

    public static final int RIGHT_TOP = 0x00100;

    public static final int RIGHT_BOTTOM = 0x01000;

    public static final int LEFT_BOTTOM = 0x10000;


    public static final int SHAPE_RECTANGLE = 0x0001;

    public static final int SHAPE_OVAL = 0x0010;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaintCenter = new Paint(Paint.ANTI_ALIAS_FLAG);

    private RectF mRectF = new RectF();
    //    private RectF mRectF = new RectF();
    private Path path = new Path();

    /**
     * 阴影的颜色
     */
    private int mShadowColor = Color.TRANSPARENT;

    /**
     * 阴影的大小范围
     */
    private float mShadowRadius = 0;

    /**
     * 阴影 x 轴的偏移量
     */
    private float mShadowDx = 0;

    /**
     * 阴影 y 轴的偏移量
     */
    private float mShadowDy = 0;

    /**
     * 阴影显示的边界
     */
    private int mShadowSide = ALL;

    /**
     * 阴影的形状，圆形/矩形
     */
    private int mShadowShape = SHAPE_RECTANGLE;

    /**
     * 内容区半径
     */
    private float mContentRadius;
    /**
     * 内容区半径
     */
    private int mContentColor = Color.WHITE;
    /**
     * 内容区的圆角方向
     */
    private int mContentRadiusDirection;

    public ShadowFrameLayout(Context context) {
        this(context, null);
    }

    public ShadowFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private int paddingLeft = 0;
    private int paddingTop = 0;
    private int paddingRight = 0;
    private int paddingBottom = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
//        XLog.e("ShadowFrameLayout", "width = " + width + ", height = " + height);
        float effect = mShadowRadius;
        float rectLeft = 0;
        float rectTop = 0;
        float rectRight = width;
        float rectBottom = height;
        if ((mShadowSide & LEFT) == LEFT) {
            rectLeft = effect;
        }
        if ((mShadowSide & TOP) == TOP) {
            rectTop = effect;
        }
        if ((mShadowSide & RIGHT) == RIGHT) {
            rectRight = width - effect;
        }
        if ((mShadowSide & BOTTOM) == BOTTOM) {
            rectBottom = height - effect;
        }
        if (mShadowDy != 0.0f) {
            rectBottom = rectBottom - mShadowDy;
            paddingBottom = paddingBottom + (int) mShadowDy;
        }
        if (mShadowDx != 0.0f) {
            rectRight = rectRight - mShadowDx;
        }
        mRectF.left = rectLeft;
        mRectF.top = rectTop;
        mRectF.right = rectRight;
        mRectF.bottom = rectBottom;
        setMeasuredDimension(width, height);
        // 直接设置setPadding()会导致在Recyclerview测量不准确
    }

    /**
     * 设置逻辑内边距
     */
    private void setLogicPadding() {
        float effect = mShadowRadius;
        if ((mShadowSide & LEFT) == LEFT) {
            paddingLeft = (int) mShadowRadius;
        }
        if ((mShadowSide & TOP) == TOP) {
            paddingTop = (int) effect;
        }
        if ((mShadowSide & RIGHT) == RIGHT) {
            paddingRight = (int) effect;
        }
        if ((mShadowSide & BOTTOM) == BOTTOM) {
            paddingBottom = (int) effect;
        }
        if (mShadowDy != 0.0f) {
            paddingBottom = paddingBottom + (int) mShadowDy;
        }
        if (mShadowDx != 0.0f) {
            paddingRight = paddingRight + (int) mShadowDx;
        }
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    /**
     * 真正绘制阴影的方法
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setUpShadowPaint();
        if (mShadowShape == SHAPE_RECTANGLE) {
            canvas.drawRect(mRectF, mPaint);
            if ((mContentRadiusDirection & ALL_ROUND) == NONE_ROUND) {
                canvas.drawRect(mRectF, mPaintCenter);
                return;
            }
            /********begin这段是为了容错，以防设置的内容半径大于整体的宽高*******/
            float widthRadius = 0, heightRadius = 0;
            if (mRectF.bottom - mRectF.top < mContentRadius * 2) {
                widthRadius = (mRectF.bottom - mRectF.top) / 2;
            }
            if (mRectF.right - mRectF.left < mContentRadius * 2) {
                heightRadius = (mRectF.right - mRectF.left) / 2;
            }
            heightRadius = Math.min((int) widthRadius, (int) heightRadius);
            if (heightRadius > 0 && heightRadius < mContentRadius)
                mContentRadius = heightRadius;
            /********end*******/
            path.rewind();
            path.moveTo(mRectF.left, mRectF.top + mContentRadius);
            if ((mContentRadiusDirection & LEFT_TOP) == LEFT_TOP) {
                path.quadTo(mRectF.left, mRectF.top, mRectF.left + mContentRadius, mRectF.top);
            } else {
                path.lineTo(mRectF.left, mRectF.top);
                path.lineTo(mRectF.left + mContentRadius, mRectF.top);
            }
            path.lineTo(mRectF.right - mContentRadius, mRectF.top);
            if ((mContentRadiusDirection & RIGHT_TOP) == RIGHT_TOP) {
                path.quadTo(mRectF.right, mRectF.top, mRectF.right, mRectF.top + mContentRadius);
            } else {
                path.lineTo(mRectF.right, mRectF.top);
                path.lineTo(mRectF.right, mRectF.top + mContentRadius);
            }
            path.lineTo(mRectF.right, mRectF.bottom - mContentRadius);
            if ((mContentRadiusDirection & RIGHT_BOTTOM) == RIGHT_BOTTOM) {
                path.quadTo(mRectF.right, mRectF.bottom, mRectF.right - mContentRadius, mRectF.bottom);
            } else {
                path.lineTo(mRectF.right, mRectF.bottom);
                path.lineTo(mRectF.right - mContentRadius, mRectF.bottom);
            }
            path.lineTo(mRectF.left + mContentRadius, mRectF.bottom);
            if ((mContentRadiusDirection & LEFT_BOTTOM) == LEFT_BOTTOM) {
                path.quadTo(mRectF.left, mRectF.bottom, mRectF.left, mRectF.bottom - mContentRadius);
            } else {
                path.lineTo(mRectF.left, mRectF.bottom);
                path.lineTo(mRectF.left, mRectF.bottom - mContentRadius);
            }
            path.lineTo(mRectF.left, mRectF.top + mContentRadius);
            canvas.drawPath(path, mPaintCenter);
        } else if (mShadowShape == SHAPE_OVAL) {
            canvas.drawCircle(mRectF.centerX(), mRectF.centerY(), Math.min(mRectF.width(), mRectF.height()) / 2, mPaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void setShadowColor(int shadowColor) {
        mShadowColor = shadowColor;
        requestLayout();
        postInvalidate();
    }

    public void setShadowRadius(float shadowRadius) {
        mShadowRadius = shadowRadius;
        setLogicPadding();
        requestLayout();
        postInvalidate();
    }

    /**
     * 读取设置的阴影的属性
     *
     * @param attrs 从其中获取设置的值
     */
    private void init(AttributeSet attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);  // 关闭硬件加速
        this.setWillNotDraw(false);                    // 调用此方法后，才会执行 onDraw(Canvas) 方法

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ShadowLayout);
        if (typedArray != null) {
            mShadowColor = typedArray.getColor(R.styleable.ShadowLayout_shadowColor,
                    getContext().getResources().getColor(android.R.color.black));
            mShadowRadius = typedArray.getDimension(R.styleable.ShadowLayout_shadowWidth, 0);
            mShadowDx = typedArray.getDimension(R.styleable.ShadowLayout_shadowDx, 0);
            mShadowDy = typedArray.getDimension(R.styleable.ShadowLayout_shadowDy, 0);
            mShadowSide = typedArray.getInt(R.styleable.ShadowLayout_shadowSide, ALL);
            mContentRadiusDirection = typedArray.getInt(R.styleable.ShadowLayout_contentRadiusDirection, NONE_ROUND);
            mShadowShape = typedArray.getInt(R.styleable.ShadowLayout_shadowShape, SHAPE_RECTANGLE);
            mContentRadius = typedArray.getDimension(R.styleable.ShadowLayout_contentRadius, 0);
            mContentColor = typedArray.getColor(R.styleable.ShadowLayout_contentBackgroundColor,
                    getContext().getResources().getColor(android.R.color.white));
            typedArray.recycle();
        }
        setUpShadowPaint();
        setLogicPadding();
    }

    private void setUpShadowPaint() {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
        mPaintCenter.reset();
        mPaintCenter.setAntiAlias(true);
        mPaintCenter.setColor(mContentColor);
    }

    public void setShadowSide(int shadowSide) {
        this.mShadowSide = shadowSide;
        requestLayout();
        postInvalidate();
    }

    public void setContentRadiusDirection(int contentRadiusDirection) {
        this.mContentRadiusDirection = contentRadiusDirection;
        requestLayout();
        postInvalidate();
    }

    /**
     * dip2px dp 值转 px 值
     *
     * @param dpValue dp 值
     * @return px 值
     */
    private float dip2px(float dpValue) {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        float scale = dm.density;
        return (dpValue * scale + 0.5F);
    }
}
