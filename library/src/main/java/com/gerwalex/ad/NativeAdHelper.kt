package com.gerwalex.ad

import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.delay
import kotlin.time.Duration

/**
 * Lädt eine NativeAd und zeigt sie an.
 * @param adUnitId Die ID des Ads.
 * @param modifier Der Modifier.
 * @param onError Wird aufgerufen, wenn die Load fehlschlägt.
 */
@Composable
fun NativeAd(
    adUnitId: String,
    modifier: Modifier,
    adOptions: NativeAdOptions = NativeAdOptions.Builder()
//        .setRequestCustomMuteThisAd(true)
        .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT)
        .build(),
    onError: (LoadAdError) -> Unit, delay: Duration
) {
    var ad by remember { mutableStateOf<NativeAd?>(null) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                ad = nativeAd
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    onError(adError)
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
    DisposableEffect(key1 = ad) {
        val currentAd = ad
        onDispose {
            currentAd?.destroy()
        }
    }
    ad?.let {
        MyNativeAdView(ad = it, delay = delay) { ad, view ->
            LoadAdContent(ad, view, modifier)
        }
    }
}


@Composable
fun MyNativeAdView(
    ad: NativeAd,
    delay: Duration,
    adContent: @Composable (ad: NativeAd, contentView: View) -> Unit,
) {
    val contentViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val adViewId by remember { mutableIntStateOf(View.generateViewId()) }
    var isAdVisible by remember { mutableStateOf(false) }
    LaunchedEffect(delay) {
        delay(delay)
        isAdVisible = true
    }
    AnimatedVisibility(isAdVisible) {
        AndroidView(
            factory = { context ->
                val contentView = ComposeView(context).apply {
                    id = contentViewId
                }
                NativeAdView(context).apply {
                    id = adViewId
                    addView(contentView)
                }
            },
            update = { view ->
                val adView = view.findViewById<NativeAdView>(adViewId)
                val contentView = view.findViewById<ComposeView>(contentViewId)
                adView.setNativeAd(ad)
                adView.callToActionView = contentView
                contentView.setContent { adContent(ad, contentView) }
            }
        )
    }
}

@Composable
private fun LoadAdContent(nativeAd: NativeAd, composeView: View, modifier: Modifier) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .decoderFactory(SvgDecoder.Factory())
            .data("https://storage.googleapis.com/support-kms-prod/B39986E0A9616888E3CB26D02451EC166BAD")
            .size(Size.ORIGINAL) // Set the target size to load the image at.
            .build()
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .heightIn(min = 50.dp)
            .clip(CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
        ) {
            Row(
                modifier = Modifier
            ) {
                Column(
                    modifier = Modifier
                        .width(56.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Spacer(
                        modifier = Modifier.height(30.dp)
                    )
                    nativeAd.icon?.drawable?.let { drawable ->
                        Image(
                            painter = rememberAsyncImagePainter(model = drawable),
                            contentDescription = "Ad"/*it.icon?.contentDescription*/,
                            modifier = Modifier
                                .size(40.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = nativeAd.advertiser ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(
                        modifier = Modifier.height(30.dp)
                    )
                    Text(
                        text = nativeAd.headline ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = nativeAd.body ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    nativeAd.callToAction?.let { cta ->
                        TextButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.End),
                            onClick = {
                                composeView.performClick()
                            },
                            content = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End,
                                    text = cta.uppercase(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        )
                    }
                }
            }
            Image(
                painter = painter,
                modifier = Modifier
//                    .padding(start = 8.dp)
                    .size(15.dp),
                contentDescription = null
            )


        }
    }
}

