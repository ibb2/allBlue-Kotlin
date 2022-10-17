package com.subsolis.allblue.Adapty

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.adapty.Adapty
import com.adapty.models.PaywallModel
import com.adapty.models.ProductModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class AdaptyState(
    val subscribed: Boolean = false,
    val premiumAccessLevel: Boolean = false,
    val paywalls: PaywallModel? = null,
    val products: List<ProductModel> = emptyList(),
)

@HiltViewModel
class AdaptyViewModel @Inject constructor(
) : ViewModel() {

    val _viewstate = mutableStateOf(AdaptyState())
    var viewState = _viewstate

    init {
        remotePaywall()
        setPermissions()
    }

    fun setPermissions() {
        Adapty.getPurchaserInfo { purchaserInfo, error ->
            if (error == null) {
                _viewstate.value = _viewstate.value.copy(
                    subscribed = purchaserInfo!!.accessLevels["premium"]?.isActive == true
                )
            }
        }

    }

    fun remotePaywall() {
        Adapty.getPaywalls { paywalls, products, error ->
            if (error == null) {
                // retrieve the products from paywalls

                val paywall = paywalls?.firstOrNull()
                val product = paywall!!.products

                _viewstate.value = _viewstate.value.copy(
                    paywalls = paywall,
                    products = product,
                )

                // Log Paywalls
                Adapty.logShowPaywall(paywall)
            }
        }
    }

    fun makePurchase(a: Activity, p: ProductModel) {
        Adapty.makePurchase(a,
            p) { purchaserInfo, purchaseToken, googleValidationResult, product, error ->
            if (error == null) {
                // "premium" is an identifier of default access level
                if (purchaserInfo?.accessLevels?.get("premium")?.isActive == true) {
                    // grant access to premium features
                    _viewstate.value = _viewstate.value.copy(
                        premiumAccessLevel = purchaserInfo.accessLevels["premium"]?.isActive == true,
                        subscribed = purchaserInfo.accessLevels["premium"]?.isActive == true,
                    )
                }
            }
        }
    }
}