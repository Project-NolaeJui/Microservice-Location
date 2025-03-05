package kan9hee.nolaejui_location.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kan9hee.nolaejui_location.dto.CurrentLocationDto
import kan9hee.nolaejui_location.dto.PlayLogByLocationDto
import kan9hee.nolaejui_location.dto.PlayLogRedisOnlyDto
import kan9hee.nolaejui_location.entity.PlayLogByLocation
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class PlayLogService(private val mongoTemplate: MongoTemplate,
                     private val redisTemplate: StringRedisTemplate) {

    private val logger = LoggerFactory.getLogger(PlayLogService::class.java)

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
            addPickablePlayLog(playLogByLocationDto)
            true
        } catch (e: Exception) {
            logger.error("Failed to add play log", e)
            false
        }
    }

    fun addPickablePlayLog(playLogByLocationDto: PlayLogByLocationDto){
        val redisCoordinates = normalizeCoordinates(
            playLogByLocationDto.latitude,
            playLogByLocationDto.longitude
        )
        val redisKey = "pickable_playlog:$redisCoordinates"
        val redisValue = jacksonObjectMapper().writeValueAsString(
            PlayLogRedisOnlyDto(
                playLogByLocationDto.musicId,
                playLogByLocationDto.userInfo,
                System.currentTimeMillis()
            )
        )
        val currentTime = System.currentTimeMillis().toDouble()

        redisTemplate.opsForZSet().add(redisKey, redisValue, currentTime)
    }

    fun getNearbyPlayLog(currentLocationDto: CurrentLocationDto): List<Long> {
        val normalizedCoordinates = normalizeCoordinates(
            currentLocationDto.latitude,
            currentLocationDto.longitude
        )
        val redisKey = "pickable_playlog:$normalizedCoordinates"

        val cachedResults = redisTemplate.opsForSet().members(redisKey)
            ?: return emptyList()

        return cachedResults.mapNotNull { result ->
            try {
                jacksonObjectMapper().readValue(result, PlayLogRedisOnlyDto::class.java).musicId
            } catch (e: Exception) {
                logger.error("Failed to parse Redis data: $result", e)
                null
            }
        }
    }

    @Scheduled(fixedRate = 3600000)
    fun cleanOldPlayLogs() {
        val cutLineTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(6)
        val keys = redisTemplate.keys("pickable_playlog:*")

        keys.forEach { key ->
            try {
                val values = redisTemplate.opsForSet().members(key)
                    ?: return@forEach
                val expiredValues = values.filter { value ->
                    try {
                        jacksonObjectMapper().readValue(value, PlayLogRedisOnlyDto::class.java).timestamp < cutLineTime
                    } catch (e: Exception) {
                        logger.error("Error parsing Redis data for cleanup", e)
                        false
                    }
                }
                if (expiredValues.isNotEmpty()) {
                    redisTemplate.opsForSet().remove(key, *expiredValues.toTypedArray())
                }
            } catch (e: Exception) {
                logger.error("Error cleaning old play logs", e)
            }
        }
    }

    fun deletePlayLog(logId:String): Boolean {
        val query = Query().addCriteria(
            Criteria.where("_id").`is`(logId)
        )

        val result = mongoTemplate.remove(query,PlayLogByLocation::class.java)
        return result.deletedCount>0
    }

    private fun normalizeCoordinates(latitude: Double, longitude: Double): String {
        val lat = (latitude * 1000).toInt() / 1000.0
        val lon = (longitude * 1000).toInt() / 1000.0
        return "$lat:$lon"
    }
}