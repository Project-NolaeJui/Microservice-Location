package kan9hee.nolaejui_location.service

import kan9hee.nolaejui_location.dto.PlayLogReportDto
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service

@Service
class ExternalService(@GrpcClient("nolaejui-auth")
                      private val authStub: AuthServerGrpcKt.AuthServerCoroutineStub,
                      @GrpcClient("nolaejui-management")
                      private val managementStub: AdminResponseServerGrpcKt.AdminResponseServerCoroutineStub) {

    suspend fun getUsername(accessTokenString:String): String {
        val request = Location.AccessToken.newBuilder()
            .setAccessToken(accessTokenString)
            .build()

        val response = authStub.getUserName(request)
        if(!response.isSuccess)
            throw RuntimeException(response.resultMessage)

        return response.resultMessage
    }

    suspend fun reportPlayLogProblem(playLogReportDto: PlayLogReportDto): String? {
        val request = Location.PlayLogProblem.newBuilder()
            .setPlayLog(Location.PlayLogByLocation.newBuilder()
                .setLogId(playLogReportDto.playLogId)
                .setMusicId(playLogReportDto.playLog.musicId)
                .setUserName(playLogReportDto.playLog.userInfo)
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