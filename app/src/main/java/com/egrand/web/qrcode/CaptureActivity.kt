package com.egrand.web.qrcode

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.text.TextUtils
import android.view.*
import android.view.SurfaceHolder.Callback
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.egrand.web.R
import com.egrand.web.qrcode.camera.CameraManager
import com.egrand.web.qrcode.decoding.BitmapLuminanceSource
import com.egrand.web.qrcode.decoding.CaptureActivityHandler
import com.egrand.web.qrcode.decoding.DecodeFormatManager
import com.egrand.web.qrcode.decoding.InactivityTimer
import com.egrand.web.qrcode.view.ViewfinderView
import com.egrand.web.view.BaseFragmentDialog
import com.egrand.web.view.ScanResultDialog
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.io.IOException
import java.util.*


class CaptureActivity : FragmentActivity(), Callback {
    /**
     * When the beep has finished playing, rewind to queue up another one.
     * 当扫码声音播放就把进度置零等待下一次扫码。
     */
    private val beepListener = OnCompletionListener { mediaPlayer -> mediaPlayer.seekTo(0) }
    private var handler: CaptureActivityHandler? = null
    var viewfinderView: ViewfinderView? = null
        private set
    private var surfaceView: SurfaceView? = null
    private var hasSurface: Boolean = false
    private var decodeFormats: Vector<BarcodeFormat>? = null
    private var characterSet: String? = null
    private var inactivityTimer: InactivityTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var on: Boolean = false
    var cameraManager: CameraManager? = null
        private set
    private var playBeep: Boolean = false
    // private static final float BEEP_VOLUME = 0.10f;
    private var vibrate: Boolean = false

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        /*
         * this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
         * WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
         *
         * RelativeLayout layout = new RelativeLayout(this);
         * layout.setLayoutParams(new
         * ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
         * LayoutParams.FILL_PARENT));
         *
         * this.surfaceView = new SurfaceView(this); this.surfaceView
         * .setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
         * LayoutParams.FILL_PARENT));
         *
         * layout.addView(this.surfaceView);
         *
         * this.viewfinderView = new ViewfinderView(this);
         * this.viewfinderView.setBackgroundColor(0x00000000);
         * this.viewfinderView.setLayoutParams(new
         * ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
         * LayoutParams.FILL_PARENT)); layout.addView(this.viewfinderView);
         *
         * TextView status = new TextView(this); RelativeLayout.LayoutParams
         * params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
         * LayoutParams.WRAP_CONTENT);
         * params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
         * params.addRule(RelativeLayout.CENTER_HORIZONTAL);
         * status.setLayoutParams(params);
         * status.setBackgroundColor(0x00000000);
         * status.setTextColor(0xFFFFFFFF); status.setText("请将条码置于取景框内扫描。");
         * status.setTextSize(14.0f);
         *
         * layout.addView(status); setContentView(layout);
         */

        setContentView(R.layout.activity_capture)
        surfaceView = findViewById(R.id.surfaceView)
        viewfinderView = findViewById(R.id.viewFinderView)

        surfaceView!!.setOnClickListener {
            toggleFlashlight()
        }

        // 保持屏幕常亮
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        hasSurface = false
        inactivityTimer = InactivityTimer(this)
    }

    private fun toggleFlashlight() {
        if (this.on) {
            this.cameraManager?.offFlashLight()
            this.on = false
        } else {
            this.cameraManager?.openFlashLight()
            this.on = true
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()
        //调整为竖屏
        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        cameraManager = CameraManager(application)

        val width = (resources.displayMetrics.widthPixels * 0.6).toInt()

        cameraManager!!.setManualFramingRect(width, width)

        viewfinderView!!.setCameraManager(cameraManager!!)

        val surfaceHolder = surfaceView!!.holder
        if (hasSurface) {
            initCamera(surfaceHolder)
        } else {
            surfaceHolder.addCallback(this)
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }
        decodeFormats = null
        characterSet = null

        playBeep = true
        val audioService = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioService != null && audioService.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false
        }
        initBeepSound()
        vibrate = false
    }

    /**
     * 应用暂停时清理handler，关闭cameraManager
     */
    override fun onPause() {
        super.onPause()

        this.cameraManager?.offFlashLight()
        this.on = false

        if (handler != null) {
            handler!!.quitSynchronously()
            handler = null
        }
        cameraManager?.closeDriver()
    }

    override fun onDestroy() {
        inactivityTimer!!.shutdown()
        super.onDestroy()
    }

    /**
     * 初始化相机
     *
     * @param surfaceHolder
     */
    private fun initCamera(surfaceHolder: SurfaceHolder) {
        try {
            // CameraManager.get().openDriver(surfaceHolder);
            cameraManager!!.openDriver(surfaceHolder)
        } catch (ioe: IOException) {
            return
        } catch (e: RuntimeException) {
            return
        }

        if (handler == null) {
            handler = CaptureActivityHandler(this, decodeFormats, characterSet)
        }
    }

    /**
     * 表面改变完成时触发
     */
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!hasSurface) {
            hasSurface = true
            initCamera(holder)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        hasSurface = false
    }

    fun getHandler(): Handler? {
        return handler
    }

    fun drawViewfinder() {
        viewfinderView!!.drawViewfinder()
    }

    fun handleDecode(result: Result, barcode: Bitmap?) {
        inactivityTimer!!.onActivity()
        playBeepSoundAndVibrate()
        showResult(result, barcode)
    }

    /**
     * 显示扫码结果
     *
     * @param rawResult
     * @param barcode
     */
    private fun showResult(rawResult: Result?, barcode: Bitmap?) {
        if (rawResult != null && !TextUtils.isEmpty(rawResult.text)) {
            val text = rawResult.text
            val dialog = ScanResultDialog(text,
                View.OnClickListener {
                    val uri = Uri.parse(text)
                    val intent = Intent()
                    intent.putExtra("content", text)
                    intent.putExtra("barcode", barcode)
                    if ("http" == uri.scheme || "https" == uri.scheme) {
                        intent.putExtra("url", text)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                },
                View.OnClickListener {
                    restartPreviewAfterDelay(0L)
                })
            dialog.setDismissListener(object : BaseFragmentDialog.LocalDismissListener {
                override fun onDismiss(dialog: DialogInterface) {
                    restartPreviewAfterDelay(0L)
                }
            })
            dialog.show(supportFragmentManager, "ScanResultDialog")
        } else {
            val toast = Toast.makeText(this, "扫描不到任何东西！", Toast.LENGTH_SHORT)
            toast.show()
            restartPreviewAfterDelay(0L)
        }
    }

    fun restartPreviewAfterDelay(delayMS: Long) {
        if (handler != null) {
            handler!!.sendEmptyMessageDelayed(MessageIDs.restart_preview, delayMS)
        }
    }

    /***
     * 初始化扫码声音player
     */
    private fun initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            volumeControlStream = AudioManager.STREAM_MUSIC
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer!!.setOnCompletionListener(beepListener)
            try {
                //                AssetFileDescriptor fileDescriptor = getAssets().openFd("qrbeep.ogg");
                val fileDescriptor = assets.openFd("beep.mp3")
                this.mediaPlayer!!.setDataSource(fileDescriptor.fileDescriptor, fileDescriptor.startOffset, fileDescriptor.length)
                this.mediaPlayer!!.setVolume(0.1f, 0.1f)
                this.mediaPlayer!!.prepare()
            } catch (e: IOException) {
                this.mediaPlayer = null
            }

        }
    }

    /**
     * 播放声音和震动
     */
    private fun playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer!!.start()
        }
        if (vibrate) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VIBRATE_DURATION)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return true
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 解析图片
     *
     * @param bitmap
     * @return
     */
    private fun decode(bitmap: Bitmap): Result? {
        val multiFormatReader = MultiFormatReader()
        // 解码的参数
        val hints = Hashtable<DecodeHintType, Any>(2)
        // 可以解析的编码类型
        var decodeFormats = Vector<BarcodeFormat>()
        decodeFormats = Vector()
        // 这里设置可扫描的类型，我这里选择了都支持
        decodeFormats.addAll(DecodeFormatManager.PRODUCT_FORMATS)
        decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS)
        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS)
        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS)
        hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
        // 设置继续的字符编码格式为UTF8
        // hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
        // 设置解析配置参数
        multiFormatReader.setHints(hints)
        // 开始对图像资源解码
        var rawResult: Result? = null
        try {
            rawResult = multiFormatReader.decodeWithState(BinaryBitmap(HybridBinarizer(BitmapLuminanceSource(bitmap))))
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }

        return rawResult
    }

    companion object {
        val QR_RESULT = "RESULT"
        /**
         * 震动时长
         */
        private val VIBRATE_DURATION = 200L
    }
}
