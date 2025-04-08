//package com.example.picktimeapp.util
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.util.AttributeSet
//import android.view.View
//import androidx.core.content.ContextCompat
//import com.example.picktimeapp.R
//import com.google.mediapipe.tasks.vision.core.RunningMode
//import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
//import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
//import kotlin.math.max
//import kotlin.math.min
//
//class MediapipeOverlayView(context: Context?, attrs: AttributeSet?) :
//    View(context, attrs) {
//
//    private var results: HandLandmarkerResult? = null
//    private var linePaint = Paint()
//    private var pointPaint = Paint()
//
//    private var scaleFactor: Float = 1f
//    private var imageWidth: Int = 1
//    private var imageHeight: Int = 1
//
//    init {
//        initPaints()
//    }
//
//    fun clear() {
//        results = null
//        linePaint.reset()
//        pointPaint.reset()
//        invalidate()
//        initPaints()
//    }
//
//    private fun initPaints() {
//        linePaint.color =Color.YELLOW
//        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
//        linePaint.style = Paint.Style.STROKE
//
//        pointPaint.color = Color.YELLOW
//        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
//        pointPaint.style = Paint.Style.FILL
//    }
//
//    override fun draw(canvas: Canvas) {
//        super.draw(canvas)
//        results?.let { handLandmarkerResult ->
//            for (landmark in handLandmarkerResult.landmarks()) {
//                for (normalizedLandmark in landmark) {
//                    canvas.drawPoint(
//                        (1 - normalizedLandmark.x()) * imageWidth * scaleFactor, // 👈 좌우반전
//                        normalizedLandmark.y() * imageHeight * scaleFactor,
//                        pointPaint
//                    )
//                }
//
//                // 선 그리는거
////                HandLandmarker.HAND_CONNECTIONS.forEach {
////                    canvas.drawLine(
////                        landmark.get(it!!.start())
////                            .x() * imageWidth * scaleFactor,
////                        landmark.get(it.start())
////                            .y() * imageHeight * scaleFactor,
////                        landmark.get(it.end())
////                            .x() * imageWidth * scaleFactor,
////                        landmark.get(it.end())
////                            .y() * imageHeight * scaleFactor,
////                        linePaint
////                    )
////                }
//            }
//        }
//    }
//
//    fun setResults(
//        handLandmarkerResults: HandLandmarkerResult,
//        imageHeight: Int,
//        imageWidth: Int,
//        runningMode: RunningMode = RunningMode.IMAGE
//    ) {
//        results = handLandmarkerResults
//
//        this.imageHeight = imageHeight
//        this.imageWidth = imageWidth
//
//        scaleFactor = when (runningMode) {
//            RunningMode.IMAGE,
//            RunningMode.VIDEO -> {
//                min(width * 1f / imageWidth, height * 1f / imageHeight)
//            }
//            RunningMode.LIVE_STREAM -> {
//                // PreviewView is in FILL_START mode. So we need to scale up the
//                // landmarks to match with the size that the captured images will be
//                // displayed.
//                max(width * 1f / imageWidth, height * 1f / imageHeight)
//            }
//        }
//        invalidate()
//    }
//
//    companion object {
//        private const val LANDMARK_STROKE_WIDTH = 8F
//    }
//}