package kan9hee.nolaejui_location

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class NolaejuiLocationApplication

fun main(args: Array<String>) {
	runApplication<NolaejuiLocationApplication>(*args)
}
