package com.ex2i.fisherman

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Model(
    @PrimaryKey var id: Long = 0,
    var title: String = "",
    var size: String = "",
    var weight: String = "",
    var bodyColor: String = "",
    var spawningSeason: String = "",
    var habitat: String = "",
    var distributionArea: String = "",
    var image: Int = R.drawable.ic_fish_icon,
    var description: String = ""
) : RealmObject()