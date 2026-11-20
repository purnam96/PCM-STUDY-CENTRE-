package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.AcademicClass
import com.example.model.MaterialCategory
import com.example.model.MaterialSection
import com.example.model.Subject
import org.json.JSONArray
import org.json.JSONObject

/**
 * Room TypeConverters for persisting enums, string lists, and rich sections
 * within the local SQLite database cache for offline access.
 */
class Converters {

    @TypeConverter
    fun fromAcademicClass(value: AcademicClass?): String =
        value?.name ?: AcademicClass.CLASS_10.name

    @TypeConverter
    fun toAcademicClass(value: String?): AcademicClass = try {
        AcademicClass.valueOf(value ?: "")
    } catch (e: Exception) {
        AcademicClass.CLASS_10
    }

    @TypeConverter
    fun fromSubject(value: Subject?): String =
        value?.name ?: Subject.PHYSICS.name

    @TypeConverter
    fun toSubject(value: String?): Subject = try {
        Subject.valueOf(value ?: "")
    } catch (e: Exception) {
        Subject.PHYSICS
    }

    @TypeConverter
    fun fromMaterialCategory(value: MaterialCategory?): String =
        value?.name ?: MaterialCategory.NOTES.name

    @TypeConverter
    fun toMaterialCategory(value: String?): MaterialCategory = try {
        MaterialCategory.valueOf(value ?: "")
    } catch (e: Exception) {
        MaterialCategory.NOTES
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value.isNullOrEmpty()) return ""
        val array = JSONArray()
        value.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: Exception) {
            value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    @TypeConverter
    fun fromSectionList(sections: List<MaterialSection>?): String {
        if (sections.isNullOrEmpty()) return ""
        val array = JSONArray()
        for (sec in sections) {
            val obj = JSONObject()
            obj.put("title", sec.title)
            obj.put("content", sec.content)
            if (sec.formulaOrEquation != null) {
                obj.put("formula", sec.formulaOrEquation)
            }
            val hlArr = JSONArray()
            sec.highlights.forEach { hlArr.put(it) }
            obj.put("highlights", hlArr)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toSectionList(value: String?): List<MaterialSection> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = mutableListOf<MaterialSection>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val hlList = mutableListOf<String>()
                if (obj.has("highlights")) {
                    val hlArr = obj.getJSONArray("highlights")
                    for (j in 0 until hlArr.length()) {
                        hlList.add(hlArr.getString(j))
                    }
                }
                list.add(
                    MaterialSection(
                        title = obj.optString("title", ""),
                        content = obj.optString("content", ""),
                        formulaOrEquation = if (obj.has("formula")) obj.getString("formula") else null,
                        highlights = hlList
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
