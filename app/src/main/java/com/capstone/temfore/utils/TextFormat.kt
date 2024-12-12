package com.capstone.temfore.utilsimport com.capstone.temfore.Robject TextFormat {    fun formatType(type: String?, delimiter: String = "--"): String {        val formattedType = type?.split(delimiter)?.mapIndexed { index, item ->            when (index) {                // Elemen terakhir hanya teks, tanpa koma                type.split(delimiter).lastIndex -> item.trim()                // Elemen sebelum terakhir diberi ", dan"                type.split(delimiter).lastIndex - 1 -> "dan ${item.trim()}"                // Elemen lainnya diberi koma biasa                else -> "${item.trim()},"            }        }?.joinToString(" ") ?: ""        return "Hidangan ini untuk  $formattedType"    }    fun formatSteps(steps: String?, delimiter: String = "--"): String {        return steps?.split(delimiter)?.mapIndexed { index, item ->            if (index == steps.split(delimiter).lastIndex) {                item.trim() // Elemen terakhir tidak diberi nomor            } else {                "${index + 1}. ${item.trim()}" // Elemen lainnya diberi nomor            }        }?.joinToString("\n\n") ?: ""    }    fun formatIngredients(ingredients: String?, delimiter: String = "--", lineSeparator: String = ",\n"): String {        return ingredients?.replace(delimiter, lineSeparator) ?: ""    }    fun formatEnter(ingredients: String?, delimiter: String = "--", lineSeparator: String = "\n"): String {        return ingredients?.replace(delimiter, lineSeparator) ?: ""    }    fun formatTemperatureMessage(tempCold: Int?, prefix: String = "Cocok untuk cuaca"): String {        val temperature = if (tempCold != 0) "dingin" else "panas"        return "$prefix $temperature"    }    fun formatScoreMessage(score: Double): String {        val scorePercentage = (score * 100).toInt()        return when (scorePercentage) {            in 99..100 -> "Sangat Direkomendasikan"            in 97..98 -> "Direkomendasikan"            in 95..96 -> "Cukup Direkomendasikan"            else -> "Kurang Direkomendasikan"        }    }    fun getCategoryImage(category: String): Int {        return when (category) {            "ayam" -> R.drawable.img_category_1            "ikan" -> R.drawable.img_category_2            "kambing" -> R.drawable.img_category_3            "sapi" -> R.drawable.img_category_4            "tempe" -> R.drawable.img_category_5            "telur" -> R.drawable.img_category_6            "udang" -> R.drawable.img_category_7            else -> R.drawable.img_category_1 // Gambar default jika kategori tidak ditemukan        }    }}