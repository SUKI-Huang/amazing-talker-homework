package com.amazing.module.network.component

import android.content.Context
import android.util.Log
import com.amazing.extensions.className
import com.amazing.extensions.isDebugMode
import com.amazing.extensions.isJson
import com.amazing.extensions.stringifyRequestBody
import com.google.gson.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.net.URLDecoder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NetworkModule {
    companion object {
        private fun defaultGson(): Gson = GsonBuilder().setPrettyPrinting().registerTypeAdapter(Date::class.java, DateGsonAdapter()).create()
        fun defaultRetrofit(context: Context) = createRetrofit(context, defaultGson())
        private fun createRetrofit(context: Context, gson: Gson): Retrofit {
            fun getHeaders(request: Request?): String {
                val emptyHeaders = String.format("%14s%s", "headers : ", "null")
                if (request == null) return emptyHeaders
                return request.headers.names().joinToString("\n") { String.format("%14s%s", "headers : ", it + ":" + request.headers[it]) }
            }

            fun getParameters(request: Request?): String {
                val emptyParametersMessages = String.format("%14s%s", "parameters : ", "null")
                val stringifyBody: String = request?.stringifyRequestBody() ?: return emptyParametersMessages
                if (stringifyBody.isEmpty()) return emptyParametersMessages

                when {
                    stringifyBody.isJson -> {
                        // Raw data
                        return listOf(
                            String.format("%14s%s", "parameters : ", "raw json"),
                            stringifyBody.prependIndent("              ")
                        ).joinToString("\n")
                    }
                    stringifyBody.contains("Content-Transfer-Encoding:") -> {
                        // multi part
                        return listOf(
                            String.format("%14s%s", "parameters : ", "multipart"),
                            stringifyBody.prependIndent("              ")
                        ).joinToString("\n")
                    }
                    else -> {
                        // form data or other
                        val parametersMessages = arrayListOf<String>()
                        val parameters = stringifyBody.split("&".toRegex())
                        for (row in parameters) {
                            if (!row.contains("=")) continue
                            val elements = row.split("=")
                            if (elements.size != 2) continue
                            val key = URLDecoder.decode(elements[0], "UTF-8")
                            val value = URLDecoder.decode(elements[1], "UTF-8")
                            parametersMessages.add(String.format("%14s", "parameters : ") + String.format("%s:%s", key, value))
                        }
                        return if (parametersMessages.isEmpty()) {
                            listOf(
                                String.format("%14s%s", "parameters : ", "unknown"),
                                stringifyBody.prependIndent("              ")
                            ).joinToString("\n")
                        } else {
                            parametersMessages.joinToString("\n")
                        }
                    }
                }
            }

            // timeout value is depends on the server config,
            val defaultConnectTimeout = 60L
            val defaultWriteTimeout = 60L
            val defaultReadTimeout = 60L
            val builder = OkHttpClient.Builder()
                .connectTimeout(defaultConnectTimeout, TimeUnit.SECONDS)
                .writeTimeout(defaultWriteTimeout, TimeUnit.SECONDS)
                .readTimeout(defaultReadTimeout, TimeUnit.SECONDS)

            if (context.isDebugMode) {
                builder.addNetworkInterceptor { chain ->
                    val request = chain.request()
                    Log.i(
                        className,
                        "=\nNetworkInterceptor====API REQUEST==============================================================\n" +
                            String.format("%14s", "method : ") + request.method + "\n" +
                            String.format("%14s", "endpoint : ") + request.url + "\n" +
                            getHeaders(request) + "\n" +
                            getParameters(request) + "\n\n" +
                            "="
                    )
                    return@addNetworkInterceptor chain.proceed(request)
                }
            }

            val client = builder.cache(null).build()
            return Retrofit.Builder()
                // must set base url so Google is good :D
                .baseUrl("https://www.google.com")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                // enable it if you are using Rx Flowable as api interface
                // .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
        }
    }

    class DateGsonAdapter : JsonSerializer<Date>, JsonDeserializer<Date> {
        private val dateFormatFirst = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("GMT") }

        // if you have different data format from each apis, add it as you want or make it as array to process
        // private val dateFormatSecond = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply { timeZone = TimeZone.getTimeZone("GMT") }

        @Synchronized
        override fun serialize(date: Date, type: Type, jsonSerializationContext: JsonSerializationContext): JsonElement {
            return JsonPrimitive(dateFormatFirst.format(date))
        }

        @Synchronized
        override fun deserialize(jsonElement: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext): Date? {
            return try {
                // due to the data format is string from api, so consider the element as String
                dateFormatFirst.parse(jsonElement.asString)
            } catch (exception: ParseException) {
                throw JsonParseException(exception)
            }
        }
    }
}
