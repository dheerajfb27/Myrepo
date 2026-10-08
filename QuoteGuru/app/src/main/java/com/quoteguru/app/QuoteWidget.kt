package com.quoteguru.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import androidx.glance.unit.sp

class QuoteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val settings = QuotePrefs.flow(context)
                .collectAsState(initial = QuoteSettings())
                .value
            QuoteWidgetContent(settings)
        }
    }

    @Composable
    private fun QuoteWidgetContent(settings: QuoteSettings) {
        val drawable = when (settings.shape) {
            1 -> if (settings.dark) R.drawable.qg_widget_circle else R.drawable.qg_widget_light_circle
            2 -> if (settings.dark) R.drawable.qg_widget_rect else R.drawable.qg_widget_light_rect
            3 -> if (settings.dark) R.drawable.qg_widget_cut else R.drawable.qg_widget_light_cut
            else -> if (settings.dark) R.drawable.qg_widget_rounded else R.drawable.qg_widget_light_rounded
        }

        val textColor = if (settings.dark) {
            ColorProvider(R.color.qg_white)
        } else {
            ColorProvider(android.R.color.black)
        }

        Box(
            GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(drawable))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "“${settings.quote}”",
                    style = TextStyle(
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.size(8.dp))
                Text(
                    text = "— ${settings.author}",
                    style = TextStyle(
                        color = textColor,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
