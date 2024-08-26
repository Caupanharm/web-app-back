package perso.caupanharm.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Value("\${henrik3.api.baseurl}")
    lateinit var henrik3BaseUrl: String

    @Value("\${henrik3.api.key}")
    lateinit var henrik3ApiKey: String

    @Bean
    fun Henrik3Client(builder: WebClient.Builder): WebClient =
        builder
            .baseUrl(henrik3BaseUrl)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, henrik3ApiKey)
            .build()
}