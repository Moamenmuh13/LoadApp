package com.udacity

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.udacity.ButtonState.*
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var bgColor: Int = Color.BLACK
    private var textColor: Int = Color.BLACK // default color

    private var widthSize = 0
    private var heightSize = 0

    private var btnTxt: String? = "Download"

    @Volatile
    private var progressWidth: Double = 0.0
    private var progressCircle: Double = 0.0

    private var valueAnimator = ValueAnimator()

    var buttonState: ButtonState by Delegates.observable<ButtonState>(Completed) { p, old, new ->
        when (new) {
            Clicked -> {
                btnTxt = "Clicked"
                invalidate()
            }
            Loading -> {
                btnTxt = resources.getString(R.string.button_loading)
                valueAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat())
                valueAnimator.duration = 5000
                valueAnimator.addUpdateListener { animation ->
                    progressWidth = (animation.animatedValue as Float).toDouble()
                    progressCircle = (widthSize.toFloat() / 365) * progressWidth
                    invalidate()
                }
                valueAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        progressWidth = 0.0
                        if (buttonState == Loading) {
                            buttonState = Loading
                        }
                    }
                })
                valueAnimator.start()
            }
            Completed -> {
                valueAnimator.cancel()
                progressWidth = 0.0
                progressCircle = 0.0
                btnTxt = resources.getString(R.string.download)
                invalidate()
            }
        }
    }
    private val updateListener = ValueAnimator.AnimatorUpdateListener {
        progressWidth = (it.animatedValue as Float).toDouble()

        invalidate()
        requestLayout()
    }

//    fun hasCompletedDownload() {
//        // cancel the animation when file is downloaded
//        valueAnimator.cancel()
//        btnTxt = "Download"
//        buttonState = Completed
//        invalidate()
//        requestLayout()
//    }

    init {
        isClickable = true
        valueAnimator = AnimatorInflater.loadAnimator(
            context, R.animator.loading_animation
        ) as ValueAnimator

        valueAnimator.addUpdateListener(updateListener)

        val attr = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            0,
            0
        )
        try {
            bgColor = attr.getColor(
                R.styleable.LoadingButton_bgColor,
                ContextCompat.getColor(context, R.color.colorPrimary)
            )

            textColor = attr.getColor(
                R.styleable.LoadingButton_textColor,
                ContextCompat.getColor(context, R.color.colorPrimary)
            )
        } finally {
            attr.recycle()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == Completed) buttonState = Loading
        animation()
        return true
    }

    private fun animation() {
        valueAnimator.start()
    }

    private val rect = RectF(
        740f,
        50f,
        810f,
        110f
    )


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.strokeWidth = 0f
        paint.color = bgColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        when (buttonState) {
            Loading -> {
                paint.color = Color.parseColor("#004349")
                drawBackgroundProgress(canvas)
                drawCircleProgress(canvas)
                btnTxt = resources.getString(R.string.loading)
            }
            Completed -> {
                buttonState = Completed
                btnTxt = resources.getString(R.string.download)
            }
            Clicked -> {
                buttonState = Clicked
                btnTxt = resources.getString(R.string.download)
            }
        }
        drawText(canvas)
    }

    private fun drawText(canvas: Canvas) {
        paint.color = textColor
        canvas.drawText(
            btnTxt!!, (width / 2).toFloat(), ((height + 30) / 2).toFloat(),
            paint
        )
    }

    private fun drawBackgroundProgress(canvas: Canvas) {
        canvas.drawRect(
            0f, 0f,
            (width * (progressWidth / 100)).toFloat(), height.toFloat(), paint
        )
    }

    private fun drawCircleProgress(canvas: Canvas) {
        paint.color = Color.parseColor("#F9A825")
        canvas.drawArc(rect, 0f, (360 * (progressWidth / 100)).toFloat(), true, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}