package com.gerwalex.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gerwalex.ad.AdaptiveBannerAd
import com.gerwalex.ad.BannerAdView
import com.gerwalex.example.ui.theme.LibraryTheme
import com.google.android.gms.ads.AdSize


class MainActivity : ComponentActivity() {
    private val ADUNITID = "ca-app-pub-3940256099942544/9214589741"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainContent(ADUNITID)
        }
    }

}

@Composable
fun MainContent(adUnitId: String) {
    LibraryTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                BannerAdView(adSize = AdSize.BANNER, adUnitId = adUnitId)
                HorizontalDivider()
                AdaptiveBannerAd(adUnitId, maxHeight = 50.dp)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    MainContent("123")
}