package com.deary.server.domain.diary.repository;

import com.deary.server.domain.diary.entity.Diary;
import com.deary.server.domain.profile.entity.Language;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

	Optional<Diary> findDiaryByUserIdAndDiaryDate(Long userId, LocalDate diaryDate);

	List<Diary> findAllDiaryByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(Long userId, LocalDate from, LocalDate to);

	@Query("""
			select d from Diary d
			where (:language is null or d.language = :language)
			and (:cursor is null or d.id < :cursor)
			order by d.id desc
			""")
	List<Diary> findAllPublicDiary(@Param("language") Language language, @Param("cursor") Long cursor, Limit limit);

	@Query("""
			select d from Diary d
			where d.user.id = :userId
			and (:cursor is null or d.id < :cursor)
			order by d.id desc
			""")
	List<Diary> findAllPublicDiaryByUserId(@Param("userId") Long userId, @Param("cursor") Long cursor, Limit limit);

}
