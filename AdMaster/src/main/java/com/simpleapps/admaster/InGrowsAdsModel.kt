package com.simpleapps.admaster

data class InGrowsAdsModel(
    val name: String,
    val type: Int,
    val url: String?,
    val appDetails: AppListModel.AppDetails? = null,
)