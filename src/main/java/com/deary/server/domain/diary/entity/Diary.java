package com.deary.server.domain.diary.entity;

import com.deary.server.domain.profile.entity.Language;
import com.deary.server.domain.user.entity.User;
import com.deary.server.global.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "diaries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "diary_date", nullable = false)
	private LocalDate diaryDate;

	@Column(name = "title", nullable = false, length = 80)
	private String title;

	@Column(name = "content", nullable = false, length = 2000)
	private String content;

	@Column(name = "language", nullable = false, length = 2)
	private Language language;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OrderBy("sortOrder asc")
	@OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DiaryPhoto> photos = new ArrayList<>();

	@OrderBy("sentenceIndex asc")
	@OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DiarySentence> sentences = new ArrayList<>();

	private Diary(User user, LocalDate diaryDate, String title, String content, Language language) {
		this.user = user;
		this.diaryDate = diaryDate;
		this.title = title;
		this.content = content;
		this.language = language;
	}

	public static Diary create(User user, LocalDate diaryDate, String title, String content, Language language) {
		return new Diary(user, diaryDate, title, content, language);
	}

	public void addPhoto(String storagePath, DiaryPhoto.Layout layout) {
		photos.add(DiaryPhoto.create(this, storagePath, photos.size(), layout));
	}

	public void addSentence(String sentence) {
		sentences.add(DiarySentence.create(this, sentences.size(), sentence));
	}

	public boolean isWrittenBy(Long userId) {
		return user.getId().equals(userId);
	}

}
