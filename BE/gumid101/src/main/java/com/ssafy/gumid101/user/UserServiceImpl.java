package com.ssafy.gumid101.user;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.gumid101.aws.S3FileService;
import com.ssafy.gumid101.customexception.CustomException;
import com.ssafy.gumid101.customexception.DuplicateException;
import com.ssafy.gumid101.customexception.IllegalParameterException;
import com.ssafy.gumid101.customexception.NotFoundUserException;
import com.ssafy.gumid101.customexception.ThirdPartyException;
import com.ssafy.gumid101.dto.CrewTotalRecordDto;
import com.ssafy.gumid101.dto.ImageFileDto;
import com.ssafy.gumid101.dto.UserDto;
import com.ssafy.gumid101.entity.CrewBoardEntity;
import com.ssafy.gumid101.entity.ImageFileEntity;
import com.ssafy.gumid101.entity.UserEntity;
import com.ssafy.gumid101.firebase.FcmMessage;
import com.ssafy.gumid101.firebase.FirebaseMessageUtil;
import com.ssafy.gumid101.imgfile.ImageDirectory;
import com.ssafy.gumid101.imgfile.ImageFileRepository;
import com.ssafy.gumid101.res.CrewBoardFileDto;
import com.ssafy.gumid101.res.CrewBoardRes;
import com.ssafy.gumid101.res.UserFileDto;
import com.ssafy.gumid101.util.Nickname;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepo;
	private final S3FileService s3FileService;
	private final ImageFileRepository imageRepo;

	private final FirebaseMessageUtil fcmUtil;

	@Transactional
	@Override
	public UserDto setMyProfile(UserDto userDto) throws Exception {
		if (0 != userRepo.countByEmail(userDto.getEmail())) {
			// 이미 해당 이메일이 있으면
			throw new DuplicateException(String.format("%s은 이미 등록된 이메일 입니다.", userDto.getEmail()));
		}
		if (!Nickname.nickOk(userDto.getNickName())) {
			throw new IllegalParameterException("닉네임 규칙을 위반했습니다.");
		}
		UserEntity userEntity = UserEntity.builder().email(userDto.getEmail()).nickName(userDto.getNickName())
				.weight(userDto.getWeight()).height(userDto.getHeight()).role(Role.USER).build();
		try {
			userEntity.setPoint(100000);
			userRepo.save(userEntity);
		} catch (Exception e) {
			throw new CustomException("초기 프로필 설정 중 오류가 발생하였습니다.");
		}

		return UserDto.of(userEntity);
	}

	@Transactional
	@Override
	public int checkDupNickname(String nickname) throws Exception {

		return userRepo.countByNickName(nickname);

	}

	@Transactional
	@Override
	public UserFileDto getUserFileDtoById(Long userSeq) throws Exception {

		UserEntity userEntity = userRepo.findById(userSeq)
				.orElseThrow(() -> new NotFoundUserException("해당 유저를 찾을 수 없습니다."));

		ImageFileDto imgDto = ImageFileDto.of(userEntity.getImageFile());

		UserDto userDto = UserDto.of(userEntity);

		return new UserFileDto(userDto, imgDto);
	}

	@Transactional
	@Override
	public UserFileDto getUserProfileByNickname(String nickname) throws Exception {

		UserEntity user = userRepo.findByNickNameAndUserState(nickname, "N")
				.orElseThrow(() -> new NotFoundUserException("닉네임이 일치하는 사용자가 없습니다."));
		ImageFileDto imgDto = ImageFileDto.of(user.getImageFile());
		UserDto userDto = UserDto.of(user);
		// 타인이 사용가능하므로 주요정보는 가림
		userDto.hideInfo();
		return new UserFileDto(userDto, imgDto);
	}

	@Transactional
	@Override
	public UserFileDto editMyProfile(UserDto userDto, MultipartFile imgFile) throws Exception {

		UserEntity userEntity = userRepo.findById(userDto.getUserSeq())
				.orElseThrow(() -> new NotFoundUserException("해당 유저를 찾을 수 없습니다."));

		userEntity.setHeight(userDto.getHeight());
		userEntity.setWeight(userDto.getWeight());
		userEntity.setNickName(userDto.getNickName());
		userEntity.setGender(userDto.getGender());
		userEntity.setBirthYear(userDto.getBirthYear());
		userEntity.setRegion(userDto.getRegion());
		userEntity.setJob(userDto.getJob());

		ImageFileDto imageFileDto = null;
		ImageFileEntity imageEntity = null;
		if (imgFile != null && !imgFile.isEmpty()) {

			try {

				imageFileDto = s3FileService.upload(imgFile, ImageDirectory.PROFILE.getPath());

			} catch (Exception e) {
				throw new ThirdPartyException("S3 Buket 파일 업로드 중 오류가 발생하였습니다.");
			}

			imageEntity = ImageFileEntity.builder().imgOriginalName(imgFile.getOriginalFilename())
					.imgSavedName(imageFileDto.getImgSavedName()).imgSavedPath(imageFileDto.getImgSavedPath()).build();

			imageRepo.save(imageEntity);
			userEntity.setImageFile(imageEntity);
		}

		return new UserFileDto(UserDto.of(userEntity), ImageFileDto.of(imageEntity));
	}

	@Transactional // 쿼리 dsl을 같은 트랜잭션에 포함시켜 영속성을 살려야함
	@Override
	public List<CrewBoardFileDto> getMyBoards(Long userSeq, Long size, Long maxBoardSeq) throws Exception {

		UserEntity user = userRepo.findById(userSeq)
				.orElseThrow(() -> new UsernameNotFoundException("자신의 글 조회중, 유저를 특정할 수 없습니다."));

		List<CrewBoardEntity> myBoards = userRepo.findUserBoardsWithOffestAndSize(user, size, maxBoardSeq);

		List<CrewBoardFileDto> myBoardList = myBoards.stream().map(

				(entity) -> {					
					CrewBoardRes crewBoardRes = CrewBoardRes.of(entity);
				
				CrewBoardFileDto cbf = CrewBoardFileDto.builder().crewBoardDto(crewBoardRes)
						.imageFileDto(ImageFileDto.of(entity.getImgFile())).build();

				return cbf;}

		).collect(Collectors.toList());

		return myBoardList;
	}

	@Transactional
	@Override
	public CrewTotalRecordDto getMyTotalRecord(Long userSeq) throws Exception {
		UserEntity userEntity = userRepo.findById(userSeq)
				.orElseThrow(() -> new NotFoundUserException("해당 유저를 찾을 수 없습니다."));

		CrewTotalRecordDto crewTotalRecordDto = userRepo.getMyTotalRecord(userEntity);

		if (crewTotalRecordDto == null || crewTotalRecordDto.getTotalTime() == null
				|| crewTotalRecordDto.getTotalTime() == 0) {
			return CrewTotalRecordDto.builder() //
					.totalAvgSpeed(0.0) //
					.totalCalorie(0.0) //
					.totalDistance(0) //
					.totalLongestDistance(0) //
					.totalLongestTime(0) //
					.totalTime(0) //
					.build();
		} else {
			// 1m/s == 3.6km/h
			crewTotalRecordDto
					.setTotalAvgSpeed(3.6 * crewTotalRecordDto.getTotalDistance() / crewTotalRecordDto.getTotalTime());
		}
		return crewTotalRecordDto;
	}

	@Transactional
	@Override
	public boolean setUserFcmToken(Long userSeq, String fcmToken) throws Exception {

		UserEntity user = userRepo.findById(userSeq)
				.orElseThrow(() -> new NotFoundUserException("FCM 토큰 설정중, 유저를 특정할 수 없습니다."));

		user.setFcmToken(fcmToken);

		String meessageBody = String.format("%s 님 알림 설정이 정상적으로 완료되었습니다.", user.getNickName());
		fcmUtil.sendMessageTo(fcmToken, "RunWime [알림 설정]", meessageBody);
		return true;
	}

	@Transactional
	@Override
	public boolean deleteUserFcmToken(Long userSeq) throws Exception {
		UserEntity user = userRepo.findById(userSeq)
				.orElseThrow(() -> new NotFoundUserException("FCM 토큰 설정중, 유저를 특정할 수 없습니다."));
		user.setFcmToken(null);

		return true;
	}

	@Transactional
	@Override
	public boolean deleteMyAccount(Long userSeq) throws Exception {

		userRepo.deleteById(userSeq);

		return true;
	}
}
