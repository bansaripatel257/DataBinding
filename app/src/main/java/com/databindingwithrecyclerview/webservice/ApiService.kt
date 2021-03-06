package com.shopomy.webservice

import com.databindingwithrecyclerview.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by sa on 31/03/17.
 *
 *
 * Initialisation of Retrofit library with Okhttp3 and bind a base url.
 */

class ApiService {

    var apiInterface: ApiInterface? = null
        private set
    var secureNetworkApi: ApiInterface? = null
        private set

    init {
        initDefaultRetrofitService()
    }

    private fun initSecureRetrofitService() {
        try {

            //set default time out
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(5, TimeUnit.MINUTES)
            builder.readTimeout(5, TimeUnit.MINUTES)
            builder.writeTimeout(5, TimeUnit.MINUTES)


            /* builder.addInterceptor(object : Interceptor {
                 override fun intercept(chain: Interceptor.Chain?): Response {
                     val requestBuilder = chain!!.request().newBuilder()
                     requestBuilder.addHeader(ApiConstant.HEADER_LOCAL_LANGUAGE, getCountryShortName())
                     val secretToken = App.instance!!.preference!!.getData(Constants.PREF_SECRETE_TOKEN, "")
                     Log.e("secreteToken", secretToken)
                     Log.e("countrykey", getCountryShortName())
                     if (!TextUtils.isEmpty(secretToken)) {
                         requestBuilder.addHeader(ApiConstant.HEADER_AUTHORIZATION_NAME, secretToken)
                     }
                     return chain.proceed(requestBuilder.build())
                 }
             })*/


            // enable logging
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(logging)
            } else {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.NONE
                builder.addInterceptor(logging)
            }
            val retrofit = Retrofit.Builder()
                    .baseUrl(ApiConstant.HTTP_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(builder.build())
                    .build()

            apiInterface = retrofit.create<ApiInterface>(ApiInterface::class.java!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun initDefaultRetrofitService() {
        //set default time out
        try {
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            builder.readTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            // Install the all-trusting trust manager
            //            final SSLContext sslContext = SSLContext.getInstance("TLS");
            //            TLSSocketFactory tlsSocketFactory = new TLSSocketFactory();
            //builder.socketFactory(tlsSocketFactory);
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers
            if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
            }
            val trustManager = trustManagers[0] as X509TrustManager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
            val sslSocketFactory = sslContext.socketFactory
            builder.sslSocketFactory(sslSocketFactory, trustManager)
            //builder.followSslRedirects(true);
            builder.addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain?): Response {
                    val requestBuilder = chain!!.request().newBuilder()
                    requestBuilder.addHeader(ApiConstant.HEADER_NEWS_API_KEY, ApiConstant.NEWS_KEY)
                    return chain.proceed(requestBuilder.build())
                }
            })

            // enable logging
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(logging)
            } else {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.NONE
                builder.addInterceptor(logging)
            }


            val retrofit = Retrofit.Builder()
                    .baseUrl(ApiConstant.HTTP_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(builder.build())
                    .build()

            apiInterface = retrofit.create<ApiInterface>(ApiInterface::class.java!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    companion object {
        private val DEFAULT_TIMEOUT = 180*1000
    }

}
