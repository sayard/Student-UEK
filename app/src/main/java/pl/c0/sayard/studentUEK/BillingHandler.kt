package pl.c0.sayard.studentUEK

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.core.content.edit
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails

class BillingHandler(val context: Context, val activity: Activity): BillingProcessor.IBillingHandler{
    override fun onBillingInitialized() {
    }

    override fun onPurchaseHistoryRestored() {
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        if(productId == context.getString(R.string.student_uek_premium_item_id)){
            activity.finish()
            activity.startActivity(Intent(activity, activity.javaClass))
            Toast.makeText(context, context.getString(R.string.thanks_for_support), Toast.LENGTH_SHORT).show()
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit {
                putBoolean(context.getString(R.string.PREFS_PREMIUM_PURCHASED), true)
            }
        }
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        val message:String? = when(errorCode){
            2 -> context.getString(R.string.billing_no_network_error)
            4 -> context.getString(R.string.billing_item_not_available_error)
            7 -> context.getString(R.string.billing_item_already_owned_error)
            else -> null
        }
        if(message!=null){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

}