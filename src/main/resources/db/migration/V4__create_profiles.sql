create table profiles (
    user_id bigint primary key references users (id) on delete cascade,
    nickname varchar(20) not null,
    birth_date date not null,
    nationality varchar(2) not null,
    native_language varchar(2) not null,
    learning_language varchar(2) not null,
    learning_level varchar(10) not null,
    gender varchar(15) not null,
    profile_image_path varchar(255),
    version bigint not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint ck_profiles_native_language check (native_language in ('ko', 'ja', 'en')),
    constraint ck_profiles_learning_language check (learning_language in ('ko', 'ja', 'en')),
    constraint ck_profiles_languages_differ check (native_language <> learning_language),
    constraint ck_profiles_learning_level check (learning_level in ('HIGH', 'MEDIUM', 'LOW')),
    constraint ck_profiles_gender check (gender in ('FEMALE', 'MALE', 'UNDISCLOSED'))
);
