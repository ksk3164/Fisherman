package com.ex2i.fisherman.retrofit

import com.google.gson.annotations.SerializedName

class Model_Item {
    @SerializedName ("list")
    var List: List<List_Model>? = null

    @SerializedName("parm")
    var parm : Parm? = null

    @SerializedName("ArpltnInforInqireSvcVo")
    var arpltnInforInqireSvcVo : ArpltnInforInqireSvcVo?=null

    @SerializedName("totalCount")
    var totalCount : String?=null

}

class List_Model {

    @SerializedName("_returnType")
    var returnType : String? = null

    @SerializedName("coGrade")
    var coGrade : String? = null

    @SerializedName("coValue")
    var coValue : String? = null

    @SerializedName("dataTerm")
    var dataTerm : String? = null

    @SerializedName("dataTime")
    var dataTime : String? = null

    @SerializedName("khaiGrade")
    var khaiGrade : String? = null

    @SerializedName("khaiValue")
    var khaiValue : String? = null

    @SerializedName("mangName")
    var mangName : String? = null

    @SerializedName("no2Grade")
    var no2Grade : String? =null

    @SerializedName("no2Value")
    var no2Value : String? = null

    @SerializedName("numOfRows")
    var numOfRows:String?= null

    @SerializedName("o3Grade")
    var o3Grade:String?=null

    @SerializedName("o3Value")
    var o3Value:String?=null

    @SerializedName("pageNo")
    var pageNo:String?=null

    @SerializedName ("pm10Grade")
    var pm10Grade : String? = null

    @SerializedName("pm10Grade1h")
    var pm10Grade1h:String?=null

    @SerializedName("pm10Value")
    var pm10Value:String?=null

    @SerializedName("pm10Value24")
    var pm10Value24 : String?=null

    @SerializedName("pm25Grade")
    var pm25Grade:String?=null

    @SerializedName("pm25Grade1h")
    var pm25Grade1h : String?=null

    @SerializedName("pm25Value")
    var pm25Value : String?=null

    @SerializedName("pm25Value24")
    var pm25Value24: String?=null

    @SerializedName("resultCode")
    var resultCode : String?=null

    @SerializedName("resultMsg")
    var resultMsg : String?= null

    @SerializedName("rnum")
    var rnum : String? =null

    @SerializedName("serviceKey")
    var serviceKey : String?=null

    @SerializedName("sidoName")
    var sidoName: String?=null

    @SerializedName("so2Grade")
    var so2Grade : String?=null

    @SerializedName("so2Value")
    var so2Value:String?=null

    @SerializedName("stationCode")
    var stationCode:String?=null

    @SerializedName("stationName")
    var stationName:String?=null

    @SerializedName("statotalCount")
    var statotalCount:String?=null

    @SerializedName("ver")
    var ver : String?=null

}

class Parm {

    @SerializedName("_returnType")
    var returnType : String? = null
    @SerializedName("coGrade")
    var coGrade : String? = null

    @SerializedName("coValue")
    var coValue : String? = null

    @SerializedName("dataTerm")
    var dataTerm : String? = null

    @SerializedName("dataTime")
    var dataTime : String? = null

    @SerializedName("khaiGrade")
    var khaiGrade : String? = null

    @SerializedName("khaiValue")
    var khaiValue : String? = null

    @SerializedName("mangName")
    var mangName : String? = null

    @SerializedName("no2Grade")
    var no2Grade : String? =null

    @SerializedName("no2Value")
    var no2Value : String? = null

    @SerializedName("numOfRows")
    var numOfRows:String?= null

    @SerializedName("o3Grade")
    var o3Grade:String?=null

    @SerializedName("o3Value")
    var o3Value:String?=null

    @SerializedName("pageNo")
    var pageNo:String?=null

    @SerializedName ("pm10Grade")
    var pm10Grade : String? = null

    @SerializedName("pm10Grade1h")
    var pm10Grade1h:String?=null

    @SerializedName("pm10Value")
    var pm10Value:String?=null

    @SerializedName("pm10Value24")
    var pm10Value24 : String?=null

    @SerializedName("pm25Grade")
    var pm25Grade:String?=null

    @SerializedName("pm25Grade1h")
    var pm25Grade1h : String?=null

    @SerializedName("pm25Value")
    var pm25Value : String?=null

    @SerializedName("pm25Value24")
    var pm25Value24: String?=null

    @SerializedName("resultCode")
    var resultCode : String?=null

    @SerializedName("resultMsg")
    var resultMsg : String?= null

    @SerializedName("rnum")
    var rnum : String? =null

    @SerializedName("serviceKey")
    var serviceKey : String?=null

    @SerializedName("sidoName")
    var sidoName: String?=null

    @SerializedName("so2Grade")
    var so2Grade : String?=null

    @SerializedName("so2Value")
    var so2Value:String?=null

    @SerializedName("stationCode")
    var stationCode:String?=null

    @SerializedName("stationName")
    var stationName:String?=null

    @SerializedName("statotalCount")
    var statotalCount:String?=null

    @SerializedName("ver")
    var ver : String?=null

}

class ArpltnInforInqireSvcVo {
    @SerializedName("_returnType")
    var returnType : String? = null

    @SerializedName("coGrade")
    var coGrade : String? = null

    @SerializedName("coValue")
    var coValue : String? = null

    @SerializedName("dataTerm")
    var dataTerm : String? = null

    @SerializedName("dataTime")
    var dataTime : String? = null

    @SerializedName("khaiGrade")
    var khaiGrade : String? = null

    @SerializedName("khaiValue")
    var khaiValue : String? = null

    @SerializedName("mangName")
    var mangName : String? = null

    @SerializedName("no2Grade")
    var no2Grade : String? =null

    @SerializedName("no2Value")
    var no2Value : String? = null

    @SerializedName("numOfRows")
    var numOfRows:String?= null

    @SerializedName("o3Grade")
    var o3Grade:String?=null

    @SerializedName("o3Value")
    var o3Value:String?=null

    @SerializedName("pageNo")
    var pageNo:String?=null

    @SerializedName ("pm10Grade")
    var pm10Grade : String? = null

    @SerializedName("pm10Grade1h")
    var pm10Grade1h:String?=null

    @SerializedName("pm10Value")
    var pm10Value:String?=null

    @SerializedName("pm10Value24")
    var pm10Value24 : String?=null

    @SerializedName("pm25Grade")
    var pm25Grade:String?=null

    @SerializedName("pm25Grade1h")
    var pm25Grade1h : String?=null

    @SerializedName("pm25Value")
    var pm25Value : String?=null

    @SerializedName("pm25Value24")
    var pm25Value24: String?=null

    @SerializedName("resultCode")
    var resultCode : String?=null

    @SerializedName("resultMsg")
    var resultMsg : String?= null

    @SerializedName("rnum")
    var rnum : String? =null

    @SerializedName("serviceKey")
    var serviceKey : String?=null

    @SerializedName("sidoName")
    var sidoName: String?=null

    @SerializedName("so2Grade")
    var so2Grade : String?=null

    @SerializedName("so2Value")
    var so2Value:String?=null

    @SerializedName("stationCode")
    var stationCode:String?=null

    @SerializedName("stationName")
    var stationName:String?=null

    @SerializedName("statotalCount")
    var statotalCount:String?=null

    @SerializedName("ver")
    var ver : String?=null
}

