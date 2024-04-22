-- School Table
CREATE TABLE School (
  id INT identity(1000,1) primary key,
  name varchar(255),
  division varchar(50) 
  CHECK (division IN ('D1', 'D2', 'D3', 'NAIA', 'JUCO')),
  city varchar(255),
  state varchar(255),
  country varchar(255)
);

-- Athlete Table
CREATE TABLE Athlete (
  id int identity(1000,1) primary key,
  school int,
  fname varchar(255),
  lname varchar(255),
  year int,
  status varchar(50),
  CHECK (status IN ('active', 'inactive', 'injured')),
  foreign key (school) REFERENCES School(id)
);

-- Meet Table
CREATE TABLE Meet (
  id int identity(1,1) primary key,
  host int NOT NULL,
  name varchar(255),
  start_date DATE,
  start_time TIME,
  season varchar(255),
  status varchar(255),
  foreign key (host) REFERENCES School(id),
  CHECK (status IN ('upcoming', 'in progress', 'completed'))
);

-- Race Table
CREATE TABLE Race (
  id int identity(1000,1) primary key,
  name varchar(255),
  distance int,
  gender CHAR(1)
);

-- School_Meet Table
CREATE TABLE School_Meet (
  school_id int NOT NULL,
  meet_id int NOT NULL,
  primary key (school_id, meet_id),
  foreign key (school_id) REFERENCES School(id),
  foreign key (meet_id) REFERENCES Meet(id)
);

-- Result Table
CREATE TABLE Result (
  athlete_id int NOT NULL,
  event_id int NOT NULL,
  time TIME,
  place int,
  primary key (athlete_id, event_id),
  foreign key (athlete_id) REFERENCES Athlete(id),
  foreign key (event_id) REFERENCES Event(id)
);

-- Personal_Best Table
CREATE TABLE Personal_Best (
  athlete_id int  NOT NULL,
  race_id int NOT NULL,
  time TIME,
  primary key (athlete_id, race_id),
  foreign key (athlete_id) REFERENCES Athlete(id),
  foreign key (race_id) REFERENCES Race(id)
);

CREATE TABLE Score (
  event_id int NOT NULL,
  school_id int NOT NULL,
  score int,
  primary key (event_id, school_id),
  foreign key (event_id) REFERENCES Event(id),
  foreign key (school_id) REFERENCES School(id)
);

CREATE TABLE Event (
  event_id int identity(1000,1) primary key,
  meet_id int,
  race_id int,
  start_time TIME,
  foreign key (meet_id) REFERENCES Meet(id),
  foreign key (race_id) REFERENCES Race(id)
);
