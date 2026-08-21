package com.example.zhuzhiliao.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import com.example.zhuzhiliao.domain.ZhuzhiliaoEngine
import com.example.zhuzhiliao.audio.WahSoundPlayer
import com.example.zhuzhiliao.scene.ZhuzhiliaoScene
import com.example.zhuzhiliao.ui.ZhuzhiliaoHud
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.ui.foundation.content.SpatialView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val FRAME_DELAY_MS = 33L

@Composable
fun HomeVolume(modifier: Modifier = Modifier.fillMaxSize()) {
    val context = LocalContext.current
    val engine = remember { ZhuzhiliaoEngine() }
    val scene = remember { ZhuzhiliaoScene() }
    val sound = remember(context) { WahSoundPlayer(context) }
    var state by remember { mutableStateOf(engine.snapshot()) }

    LaunchedEffect(Unit) {
        var last = System.nanoTime()
        var lastRendered = engine.snapshot()
        var previousWahCount = state.wahCount
        while (isActive) {
            delay(FRAME_DELAY_MS)
            val now = System.nanoTime()
            val delta = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
            last = now
            val next = engine.advance(delta)
            if (next.wahCount > previousWahCount) {
                repeat(next.wahCount - previousWahCount) { sound.play() }
            }
            previousWahCount = next.wahCount
            // IDLE produces an identical state every tick. Avoid submitting
            // redundant ECS transforms while the toy is stationary.
            if (next != lastRendered) {
                state = next
                scene.render(next)
                lastRendered = next
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scene.dispose()
            sound.dispose()
        }
    }

    SpatialView(
        modifier = modifier,
        initial = { content, attachments ->
            scene.initialize(content)
            attachments.entity("zhuzhiliao_hud")?.apply {
                components[TransformComponent::class.java]?.setPosition(
                    // The shallow strip sits below and to the right of the
                    // toy, with enough bottom margin for the full attachment.
                    Vector3(0.15f, -0.25f, 0.07f)
                )
                components[TransformComponent::class.java]?.setScaleVector(Vector3(1.0f))
                content.addEntity(this)
            }
        },
        attachments = {
            AttachmentPanel(id = "zhuzhiliao_hud") {
                ZhuzhiliaoHud(
                    state = state,
                    onStart = { engine.startAuto() },
                    onStop = { engine.stop() },
                    onReset = { engine.reset() }
                )
            }
        }
    )
}
