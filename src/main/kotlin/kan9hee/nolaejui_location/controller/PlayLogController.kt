package kan9hee.nolaejui_location.controller

import kan9hee.nolaejui_location.dto.PlayLogReportDto
import kan9hee.nolaejui_location.service.PlayLogService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/playLog")
class PlayLogController(private val playLogService: PlayLogService) {

    @GetMapping("/getNearbyMusicId")
    fun getNearbyMusicId(
        @RequestParam(value = "longitude")
        longitude:Double,
        @RequestParam(value = "latitude")
        latitude:Double): List<Long> {
        return playLogService.getNearbyPlayLog(longitude,latitude)
    }

    @PostMapping("/reportMusicPlayLog")
    suspend fun reportMusicPlayLog(@RequestBody playLogReportDto: PlayLogReportDto): String? {
        return playLogService.reportPlayLogProblem(playLogReportDto)
    }
}