package pl.c0.sayard.studentUEK

import android.content.Context
import android.widget.Toast
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener

class HideAdsListener(val context: Context, private val rewardedVideoAd: RewardedVideoAd): RewardedVideoAdListener {
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
    }

    override fun onRewardedVideoStarted() {
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        Toast.makeText(context, context.getString(R.string.video_ad_failed_to_load), Toast.LENGTH_LONG).show()
    }
}