package kan9hee.nolaejui_location.service

import PlayLogServerGrpcKt
import kan9hee.nolaejui_location.dto.CurrentLocationDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class GrpcService(private val playLogService: PlayLogService):PlayLogServerGrpcKt.PlayLogServerCoroutineImplBase() {

    override suspend fun deletePlayLog(request: Location.PlayLogId): Location.GrpcResult {
        val deleteResult = playLogService.deletePlayLog(request.id)
        val resultMessage = if(deleteResult) "delete success" else "delete failed"

        return withContext(Dispatchers.Default){
            Location.GrpcResult.newBuilder()
                .setIsSuccess(deleteResult)
                .setResultMessage(resultMessage)
                .build()
        }
    }

    override suspend fun pickupMusics(request: Location.LocationInfo): Location.PickupResult {
        val musicIds = playLogService.getNearbyPlayLog(
            CurrentLocationDto(
                request.longitude,
                request.latitude)
        )

        return withContext(Dispatchers.Default){
            Location.PickupResult.newBuilder()
                .addAllMusicIds(musicIds)
                .build()
        }
    }
}