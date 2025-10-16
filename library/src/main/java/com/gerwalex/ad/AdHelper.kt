package com.gerwalex.ad

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.gerwalex.library.modifier.conditional
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Composable function that displays a banner ad of a fixed size.
 *
 * This function creates a banner ad with the specified [AdSize] and [adUnitId].
 * It uses the Google Mobile Ads SDK to display the ad. The ad is contained within a [BoxWithConstraints]
 * which allows the ad to determine its size based on the available space.
 * The Box has a hairline border and padding. Its height animates when the ad loads or disappears.
 * Initially, the Box has a minimal height (hairline) and expands to the ad's height when the ad is visible.
 *
 * In preview mode (when `LocalInspectionMode.current` is true), it displays a placeholder
 * with a red background and "BannerAdView Here" text.
 *
 * When an ad is loaded, the composable animates its size and displays a small "Ad" label
 * above the banner. This label is positioned at the top-start of the Box and offset vertically
 * to appear slightly above the Box's border.
 *
 * @param adSize The size of the banner ad to display (e.g., [AdSize.BANNER], [AdSize.FULL_BANNER]).
 * @param adUnitId The ad unit ID for the banner ad.
 * @param modifier Optional [Modifier] to be applied to the ad container.
 * @param animationSpec The [FiniteAnimationSpec] to use for the content size animation when the ad loads.
 *                      Defaults to a low stiffness spring animation.
 */
@Composable
fun BannerAdView(
    adSize: AdSize,
    adUnitId: String,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<IntSize> = spring(stiffness = Spring.StiffnessLow),
) {
    val isInEditMode = LocalInspectionMode.current
    var isAdVisible by remember { mutableStateOf(isInEditMode) }
    val size by remember {
        derivedStateOf {
            when (adSize) {
                AdSize.BANNER -> DpSize(320.dp, 50.dp)
                AdSize.LARGE_BANNER -> DpSize(320.dp, 100.dp)
                AdSize.MEDIUM_RECTANGLE -> DpSize(300.dp, 250.dp)
                AdSize.FULL_BANNER -> DpSize(468.dp, 60.dp)
                AdSize.LEADERBOARD -> DpSize(728.dp, 90.dp)
                else -> throw IllegalArgumentException("Unsupported ad size: $adSize")
            }
        }
    }
    val builder = remember { AdRequest.Builder().build() }
    val borderColor = MaterialTheme.colorScheme.onSurface
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .animateContentSize(animationSpec)
            .padding(top = 4.dp)
            .conditional(
                condition = isAdVisible,
                ifTrue = {
                    height(size.height + 8.dp)
                    width(size.width + 4.dp)
                    wrapContentSize()
                    drawBehind {
                        drawRect(
                            color = borderColor,
                            style = Stroke(width = Dp.Hairline.toPx()) // Border stroke
                        )
                    }
                },
                ifFalse = { height(Dp.Hairline) },
            )
            .padding(2.dp),
    ) {
        if (isInEditMode) {
            Column(
                modifier = Modifier
                    .size(size)
                    .background(Color.Red),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    color = Color.White,
                    text = "BannerAdView Here",
                )
                Text(
                    color = Color.White,
                    text = "BannerAdView Here",
                )
            }
        } else {
            AndroidView(
                modifier = Modifier.align(Alignment.Center),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(adSize)
                        this.adUnitId = adUnitId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                isAdVisible = true
                            }
                        }
                        runCatching {
                            loadAd(builder)
                        }
                    }
                }
            )
        }
        if (isAdVisible) {
            Text(
                text = " ${stringResource(R.string.ad)} ",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.TopStart) // Positioniert den Text oben
                    .offset(y = (-10).dp) // <-- Schiebt den Text nach oben aus der Box heraus
                    .padding(start = 16.dp) // Kleiner horizontaler Puffer
                    .background(MaterialTheme.colorScheme.surface) // Hintergrund, um den Text hervorzuheben
            )
        }
    }
}

/**
 * Composable function that displays an adaptive banner ad.
 *
 * This function creates a banner ad that adapts its size to the available width and height.
 * It uses the Google Mobile Ads SDK to display the ad. The ad is contained within a [BoxWithConstraints]
 * which allows the ad to determine its size based on the available space.
 * The Box has a hairline border and padding. Its height animates when the ad loads or disappears.
 * Initially, the Box has a minimal height (hairline) and expands to `maxHeight` when the ad is visible.
 *
 * In preview mode (when `LocalInspectionMode.current` is true), it displays a placeholder
 * with a red background and "AdaptiveBannerAdView Here" text.
 *
 * When an ad is loaded, the composable animates its size and displays a small "Ad" label
 * above the banner. This label is positioned at the top-start of the Box and offset vertically
 * to appear slightly above the Box's border.
 *
 * @param adUnitId The ad unit ID for the banner ad.
 * @param maxHeight The maximum height the banner ad can occupy.
 * @param modifier Optional [Modifier] to be applied to the ad container.
 * @param animationSpec The [FiniteAnimationSpec] to use for the content size animation when the ad loads.
 *                      Defaults to a low stiffness spring animation.
 */
@Composable
fun AdaptiveBannerAd(
    adUnitId: String,
    maxHeight: Dp,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<IntSize> = spring(stiffness = Spring.StiffnessLow),
) {
    val isInEditMode = LocalInspectionMode.current
    var isAdVisible by remember { mutableStateOf(isInEditMode) }
    val height = (maxHeight - 8.dp)
    val builder = remember { AdRequest.Builder().build() }
    val borderColor = MaterialTheme.colorScheme.onSurface
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .animateContentSize(animationSpec)
            .padding(top = 4.dp)
            .conditional(
                condition = isAdVisible,
                ifTrue = {
                    height(maxHeight)
                    drawBehind {
                        drawRect(
                            color = borderColor,
                            style = Stroke(width = Dp.Hairline.toPx()) // Border stroke
                        )
                    }
                },
                ifFalse = { height(Dp.Hairline) },
            )
            .padding(2.dp),
    ) {
        if (isInEditMode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(Color.Red),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    color = Color.White,
                    text = "AdaptiveBannerAdView Here",
                )
                Text(
                    color = Color.White,
                    text = "AdaptiveBannerAdView Here",
                )
            }
        } else {
            val adSize = remember {
                AdSize.getInlineAdaptiveBannerAdSize(
                    maxWidth.value.toInt(), height.value.toInt()
                )
            }
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(adSize)
                        adListener =
                            object : AdListener() {
                                override fun onAdLoaded() {
                                    super.onAdLoaded()
                                    isAdVisible = true
                                }
                            }
                        this.adUnitId = adUnitId
                        runCatching {
                            loadAd(builder)
                        }
                    }
                })
        }
        if (isAdVisible) {
            Text(
                text = " ${stringResource(R.string.ad)} ",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.TopStart) // Positioniert den Text oben
                    .offset(y = (-10).dp) // <-- Schiebt den Text nach oben aus der Box heraus
                    .padding(start = 16.dp) // Kleiner horizontaler Puffer
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}

@Composable
fun rememberRewardedInterstatialAd(
    rewarded_interstitial_ad_id: String,
    modifier: Modifier = Modifier,
    onAdLoadAdError: (LoadAdError) -> Unit,
): StateFlow<RewardedInterstitialAd?> {
    val loadedAd = MutableStateFlow<RewardedInterstitialAd?>(null)
    val context = LocalContext.current

    RewardedInterstitialAd.load(
        context,
        rewarded_interstitial_ad_id,
        AdRequest.Builder().build(),
        object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                loadedAd.value = ad
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                onAdLoadAdError(loadAdError)
            }
        })
    return remember { loadedAd }
}

@Preview
@Composable
private fun AdaptiveBannerAdPreview() {
    Surface {
        AdaptiveBannerAd(
            adUnitId = "Test",
            maxHeight = 80.dp,
        )
    }
}

@Preview
@Composable
private fun BannerAdBannerPreview() {
    Surface {
        BannerAdView(adSize = AdSize.BANNER, adUnitId = "")
    }
}

@Preview
@Composable
private fun LeaderBoardBannerAdBannerPreview() {
    Surface {
        BannerAdView(adSize = AdSize.LEADERBOARD, adUnitId = "")
    }
}