package com.example.elearning.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MaterialDaoTest : InMemoryDatabaseTest() {
    @Test
    fun filterDownloadClearAndCascade() = runTest {
        seedIfEmpty(db)
        val dao = db.materialDao()
        val material = SeedData.materials.first()
        assertEquals(listOf(material), dao.observeByCourse(material.courseId).first())

        val path = "/offline/zmienne.txt"
        dao.markDownloaded(material.id, path)
        assertEquals(material.copy(isDownloaded = true, localPath = path), dao.observeById(material.id).first())
        dao.clearDownload(material.id)
        assertEquals(material, dao.observeById(material.id).first())

        db.openHelper.writableDatabase.delete("courses", "id = ?", arrayOf(material.courseId))
        assertEquals(null, dao.observeById(material.id).first())
        assertEquals(2, dao.observeByCourse("android").first().size)
    }
}
