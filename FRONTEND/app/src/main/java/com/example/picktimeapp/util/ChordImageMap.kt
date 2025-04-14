package com.example.picktimeapp.util

import com.example.picktimeapp.R

object ChordImageMap {
    private val map = mapOf(
        "g" to R.drawable.code_g,
        "c" to R.drawable.code_c,
        "d" to R.drawable.code_d,
        "a" to R.drawable.code_a,
        "b" to R.drawable.code_b,
        "e" to R.drawable.code_e,
        "f" to R.drawable.code_f,

        "g7" to R.drawable.code_g7,
        "c7" to R.drawable.code_c7,
        "d7" to R.drawable.code_d7,
        "a7" to R.drawable.code_a7,
        "b7" to R.drawable.code_b7,
        "e7" to R.drawable.code_e7,
        "f7" to R.drawable.code_f7,

        "cm" to R.drawable.code_cm,
        "dm" to R.drawable.code_dm,
        "em" to R.drawable.code_em,
        "fm" to R.drawable.code_fm,
        "gm" to R.drawable.code_gm,
        "am" to R.drawable.code_am,
        "bm" to R.drawable.code_bm,

        "cm7" to R.drawable.code_cm7,
        "dm7" to R.drawable.code_dm7,
        "em7" to R.drawable.code_em7,
        "fm7" to R.drawable.code_fm7,
        "gm7" to R.drawable.code_gm7,
        "am7" to R.drawable.code_am7,
        "bm7" to R.drawable.code_bm7,

        "cm7" to R.drawable.code_cm7,
        "dm7" to R.drawable.code_dbigm7,
        "em7" to R.drawable.code_ebigm7,
        "fm7" to R.drawable.code_fbigm7,
        "gm7" to R.drawable.code_gbigm7,
        "am7" to R.drawable.code_abigm7,
        "bm7" to R.drawable.code_bbigm7,

        "f#m" to R.drawable.code_fsm,
        "c#m" to R.drawable.code_csm,
        "f#m7" to R.drawable.code_fsm7,
        "dsus4" to R.drawable.code_dsus4,
        "asus4" to R.drawable.code_asus4,
        "cadd9" to R.drawable.code_cadd9,
        "gadd9" to R.drawable.code_gadd9,
        "fmaj7" to R.drawable.code_fmaj7,
        "emaj7" to R.drawable.code_emaj7,
        "g#m7" to R.drawable.code_gsm7,
        "c#m7" to R.drawable.code_csm7,

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
