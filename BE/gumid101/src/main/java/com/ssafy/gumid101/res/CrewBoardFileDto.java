package com.ssafy.gumid101.res;

import com.ssafy.gumid101.dto.ImageFileDto;

import lombok.Builder;
import lombok.Data;

/**
 * 크루 보드를 검색하는 모든 메소드에서 사용
 * @author start
 *
 */
@Builder
@Data
public class CrewBoardFileDto {
	
	
	private CrewBoardRes crewBoardDto;
	
	private  ImageFileDto imageFileDto;
}
