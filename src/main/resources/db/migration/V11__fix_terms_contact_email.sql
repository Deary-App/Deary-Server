-- 약관 v3 문의처·개인정보 보호 담당 연락처의 메일 주소를 바로잡는다. 약관 버전은 그대로라 다시 동의받지 않는다.
update terms
set content = replace(content, 'deary1024@gmail.com', 'deary1025@gmail.com'),
    updated_at = now()
where content like '%deary1024@gmail.com%';
