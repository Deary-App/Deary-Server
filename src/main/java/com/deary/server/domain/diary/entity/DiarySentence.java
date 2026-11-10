package com.deary.server.domain.diary.entity;

import com.deary.server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "diary_sentences")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiarySentence extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "sentence_index", nullable = false)
	private int sentenceIndex;

	@Column(name = "content", nullable = false, length = 2000)
	private String content;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "diary_id", nullable = false)
	private Diary diary;

	private DiarySentence(Diary diary, int sentenceIndex, String content) {
		this.diary = diary;
		this.sentenceIndex = sentenceIndex;
		this.content = content;
	}

	static DiarySentence create(Diary diary, int sentenceIndex, String content) {
		return new DiarySentence(diary, sentenceIndex, content);
	}

}
