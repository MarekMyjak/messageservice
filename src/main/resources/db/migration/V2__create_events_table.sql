create table events (
    id uuid primary key,
    state varchar(32) not null,
    type varchar(255) not null,
    payload jsonb not null,
    created_date timestamptz not null default current_timestamp,
    published_date timestamptz
);

create index events_state_idx on events(state);