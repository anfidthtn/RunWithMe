package com.ssafy.gumid101.user;

import java.util.List;

import com.ssafy.gumid101.dto.CrewTotalRecordDto;
import com.ssafy.gumid101.entity.CrewBoardEntity;
import com.ssafy.gumid101.entity.UserEntity;
import com.ssafy.gumid101.res.RankingDto;

public interface UserCustomRepository {

	List<CrewBoardEntity> findUserBoardsWithOffestAndSize(UserEntity user, Long size, Long boardMaxSeq);

	List<RankingDto> getUserTotalPointRanking(Long size, Long offset);

	CrewTotalRecordDto getMyTotalRecord(UserEntity userEntity) throws Exception;
}
