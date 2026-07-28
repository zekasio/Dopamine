package com.dopamine.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import android.os.Build
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import org.intellij.lang.annotations.Language

@Language("AGSL")
private const val LiquidGlassShader = """
uniform shader content;
uniform float2 size;
uniform float distortionAmount;

half4 main(float2 coord) {
    float2 center = size / 2.0;
    float2 diff = coord - center;
    float dist = length(diff);
    float maxDist = min(size.x, size.y) / 2.0;
    
    // Lens distortion
    float2 uv = coord;
    if (dist < maxDist) {
        float factor = 1.0 - (dist / maxDist);
        uv = coord - diff * factor * distortionAmount;
    }
    
    // Chromatic aberration
    float2 rUv = uv - diff * 0.02 * distortionAmount;
    float2 bUv = uv + diff * 0.02 * distortionAmount;
    
    half4 baseColor = content.eval(uv);
    half4 rColor = content.eval(rUv);
    half4 bColor = content.eval(bUv);
    
    return half4(rColor.r, baseColor.g, bColor.b, baseColor.a);
}
"""

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSegmentSelected: (Int) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val runtimeShader = android.graphics.RuntimeShader(LiquidGlassShader)
                    runtimeShader.setFloatUniform("size", size.width, size.height)
                    runtimeShader.setFloatUniform("distortionAmount", 0.15f)
                    renderEffect = android.graphics.RenderEffect.createRuntimeShaderEffect(
                        runtimeShader,
                        "content"
                    ).asComposeRenderEffect()
                }
            }
            .hazeChild(state = hazeState, shape = RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
            .padding(6.dp)
    ) {
        val segmentWidth = maxWidth / items.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(stiffness = 300f, dampingRatio = 0.7f),
            label = "segmentedIndicator"
        )

        // Sliding indicator pill (The white glowing glass part)
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp), spotColor = Color(0xFF00E5FF))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.15f)) 
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSegmentSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
