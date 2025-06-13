package org.wbftw.weil.chinese_keyboard

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.xr.compose.platform.LocalHasXrSpatialFeature
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.platform.LocalSpatialConfiguration
import androidx.xr.compose.spatial.EdgeOffset
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SpatialRoundedCornerShape
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.width
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.wbftw.weil.chinese_keyboard.converter.JsonConverter
import org.wbftw.weil.chinese_keyboard.input.ime.InputMethodAssociativeWordManager
import org.wbftw.weil.chinese_keyboard.input.ime.InputMethodCandidateManager
import org.wbftw.weil.chinese_keyboard.input.ime.InputMethodPathOptimizer
import org.wbftw.weil.chinese_keyboard.input.ime.PersonalizedScoreManager
import org.wbftw.weil.chinese_keyboard.input.utils.CandidateClass
import org.wbftw.weil.chinese_keyboard.loader.DictionaryLoader
import org.wbftw.weil.chinese_keyboard.ui.theme.Chinese_keyboardTheme
import java.io.File

// JavaScript Bridge
@Suppress("unused", "MemberVisibilityCanBePrivate")
class InputMethodBridge(
    private val context: Context,
) {

    private val TAG = "InputMethodBridge"
    val pageConfigure = CandidateClass.PageConfigure(
        perPageSize = 9,         // 每頁顯示的候選字數量
        maxCandidatesPerPath = 3 // 每條路徑的候選字數量限制
    )
    val rootNode = DictionaryLoader.loadDictionary(context)
    val defaultResponse = """"{"words":[], "chars": [], "hasNextPage": false}"""
    var engine: InputMethodCandidateManager? = null
    var optimizer: InputMethodPathOptimizer? = null
    var wordAssist: InputMethodAssociativeWordManager? = null
    var nextPossiblePath: List<Char>? = listOf()

    init {
        rootNode?.let {
            engine = InputMethodCandidateManager(it, pageConfigure)
            optimizer = InputMethodPathOptimizer(it)
            wordAssist = InputMethodAssociativeWordManager(context, it)
        }
    }

    /**
     * 取得當前頁的候選字列表 JSON 格式
     * @param input 用戶輸入的字符串，例如 "e04"
     * @return 候選字列表的 JSON 字符串
     */
    @JavascriptInterface
    fun getCandidatesFromInput(input: String): String {
        engine?.let {
            nextPossiblePath = it.setInput(input)
            val result = it.getCurrentPageCandidates()
            val hasNextPage = it.hasNextPage()
            return JsonConverter.candidatesResultToJson(
                result,
                nextPossiblePath,
                hasNextPage
            )
        }
        return defaultResponse
    }

    @JavascriptInterface
    fun getNextCandidates(): String {
        engine?.let {
            val result = it.getNextPageCandidates()
            val hasNextPage = it.hasNextPage()
            return JsonConverter.candidatesResultToJson(
                result,
                nextPossiblePath,
                hasNextPage
            )
        }
        return defaultResponse
    }

    @JavascriptInterface
    fun getPrevCandidates(): String {
        engine?.let {
            val result = it.getPrevPageCandidates()
            val hasNextPage = it.hasNextPage()
            return JsonConverter.candidatesResultToJson(
                result,
                nextPossiblePath,
                hasNextPage
            )
        }
        return defaultResponse
    }

    /**
     * 推廣候選字對應的路徑
     * @param candidatePairJson 候選字對象的 JSON 字符串，例如 {"w": "幹", "p": "e04"}
     */
    @JavascriptInterface
    fun setPromoteCandidate(candidatePairJson: String) {
        optimizer?.let {
            val candidatePair = JsonConverter.parseCandidatePairFromJson(candidatePairJson)
            if (candidatePair != null) {
                it.optimizePath(candidatePair)
                Log.d(TAG, "Promoted candidate pair: $candidatePairJson")
            } else {
                Log.e(TAG, "Failed to parse candidate pair from JSON: $candidatePairJson")
            }
        } ?: run {
            Log.e(TAG, "Optimizer is not initialized.")
        }
    }

    @JavascriptInterface
    fun getAssociativeWords(input: String): String {
        wordAssist?.let {
            val words = it.getAssociativeWords(input)
            return JsonConverter.charListToJson(words)
        }
        return "[]"
    }

    @JavascriptInterface
    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("輸入文字", text)
        clipboard.setPrimaryClip(clip)
        Log.d(TAG, "Copied to clipboard: $text")
    }

    @JavascriptInterface
    fun getClipboardText(): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val item = clip.getItemAt(0)
            val text = item.text.toString()
            Log.d(TAG, "Clipboard text: $text")
            return text
        } else {
            Log.d(TAG, "Clipboard is empty.")
            return ""
        }
    }

    @JavascriptInterface
    fun log(message: String) {
        Log.d(TAG, "JavaScript Log: $message")
    }
}

class MainActivity : ComponentActivity() {

    private val autoSaveHandler = Handler(Looper.getMainLooper())
    private lateinit var autoSaveRunnable: Runnable

    private fun startAutoSaveTask() {
        autoSaveRunnable = object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.IO).launch {
                    if (PersonalizedScoreManager.getValueChanged()){
                        Log.d("AutoSave", "Saving personalized scores to JSON")
                        try{
                            val jsonString = PersonalizedScoreManager.saveToJson()
                            val file = File(filesDir, "personalized_scores.json")
                            file.writeText(jsonString)
                            PersonalizedScoreManager.setValueChanged(false)
                            Log.d("AutoSave", "Personalized scores saved successfully")
                        }catch (e: Exception) {
                            Log.e("AutoSave", "Error saving personalized scores: ${e.message}")
                            e.printStackTrace()
                        }
                    } else {
                        Log.d("AutoSave", "No changes detected, skipping save")
                    }
                }
                autoSaveHandler.postDelayed(this, 10000L)
            }
        }
        autoSaveHandler.postDelayed(autoSaveRunnable, 10_000L)
    }

    private fun readPersonalizedScoresFromFile() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(filesDir, "personalized_scores.json")
                if (file.exists()) {
                    val jsonString = file.readText()
                    PersonalizedScoreManager.loadFromJson(jsonString)
                    Log.d("AutoSave", "Personalized scores loaded successfully")
                } else {
                    Log.d("AutoSave", "No personalized scores file found, using defaults")
                }
            } catch (e: Exception) {
                Log.e("AutoSave", "Error loading personalized scores: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 全螢幕沉浸式模式：隱藏狀態列和導覽列
        window.decorView.windowInsetsController?.let { controller ->
            controller.hide(WindowInsets.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // 這行可避免狀態列影響佈局
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            Chinese_keyboardTheme {
                val spatialConfiguration = LocalSpatialConfiguration.current
                if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                    Subspace {
                        MySpatialContent(
                            onRequestHomeSpaceMode = spatialConfiguration::requestHomeSpaceMode
                        )
                    }
                } else {
                    My2DContent(onRequestFullSpaceMode = spatialConfiguration::requestFullSpaceMode)
                }
            }
        }

        startAutoSaveTask()
        readPersonalizedScoresFromFile()

    }

    override fun onDestroy() {
        autoSaveHandler.removeCallbacks(autoSaveRunnable)
        super.onDestroy()
    }

    // ============ End of MainActivity ============
}

@SuppressLint("RestrictedApi")
@Composable
fun MySpatialContent(onRequestHomeSpaceMode: () -> Unit) {
    SpatialPanel(SubspaceModifier.width(1280.dp).height(400.dp).resizable().movable()) {
        Surface (
            color = androidx.compose.ui.graphics.Color.Transparent
        ) {
            InputMethodWebView(
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Orbiter(
            position = OrbiterEdge.Top,
            offset = EdgeOffset.inner(offset = 20.dp),
            alignment = Alignment.End,
            shape = SpatialRoundedCornerShape(CornerSize(28.dp))
        ) {
            HomeSpaceModeIconButton(
                onClick = onRequestHomeSpaceMode,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun My2DContent(onRequestFullSpaceMode: () -> Unit) {
    Surface (
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.fillMaxSize().height(400.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InputMethodWebView(
                modifier = Modifier
                    .weight(1f)
            )
            if (LocalHasXrSpatialFeature.current) {
                FullSpaceModeIconButton(
                    onClick = onRequestFullSpaceMode,
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun InputMethodWebView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    //val engine = remember { InputMethodEngine() }
    //val bridge = remember { InputMethodBridge(context, engine) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                // 設定WebView背景透明
                setBackgroundColor(Color.TRANSPARENT)
                background = null

                // 啟用硬體加速（XR需要）
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                settings.apply {
                    // 支援透明度混合
                    setRenderPriority(WebSettings.RenderPriority.HIGH)
                    cacheMode = WebSettings.LOAD_NO_CACHE

                    // XR相關設定
                    allowFileAccess = true
                    allowContentAccess = true
                    domStorageEnabled = true
                }

                addJavascriptInterface(
                    InputMethodBridge(context),
                    "InputMethodBridge"
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // 頁面載入完成後的初始化
                    }
                }

                // 載入 HTML
                loadUrl(getInputKeyboardUrl())

                // 使 webview size 適應螢幕
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

        }
    )
}

fun getInputKeyboardUrl(): String {
    // get file from assets
    return "file:///android_asset/keyboard-ui/keyboard.html"
}

@Composable
fun FullSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_full_space_mode_switch),
            contentDescription = stringResource(R.string.switch_to_full_space_mode)
        )
    }
}

@Composable
fun HomeSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_home_space_mode_switch),
            contentDescription = stringResource(R.string.switch_to_home_space_mode)
        )
    }
}

@PreviewLightDark
@Composable
fun My2dContentPreview() {
    Chinese_keyboardTheme {
        My2DContent(onRequestFullSpaceMode = {})
    }
}

@Preview(showBackground = true)
@Composable
fun FullSpaceModeButtonPreview() {
    Chinese_keyboardTheme {
        FullSpaceModeIconButton(onClick = {})
    }
}

@PreviewLightDark
@Composable
fun HomeSpaceModeButtonPreview() {
    Chinese_keyboardTheme {
        HomeSpaceModeIconButton(onClick = {})
    }
}