package com.deary.server.domain.diary.repository;

import com.deary.server.domain.diary.entity.DiaryPhoto;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryPhotoRepository extends JpaRepository<DiaryPhoto, Long> {

	List<DiaryPhoto> findAllDiaryPhotoByDiaryIdInAndSortOrder(Collection<Long> diaryIds, int sortOrder);

}
