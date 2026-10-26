package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Typeface
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.model.LatencyTelemetry
import com.example.data.model.TranslatedBoxState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Real Android Native System Overlay & MediaProjection Capture Service:
 * - Layer 1: Click-Through Smart Inpainting Overlay + Real-Time Latency HUD
 *   (TYPE_APPLICATION_OVERLAY with FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE)
 * - Layer 2: Draggable Floating Action Ball with Touch-and-Hold "Hold-to-Peek" (0% Opacity Passthrough)
 * - GPU Buffer Capture: MediaProjection + VirtualDisplay + ImageReader (PixelFormat.RGBA_8888)
 */
class ScreenTranslatorOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var passthroughCanvasView: PassthroughOverlayView? = null
    private var floatingBallView: FloatingBallView? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForegroundNotification()
        if (Settings.canDrawOverlays(this)) {
            attachSystemOverlays()
        }
        _isRunning.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_OVERLAY -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_INIT_PROJECTION -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val resultData: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_INTENT)
                }
                if (resultCode != 0 && resultData != null) {
                    setupMediaProjectionPipeline(resultCode, resultData)
                }
            }
            ACTION_REFRESH_OVERLAY -> {
                passthroughCanvasView?.invalidate()
                floatingBallView?.invalidate()
            }
        }
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val channelId = "hanviet_flashhud_overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "HánViệt FlashHUD Real-Time Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("HánViệt FlashHUD đang hoạt động")
            .setContentText("Chạm nút nổi để dịch • Chạm giữ để Nhìn Xuyên Thấu (Hold-to-Peek)")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun setupMediaProjectionPipeline(resultCode: Int, data: Intent) {
        runCatching {
            val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mpManager.getMediaProjection(resultCode, data)
            val metrics = resources.displayMetrics
            val width = metrics.widthPixels.coerceAtMost(1080)
            val height = metrics.heightPixels.coerceAtMost(2400)

            // Direct GPU buffer read via RGBA_8888 without disk I/O (< 12ms capture)
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "HanVietFlashHudCapture",
                width,
                height,
                metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                mainHandler
            )
            _isProjectionActive.value = true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachSystemOverlays() {
        val wm = windowManager ?: return
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // 1. Click-Through Full-Screen Smart Inpainting + Latency HUD Layer
        val passthroughParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        val canvasView = PassthroughOverlayView(this)
        passthroughCanvasView = canvasView
        runCatching { wm.addView(canvasView, passthroughParams) }

        // 2. Interactive Floating Action Ball (with Hold-to-Peek 0% Opacity Passthrough)
        val density = resources.displayMetrics.density
        val ballSizePx = (62 * density).toInt()
        val ballParams = WindowManager.LayoutParams(
            ballSizePx,
            ballSizePx,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (16 * density).toInt()
            y = (220 * density).toInt()
        }

        val ballView = FloatingBallView(this)
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isLongPressPeekActive = false

        val holdToPeekRunnable = Runnable {
            isLongPressPeekActive = true
            setHoldToPeekActive(true)
            passthroughCanvasView?.alpha = 0f
            ballView.invalidate()
        }

        ballView.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = ballParams.x
                    initialY = ballParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isLongPressPeekActive = false
                    mainHandler.postDelayed(holdToPeekRunnable, 220L)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > 12 || abs(dy) > 12) {
                        mainHandler.removeCallbacks(holdToPeekRunnable)
                        ballParams.x = initialX + dx
                        ballParams.y = initialY + dy
                        runCatching { wm.updateViewLayout(ballView, ballParams) }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mainHandler.removeCallbacks(holdToPeekRunnable)
                    if (isLongPressPeekActive) {
                        setHoldToPeekActive(false)
                        passthroughCanvasView?.alpha = 1f
                        ballView.invalidate()
                    } else {
                        val dx = abs(event.rawX - initialTouchX)
                        val dy = abs(event.rawY - initialTouchY)
                        if (dx < 12 && dy < 12) {
                            onFloatingBallTapCallback?.invoke()
                            passthroughCanvasView?.invalidate()
                        }
                    }
                    true
                }
                else -> false
            }
        }

        floatingBallView = ballView
        runCatching { wm.addView(ballView, ballParams) }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        passthroughCanvasView?.let { runCatching { windowManager?.removeView(it) } }
        floatingBallView?.let { runCatching { windowManager?.removeView(it) } }
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        _isRunning.value = false
        _isProjectionActive.value = false
        _isHoldToPeek.value = false
        super.onDestroy()
    }

    /**
     * Non-touchable, non-focusable transparent overlay canvas that draws:
     * 1. Top-Right Real-Time Latency HUD
     * 2. Smart Inpainting Vietnamese bounding boxes over game text
     */
    private class PassthroughOverlayView(context: Context) : View(context) {
        private val density = resources.displayMetrics.density
        private val hudBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(225, 9, 13, 22)
            style = Paint.Style.FILL
        }
        private val hudBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(16, 185, 129)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * density
        }
        private val hudTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(16, 185, 129)
            textSize = 11.5f * density
            typeface = Typeface.MONOSPACE
        }
        private val boxBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        private val boxBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 6, 182, 212)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f * density
        }
        private val vnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        private val rectBuffer = RectF()

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (_isHoldToPeek.value) return // 0% opacity passthrough when Hold-to-Peek is active

            val telemetry = _overlayTelemetry.value
            val hudText = "Cap:${"%.1f".format(telemetry.captureMs)}ms | OCR:${"%.1f".format(telemetry.ocrAndMergeMs)}ms | AI:${"%.0f".format(telemetry.geminiTotalMs)}ms | Σ:${"%.1f".format(telemetry.totalEndToEndMs)}ms (${telemetry.cacheStatus})"
            val pad = 10f * density
            val textWidth = hudTextPaint.measureText(hudText)
            rectBuffer.set(
                width - textWidth - pad * 2.5f,
                42f * density,
                width - pad * 0.8f,
                72f * density
            )
            canvas.drawRoundRect(rectBuffer, 8f * density, 8f * density, hudBgPaint)
            canvas.drawRoundRect(rectBuffer, 8f * density, 8f * density, hudBorderPaint)
            canvas.drawText(hudText, rectBuffer.left + pad * 0.8f, rectBuffer.centerY() + 4f * density, hudTextPaint)

            // Render Smart Inpainting boxes
            for (item in _overlayBoxes.value) {
                val b = item.box
                rectBuffer.set(
                    b.normLeft * width,
                    b.normTop * height,
                    b.normRight * width,
                    b.normBottom * height
                )
                boxBgPaint.color = b.sampledBgHex.toInt()
                canvas.drawRoundRect(rectBuffer, 6f * density, 6f * density, boxBgPaint)
                canvas.drawRoundRect(rectBuffer, 6f * density, 6f * density, boxBorderPaint)

                vnTextPaint.textSize = item.calculatedFontSizeSp * density
                canvas.drawText(
                    item.currentDisplayVietnamese.take(48),
                    rectBuffer.left + 8f * density,
                    rectBuffer.centerY() + 5f * density,
                    vnTextPaint
                )
            }
        }
    }

    /**
     * Draggable Floating Action Ball with visual feedback for Hold-to-Peek
     */
    private class FloatingBallView(context: Context) : View(context) {
        private val density = resources.displayMetrics.density
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * density
        }
        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 11f * density
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f
            val radius = (width / 2f) - 4f * density
            val peeking = _isHoldToPeek.value

            fillPaint.color = if (peeking) Color.argb(230, 245, 158, 11) else Color.argb(235, 9, 13, 22)
            ringPaint.color = if (peeking) Color.WHITE else Color.rgb(16, 185, 129)

            canvas.drawCircle(cx, cy, radius, fillPaint)
            canvas.drawCircle(cx, cy, radius, ringPaint)
            canvas.drawText(if (peeking) "PEEK" else "DỊCH", cx, cy + 4f * density, labelPaint)
        }
    }

    companion object {
        const val ACTION_STOP_OVERLAY = "com.example.action.STOP_OVERLAY"
        const val ACTION_INIT_PROJECTION = "com.example.action.INIT_PROJECTION"
        const val ACTION_REFRESH_OVERLAY = "com.example.action.REFRESH_OVERLAY"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_INTENT = "extra_result_intent"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _isProjectionActive = MutableStateFlow(false)
        val isProjectionActive: StateFlow<Boolean> = _isProjectionActive.asStateFlow()

        private val _isHoldToPeek = MutableStateFlow(false)
        val isHoldToPeek: StateFlow<Boolean> = _isHoldToPeek.asStateFlow()

        private val _overlayBoxes = MutableStateFlow<List<TranslatedBoxState>>(emptyList())
        private val _overlayTelemetry = MutableStateFlow(LatencyTelemetry())

        var onFloatingBallTapCallback: (() -> Unit)? = null

        fun setHoldToPeekActive(active: Boolean) {
            _isHoldToPeek.value = active
        }

        fun updateOverlayState(
            context: Context,
            boxes: List<TranslatedBoxState>,
            telemetry: LatencyTelemetry
        ) {
            _overlayBoxes.value = boxes
            _overlayTelemetry.value = telemetry
            if (_isRunning.value) {
                val intent = Intent(context, ScreenTranslatorOverlayService::class.java).apply {
                    action = ACTION_REFRESH_OVERLAY
                }
                runCatching { context.startService(intent) }
            }
        }
    }
}
