package android.lib.verticalmarqueetextview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * A {@link TextView} with vertical marquee effect. The animation speed can be set using
 * {@link #setMarqueeSpeed(int)} or through XML declaration. By default, the marquee effect
 * animation starts automatically when this view is attached to a {@link Window}.
 * Set <code>autoStartMarquee</code> to <code>false</code> to disable this behavior.
 */
public final class VerticalMarqueeTextView extends ScrollView {
    private static final int MIN_MARQUEE_SPEED = 1;
    private static final int MAX_MARQUEE_SPEED = 1000;

    private TextView textView;

    private int     marqueeSpeed;
    private boolean autoStartMarquee;
    private boolean marqueeStarted;
    private boolean marqueePaused;

    public VerticalMarqueeTextView(final Context context) {
        super(context);

        this.init(context, null);
    }

    public VerticalMarqueeTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        this.init(context, attrs);
    }

    public VerticalMarqueeTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        this.init(context, attrs);
    }

    /**
     * Returns the text content of this text view.
     * @return The text content of this text view.
     */
    public CharSequence getText() {
        return this.textView.getText();
    }

    /**
     * Sets a string for this text view.
     * @param text The text to set.
     */
    public void setText(final CharSequence text) {
        this.textView.setText(text);
    }

    /**
     * Returns the speed of the marquee effect animation.
     * @return The speed of the marquee effect animation.
     */
    public int getMarqueeSpeed() {
        return this.marqueeSpeed;
    }

    /**
     * Sets the speed of the marquee effect animation. Valid range is [1, 1000].
     * @param marqueeSpeed The speed of the marquee effect animation to set. Valid range is [1, 1000].
     */
    public void setMarqueeSpeed(final int marqueeSpeed) {
        this.marqueeSpeed = Math.min(VerticalMarqueeTextView.MAX_MARQUEE_SPEED, Math.max(VerticalMarqueeTextView.MIN_MARQUEE_SPEED, marqueeSpeed));
    }

    /**
     * Starts the marquee effect animation.
     */
    public void startMarquee() {
        this.marqueeStarted = true;
        this.marqueePaused  = false;

        new MarqueeAsyncTask().execute(this.textView);
    }

    /**
     * Stops the marquee effect animation.
     */
    public void stopMarquee() {
        this.marqueeStarted = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (this.marqueeStarted) {
            this.startMarquee();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        this.marqueePaused = true;
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        if (this.getChildCount() == 0) {
            return 0;
        }

        final int length  = this.getVerticalFadingEdgeLength();
        final int scrollY = this.textView.getScrollY();

        if (scrollY < length) {
            return scrollY / (float)length;
        }

        return 1;
    }

    private void init(final Context context, final AttributeSet attrs) {
        this.textView = new TextView(context);
        this.textView.setGravity(Gravity.CENTER);
        this.addView(this.textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.textView.scrollTo(0, -this.getHeight());

        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalFadingEdgeEnabled(false);
        this.setVerticalFadingEdgeEnabled(true);
        this.setFadingEdgeLength(30);

        if (attrs != null) {
            final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VerticalMarqueeTextView, 0, 0);

            this.textView.setText(array.getText(R.styleable.VerticalMarqueeTextView_text));

            final int textRes = array.getResourceId(R.styleable.VerticalMarqueeTextView_text, 0);
            if (textRes > 0) {
                this.textView.setText(array.getText(textRes));
            }

            this.textView.setTextColor(array.getColor(R.styleable.VerticalMarqueeTextView_textColor, context.getResources().getColor(android.R.color.primary_text_light)));

            final int textColorRes = array.getResourceId(R.styleable.VerticalMarqueeTextView_textColor, 0);
            if (textColorRes > 0) {
                this.textView.setTextColor(context.getResources().getColor(textColorRes));
            }

            final float textSize = array.getDimension(R.styleable.VerticalMarqueeTextView_textSize, 0);
            if (textSize > 0) {
                this.textView.setTextSize(textSize);
            }

            final int textSizeRes = array.getResourceId(R.styleable.VerticalMarqueeTextView_textSize, 0);
            if (textSizeRes > 0) {
                this.textView.setTextSize(context.getResources().getDimension(textSizeRes));
            }

            final int typeface = array.getInt(R.styleable.VerticalMarqueeTextView_typeface, 0);
            this.textView.setTypeface(typeface == 1 ? Typeface.SANS_SERIF : typeface == 2 ? Typeface.SERIF : typeface == 3 ? Typeface.MONOSPACE : Typeface.DEFAULT, array.getInt(R.styleable.VerticalMarqueeTextView_textStyle, Typeface.NORMAL));

            final int textAppearance = array.getResourceId(R.styleable.VerticalMarqueeTextView_textAppearance, 0);
            if (textAppearance > 0) {
                this.textView.setTextAppearance(context, textAppearance);
            }

            this.setMarqueeSpeed(array.getInt(R.styleable.VerticalMarqueeTextView_marqueeSpeed, 0));

            final int marqueeSpeedRes = array.getResourceId(R.styleable.VerticalMarqueeTextView_marqueeSpeed, 0);
            if (marqueeSpeedRes > 0) {
                this.setMarqueeSpeed(context.getResources().getInteger(marqueeSpeedRes));
            }

            this.autoStartMarquee = array.getBoolean(R.styleable.VerticalMarqueeTextView_autoStartMarquee, true);

            if (this.autoStartMarquee) {
                this.marqueeStarted = true;
            }

            array.recycle();
        }
    }

    private final class MarqueeAsyncTask extends AsyncTask<TextView, Void, Void> {
        @Override
        protected Void doInBackground(final TextView... params) {
            final Activity activity = (Activity)params[0].getContext();
            final Runnable runnable = new MarqueeRunnable((ViewGroup)params[0].getParent(), params[0]);

            while (VerticalMarqueeTextView.this.marqueeStarted && !VerticalMarqueeTextView.this.marqueePaused) {
                activity.runOnUiThread(runnable);

                try {
                    Thread.sleep((long)(1000d / VerticalMarqueeTextView.this.marqueeSpeed));
                } catch (final InterruptedException e) {
                }
            }

            return null;
        }
    }

    private final class MarqueeRunnable implements Runnable {
        private final ViewGroup parent;
        private final TextView  textView;

        /**
         * Creates a new instance of {@link MarqueeRunnable}.
         * @param parent The parent container that hosts the specific {@link TextView}.
         * @param textView The {@link TextView} to apply marquee effect.
         */
        public MarqueeRunnable(final ViewGroup parent, final TextView textView) {
            this.parent   = parent;
            this.textView = textView;
        }

        @Override
        public void run() {
            final int height       = this.heightOf(this.textView);
            final int parentHeight = this.parent.getHeight();

            if (height > 0 && parentHeight > 0 && height > parentHeight) {
                if (this.textView.getScrollY() >= height) {
                    this.textView.scrollTo(0, -parentHeight);
                } else {
                    this.textView.scrollBy(0, 1);
                }

                this.textView.invalidate();
            }
        }

        /**
         * Returns the standard height (i.e. without text effects such as shadow) of all the text of
         * the given {@link TextView}.
         * @param textView The {@link TextView} to determine the height of all its text content.
         * @return The standard height of all the text content of the given {@link TextView}.
         */
        private int heightOf(final TextView textView) {
            return textView.getLineCount() > 0 ? textView.getLineHeight() * textView.getLineCount() : 0;
        }
    }
}
