package kan9hee.nolaejui_location.controller

import kan9hee.nolaejui_location.dto.CurrentLocationDto
import kan9hee.nolaejui_location.dto.PlayLogByLocationDto
import kan9hee.nolaejui_location.dto.PlayLogReportDto
import kan9hee.nolaejui_location.service.ExternalService
import kan9hee.nolaejui_location.service.PlayLogService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/location")
class PlayLogController(private val externalService: ExternalService,
                        private val playLogService: PlayLogService) {

    @GetMapping("/getNearbyMusicId")
    fun getNearbyMusicId(@RequestParam(value = "location") currentLocationDto: CurrentLocationDto): List<Long> {
        return playLogService.getNearbyPlayLog(currentLocationDto)
    }

    @PostMapping("/addMusicPlayLog")
    suspend fun addMusicPlayLog(@RequestBody playLogByLocationDto: PlayLogByLocationDto) {
        playLogService.addPlayLog(playLogByLocationDto)
    }

    @PostMapping("/reportMusicPlayLog")
    suspend fun reportMusicPlayLog(@RequestBody playLogReportDto: PlayLogReportDto): String? {
        return externalService.reportPlayLogProblem(playLogReportDto)
    }
}