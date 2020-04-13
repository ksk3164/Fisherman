package com.ex2i.fisherman.retrofit

import com.google.gson.annotations.SerializedName

class Model_weather {

    @SerializedName("weather")
    var weather : Weather? = null

    @SerializedName("common")
    var common : Common? = null

    @SerializedName("result")
    var result : Result? = null

}

class Weather {

    @SerializedName("hourly")
    var hourlyList : List<Hourly>? = null
}

class Hourly {

    @SerializedName("grid")
    var grid: Grid? = null
    @SerializedName("wind")
    var wind: Wind? = null

    @SerializedName("precipitation")
    var precipitation: Precipitation? = null

    @SerializedName("sky")
    var sky: Sky? = null

    @SerializedName("temperature")
    var temperature: Temperature? = null

    @SerializedName("humidity")
    var humidity: String? = null

    @SerializedName("lightning")
    var lightning: String? = null

    @SerializedName("timeRelease")
    var timeRealease: String? = null

}

class Grid {

    @SerializedName("latitude")
    var latitude: String? = null

    @SerializedName("longitude")
    var longitude: String? = null

    @SerializedName("city")
    var city: String? = null

    @SerializedName("county")
    var county: String? = null

    @SerializedName("village")
    var village: String? = null
}

class Wind {

    @SerializedName("wdir")
    var wdir: String? = null

    @SerializedName("wspd")
    var wspd: String? = null

}

class Precipitation {
    @SerializedName("sinceOntime")
    var sinceOntime: String? = null

    @SerializedName("type")
    var type: String? = null

}

class Sky {

    @SerializedName("code")
    var code: String? = null

    @SerializedName("name")
    var name: String? = null
}

class Temperature {

    @SerializedName("tc")
    var tc: String? = null
    @SerializedName("tmax")
    var tmax: String? = null

    @SerializedName("tmin")
    var tmin: String? = null

}

class Common {
    @SerializedName("alertYn")
    var alertYn: String? = null

    @SerializedName("stormYn")
    var stormYn: String? = null
}

class Result {
    @SerializedName("code")
    var code: String? = null

    @SerializedName("requestUrl")
    var requestUrl: String? = null

    @SerializedName("message")
    var message: String? = null
}
