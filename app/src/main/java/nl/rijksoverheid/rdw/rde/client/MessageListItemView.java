package nl.rijksoverheid.rdw.rde.client;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class MessageListItemView extends View
{
    //TODO remove these
    private String exampleString; // TODO: use a default from R.string...
    private int exampleColor = Color.RED; // TODO: use a default from R.color...
    private float exampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable exampleDrawable;

    private TextPaint textPaint;
    private float textWidth;
    private float textHeight;

    public MessageListItemView(final Context context)
    {
        super(context);
        init(null, 0);
    }

    public MessageListItemView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);

        if (attrs == null)
            throw new IllegalArgumentException();

        init(attrs, 0);
    }

    public MessageListItemView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(final AttributeSet attrs, final int defStyle)
    {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.MessageListItemView, defStyle, 0);

        exampleString = a.getString(
                R.styleable.MessageListItemView_exampleString);
        exampleColor = a.getColor(
                R.styleable.MessageListItemView_exampleColor,
                exampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        exampleDimension = a.getDimension(
                R.styleable.MessageListItemView_exampleDimension,
                exampleDimension);

        if (a.hasValue(R.styleable.MessageListItemView_exampleDrawable))
        {
            exampleDrawable = a.getDrawable(R.styleable.MessageListItemView_exampleDrawable);
            exampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        textPaint = new TextPaint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements()
    {
        textPaint.setTextSize(exampleDimension);
        textPaint.setColor(exampleColor);
        textWidth = textPaint.measureText(exampleString);

        final var fontMetrics = textPaint.getFontMetrics();
        textHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (canvas == null)
            throw new IllegalArgumentException();

        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();

        final int contentWidth = getWidth() - paddingLeft - paddingRight;
        final int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        canvas.drawText(exampleString,
                paddingLeft + (contentWidth - textWidth) / 2,
                paddingTop + (contentHeight + textHeight) / 2,
                textPaint);

        // Draw the example drawable on top of the text.
        if (exampleDrawable != null)
        {
            exampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            exampleDrawable.draw(canvas);
        }
    }
}