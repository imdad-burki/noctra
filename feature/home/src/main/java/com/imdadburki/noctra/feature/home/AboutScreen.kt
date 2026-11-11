package com.imdadburki.noctra.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imdadburki.noctra.core.designsystem.NoctraColors

/**
 * About — simple, as ordered. No profile, no creator account, no settings.
 * Just what the app is.
 */
@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoctraColors.Bg)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "NOCTRA",
            color = NoctraColors.Gold,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Time Beyond Ordinary",
            color = NoctraColors.Secondary, fontSize = 14.sp
        )
        Spacer(Modifier.height(28.dp))
        Text(
            "150 different clocks and 70 wallpapers — everything on your phone, offline.",
            color = NoctraColors.Text, fontSize = 16.sp, textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Set the Live Wallpaper and the clock keeps ticking on your home screen. " +
                "A static wallpaper is just a picture — it doesn't move.",
            color = NoctraColors.Secondary, fontSize = 14.sp, textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(32.dp))
        Text("Version 2.0.0", color = NoctraColors.Muted, fontSize = 13.sp)
        Text("Clocks · Wallpapers · Live Wallpaper", color = NoctraColors.Muted, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Text("No account. No internet. No ads.", color = NoctraColors.Muted, fontSize = 13.sp)
    }
}
