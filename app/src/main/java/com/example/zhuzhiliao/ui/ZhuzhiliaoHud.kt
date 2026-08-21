package com.example.zhuzhiliao.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhuzhiliao.domain.PlayState
import com.example.zhuzhiliao.domain.ZhuzhiliaoState
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.ButtonDefaults
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text

private val HudSurface = Color(0xFF172027) // design-style: fixed-figma-color opaque-HUD
private val HudText = Color(0xFFF8F2DE) // design-style: fixed-figma-color opaque-HUD
private val HudMuted = Color(0xFFB9C7C3) // design-style: fixed-figma-color opaque-HUD
private val HudAccent = Color(0xFFE7A83E) // design-style: fixed-figma-color single-accent
private val HudStop = Color(0xFFDB6B5D) // design-style: fixed-figma-color status

@Composable
fun ZhuzhiliaoHud(
    state: ZhuzhiliaoState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit
) {
    // Keep the controls in one shallow strip so the complete attachment stays
    // inside the volumetric window instead of being clipped at the bottom.
    Row(
        modifier = Modifier
            .size(width = 640.dp, height = 100.dp)
            .background(HudSurface, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Column(modifier = Modifier.width(104.dp)) {
            Text(
                text = "竹知了",
                color = HudText,
                style = PicoTheme.typography.headlineLarge.copy(fontSize = 22.sp)
            )
            Text(
                text = if (state.playState == PlayState.AUTO) "自动甩动" else "待机",
                color = HudMuted,
                style = PicoTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Row(
            modifier = Modifier.width(170.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Stat(label = "哇数", value = "%03d".format(state.wahCount), modifier = Modifier.width(76.dp))
            Stat(
                label = "转速",
                value = "%.1f 圈/秒".format(state.angularVelocity.let { kotlin.math.abs(it) / (2f * Math.PI.toFloat()) }),
                modifier = Modifier.width(82.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onStart,
                modifier = Modifier.width(96.dp),
                size = ButtonDefaults.Regular,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HudAccent,
                    contentColor = Color(0xFF1B160D) // design-style: fixed-figma-color opaque-HUD
                )
            ) {
                Text(text = "自动甩动", style = PicoTheme.typography.labelLarge.copy(fontSize = 14.sp))
            }
            Button(
                onClick = onStop,
                modifier = Modifier.width(96.dp),
                size = ButtonDefaults.Regular,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HudStop,
                    contentColor = HudText
                )
            ) {
                Text(text = "停下来", style = PicoTheme.typography.labelLarge.copy(fontSize = 14.sp))
            }
            Button(
                onClick = onReset,
                modifier = Modifier.width(80.dp),
                size = ButtonDefaults.Regular,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HudSurface,
                    contentColor = HudText
                )
            ) {
                Text(text = "重置", style = PicoTheme.typography.labelLarge.copy(fontSize = 14.sp))
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = HudMuted,
            style = PicoTheme.typography.labelMedium.copy(fontSize = 13.sp)
        )
        Text(
            text = value,
            color = HudText,
            style = PicoTheme.typography.titleLarge.copy(fontSize = 16.sp),
            textAlign = TextAlign.Start
        )
    }
}
