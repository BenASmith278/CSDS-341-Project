-- insert into SCHOOL
insert into School(name, division, city, state, country)
values('Case Western Reserve University', 'D3', 'Cleveland', 'OH', 'US');

insert into School(name, division, city, state, country)
values('Emory University', 'D3', 'Atlanta', 'GA', 'US');

insert into School(name, division, city, state, country)
values('Stanford University', 'D1', 'Stanford', 'CA', 'US');

insert into School(name, division, city, state, country)
values('Massachusets Institute of Technology', 'D3', 'Cambridge', 'MA', 'US');

insert into School(name, division, city, state, country)
values('Harvard University', 'D1', 'Boston', 'MA', 'US');

insert into School(name, division, city, state, country)
values('University of Virginia', 'D1', 'Charlottesville', 'VA', 'US');

insert into School(name, division, city, state, country)
values('University of Illinois - Urbana Champaign', 'D1', 'Champaign County', 'IL', 'US');


-- insert into ATHLETE
insert into Athlete(fname, lname, school_id, year, gender, status)
values('Rohan', 'Bhat', (SELECT id FROM School WHERE name = 'Case Western Reserve University'), 'so', 'M', 'active' );

insert into Athlete(fname, lname, school_id, year, gender, status)
values('Ben', 'Smith', (SELECT id FROM School WHERE name = 'Case Western Reserve University'), 'so', 'M', 'active');

insert into Athlete(fname, lname, school_id, year, gender, status)
values('Stephen', 'Henry', (SELECT id FROM School WHERE name = 'Case Western Reserve University'), 'sr', 'M', 'active');

insert into Athlete(fname, lname, school_id, year, gender, status)
values('Jacob', 'Slater', (SELECT id FROM School WHERE name = 'Case Western Reserve University'), 'fr', 'M', 'active');

insert into Athlete(fname, lname, school_id, year, gender, status)
values('Solomon', 'Greene', (SELECT id FROM School WHERE name = 'Case Western Reserve University'), 'so', 'M', 'active');

insert into Athlete(fname, lname, school_id, year, gender, status)
values('Shourya', 'Poddar', (SELECT id FROM School WHERE name = 'Case Western Reserve University'), 'so', 'M', 'injured');

insert into Athlete(fname, lname, school_id, year, gender, status)
values('Maxwell', 'Chen', (SELECT id FROM School WHERE name = 'Stanford University'), 'jr', 'M', 'active');

insert into Athlete(fname, lname, school_id, year, gender, status)
values('Anmay', 'Devaraj', (SELECT id FROM School WHERE name = 'University of Illinois - Urbana Champaign'), 'fr', 'M', 'inactive');

insert into Athlete(fname, lname, school_id, year, gender, status)
values('Jasmine', 'Patty', (SELECT id FROM School WHERE name = 'Harvard University'), 'sr', 'F', 'active');

insert into Athlete(fname, lname, school_id, year, gender, status)
values('Seneca', 'Feys', (SELECT id FROM School WHERE name = 'University of Virginia'), 'jr', 'F', 'active');

-- insert into MEET
insert into Meet(host, name,start_date, start_time, season, status)
values((SELECT id FROM School WHERE name = 'Case Western Reserve University'), 'Spartan Stronger Together', '2024-04-20', '09:00:00', 'spring', 'upcoming' );

insert into Meet(host, name,start_date, start_time, season, status)
values((SELECT id FROM School WHERE name = 'University of Virginia'), 'Big Red Invitational', '2023-09-21', '10:00:00', 'fall', 'completed');

insert into Meet(host, name,start_date, start_time, season, status)
values((SELECT id FROM School WHERE name = 'Stanford University'), 'West Coast Bash', '2024-4-16', '08:00:00', 'spring', 'in progress');


-- insert into RACE
insert into Race(name, distance, gender)
values('5k', 5000, 'M');

insert into Race(name, distance, gender)
values('5k', 5000, 'F');

insert into Race(name, distance, gender)
values('8k', 8000, 'M');

insert into Race(name, distance, gender)
values('8k', 8000, 'F');

insert into Race(name, distance, gender)
values('10k', 10000, 'M');

insert into Race(name, distance, gender)
values('10k', 10000, 'F');


-- insert into SCHOOL_MEET
insert into School_Meet(school_id, meet_id)
values((SELECT id FROM School WHERE name = 'Case Western Reserve University'), (SELECT id FROM Meet WHERE name = 'Spartan Stronger Together'));

insert into School_Meet(school_id, meet_id)
values((SELECT id FROM School WHERE name = 'Stanford University'), (SELECT id FROM Meet WHERE name = 'Spartan Stronger Together'));

insert into School_Meet(school_id, meet_id)
values((SELECT id FROM School WHERE name = 'Emory University'), (SELECT id FROM Meet WHERE name = 'Spartan Stronger Together'));

insert into School_Meet(school_id, meet_id)
values((SELECT id FROM School WHERE name = 'Emory University'), (SELECT id FROM Meet WHERE name = 'Big Red Invitational'));

insert into School_Meet(school_id, meet_id)
values((SELECT id FROM School WHERE name = 'Case Western Reserve University'), (SELECT id FROM Meet WHERE name = 'Big Red Invitational'));

insert into School_Meet(school_id, meet_id)
values((SELECT id FROM School WHERE name = 'University of Virginia'), (SELECT id FROM Meet WHERE name = 'Big Red Invitational'));

insert into School_Meet(school_id, meet_id)
values((SELECT id FROM School WHERE name = 'University of Virginia'), (SELECT id FROM Meet WHERE name = 'West Coast Bash'));

-- insert into Event
insert into Event(meet_id, race_id, start_time)
values((SELECT id FROM Meet WHERE name = 'Spartan Stronger Together'), 1002, '09:30:00')

insert into Event(meet_id, race_id, start_time)
values((SELECT id FROM Meet WHERE name = 'Spartan Stronger Together'), 1003, '11:30:00')

insert into Event(meet_id, race_id, start_time)
values((SELECT id FROM Meet WHERE name = 'Spartan Stronger Together'), 1004, '14:45:00')

insert into Event(meet_id, race_id, start_time)
values((SELECT id FROM Meet WHERE name = 'Spartan Stronger Together'), 1005, '16:30:00')

insert into Event(meet_id, race_id, start_time)
values((SELECT id FROM Meet WHERE name = 'Big Red Invitational'), 1002, '09:30:00')

insert into Event(meet_id, race_id, start_time)
values((SELECT id FROM Meet WHERE name = 'Big Red Invitational'), 1003, '11:30:00')

insert into Event(meet_id, race_id, start_time)
values((SELECT id FROM Meet WHERE name = 'Big Red Invitational'), 1004, '14:45:00')

insert into Event(meet_id, race_id, start_time)
values((SELECT id FROM Meet WHERE name = 'Big Red Invitational'), 1005, '16:30:00')

insert into Event(meet_id, race_id, start_time)
values((SELECT id FROM Meet WHERE name = 'West Coast Bash'), 1001, '6:30:00')


-- insert into Results
insert into Result(athlete_id, event_id, time, place)
values(1006, 9, '00:20:16', 12),
    (1001, 1, '00:20:16', 5),
    (1002, 2, '00:22:30', 5),
    (1003, 3, '00:18:45', 5),
    (1004, 4, '00:25:00', 5),
    (1005, 5, '00:21:10', 5);