package com.domio.app.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "DomioAdManager"

    // PRODUCTION ADMOB AD UNIT IDs
    const val BANNER_ID = "ca-app-pub-4633407281053827/6133886786"
    const val INTERSTITIAL_ID = "ca-app-pub-4633407281053827/4246090044"

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    fun initialize(context: Context) {
        try {
            val testDeviceIds = listOf("A526BCD686E62E189D199973630D0F03")
            val configuration = com.google.android.gms.ads.RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)

            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob Mobile Ads Initialized: $initializationStatus")
            }
            loadInterstitialAd(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MobileAds", e)
        }
    }

    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isAdLoading) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    Log.d(TAG, "Interstitial Ad Loaded Successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    Log.w(TAG, "Interstitial Ad Failed to Load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitialAd(activity)
            onAdDismissed()
        }
    }
}
