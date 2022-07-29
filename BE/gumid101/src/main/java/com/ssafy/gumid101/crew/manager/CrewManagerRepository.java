package com.ssafy.gumid101.crew.manager;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ssafy.gumid101.entity.CrewEntity;
import com.ssafy.gumid101.entity.UserEntity;

@Repository //생략가능
public interface CrewManagerRepository extends JpaRepository<CrewEntity, Long>,CrewManagerCustomRepository {

	
	@Query(value = "SELECT ce FROM UserCrewJoinEntity ucj INNER JOIN ucj.crewEntity ce where ucj.userEntity =:userEntity AND ce.crewDateEnd >= :now")
	List<CrewEntity> findByUserSeqActive(UserEntity userEntity,LocalDateTime now);
	
	List<CrewEntity> findByCrewCheckYnAndCrewDateEndBefore(String crewCheckYn, LocalDateTime nowTime);
//	List<CrewEntity> findByCrewCheckYnIsNullAndCrewDateEndBefore(LocalDateTime nowTime);

	

	
	//@Query(value="")
	//int deleteCrewOnlyCrewMasterAndBeforeStart(Long user,Long ,LocalDateTime now);



}
