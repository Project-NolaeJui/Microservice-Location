package kan9hee.nolaejui_location.dto

data class PlayLogReportDto(val playLogId:String,
                            val playLog:PlayLogByLocationDto,
                            val problemCase:String,
                            val problemDetail:String)
