package com.subsolis.allblue.Adapty

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
    }

    fun setPermission(boolean: Boolean) {
        _viewstate.value = _viewstate.value.copy(
            subscribed = boolean,
            premiumAccessLevel = boolean
        )
    }

    fun remotePaywall() {
        Adapty.getPaywalls { paywalls, products, error ->
            if (error == null) {
                // retrieve the products from paywalls

                _viewstate.value = _viewstate.value.copy(
                    paywalls = paywalls?.firstOrNull(),
                    products = paywalls!!.firstOrNull()!!.products
                )
            }
        }
    }
}