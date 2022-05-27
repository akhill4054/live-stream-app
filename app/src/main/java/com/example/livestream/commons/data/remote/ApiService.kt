package com.example.livestream.commons.data.remote

import com.example.livestream.commons.data.models.auth.AuthenticateUser
import com.example.livestream.commons.data.models.commons.LiveStream
import com.example.livestream.commons.data.models.streamings.LikeLiveStream
import com.example.livestream.commons.data.models.streamings.ScheduleLiveStream
import com.example.livestream.commons.data.models.streamings.WatchLiveStream
import com.example.livestream.commons.data.models.user.UpdateUserProfile
import com.example.livestream.commons.data.models.user.UserProfile
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Home
    @GET("/home/api/v1/get-recommended-live-streams/")
    suspend fun getRecommendedLiveStreams(
        @Query("after") after: String? = null,
        @Query("count") count: Int? = null,
        @Query("is_live") isLive: Boolean? = null,
        @Query("is_popular") isPopular: Boolean? = null,
    ): Response<List<LiveStream>>

    @GET("/home/api/v1/search-live-streams/")
    suspend fun searchLiveStreams(
        @Query("query") query: String,
        @Query("after") after: String? = null,
        @Query("count") count: Int? = null,
        @Query("is_live") isLive: Boolean? = null,
        @Query("is_popular") isPopular: Boolean? = null,
    ): Response<List<LiveStream>>


    // Auth & login
    @POST("/auth/api/v1/authenticate-user/")
    suspend fun authenticateUser(@Body body: AuthenticateUser.Request): Response<AuthenticateUser.Response>


    // User
    @GET("/users/api/v1/get-user-profile/")
    suspend fun getUserProfile(): Response<UserProfile>

    @Multipart
    @POST("/users/api/v1/update-user-profile/")
    suspend fun updateUserProfile(
        @Part picFilePart: MultipartBody.Part?,
        @Part("user_profile") userProfile: RequestBody,
    ): Response<UpdateUserProfile.Response>


    // Streaming
    @Multipart
    @POST("/streamings/api/v1/schedule-live-stream/")
    suspend fun scheduleLiveStream(
        @Part thumbnail: MultipartBody.Part?,
        @Part("streaming_details") streamingDetails: RequestBody,
    ): Response<ScheduleLiveStream.Response>

    @Multipart
    @POST("/streamings/api/v1/schedule-live-stream/")
    suspend fun editScheduleLiveStream(
        @Query("streaming_id") streamingId: String,
        @Part thumbnail: MultipartBody.Part?,
        @Part("streaming_details") streamingDetails: RequestBody,
    ): Response<ScheduleLiveStream.Response>

    @Multipart
    @POST("/streamings/api/v1/start-live-stream/")
    suspend fun startLiveStream(
        @Part thumbnail: MultipartBody.Part?,
        @Part("streaming_details") streamingDetails: RequestBody,
    ): Response<ScheduleLiveStream.Response>

    @GET("/streamings/api/v1/watch-live-stream/")
    suspend fun watchLiveStream(@Query("streaming_id") streamingId: String): Response<WatchLiveStream>

    @POST("/streamings/api/v1/like-live-stream/")
    suspend fun likeLiveStream(@Query("streaming_id") streamingId: String): Response<LikeLiveStream.Response>

    @POST("/streamings/api/v1/dislike-live-stream/")
    suspend fun dislikeLiveStream(@Query("streaming_id") streamingId: String): Response<LikeLiveStream.Response>

    @POST("/streamings/api/v1/end-live-stream/")
    suspend fun endLiveStream(@Query("streaming_id") streamingId: String): Response<LikeLiveStream.Response>
}
