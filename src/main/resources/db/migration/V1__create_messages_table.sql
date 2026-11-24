create table messages (
    id uuid primary key,
    request_id uuid not null,
    state varchar(32) not null,
    content varchar(255) not null,
    external_id uuid
);

create unique index message_request_id_unique_idx on messages(request_id);