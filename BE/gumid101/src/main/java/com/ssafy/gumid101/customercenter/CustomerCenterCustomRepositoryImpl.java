package com.ssafy.gumid101.customercenter;

import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.gumid101.entity.QQuestionEntity;
import com.ssafy.gumid101.entity.QUserEntity;
import com.ssafy.gumid101.req.DateOder;
import com.ssafy.gumid101.req.QuestionReqDto;
import com.ssafy.gumid101.req.QuestionSelectParameter;
import com.ssafy.gumid101.res.QuestionResDto;

import io.jsonwebtoken.lang.Strings;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerCenterCustomRepositoryImpl implements CustomerCenterCustomRepository {
	/*
	 * 
	 * private String title; private String questionSeq; private DateOder dateOrder;
	 * private QuestionStatus status;
	 * 
	 */
	private final JPAQueryFactory jpaQueryFactory;

	private BooleanBuilder generagteBuilder(QuestionSelectParameter params, QQuestionEntity question) {

		BooleanBuilder builder = new BooleanBuilder();

		if (Strings.hasLength(params.getTitle())) {
			builder.and(question.questionTitle.contains(params.getTitle()));
		}
		try {
			Long questionSeq = Long.parseLong(params.getQuestionSeq());
			builder.and(question.questionSeq.eq(questionSeq));
		} catch (Exception e) {

		}

		if (params.getStatus() != null) {
			if (params.getStatus() == QuestionStatus.COMPLETE) {
				builder.and(question.questionStatus.eq(QuestionStatus.COMPLETE));
			} else if (params.getStatus() == QuestionStatus.PROCESSING) {
				builder.and(question.questionStatus.eq(QuestionStatus.PROCESSING));
			} else if (params.getStatus() == QuestionStatus.WAITING) {
				builder.and(question.questionStatus.eq(QuestionStatus.WAITING));
			}
		}

		return builder;
	}

	@Override
	public List<QuestionResDto> selectQuestionByParam(QuestionSelectParameter params) {

		QQuestionEntity question = new QQuestionEntity("q");

		BooleanBuilder builder = generagteBuilder(params, question);

		JPAQuery<QuestionResDto> jpaQuery = jpaQueryFactory.from(question)
				.select(Projections.fields(QuestionResDto.class, question.questionContent.as("content"),
						question.questionTitle.as("title"), question.questionRegTime.as("regTime"),
						question.questionStatus.as("status"), question.questionSeq.as("questionSeq"),
						question.userEntity.userSeq.as("userSeq"), question.userEntity.userState.as("userState"),
						question.userEntity.nickName.as("nickName"), question.userEntity.email.as("email")))
				.leftJoin(question.userEntity).fetchJoin().where(builder);

		if (params.getDateOrder() != null) {
			if (params.getDateOrder() == DateOder.DESC) {
				jpaQuery.orderBy(question.questionRegTime.desc());
			} else if (params.getDateOrder() == DateOder.ASC) {
				jpaQuery.orderBy(question.questionRegTime.asc());
			}
		}

		Long offset = (long) ((params.getCurrentPage() - 1) * params.getPageItemSize());
		Long limit = (long) params.getPageItemSize();

		List<QuestionResDto> resultQuestionList = jpaQuery.offset(offset).limit(limit).fetch(); // 검색 조건 해당하는 것들 불러온다.

		return resultQuestionList;

	}

	public Long selectCountQuestionByParam(QuestionSelectParameter params) {

		QQuestionEntity question = new QQuestionEntity("q");

		BooleanBuilder builder = generagteBuilder(params, question);

		Long result = jpaQueryFactory.from(question).select(Projections.fields(Long.class, question.count()))
				.where(builder).fetchOne();

		return result;
	}
}