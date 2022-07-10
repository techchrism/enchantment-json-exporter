package me.techchrism.enchantmentjsonexporter

data class VersionManifestV2(
    val latest: LatestVersions,
    val versions: List<VersionV2>
)

data class VersionV2(
    val id: String,
    val type: String,
    val url: String,
    val time: String,
    val releaseTime: String,
    val sha1: String,
    val complianceLevel: Int
)

data class LatestVersions(
    val release: String,
    val snapshot: String
)
