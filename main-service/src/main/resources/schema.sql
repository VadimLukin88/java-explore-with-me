DROP TABLE IF EXISTS req_event_binding CASCADE;
DROP TABLE IF EXISTS participation_requests CASCADE;
DROP TABLE IF EXISTS comp_event_binding CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS locations CASCADE;
DROP TABLE IF EXISTS compilations CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(250) NOT NULL,
  email VARCHAR(254) NOT NULL,
  CONSTRAINT PK_USERS PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(50) NOT NULL,
  CONSTRAINT PK_CATEGORIES PRIMARY KEY (id),
  CONSTRAINT UQ_CATEGORY_NAME UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS compilations (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  title VARCHAR(50) NOT NULL,
  pinned BOOLEAN NOT NULL,
  CONSTRAINT PK_COMPILATIONS PRIMARY KEY (id),
  CONSTRAINT UQ_COMPILATION_NAME UNIQUE (title)
);

CREATE TABLE IF NOT EXISTS locations (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  lat FLOAT NOT NULL,
  lon FLOAT NOT NULL,
  CONSTRAINT PK_LOCATIONS PRIMARY KEY (id),
  CONSTRAINT UQ_LOCATIONS UNIQUE (lat, lon)
);


CREATE TABLE IF NOT EXISTS events (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  title VARCHAR(120) NOT NULL,
  annotation VARCHAR(2000) NOT NULL,
  description VARCHAR(7000) NOT NULL,
  event_date TIMESTAMP NOT NULL,
  category_id BIGINT NOT NULL,
  initiator_id BIGINT NOT NULL,
  location_id BIGINT NOT NULL,
  paid BOOLEAN NOT NULL,
  participant_limit INT NOT NULL,
  request_moderation BOOLEAN NOT NULL,
  state VARCHAR(50) NOT NULL,
  created_on TIMESTAMP NOT NULL,
  published_on TIMESTAMP,
  admin_comment VARCHAR,
  CONSTRAINT PK_EVENTS PRIMARY KEY (id),
  CONSTRAINT FK_CATEGORY FOREIGN KEY (category_id) REFERENCES categories (id),
  CONSTRAINT FK_INITIATOR FOREIGN KEY (initiator_id) REFERENCES users (id),
  CONSTRAINT FK_LOCATION FOREIGN KEY (location_id) REFERENCES locations (id)
);


CREATE TABLE IF NOT EXISTS comp_event_binding (
  comp_id BIGINT NOT NULL,
  event_id BIGINT NOT NULL,
  CONSTRAINT PK_COMP_EVENT_BINDING PRIMARY KEY (comp_id, event_id),
  CONSTRAINT FK_COMPILATION_ID FOREIGN KEY (comp_id) REFERENCES compilations (id),
  CONSTRAINT FK_EVENT_ID FOREIGN KEY (event_id) REFERENCES events (id)
);

CREATE TABLE IF NOT EXISTS participation_requests (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  created TIMESTAMP NOT NULL,
  event_id BIGINT NOT NULL,
  requester_id BIGINT NOT NULL,
  status VARCHAR(50) NOT NULL,
  CONSTRAINT PK_PARTICIPATION_REQUESTS PRIMARY KEY (id),
  CONSTRAINT FK_EVENT FOREIGN KEY (event_id) REFERENCES events (id),
  CONSTRAINT FK_REQUESTER FOREIGN KEY (requester_id) REFERENCES users (id)
);

