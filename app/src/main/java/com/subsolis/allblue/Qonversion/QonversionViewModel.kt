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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class QonversionState(
    val loadedOfferings: List<QOffering> = emptyList(),
    val hasAndroidPremiumPermission: Boolean = false,
    val testing: Boolean = true,
)

class QonversionViewModel : ViewModel() {

    private val _viewState = MutableStateFlow(QonversionState())
    var viewState: StateFlow<QonversionState> = _viewState

    init {
        loadOfferings()
        updatePermissions()
    }

    fun loadOfferings() {
        viewModelScope.launch {
            Qonversion.offerings(object : QonversionOfferingsCallback {
                override fun onError(error: QonversionError) {
                    TODO("Not yet implemented")
                }

                override fun onSuccess(offerings: QOfferings) {
                    _viewState.value = _viewState.value.copy(
                        loadedOfferings = offerings.availableOfferings
                    )
                }
            })
        }
    }

    fun updatePermissions() {
        // Checks and updates permissions

        viewModelScope.launch {

            Qonversion.checkPermissions(object : QonversionPermissionsCallback {
                override fun onError(error: QonversionError) {
                    TODO("Not yet implemented")
                }

                override fun onSuccess(permissions: Map<String, QPermission>) {
                    _viewState.value = _viewState.value.copy(
                        hasAndroidPremiumPermission = permissions["Android-App-Premium"]?.isActive() == true
                    )
                }
            })


        }
    }
}