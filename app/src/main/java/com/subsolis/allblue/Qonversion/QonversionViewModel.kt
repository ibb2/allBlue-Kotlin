package com.subsolis.allblue.Qonversion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionOfferingsCallback
import com.qonversion.android.sdk.QonversionPermissionsCallback
import com.qonversion.android.sdk.dto.QPermission
import com.qonversion.android.sdk.dto.offerings.QOffering
import com.qonversion.android.sdk.dto.offerings.QOfferings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class QonversionState(
    val loadedOfferings: List<QOffering> = emptyList(),
    val hasAndroidPremiumPermission: Boolean = false,
)

class QonversionViewModel : ViewModel() {

    private val _viewstate = MutableStateFlow(QonversionState())
    var viewstate = _viewstate

    init {
        loadOfferings()
        updatePremissions()
    }

    fun loadOfferings() {
        viewModelScope.launch {
            Qonversion.offerings(object : QonversionOfferingsCallback {
                override fun onError(error: QonversionError) {
                    TODO("Not yet implemented")
                }

                override fun onSuccess(offerings: QOfferings) {
                    _viewstate.value = _viewstate.value.copy(
                        loadedOfferings = offerings.availableOfferings
                    )
                }
            })
        }
    }

    fun updatePremissions() {
        // Checks and updates permissions

        viewModelScope.launch {

            Qonversion.checkPermissions(object : QonversionPermissionsCallback {
                override fun onError(error: QonversionError) {
                    TODO("Not yet implemented")
                }

                override fun onSuccess(permissions: Map<String, QPermission>) {
                    _viewstate.value = _viewstate.value.copy(
                        hasAndroidPremiumPermission = permissions["Android-App-Premium"]?.isActive() == true
                    )
                }
            })


        }
    }
}