package kan9hee.nolaejui_location.service

import PlayLogServerGrpcKt
import kan9hee.nolaejui_location.dto.PlayLogByLocationDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lognet.springboot.grpc.GRpcService

@GRpcService
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

    override suspend fun addMusicPlayLog(request: Location.PlayLogByLocation): Location.GrpcResult {
        val addResult = playLogService.addPlayLog(
            PlayLogByLocationDto(
                request.musicId,
                request.userName,
                request.longitude,
                request.latitude
            )
        )
        val resultMessage = if(addResult) "delete success" else "delete failed"

        return withContext(Dispatchers.Default){
            Location.GrpcResult.newBuilder()
                .setIsSuccess(addResult)
                .setResultMessage(resultMessage)
                .build()
        }
    }
}