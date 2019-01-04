package pl.c0.sayard.studentUEK

import android.content.Context
import android.support.design.widget.NavigationView
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener

class HideAdsListener(val context: Context,
                      private val rewardedVideoAd: RewardedVideoAd,
                      private val navigationView: NavigationView,
                      private val adView: AdView): RewardedVideoAdListener {

    override fun onRewardedVideoAdClosed() {
    }

    override fun onRewardedVideoAdLeftApplication() {
    }

    override fun onRewardedVideoAdLoaded() {
        if (rewardedVideoAd.isLoaded) {
            rewardedVideoAd.show()
        }
    }

    override fun onRewardedVideoAdOpened() {
    }

    override fun onRewardedVideoCompleted() {
    }

    override fun onRewarded(p0: RewardItem?) {
        Utils.setHideAdsDatePrefs(context)
        navigationView.menu.findItem(R.id.navigation_hide_ads).isVisible = false
        adView.visibility = View.GONE
    }

    override fun onRewardedVideoStarted() {
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        Toast.makeText(context, context.getString(R.string.video_ad_failed_to_load), Toast.LENGTH_LONG).show()
    }
}