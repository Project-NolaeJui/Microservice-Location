package kan9hee.nolaejui_location.entity

import lombok.Getter
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "play-log")
@Getter
class PlayLogByLocation(val musicId:Long,
                        val userInfo:String,

                        @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
                        val location: GeoJsonPoint) {
}