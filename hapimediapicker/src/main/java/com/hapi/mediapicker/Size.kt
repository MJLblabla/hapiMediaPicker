package com.hapi.mediapicker

/**
 * @author athoucai
 * @date 2018/12/14
 */
enum class Size constructor(var aspectX: Int, var aspectY: Int) {
    Origin(-1, -1),
    Square(1, 1),
    Tall(3, 4),
    Width(4, 3),
    Cover(11,12);
}