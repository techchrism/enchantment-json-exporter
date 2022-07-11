package me.techchrism.enchantmentjsonexporter

import java.util.*

data class VersionManifestV2(
    val latest: LatestVersions,
    val versions: List<VersionV2>
)

data class VersionV2(
    val id: String,
    val type: String,
    val url: String,
    val time: Date,
    val releaseTime: Date,
    val sha1: String,
    val complianceLevel: Int
)

data class LatestVersions(
    val release: String,
    val snapshot: String
)

data class VersionDownload(
    val sha1: String,
    val size: Int,
    val url: String
)

data class VersionDownloads(
    val client: VersionDownload,
    val client_mappings: VersionDownload,
    val server: VersionDownload,
    val server_mappings: VersionDownload
)

data class VersionJson(
    val downloads: VersionDownloads
)

data class VersionListing(val id: String, val url: String, val type: String, val releaseTime: Date)

data class VersionsListings(val latest: LatestVersions, val listings: List<VersionListing>)