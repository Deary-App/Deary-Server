-- AI 자동 검수: 신고한 사람 없이 시스템이 올린 신고를 같은 신고함에 기록한다
alter table reports alter column reporter_id drop not null;

alter table reports add column source varchar(10) not null default 'USER';
alter table reports add constraint ck_reports_source check (source in ('USER', 'AUTO'));
alter table reports add constraint ck_reports_reporter_source check (source = 'AUTO' or reporter_id is not null);

alter table reports drop constraint ck_reports_status;
alter table reports add constraint ck_reports_status
    check (status in ('PENDING', 'AUTO_HIDDEN', 'DISMISSED', 'HIDDEN', 'SUSPENDED'));

-- 대상마다 자동 검수 신고는 하나만 두고, 다시 걸리면 그 행을 갱신한다
create unique index uk_reports_auto_target on reports (target_type, target_id) where source = 'AUTO';
