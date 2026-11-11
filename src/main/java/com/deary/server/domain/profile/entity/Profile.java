package com.deary.server.domain.profile.entity;

import com.deary.server.domain.user.entity.User;
import com.deary.server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseTimeEntity {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "nickname", nullable = false, length = 20)
	private String nickname;

	@Column(name = "birth_date", nullable = false)
	private LocalDate birthDate;

	@Column(name = "nationality", nullable = false, length = 2)
	private String nationality;

	@Column(name = "native_language", nullable = false, length = 2)
	private Language nativeLanguage;

	@Column(name = "learning_language", nullable = false, length = 2)
	private Language learningLanguage;

	@Enumerated(EnumType.STRING)
	@Column(name = "learning_level", nullable = false, length = 10)
	private LearningLevel learningLevel;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender", nullable = false, length = 15)
	private Gender gender;

	@Column(name = "profile_image_path")
	private String profileImagePath;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	private Profile(User user, ProfileInfo info) {
		this.user = user;
		apply(info);
	}

	public static Profile create(User user, ProfileInfo info) {
		return new Profile(user, info);
	}

	public void modify(ProfileInfo info) {
		apply(info);
	}

	private void apply(ProfileInfo info) {
		this.nickname = info.nickname();
		this.birthDate = info.birthDate();
		this.nationality = info.nationality();
		this.nativeLanguage = info.nativeLanguage();
		this.learningLanguage = info.learningLanguage();
		this.learningLevel = info.learningLevel();
		this.gender = info.gender();
		this.profileImagePath = info.profileImagePath();
	}

	public record ProfileInfo(
			String nickname,
			LocalDate birthDate,
			String nationality,
			Language nativeLanguage,
			Language learningLanguage,
			LearningLevel learningLevel,
			Gender gender,
			String profileImagePath
	) {
	}

}
