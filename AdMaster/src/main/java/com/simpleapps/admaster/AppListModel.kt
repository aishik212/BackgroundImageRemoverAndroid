package com.simpleapps.admaster


import com.google.gson.annotations.SerializedName

data class AppListModel(
    @SerializedName("AppLists")
    var appLists: List<AppDetails> = listOf(),
) {
    data class AppDetails(
        var appName: String = "",
        var type: String = "NATIVE_APP",
        var iconUrl: String = "",
        var packageName: String = "",
        var titles: List<String> = listOf(),
    )
}