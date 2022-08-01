package com.ssafy.runwithme.view.crew_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssafy.runwithme.base.BaseResponse
import com.ssafy.runwithme.model.dto.CrewBoardDto
import com.ssafy.runwithme.model.dto.ImageFileDto
import com.ssafy.runwithme.model.dto.PasswordDto
import com.ssafy.runwithme.model.dto.RunRecordDto
import com.ssafy.runwithme.model.response.CrewBoardResponse
import com.ssafy.runwithme.model.response.MyCurrentCrewResponse
import com.ssafy.runwithme.repository.CrewActivityRepository
import com.ssafy.runwithme.repository.CrewManagerRepository
import com.ssafy.runwithme.repository.CrewRepository
import com.ssafy.runwithme.utils.Result
import com.ssafy.runwithme.utils.SingleLiveEvent
import com.ssafy.runwithme.utils.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrewDetailViewModel @Inject constructor(
    private val crewActivityRepository: CrewActivityRepository,
    private val crewManagerRepository: CrewManagerRepository,
    private val crewRepository: CrewRepository
) : ViewModel(){

    private val _crewRunRecordList: MutableStateFlow<List<RunRecordDto>>
            = MutableStateFlow(listOf<RunRecordDto>
        (RunRecordDto(0, 0, "00:00:00", "23:59:59",
        0, 0, 0.0, 0,
        0.0, 0.0, "Y")))
    val crewRunRecordList get() = _crewRunRecordList.asStateFlow()

    private val _crewBoardsList: MutableStateFlow<List<CrewBoardResponse>>
        = MutableStateFlow(listOf(CrewBoardResponse(CrewBoardDto(0, "게시글이 없습니다.", "2022-01-01", "관리자", 0, ""), ImageFileDto(0, "", "", ""))))
    val crewBoardsList get() = _crewBoardsList.asStateFlow()

    private val _isCrewMember: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isCrewMember get() = _isCrewMember.asStateFlow()

//    private val _crewState: MutableStateFlow<>

    private val _successMsgEvent = SingleLiveEvent<String>()
    val successMsgEvent get() = _successMsgEvent

    private val _errorMsgEvent = SingleLiveEvent<String>()
    val errorMsgEvent get() = _errorMsgEvent


    fun getCrewBoards(crewSeq: Int, size: Int): Flow<PagingData<CrewBoardResponse>> {
        return crewActivityRepository.getCrewBoards(crewSeq, size).cachedIn(viewModelScope)
    }

    fun getRunRecords(crewSeq: String, size: Int): Flow<PagingData<RunRecordDto>> {
        return crewActivityRepository.getCrewRecords(crewSeq, size).cachedIn(viewModelScope)
    }

    fun getRunRecordsTop3(crewSeq: String, size: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            crewActivityRepository.getCrewRecordsTop3(crewSeq, size).collectLatest {
                if(it is Result.Success){
                    if(it.data.count != 0) {
                        _crewRunRecordList.value = it.data.data
                    }
                }else if(it is Result.Error){
                    _errorMsgEvent.postValue("오류가 발생했습니다.")
                }
            }
        }
    }

    fun getCrewBoardsTop3(crewSeq: Int, size: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            crewActivityRepository.getCrewBoardsTop3(crewSeq, size).collectLatest {
                if(it is Result.Success){
                    if(it.data.count != 0){
                        _crewBoardsList.value = it.data.data
                    }
                }else if(it is Result.Error){
                    _errorMsgEvent.postValue("오류가 발생했습니다.")
                }
            }
        }
    }

    fun checkCrewMember(crewSeq: Int){
        viewModelScope.launch (Dispatchers.IO) {
            crewManagerRepository.checkCrewMember(crewSeq).collectLatest {
                if(it is Result.Success){
                    _isCrewMember.value = it.data.data
                }
            }
        }
    }

    fun joinCrew(crewId: Int, password: String?){
        var passwordDto : PasswordDto? = null
        if(password != null){
            passwordDto = PasswordDto(password)
        }
        Log.d(TAG, "joinCrew: password : $passwordDto")
        viewModelScope.launch (Dispatchers.IO) {
            crewRepository.joinCrew(crewId, passwordDto).collectLatest {
                Log.d(TAG, "joinCrew: $it")
                if(it is Result.Success){
                    _successMsgEvent.postValue("가입 완료했습니다.")
                }else if(it is Result.Fail){
                    _errorMsgEvent.postValue(it.data.msg)
                }else if(it is Result.Error){
                    _errorMsgEvent.postValue("서버 에러 입니다.")
                }
            }
        }
    }

}