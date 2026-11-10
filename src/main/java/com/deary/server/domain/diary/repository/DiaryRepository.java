package com.deary.server.domain.diary.repository;

import com.deary.server.domain.diary.entity.Diary;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

	Optional<Diary> findDiaryByUserIdAndDiaryDate(Long userId, LocalDate diaryDate);

	List<Diary> findAllDiaryByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(Long userId, LocalDate from, LocalDate to);

}
