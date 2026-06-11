package com.juntang2.unlink.di

import com.juntang2.unlink.data.di.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
object TestNetworkModule {
    
    @Provides
    @Singleton
    fun provideMockOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request()
                val url = request.url.toString()
                
                val responseBuilder = Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                
                when {
                    url.contains("bit.ly/short1") -> {
                        responseBuilder
                            .code(302)
                            .message("Found")
                            .header("Location", "https://example.com/target?utm_source=bitly&id=42")
                            .body("".toResponseBody("text/plain".toMediaType()))
                    }
                    url.contains("youtu.be/abc") -> {
                        responseBuilder
                            .code(302)
                            .message("Found")
                            .header("Location", "https://youtube.com/watch?v=abc&si=123")
                            .body("".toResponseBody("text/plain".toMediaType()))
                    }
                    url.contains("t.co/chain1") -> {
                        responseBuilder
                            .code(302)
                            .message("Found")
                            .header("Location", "https://bit.ly/short1")
                            .body("".toResponseBody("text/plain".toMediaType()))
                    }
                    url.contains("loop.com/a") -> {
                        responseBuilder
                            .code(302)
                            .message("Found")
                            .header("Location", "http://loop.com/b")
                            .body("".toResponseBody("text/plain".toMediaType()))
                    }
                    url.contains("loop.com/b") -> {
                        responseBuilder
                            .code(302)
                            .message("Found")
                            .header("Location", "http://loop.com/a")
                            .body("".toResponseBody("text/plain".toMediaType()))
                    }
                    url.contains("error500.com") -> {
                        responseBuilder
                            .code(500)
                            .message("Internal Server Error")
                            .body("Error".toResponseBody("text/plain".toMediaType()))
                    }
                    else -> {
                        responseBuilder
                            .code(200)
                            .message("OK")
                            .body("".toResponseBody("text/plain".toMediaType()))
                    }
                }
                responseBuilder.build()
            })
            .build()
    }
}
