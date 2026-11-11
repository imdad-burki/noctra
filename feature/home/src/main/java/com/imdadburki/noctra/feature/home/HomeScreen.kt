package com.imdadburki.noctra.feature.home

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imdadburki.noctra.core.clock.NoctraClock
import com.imdadburki.noctra.core.common.clockFaceByName
import com.imdadburki.noctra.core.datastore.SettingsStore
import com.imdadburki.noctra.core.designsystem.NoctraButton
import com.imdadburki.noctra.core.designsystem.NoctraColors

/**
 * Home v2 — clean and big. One live hero clock (the user's selected
 * catalogue clock, full width), then two big doors: Clocks and Wallpapers,
 * plus the one-tap Live Wallpaper button.
 */
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val store = SettingsStore(context)
    val clockName by store.defaultClockName.collectAsState(initial = "Solid Standard Gold")
    val is24Hour by store.is24Hour.collectAsState(initial = false)
    val showSeconds by store.showSeconds.collectAsState(initial = true)
    val showDate by store.showDate.collectAsState(initial = true)

    val face = clockFaceByName(clockName).copy(
        is24Hour = is24Hour, showSeconds = showSeconds, showDate = showDate
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoctraColors.Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "NOCTRA",
            color = NoctraColors.Gold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 6.sp
        )
        Text(
            "Time Beyond Ordinary",
            color = NoctraColors.Muted, fontSize = 12.sp
        )
        Spacer(Modifier.height(20.dp))

        // Hero: the selected clock, LIVE and big.
        NoctraClock(face, size = 320.dp)
        Spacer(Modifier.height(12.dp))
        Text(
            face.displayName,
            color = NoctraColors.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            "Tap Clocks to change it",
            color = NoctraColors.Secondary, fontSize = 13.sp
        )
        Spacer(Modifier.height(24.dp))

        // The one-tap live wallpaper button — this is the LIVE clock on the
        // home screen (it keeps ticking). Static wallpapers do not tick.
        NoctraButton(
            "Set Live Wallpaper — the clock keeps ticking",
            modifier = Modifier.fillMaxWidth()
        ) {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(
                        context,
                        "com.imdadburki.noctra.service.livewallpaper.NoctraWallpaperService"
                    )
                )
            }
            context.startActivity(intent)
        }
        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = NoctraColors.Surface),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate("clocks") }
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("150 Clocks", color = NoctraColors.Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("Each one different", color = NoctraColors.Secondary, fontSize = 13.sp)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = NoctraColors.Surface),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate("wallpapers") }
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Wallpapers", color = NoctraColors.Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("70 designs", color = NoctraColors.Secondary, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Clock + Wallpaper — that's it. Nothing extra.",
            color = NoctraColors.Muted, fontSize = 12.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
    }
}
