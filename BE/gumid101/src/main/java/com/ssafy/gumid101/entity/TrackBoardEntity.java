package com.ssafy.gumid101.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

/**
 * 추천 경로게시판
 * @author start
 *
 */
@Getter
@Setter
@Entity
@Table(name="t_track_board")
@EntityListeners(AuditingEntityListener.class)
public class TrackBoardEntity {

	@Id
	@Column(name="track_board_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long trackBoardSeq;
	
	@JoinColumn(name="run_record_seq")
	@ManyToOne
	private RunRecordEntity runRecord;
	
	@Column(name="track_board_hard_point")
	private int trackBoardHardPoint;
	
	@Column(name="track_board_enviroment_point")
	private int trackBoardEnviromentPoint;
	
	@CreatedDate
	@Column(name="track_board_reg_time")
	private LocalDateTime trackBoardRegTime;
}