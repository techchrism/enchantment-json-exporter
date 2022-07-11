@file:JvmName("CommandLine")
package me.techchrism.enchantmentjsonexporter

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    prepareJsonFiles()
}

fun writeGithubEnvironmentVariable(name: String, value: String) {
    val envFilePath = System.getenv("GITHUB_ENV") ?: return
    FileOutputStream(envFilePath, true).bufferedWriter().use { 
        it.write("${name}=${value}\n")
    }
}

fun prepareJsonFiles() {
    // Prepare data and version directories if they don't already exist
    val dataDir = File("data")
    val versionsDir = File(dataDir, "versions")
    versionsDir.mkdirs()
    
    // Find existing versions
    val gson = GsonBuilder().setPrettyPrinting().create()
    val listings = versionsDir.listFiles() ?: emptyArray()
    val existingVersionIDs = HashSet<String>()
    val errorVersionIDs = HashSet<String>()
    for(listing in listings) {
        if(listing.name.endsWith(".json")) {
            val versionID = listing.name.substring(0, listing.name.length - ".json".length)
            val data = gson.fromJson(listing.readText(), JsonObject::class.java)
            if(data.get("error")?.asBoolean != true) {
                existingVersionIDs.add(versionID)
            } else {
                errorVersionIDs.add(versionID)
            }
        }
    }
    println("Loaded ${existingVersionIDs.size} existing versions and ${errorVersionIDs.size} errors")
    
    // Load versions manifest and find missing versions
    val versionManifest = ServerJarLoader.loadVersionManifest()
    // Release time of 1.19
    val afterDate = Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse("2022-06-07T09:42:17+00:00")))
    
    val missingVersions = versionManifest.versions.filter {
        it.releaseTime.after(afterDate) && !existingVersionIDs.contains(it.id) && !errorVersionIDs.contains(it.id)
    }.sortedBy { it.releaseTime }
    
    // Populate meta with existing version info
    val versionsMeta = ArrayList<VersionListing>()
    val existingVersions = versionManifest.versions.filter { existingVersionIDs.contains(it.id) }
    val urlPrefix = System.getenv("URL_PREFIX") ?: "./versions/"
    for(existingVersion in existingVersions) {
        versionsMeta.add(VersionListing(
            existingVersion.id, 
            "${urlPrefix}${existingVersion.id}.json",
            existingVersion.type,
            existingVersion.releaseTime))
    }
    
    println("Found ${missingVersions.size} new version${if(missingVersions.size == 1) "" else "s"}")
    
    // Load missing versions
    var errored = false
    for(missing in missingVersions) {
        println("Loading missing version ${missing.id}...")
        var outputString: String
        try {
            //TODO remove testing exception
            if(missing.id == "1.19.1-pre4") throw Exception("Testing exception")
            val versionJson = ServerJarLoader.loadVersionJson(missing.url)
            outputString = ServerJarLoader.getEnchantmentsForVersionJson(versionJson)
            versionsMeta.add(VersionListing(
                missing.id,
                "${urlPrefix}${missing.id}.json",
                missing.type,
                missing.releaseTime))
        } catch(t: Throwable) {
            writeGithubEnvironmentVariable("ERROR_VERSION", missing.id)
            println("Error while loading version ${missing.id}:")
            println(t)
            t.printStackTrace()
            
            errored = true
            val errorObj = JsonObject()
            errorObj.addProperty("version", missing.id)
            errorObj.addProperty("exporter_version", ServerJarLoader.VERSION)
            errorObj.addProperty("error", true)
            outputString = gson.toJson(errorObj)
        }
        
        FileOutputStream(File(versionsDir, "${missing.id}.json")).use { 
            it.write(outputString.toByteArray())
        }
        if(errored) break
        println("Done loading ${missing.id}")
    }

    val latestSavedVersions = LatestVersions(
        versionsMeta.filter { it.type == "release" }.maxByOrNull { it.releaseTime }?.id ?: "",
        versionsMeta.filter { it.type == "snapshot" }.maxByOrNull { it.releaseTime }?.id ?: ""
    )
    val versionListings = VersionsListings(latestSavedVersions, versionsMeta)
    FileOutputStream(File(dataDir, "versions.json")).use {
        it.write(gson.toJson(versionListings).toByteArray())
    }
    
    if(errored) exitProcess(1)
}
