package com.ssafy.runwithme.datasource

import com.ssafy.runwithme.api.RecommendApi
import com.ssafy.runwithme.base.BaseResponse
import com.ssafy.runwithme.model.dto.TrackBoardDto
import com.ssafy.runwithme.model.response.RecommendResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RecommendRemoteDataSource @Inject constructor(
    private val recommendApi: RecommendApi
){
    fun createRecommend(environmentPoint: Int, hardPoint: Int, RunRecordSeq: Int): Flow<BaseResponse<TrackBoardDto>> = flow {
        emit(recommendApi.createRecommend(environmentPoint, hardPoint, RunRecordSeq))
    }

    fun getRecommends(leftLng: Double, lowerLat: Double, rightLng: Double, upperLat: Double): Flow<BaseResponse<List<RecommendResponse>>> = flow {
        emit(recommendApi.getRecommends(leftLng, lowerLat, rightLng, upperLat))
    }
}