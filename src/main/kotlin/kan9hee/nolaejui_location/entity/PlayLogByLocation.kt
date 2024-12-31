package kan9hee.nolaejui_location.entity

import lombok.Getter
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "play_log")
@Getter
class PlayLogByLocation(@Id
                        val id:ObjectId,

                        val musicId:Long,
                        val userInfo:String,

                        @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
                        val location: GeoJsonPoint) {
}