package kan9hee.nolaejui_location.service

import kan9hee.nolaejui_location.dto.PlayLogByLocationDto
import kan9hee.nolaejui_location.dto.PlayLogReportDto
import kan9hee.nolaejui_location.entity.PlayLogByLocation
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class PlayLogService(@GrpcClient("nolaejui-management")
                     private val managementStub: AdminResponseServerGrpcKt.AdminResponseServerCoroutineStub,
                     private val mongoTemplate: MongoTemplate) {
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

    fun getNearbyPlayLog(longitude: Double,latitude: Double): List<Long> {
        val query = Query().addCriteria(
            Criteria.where("location").nearSphere(
                Point(longitude,latitude)
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

    suspend fun reportPlayLogProblem(playLogReportDto: PlayLogReportDto): String? {
        val request = Location.PlayLogProblem.newBuilder()
            .addPlayLog(
                Location.PlayLogByLocation.newBuilder()
                    .setLogId(playLogReportDto.playLogId)
                    .setMusicId(playLogReportDto.playLog.musicId)
                    .setUserName(playLogReportDto.playLog.userInfo)
                    .setLongitude(playLogReportDto.playLog.longitude)
                    .setLatitude(playLogReportDto.playLog.latitude)
                    .build()
            )
            .setProblemCase(playLogReportDto.problemCase)
            .setProblemDetail(playLogReportDto.problemDetail)
            .build()

        val response = managementStub.reportPlayLogProblem(request)
        return response.resultMessage
    }
}