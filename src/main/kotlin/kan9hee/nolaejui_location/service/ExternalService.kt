package kan9hee.nolaejui_location.service

import kan9hee.nolaejui_location.dto.PlayLogReportDto
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service

@Service
class ExternalService(@GrpcClient("nolaejui-management")
                      private val managementStub: AdminResponseServerGrpcKt.AdminResponseServerCoroutineStub) {

    suspend fun reportPlayLogProblem(
        userId:String,
        playLogReportDto: PlayLogReportDto
    ): String? {
        val request = Location.PlayLogProblem.newBuilder()
            .setPlayLog(Location.PlayLogByLocation.newBuilder()
                .setLogId(playLogReportDto.playLogId)
                .setMusicId(playLogReportDto.playLog.musicId)
                .setUserName(userId)
                .setLocationInfo(
                    Location.LocationInfo.newBuilder()
                        .setLongitude(playLogReportDto.playLog.longitude)
                        .setLatitude(playLogReportDto.playLog.latitude)
                )
            )
            .setProblemCase(playLogReportDto.problemCase)
            .setProblemDetail(playLogReportDto.problemDetail)
            .build()

        val response = managementStub.reportPlayLogProblem(request)
        return response.resultMessage
    }
}