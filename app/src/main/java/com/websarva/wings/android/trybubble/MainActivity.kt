package com.websarva.wings.android.trybubble

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.os.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import java.security.SecureRandom
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate


class MainActivity : AppCompatActivity() {
    companion object {
        private var preBubble = 0
        private var count = 0
        private var pointX = 0f
        private var pointY = 0f
        private var set: AnimatorSet = AnimatorSet()
        private val handler = Handler(Looper.getMainLooper())
        private var stopButtonBoolean = false
        private var limit0 = 0
        private var limit1 = 0
        private var limit2 = 0
        private var limitChecker = 0
        private var count0 = 0
        private var count1 = 0
        private var count2 = 0
        private var allCount = 0
        private var boolean = true
        private var animation = ObjectAnimator()
        private var animator = ObjectAnimator()
        private var viewIdList: MutableList<Int> = mutableListOf()
        private var viewIdAndAnimationMap: MutableMap<Int,ObjectAnimator> = mutableMapOf()
        private var viewIdAndAnimationSetMap: MutableMap<Int, AnimatorSet> = mutableMapOf()
        private var timer = Timer()
        private var time:Long = 0
        private var animationX = ObjectAnimator()
        private var scaleAnimation = ObjectAnimator.ofPropertyValuesHolder()
        private var preImageViewId = 0
        private lateinit var timerLineButton: CountDownTimer
        private var remainingTime:Long = 10000
        private var lineButtonOn = 0
        private var frameLayoutHeight = 0
        private var maxDuration = 10000
        private var middleDuration = 7000
        private var minimumDuration = 3000
        private var noStartCount = 1
    }

    //TODO 非同期処理にする
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.startButton)
        button.setOnClickListener(StartButtonListener(100.0f))

        val stopButton = findViewById<Button>(R.id.stop)
        stopButton.setOnClickListener(StopButtonListener())

        val bombButton = findViewById<Button>(R.id.bombButton)
        bombButton.setOnClickListener(BombButtonListener())

        val lineButton = findViewById<Button>(R.id.lineButton)
        lineButton.setOnClickListener(LineButtonListener(10000))

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.Navy)
        hideSystemUI()

        limit0 = 2
        limit1 = 2
        limit2 = 4
        limitChecker = limit0 + limit1 + limit2

    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    override fun onPause() {
        super.onPause()
        preBubble = 0
        count = 0
        pointX = 0f
        pointY = 0f
        count0 = 0
        count1 = 0
        count2 = 0
        set = AnimatorSet()
        stopButtonBoolean = false
        allCount = 0
        boolean = true
        Log.d("onPause","onPause")
    }
    
    //TODO ストップ解除後勝手に動くのを解決する->durationの問題をどうにかする
    private inner class StopButtonListener : View.OnClickListener {
        private var count = 0
        private var innerTime: Long = 0
        override fun onClick(v: View?) {
            Log.d("viewIdList","$viewIdList,$viewIdAndAnimationMap")
            val stopButton = findViewById<Button>(R.id.stop)
            val bombButton = findViewById<Button>(R.id.bombButton)
            val lineButton = findViewById<Button>(R.id.lineButton)
            val startButton = findViewById<Button>(R.id.startButton)
            count++
            if(count%2 == 0){
                bombButton.isEnabled = true
                lineButton.isEnabled = true
                startButton.isEnabled = true
                if(noStartCount == 0) {
                    stopButtonBoolean = true
                    boolean = true
                    stopButton.text = getString(R.string.stop)
                    for(viewId in viewIdList) {
                        val animation = viewIdAndAnimationMap[viewId]
                        animation?.resume()
                        val animationSet = viewIdAndAnimationSetMap[viewId]
                        animationSet?.resume()
                        Log.d("time", "$innerTime,${time}")
                    }
                    timer = Timer()
                    if(lineButtonOn == 1) {
                        lineButton(remainingTime)
                        lineButtonOn = 0
                    }
                    supportProduceBubble(100f, innerTime)
                }
            } else {
                bombButton.isEnabled = false
                lineButton.isEnabled = false
                startButton.isEnabled = false
                if(noStartCount == 0) {
                    boolean = false
                    innerTime = time
                    stopButton.text = "リスタート"
                    for(viewId in viewIdList) {
                        val animation = viewIdAndAnimationMap[viewId]
                        animation?.pause()
                        val animator = viewIdAndAnimationSetMap[viewId]
                        animator?.pause()
                    }
                    timer.cancel()
                    timer.purge()
                    if ( lineButtonOn == 1) {
                        timerLineButton.cancel()
                    }
                }
            }
        }
    }

    private inner class StartButtonListener(private val transY: Float): View.OnClickListener {
        override fun onClick(v: View?) {
            noStartCount = 0
            val startButton = findViewById<Button>(R.id.startButton)
            startButton.isEnabled = false
            supportProduceBubble(transY, 0)
        }
    }

    private inner class BombButtonListener: View.OnClickListener {
        override fun onClick(v: View?) {
            Log.d("viewIdList", "$viewIdList")
            for(viewId in viewIdList) {
                val imageView = findViewById<ImageView>(viewId)
                val animation = viewIdAndAnimationSetMap[viewId]
                animation?.cancel()
                val layout = findViewById<FrameLayout>(R.id.frameLayout)
                layout.removeView(imageView)
            }
        }
    }

    private inner class LineButtonListener(private val st: Long): View.OnClickListener{
        override fun onClick(v: View?) {
            lineButton(st)
        }
    }

    private fun lineButton(st: Long) {
        lineButtonOn = 1
        val button = findViewById<Button>(R.id.lineButton)
        timerLineButton = object :CountDownTimer(st,10) {
            override fun onTick(millisUntilFinished: Long) {
                for(viewId in viewIdList) {
                    button.text = millisUntilFinished.toString()
                    remainingTime = millisUntilFinished
                    val imageView = findViewById<ImageView>(viewId)
                    val layout = findViewById<FrameLayout>(R.id.frameLayout)
                    if(imageView != null) {
                        if((imageView.y >= 500) && (imageView.y <= 600)){
                            Log.d("height","${imageView.y}")
                            handler.post{
                                val animation = viewIdAndAnimationSetMap[viewId]
                                animation?.cancel()
                                layout.removeView(imageView)
                            }
                        }
                    }
                }
            }
            override fun onFinish() {
                lineButtonOn = 0
                button.text = "終わりました"
            }
        }
        Thread{
            timerLineButton.start()
        }.start()
    }

    private fun supportProduceBubble(transY:Float,x:Long){
        timer.scheduleAtFixedRate(x, 1000) {
            Thread{
                val timer2 = Timer()
                timer2.scheduleAtFixedRate(0,1){
                    time++
                    if(time >= 1000){
                        time -= 1000
                    }
                }
            }.start()

            if (allCount >= limitChecker) {
                Log.d("cancel", "cancel")
                this.cancel()
            } else if (boolean) {
                allCount++
                produceBubble(transY)
            }
        }
    }

    //Timer().scheduleAtFixedRate(0,3000) {
    //                if(timerCount == timerCount/2 ){
    //                    this.cancel()
    //                    allCount = 0
    //                } else{
    //                    if(allCount < limitChecker) {
    //                        allCount ++
    //                        Log.d("limitChecker", "$limitChecker")
    //                        produceBubble(transY)
    //                    }
    //                }
    //            }

    //Thread{
    //                val exec: ScheduledExecutorService =
    //                    Executors.newSingleThreadScheduledExecutor()
    //                val a = Runnable {time++ }
    //                exec.scheduleAtFixedRate(a, 0, 1, TimeUnit.MILLISECONDS)
    //            }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        //X軸の取得
        pointX = event.x

        //Y軸の取得
        pointY = event.y

        //取得した内容をログに表示
        Log.d("point", "${pointX},${pointY} ")
        return true
    }


    class CustomImageView(context: Context?, attrs: AttributeSet?) :
        ShapeableImageView(context,attrs) {
        override fun performClick(): Boolean {
            super.performClick()
            return true
        }
    }

    private fun produceBubble(transY: Float){
        Thread { // Handlerを使用してメイン(UI)スレッドに処理を依頼する
            handler.post {
                val imageView = CustomImageView(this@MainActivity, null)
                imageView.shapeAppearanceModel = ShapeAppearanceModel().toBuilder().setAllCornerSizes(
                    ShapeAppearanceModel.PILL
                ).build()
                imageView.setImageResource(R.drawable.bubble3)
                val layout = findViewById<FrameLayout>(R.id.frameLayout)
                layout.addView(imageView)
                val layoutParams = imageView.layoutParams as FrameLayout.LayoutParams
                layoutParams.setMargins(0, 0, 0, 0)
                imageView.layoutParams = layoutParams
                imageView.setOnTouchListener(TouchListener())
                imageView.z = 20f

                var randomNumber1 = 0
                while(allCount <= limitChecker) {
                    val secureRandom = SecureRandom()
                    randomNumber1 = secureRandom.nextInt(3)
                    Log.d("randomNumber", "$randomNumber1")
                    when(randomNumber1) {
                        0 -> if(count0 < limit0) {
                            count0 ++
                            Log.d("count0", "$count0")
                            break
                        }
                        1 -> if(count1 < limit1) {
                            count1 ++
                            Log.d("count1", "$count1")
                            break
                        }
                        2 -> if (count2 < limit2) {
                            count2++
                            break
                        }
                    }
                }

                when (randomNumber1) {
                    0 -> {
                        imageView.layoutParams.height = 370
                        imageView.layoutParams.width = 370
                        imageView.scaleX = 1.0f
                        imageView.scaleY = 1.0f}
                    1 -> {
                        imageView.layoutParams.height = 370
                        imageView.layoutParams.width = 370
                        imageView.scaleX = 0.7f
                        imageView.scaleY = 0.7f
                    }
                    2 -> {
                        imageView.layoutParams.height = 370
                        imageView.layoutParams.width = 370
                        imageView.scaleX = 0.4f
                        imageView.scaleY = 0.4f
                    }
                    else -> {
                    }
                }
                imageView.alpha = when (randomNumber1) {
                    0 -> 0.8f
                    1 -> 0.6f
                    2 -> 0.3f
                    else -> 0f
                }

                val randomNumber2 = SecureRandom().nextInt(5)
                val imageViewWidthOneFifth = (layout.width - imageView.layoutParams.width) / 5
                val randomX = when (randomNumber2) {
                    0 -> (0..(imageViewWidthOneFifth)).random().toFloat()
                    1 -> (imageViewWidthOneFifth..2 * imageViewWidthOneFifth).random().toFloat()
                    2 -> (2 * imageViewWidthOneFifth..3 * imageViewWidthOneFifth).random().toFloat()
                    3 -> (3 * imageViewWidthOneFifth..4 * imageViewWidthOneFifth).random().toFloat()
                    4 -> (4 * imageViewWidthOneFifth..5 * imageViewWidthOneFifth).random().toFloat()
                    else -> 0f
                }
                imageView.x = randomX
                val y = layout.height.toFloat()
                imageView.y = y

                var number = View.generateViewId()
                var preNumber = 0
                if (count >= 1) {
                    imageView.id = number
                    while (imageView.id == preNumber) {
                        number = View.generateViewId()
                        imageView.id = number
                        preNumber = number
                    }
                    imageView.id = number
                    preNumber = number
                    Log.d("preNumber1", "$preNumber")
                } else {
                    imageView.id = number
                    preNumber = number
                    Log.d("preNumber2", "$preNumber")
                }
                count ++

                viewIdList.add(number)

                val randomTranslationX = (0..(layout.width-imageView.layoutParams.width)).random().toFloat()
                Log.d("randomTranslationX", "${(layout.width-imageView.width)},${layout.width},${imageView.layoutParams.width}")
                val translationY = PropertyValuesHolder.ofFloat(View.Y, transY)
                var specificDuration = 0
                when(imageView.scaleX) {
                    1.0f -> {specificDuration = maxDuration}
                    0.7f -> {specificDuration = middleDuration}
                    0.4f -> {specificDuration = minimumDuration}
                }
                animation =
                    ObjectAnimator.ofPropertyValuesHolder(imageView,translationY).apply {
                        duration = specificDuration.toLong()
                        interpolator = LinearInterpolator()
                    }
                val randomRotation = (-360..360).random().toFloat()
                val animator = ObjectAnimator.ofFloat(imageView, View.ROTATION, 0f, randomRotation).apply {
                    duration = specificDuration.toLong()
                }
                animation.addListener(BubbleDisappear(imageView))
                Log.d("count", "$count")

                imageView.setOnClickListener(EffectAfterTouchBubble())

                set= AnimatorSet()
                set.playTogether(animation,animator)
                set.start()
                viewIdAndAnimationMap[imageView.id] = animation
                viewIdAndAnimationSetMap[imageView.id] = set
            }
        }.start()
    }

    //TODO 大きさが小さくなる時にもアニメーションが適用できるようにする(不具合があるため）
    private inner class TouchListener: View.OnTouchListener {

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when(event?.action) {
                MotionEvent.ACTION_DOWN ->{
                    v?.performClick()
                }
            }
            return(true)
        }
    }

    private inner class TapBubbleDisappear(private val view: View): Animator.AnimatorListener {
        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationStart(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
            val layout = findViewById<FrameLayout>(R.id.frameLayout)
            layout.removeView(view)
        }

        override fun onAnimationRepeat(animation: Animator) {
        }
    }

    private inner class BubbleDisappear(private val view: View) :
        Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
        }
        override fun onAnimationEnd(animation: Animator) {
            val textView = findViewById<TextView>(R.id.textView)
            if(view.y == 100.0f){
                textView.text = getString(R.string.gameOver)
            }
        }
        override fun onAnimationCancel(animation: Animator) {
            if(view.scaleX==0.4f) {
                Thread{
                    viewIdList.remove(view.id)
                    viewIdAndAnimationMap.remove(view.id)
                    viewIdAndAnimationSetMap.remove(view.id)
                }.start()
                Log.d("animationCancel", "animationCANCEL")
            }
        }
        override fun onAnimationRepeat(animation: Animator) {}
    }


    //TODO タップしたときにはじけるアニメーションをつける
    private inner class EffectAfterTouchBubble: View.OnClickListener {
        override fun onClick(v: View) {
            val beforeScaleX = v.scaleX
            if(beforeScaleX == 0.4f) {
                viewIdList.remove(v.id)
            }
            fun scaleAnimation(Alpha: Float, Inflate: Float) {
                val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, v.alpha,Alpha)
                val inflateX = PropertyValuesHolder.ofFloat(View.SCALE_X, v.scaleX, Inflate)
                val inflateY = PropertyValuesHolder.ofFloat(View.SCALE_Y, v.scaleY, Inflate)
                scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(v,inflateX,inflateY,alpha).apply{
                    duration = 60
                    interpolator = DecelerateInterpolator()
                }
                val translationY = PropertyValuesHolder.ofFloat(View.Y, 100f)
                val layout = findViewById<FrameLayout>(R.id.frameLayout)
                frameLayoutHeight = layout.height
                val specificDuration = when (beforeScaleX) {
                    1.0f -> (middleDuration / (frameLayoutHeight-100f))*(v.y - 100f).toDouble()
                    0.7f -> (minimumDuration / (frameLayoutHeight-100f))*(v.y - 100f).toDouble()
                    else -> 0
                }

                if(beforeScaleX == 1.0f || beforeScaleX == 0.7f) {
                    animation =
                        ObjectAnimator.ofPropertyValuesHolder(v, translationY).apply {
                            duration = specificDuration.toLong()
                            interpolator = LinearInterpolator()
                        }
                    animation.addListener(BubbleDisappear(v))
                    viewIdAndAnimationMap.remove(v.id)
                    viewIdAndAnimationMap[v.id] = animation
                    if(v.rotation < 0) {
                        val randomRotation = (-360..0).random().toFloat()-v.rotation
                        animator = ObjectAnimator.ofFloat(v, View.ROTATION, v.rotation, randomRotation).apply {
                            duration = specificDuration.toLong()
                        }
                    }else{
                        val randomRotation = (0..360).random().toFloat()+v.rotation
                        animator = ObjectAnimator.ofFloat(v, View.ROTATION, v.rotation, randomRotation).apply {
                            duration = specificDuration.toLong()
                        }
                    }
                    set= AnimatorSet()
                    set.playTogether(animation,animator)
                    viewIdAndAnimationSetMap[v.id]?.cancel()
                    viewIdAndAnimationSetMap.remove(v.id)
                    viewIdAndAnimationSetMap[v.id] = set
                    set.start()
                }else{
                    viewIdAndAnimationMap[v.id]?.cancel()
                    scaleAnimation.addListener(TapBubbleDisappear(v))
                }
                scaleAnimation.start()
            }

            when(v.scaleX) {
                1.0f -> {
                    scaleAnimation(0.7f,0.7f)
                }
                0.7f -> {
                    scaleAnimation(0.4f,0.4f)
                }
                0.4f -> {
                    if(v.id != preImageViewId) {
                        preImageViewId = v.id
                        scaleAnimation(0.0f,0.8f)
                    }
                }
            }
        }
    }

    //v.updateLayoutParams { height = 130
    //width = 130}

    //val wc = RelativeLayout.LayoutParams.WRAP_CONTENT
    //            val lp = RelativeLayout.LayoutParams(wc,wc)
    //            imageView.layoutParams = lp

    //val randomNumber1 = (0..4).random()
    //            imageView.layoutParams.height = when (randomNumber1) {
    //                0 -> 750
    //                1 -> 600
    //                2 -> 450
    //                3 -> 300
    //                4 -> 150
    //                else -> 0
    //            }
    //            imageView.layoutParams.width = imageView.layoutParams.height
    //            imageView.alpha = when (randomNumber1) {
    //                0 -> 1.0f
    //                1 -> 0.8f
    //                2 -> 0.6f
    //                3 -> 0.4f
    //                4 -> 0.2f
    //                else -> 0f
    //            }
    //
    //            val randomNumber2 = (0..4).random()
    //            val imageViewWidthOneFifth = (layout.width - imageView.layoutParams.width) / 5
    //            val randomX = when (randomNumber2) {
    //                0 -> (0..(imageViewWidthOneFifth)).random().toFloat()
    //                1 -> (imageViewWidthOneFifth..2 * imageViewWidthOneFifth).random().toFloat()
    //                2 -> (2 * imageViewWidthOneFifth..3 * imageViewWidthOneFifth).random().toFloat()
    //                3 -> (3 * imageViewWidthOneFifth..4 * imageViewWidthOneFifth).random().toFloat()
    //                4 -> (4 * imageViewWidthOneFifth..5 * imageViewWidthOneFifth).random().toFloat()
    //                else -> return
    //            }
    //            imageView.x = randomX
    //            val y = layout.height.toFloat()
    //            imageView.y = y
}
