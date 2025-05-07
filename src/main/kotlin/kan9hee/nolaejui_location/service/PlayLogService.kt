package kan9hee.nolaejui_location.service

import kotlinx.coroutines.reactor.awaitSingle
import kan9hee.nolaejui_location.dto.CurrentLocationDto
import kan9hee.nolaejui_location.dto.PlayLogByLocationDto
import kan9hee.nolaejui_location.entity.PlayLogByLocation
import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class PlayLogService(private val reactiveMongoTemplate: ReactiveMongoTemplate,
                     private val redisTemplate: StringRedisTemplate) {

    suspend fun addPlayLog(
        userId:String,
        playLogByLocationDto: PlayLogByLocationDto
    ): PlayLogByLocation? {
        val newPlayLog = PlayLogByLocation(
            playLogByLocationDto.musicId,
            userId,
            GeoJsonPoint(
                playLogByLocationDto.longitude,
                playLogByLocationDto.latitude
            )
        )
        return reactiveMongoTemplate.save(newPlayLog).awaitSingle()
    }

    suspend fun addPickablePlayLog(
        userId:String,
        playLogByLocationDto: PlayLogByLocationDto
    ){
        val point = Point(playLogByLocationDto.longitude, playLogByLocationDto.latitude)
        val member = "${playLogByLocationDto.musicId}:${userId}"
        val now = System.currentTimeMillis()

        redisTemplate.opsForGeo()
            .add("pickablePlaylog", point, member)
        redisTemplate.opsForHash<String, String>()
            .put("pickablePlaylog:timestamp", member, now.toString())
    }

    suspend fun getNearbyPlayLog(currentLocationDto: CurrentLocationDto): List<Long> {
        val searchResults = redisTemplate.opsForGeo().radius(
            "pickablePlaylog",
            Circle(
                Point(currentLocationDto.longitude, currentLocationDto.latitude),
                Distance(30.0, RedisGeoCommands.DistanceUnit.METERS)
            )
        )

        val resultList = searchResults?.content?.map { geoLocation ->
            val member = geoLocation.content.name
            val (musicId, _) = member.split(":")
            musicId.toLong()
        } ?: emptyList()

        return resultList
    }

    @Scheduled(fixedRate = 3600000)
    fun cleanOldPlayLogs() {
        val geoOps = redisTemplate.opsForGeo()
        val hashOps = redisTemplate.opsForHash<String, String>()

        val timestampMap = hashOps.entries("pickablePlaylog:timestamp")
        val now = System.currentTimeMillis()
        val expireTimeMillis = 60 * 60 * 1000 * 12

        timestampMap.forEach { (member, timestamp) ->
            val timestamp = timestamp.toLongOrNull() ?: return@forEach
            if (now - timestamp > expireTimeMillis) {
                geoOps.remove("pickablePlaylog", member)
                hashOps.delete("pickablePlaylog:timestamp", member)
            }
        }
    }

    suspend fun deletePlayLog(logId:String): Boolean {
        val query = Query().addCriteria(Criteria.where("_id").`is`(logId))
        val result = reactiveMongoTemplate.remove(query,PlayLogByLocation::class.java).awaitSingle()
        return result.deletedCount > 0
    }
}