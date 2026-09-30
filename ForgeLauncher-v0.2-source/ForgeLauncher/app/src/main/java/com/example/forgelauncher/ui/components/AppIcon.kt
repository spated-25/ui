package com.example.forgelauncher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgelauncher.domain.model.LaunchableApp
import com.example.forgelauncher.ui.glass.GlassSurface

@Composable
fun AppIcon(
    app: LaunchableApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val image = remember(app.icon) { app.icon.asImageBitmap() }
    GlassSurface(
        modifier = modifier,
        onClick = onClick,
        onClickLabel = "Open ${app.label}",
        shadowElevation = if (compact) 5.dp else 7.dp,
        blurStrengthMultiplier = 0.72f,
        contentPadding = PaddingValues(
            horizontal = if (compact) 4.dp else 6.dp,
            vertical = if (compact) 6.dp else 8.dp,
        ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 7.dp),
        ) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.size(if (compact) 46.dp else 50.dp),
            )
            Text(
                text = app.label,
                color = LocalContentColor.current,
                fontSize = if (compact) 10.sp else 11.sp,
                lineHeight = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
