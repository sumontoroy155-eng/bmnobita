package com.example.domain

import com.example.data.local.PerfectSetupEntity
import com.example.data.local.SavedProfileEntity
import com.example.model.HudControlNode
import org.json.JSONArray
import org.json.JSONObject

object ProfileJsonCodec {

    fun exportProfiles(profiles: List<SavedProfileEntity>): String {
        val root = JSONObject()
        root.put("app", "BM GAMING PRO")
        root.put("version", 2)
        val array = JSONArray()
        profiles.forEach { p ->
            val obj = JSONObject().apply {
                put("name", p.name)
                put("deviceLabel", p.deviceLabel)
                put("modeId", p.modeId)
                put("general", p.general)
                put("redDot", p.redDot)
                put("scope2x", p.scope2x)
                put("scope4x", p.scope4x)
                put("sniperScope", p.sniperScope)
                put("freeLook", p.freeLook)
                put("fireButtonPercent", p.fireButtonPercent)
                put("dpiNote", p.dpiNote)
                put("isFavorite", p.isFavorite)
            }
            array.put(obj)
        }
        root.put("profiles", array)
        return root.toString(2)
    }

    fun importProfiles(jsonText: String): Result<List<SavedProfileEntity>> {
        return runCatching {
            val trimmed = jsonText.trim()
            require(trimmed.isNotEmpty()) { "Empty JSON" }
            val root = JSONObject(trimmed)
            val array = root.getJSONArray("profiles")
            val result = mutableListOf<SavedProfileEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val name = obj.optString("name", "Imported Profile").take(40).ifBlank { "Imported Profile" }
                result.add(
                    SavedProfileEntity(
                        name = name,
                        deviceLabel = obj.optString("deviceLabel", "Android Device"),
                        modeId = obj.optString("modeId", "general"),
                        general = obj.optInt("general", 175).coerceIn(0, 200),
                        redDot = obj.optInt("redDot", 168).coerceIn(0, 200),
                        scope2x = obj.optInt("scope2x", 155).coerceIn(0, 200),
                        scope4x = obj.optInt("scope4x", 145).coerceIn(0, 200),
                        sniperScope = obj.optInt("sniperScope", 110).coerceIn(0, 200),
                        freeLook = obj.optInt("freeLook", 160).coerceIn(0, 200),
                        fireButtonPercent = obj.optInt("fireButtonPercent", 48).coerceIn(35, 70),
                        dpiNote = obj.optString("dpiNote", ""),
                        isFavorite = obj.optBoolean("isFavorite", false)
                    )
                )
            }
            require(result.isNotEmpty()) { "No profiles found in JSON" }
            result
        }
    }

    fun encodeHudControls(controls: List<HudControlNode>): String {
        val arr = JSONArray()
        controls.forEach { c ->
            arr.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("label", c.label)
                    put("x", c.xFraction.toDouble())
                    put("y", c.yFraction.toDouble())
                    put("sizeDp", c.sizeDp)
                    put("opacity", c.opacity.toDouble())
                }
            )
        }
        return arr.toString()
    }

    fun decodeHudControls(json: String): List<HudControlNode> {
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                HudControlNode(
                    id = obj.getString("id"),
                    label = obj.getString("label"),
                    xFraction = obj.getDouble("x").toFloat().coerceIn(0.08f, 0.92f),
                    yFraction = obj.getDouble("y").toFloat().coerceIn(0.10f, 0.90f),
                    sizeDp = obj.getInt("sizeDp").coerceIn(30, 95),
                    opacity = obj.getDouble("opacity").toFloat().coerceIn(0.25f, 1.0f)
                )
            }
        }.getOrElse { emptyList() }
    }

    fun exportPerfectSetup(setup: PerfectSetupEntity): String {
        return JSONObject().apply {
            put("app", "BM GAMING PRO - ULTIMATE EDITION")
            put("title", setup.title)
            put("device", setup.deviceLabel)
            put("general", setup.general)
            put("redDot", setup.redDot)
            put("scope2x", setup.scope2x)
            put("scope4x", setup.scope4x)
            put("sniperScope", setup.sniperScope)
            put("freeLook", setup.freeLook)
            put("recommendedDpiRange", setup.recommendedDpiRange)
            put("fireButtonPercent", setup.fireButtonPercent)
            put("hudFingers", setup.hudFingers)
        }.toString(2)
    }
}
