-- 운영자 신고 처리: 숨김·정지는 시각을 기록하고, 비우면 되돌린다
alter table diaries add column hidden_at timestamptz;

alter table corrections add column hidden_at timestamptz;

alter table profiles add column profile_image_hidden_at timestamptz;

alter table users add column suspended_at timestamptz;

alter table reports add column status varchar(20) not null default 'PENDING';
alter table reports add column handled_at timestamptz;
alter table reports add column memo varchar(500);
alter table reports add constraint ck_reports_status
    check (status in ('PENDING', 'DISMISSED', 'HIDDEN', 'SUSPENDED'));

create index idx_reports_status_created_at on reports (status, created_at desc);
create index idx_reports_target on reports (target_type, target_id);
