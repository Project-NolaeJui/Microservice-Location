package kan9hee.nolaejui_location.service

import kan9hee.nolaejui_location.dto.CurrentLocationDto
import kan9hee.nolaejui_location.dto.PlayLogByLocationDto
import kan9hee.nolaejui_location.entity.PlayLogByLocation
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class PlayLogService(private val mongoTemplate: MongoTemplate) {
    fun addPlayLog(playLogByLocationDto: PlayLogByLocationDto): Boolean {
        return try {
            val newPlayLog = PlayLogByLocation(
                playLogByLocationDto.musicId,
                playLogByLocationDto.userInfo,
                GeoJsonPoint(
                    playLogByLocationDto.longitude,
                    playLogByLocationDto.latitude
                )
            )
            mongoTemplate.save(newPlayLog)
            true
        } catch (e: Exception) {
            println("Failed to save PlayLog: ${e.message}")
            false
        }
    }

    fun getNearbyPlayLog(currentLocationDto: CurrentLocationDto): List<Long> {
        val query = Query().addCriteria(
            Criteria.where("location").nearSphere(
                Point(currentLocationDto.longitude,currentLocationDto.latitude)
            ).maxDistance(3.0/6378137.0)
        )

        return mongoTemplate
            .find(query,PlayLogByLocation::class.java)
            .map { it -> it.musicId }
    }

    fun deletePlayLog(logId:String): Boolean {
        val query = Query().addCriteria(
            Criteria.where("_id").`is`(logId)
        )

        val result = mongoTemplate.remove(query,PlayLogByLocation::class.java)
        return result.deletedCount>0
    }
}