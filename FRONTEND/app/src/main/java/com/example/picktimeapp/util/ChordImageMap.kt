package com.example.picktimeapp.util

import com.example.picktimeapp.R

object ChordImageMap {
    private val map = mapOf(
        "a" to R.drawable.code_a,
        "b" to R.drawable.code_b,
        "c" to R.drawable.code_c,
        "d" to R.drawable.code_d,
        "e" to R.drawable.code_e,
        "f" to R.drawable.code_f,
        "g" to R.drawable.code_g,
        "am" to R.drawable.code_am,
        "bm" to R.drawable.code_bm,
        "cm" to R.drawable.code_cm,
        "dm" to R.drawable.code_dm,
        "em" to R.drawable.code_em,
        "fm" to R.drawable.code_fm,
        "gm" to R.drawable.code_gm,
        "a7" to R.drawable.code_a7,
        "b7" to R.drawable.code_b7,
        "c7" to R.drawable.code_c7,
        "d7" to R.drawable.code_d7,
        "e7" to R.drawable.code_e7,
        "f7" to R.drawable.code_f7,
        "g7" to R.drawable.code_g7,
    )

    fun getResId(chordName: String): Int {
        return map[chordName.lowercase()] ?: R.drawable.code_g
    }
}


// 코드 이미지 매핑 자동화방식(불안정성 문제로 권장하지 않는 방식이라 일단 주석함)
//@Composable
//fun getChordImageResId(chordName: String): Int {
//    val context = LocalContext.current
//    val resourceName = "code_${chordName.lowercase()}"
//    return remember(resourceName) {
//        context.resources.getIdentifier(resourceName, "drawable", context.packageName)
//    }
//}
