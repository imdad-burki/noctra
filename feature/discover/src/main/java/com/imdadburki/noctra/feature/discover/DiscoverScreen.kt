package com.imdadburki.noctra.feature.discover

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imdadburki.noctra.core.clock.ClockTime
import com.imdadburki.noctra.core.clock.StaticClock
import com.imdadburki.noctra.core.common.ClockFace
import com.imdadburki.noctra.core.common.ClockKind
import com.imdadburki.noctra.core.common.Collections
import com.imdadburki.noctra.core.common.DigitalStyle
import com.imdadburki.noctra.core.common.WallpaperDesign
import com.imdadburki.noctra.core.common.builtInDesigns
import com.imdadburki.noctra.core.database.DatabaseProvider
import com.imdadburki.noctra.core.database.WallpaperEntity
import com.imdadburki.noctra.core.designsystem.NoctraButton
import com.imdadburki.noctra.core.designsystem.NoctraColors
import com.imdadburki.noctra.core.designsystem.SectionTitle
import com.imdadburki.noctra.core.wallpaper.ApplyTarget
import com.imdadburki.noctra.core.wallpaper.WallpaperExporter
import kotlinx.coroutines.launch

private fun WallpaperDesign.toEntity(favorite: Boolean, downloaded: Boolean): WallpaperEntity =
    WallpaperEntity(
        id = id, title = title, collection = collection, creator = creator,
        backgroundHex = backgroundHex, backgroundHex2 = backgroundHex2,
        gradientAngle = gradientAngle, clockKind = clockFace.kind.name,
        digitalStyle = clockFace.digitalStyle, analogStyle = clockFace.analogStyle,
        is24Hour = clockFace.is24Hour, showSeconds = clockFace.showSeconds,
        colorHex = clockFace.colorHex, accentHex = clockFace.accentHex,
        clockScale = clockScale, isFavorite = favorite, isDownloaded = downloaded,
        isMyDesign = false, createdAtMs = System.currentTimeMillis(),
        lastViewedMs = System.currentTimeMillis()
    )

/**
 * Wallpapers v2: 70 built-in designs with live previews. Favourite toggles
 * persist to Room; the details sheet applies the design for real.
 *
 * Two kinds of wallpaper, labelled honestly:
 *  - LIVE: the NOCTRA live wallpaper — the selected clock keeps ticking.
 *  - STATIC: a picture of the clock at one moment — it does NOT tick.
 * (v1 only offered static, which is why the clock "ruk jata tha".)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen() {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.get(context) }
    val scope = rememberCoroutineScope()
    val favorites by db.wallpaperDao().getFavorites().collectAsState(initial = emptyList())
    val favoriteIds = favorites.map { it.id }.toSet()
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<WallpaperDesign?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    val all = remember { builtInDesigns() }
    val shown = if (query.isBlank()) all else all.filter {
        it.title.contains(query, ignoreCase = true) || it.collection.contains(query, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoctraColors.Bg)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text("Wallpapers", color = NoctraColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "70 designs · The clock ticks in Live wallpaper, not in Static",
            color = NoctraColors.Secondary, fontSize = 13.sp
        )
        Spacer(Modifier.height(12.dp))
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search wallpapers") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        status?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = NoctraColors.Gold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(shown, key = { it.id }) { design ->
                WallpaperCard(
                    design = design,
                    isFavorite = design.id in favoriteIds,
                    onFavorite = {
                        scope.launch {
                            val existing = db.wallpaperDao().getById(design.id)
                            if (existing != null) {
                                db.wallpaperDao().update(existing.copy(isFavorite = !existing.isFavorite))
                            } else {
                                db.wallpaperDao().insert(design.toEntity(favorite = true, downloaded = false))
                            }
                        }
                    },
                    onOpen = { selected = design }
                )
            }
        }
    }

    val design = selected
    if (design != null) {
        ModalBottomSheet(
            onDismissRequest = { selected = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = NoctraColors.Surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StaticClock(design.clockFace, ClockTime.now(), size = 220.dp)
                Spacer(Modifier.height(8.dp))
                Text(design.title, color = NoctraColors.Text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${design.collection} · by ${design.creator}",
                    color = NoctraColors.Secondary, fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Want it LIVE? Tap the button below — the clock will keep ticking. " +
                        "Static is just a picture — it doesn't move.",
                    color = NoctraColors.Secondary, fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
                NoctraButton("Set Live Wallpaper — clock keeps ticking") {
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
                    selected = null
                }
                Spacer(Modifier.height(8.dp))
                NoctraButton("Set as home wallpaper (static picture)") {
                    scope.launch {
                        val bitmap = WallpaperExporter.renderBitmap(design, 1080, 1920, ClockTime.now())
                        val result = WallpaperExporter.apply(context, bitmap, ApplyTarget.HOME)
                        status = result.fold(
                            onSuccess = { "Home wallpaper set ✓ (static — the clock won't move)" },
                            onFailure = { "Could not set wallpaper: ${it.message}" }
                        )
                        selected = null
                    }
                }
                Spacer(Modifier.height(8.dp))
                NoctraButton("Set home + lock (static picture)") {
                    scope.launch {
                        val bitmap = WallpaperExporter.renderBitmap(design, 1080, 1920, ClockTime.now())
                        val result = WallpaperExporter.apply(context, bitmap, ApplyTarget.BOTH)
                        status = result.fold(
                            onSuccess = { "Home + lock wallpaper set ✓ (static — the clock won't move)" },
                            onFailure = { "Could not set wallpaper: ${it.message}" }
                        )
                        selected = null
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun WallpaperCard(
    design: WallpaperDesign,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NoctraColors.Surface),
        modifier = Modifier.clickable(onClick = onOpen)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Live preview: the real clock engine at a big size.
            StaticClock(design.clockFace, ClockTime.now(), size = 150.dp)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(design.title, color = NoctraColors.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(design.collection, color = NoctraColors.Secondary, fontSize = 12.sp)
                }
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (isFavorite) NoctraColors.Gold else NoctraColors.Secondary
                    )
                }
            }
        }
    }
}
