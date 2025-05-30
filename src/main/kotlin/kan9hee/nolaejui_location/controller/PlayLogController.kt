package kan9hee.nolaejui_location.controller

import kan9hee.nolaejui_location.dto.CurrentLocationDto
import kan9hee.nolaejui_location.dto.PlayLogByLocationDto
import kan9hee.nolaejui_location.dto.PlayLogReportDto
import kan9hee.nolaejui_location.service.ExternalService
import kan9hee.nolaejui_location.service.PlayLogService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/location")
class PlayLogController(private val externalService: ExternalService,
                        private val playLogService: PlayLogService) {

    @GetMapping("/getNearbyMusicId")
    suspend fun getNearbyMusicId(
        @RequestParam longitude: Double,
        @RequestParam latitude: Double
    ): List<Long> {
        val currentLocationDto = CurrentLocationDto(longitude, latitude)
        return playLogService.getNearbyPlayLog(currentLocationDto)
    }

    @PostMapping("/addMusicPlayLog")
    suspend fun addMusicPlayLog(
        @RequestHeader("X-User-Id") userId:String,
        @RequestBody playLogByLocationDto: PlayLogByLocationDto
    ) {
        playLogService.addPlayLog(userId,playLogByLocationDto)
        playLogService.addPickablePlayLog(userId,playLogByLocationDto)
    }

    @PostMapping("/reportMusicPlayLog")
    suspend fun reportMusicPlayLog(
        @RequestHeader("X-User-Id") userId:String,
        @RequestBody playLogReportDto: PlayLogReportDto
    ): String? {
        return externalService.reportPlayLogProblem(userId,playLogReportDto)
    }
}