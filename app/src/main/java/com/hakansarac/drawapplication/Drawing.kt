package com.hakansarac.drawapplication

import android.content.Context
import android.graphics.* //multiple class from there
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class Drawing(context : Context, attrs : AttributeSet) : View(context, attrs) {

    private var mDrawPath : CustomPath? = null
    private var mCanvasBitmap : Bitmap? = null
    private var mDrawPaint : Paint? = null
    private var mCanvasPaint : Paint? = null
    private var mBrushSize : Float = 0.toFloat()
    private var mColor = Color.BLACK
    private var mCanvas : Canvas? = null
    private val mPaths = ArrayList<CustomPath>() //list is immutable but its elements can be changed

    init{
        setUpDrawing()
    }

    private fun setUpDrawing(){
        mDrawPath = CustomPath(mColor,mBrushSize)
        mDrawPaint = Paint()
        mDrawPaint!!.color = mColor
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 20.toFloat()
    }

    //override onSizeChanged function of View Class
    //set canvas bitmap
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //w: width
        //h: height
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mCanvasBitmap!!)
    }

    //override onDraw function of View Class
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint) //0f, 0f -> top left corner

        for(path in mPaths){
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas?.drawPath(path,mDrawPaint!!)
        }

        if(!mDrawPath!!.isEmpty){       //only if that's the case then draw something. onTouchEvent will fill the mDrawPath.
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!,mDrawPaint!!)
            //how thick the paint should be
        }
    }

    //override onTouchEvent function of View Class
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        //event can have multiple actions
        //action down: we get with our finger on the screen
        //action move: once we drag our finger over the screen
        //action up: the moment when we release the touch
        when(event?.action){

            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = mColor
                mDrawPath!!.brushThickness = mBrushSize
                //how thick the path is.

                mDrawPath!!.reset()
                if(touchX != null && touchY != null)
                    mDrawPath!!.moveTo(touchX,touchY)
            }

            MotionEvent.ACTION_MOVE -> {
                if(touchX != null && touchY != null)
                    mDrawPath!!.lineTo(touchX,touchY)
            }

            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(mColor,mBrushSize)
            }
            else -> return false
        }
        invalidate()    //Invalidate the whole view.
        return true
        //return super.onTouchEvent(event)
    }

    internal inner class CustomPath(var color : Int, var brushThickness: Float) : Path(){

    }
}