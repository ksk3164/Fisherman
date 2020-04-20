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
    @SerializedName("alert")
    var alert : List<Alert>? = null
}

class Alert {
    @SerializedName("grid")
    var grid_special : Grid_Special? = null
    @SerializedName("alert51")
    var alert51 : Alert51? = null
    @SerializedName("alert60")
    var alert60 : Alert60? = null
}

class Alert60 {
    @SerializedName("t1")
    var t1 : String? = null
    @SerializedName("t2")
    var t2 : String? = null
    @SerializedName("t3")
    var t3 : String? = null
    @SerializedName("t4")
    var t4 : String? = null
    @SerializedName("t5")
    var t5 : String? = null
    @SerializedName("t6")
    var t6 : String? = null
    @SerializedName("t7")
    var t7 : String? = null
}

class Alert51 {
    @SerializedName("cmdCode")
    var cmdCode : String? = null
    @SerializedName("cmdName")
    var cmdName : String? = null
    @SerializedName("varCode")
    var varCode : String? = null
    @SerializedName("varName")
    var varName : String? = null
    @SerializedName("stressCode")
    var stressCode : String? = null
    @SerializedName("stressName")
    var stressName : String? = null
}

class Grid_Special {
    @SerializedName("number")
    var number : String? = null
    @SerializedName("timeRelease")
    var timeRelease : String? = null
    @SerializedName("stationId")
    var stationId : String? = null
    @SerializedName("areaCode")
    var areaCode : String? = null
    @SerializedName("areaName")
    var areaName : String? = null
    @SerializedName("timeStart")
    var timeStart : String? = null
    @SerializedName("timeEnd")
    var timeEnd : String? = null
    @SerializedName("timeAllEnd")
    var timeAllEnd : String? = null

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
