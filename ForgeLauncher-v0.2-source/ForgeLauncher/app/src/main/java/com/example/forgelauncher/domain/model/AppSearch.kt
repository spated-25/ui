package com.example.forgelauncher.domain.model

import java.util.Locale

fun matchesAppSearch(label: String, packageName: String, query: String): Boolean {
    val terms = query
        .trim()
        .lowercase(Locale.ROOT)
        .split(Regex("\\s+"))
        .filter(String::isNotBlank)

    if (terms.isEmpty()) return true

    val searchable = "${label.lowercase(Locale.ROOT)} ${packageName.lowercase(Locale.ROOT)}"
    return terms.all(searchable::contains)
}
