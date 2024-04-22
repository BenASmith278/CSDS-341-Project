select * from School
select * from Athlete
select * from Meet
select * from Race
select * from School_Meet
select * from Event
select * from Result
select * from Score
select * from Personal_Best

-- addMeetResult for Rohan (FAILS since meet is upcoming)
DECLARE @output_message NVARCHAR(500);
EXEC AddMeetResult
    @athlete_id = 1000,              -- Example valid athlete ID
    @event_id = 1,                -- Example valid event ID linked to a meet
    @time = '00:19:30',           -- Example finish time
    @place = 3,                   -- Example placement in the event
    @output_message = @output_message OUTPUT;  -- Capturing output
PRINT @output_message;

-- addMeetResult for Rohan (WORKS)
DECLARE @output_message1 NVARCHAR(500);
EXEC AddMeetResult
    @athlete_id = 1000,              -- Example valid athlete ID
    @event_id = 5,                -- Example valid event ID linked to a meet
    @time = '00:19:30',           -- Example finish time
    @place = 3,                   -- Example placement in the event
    @output_message = @output_message1 OUTPUT;  -- Capturing output
PRINT @output_message1;


--transfer Shourya from CWRU to Harvard
DECLARE @athlete_id INT = 1002;
DECLARE @new_school_id INT = 1000;
DECLARE @OutputMessage NVARCHAR(500);
EXEC TransferAthlete @athlete_id, @new_school_id, @output_message = @OutputMessage OUTPUT;
PRINT @OutputMessage;

--transfer Seneca from UVA to CWRU but will fail since competing in meet 'in progress'
DECLARE @athlete_id2 INT = 1006;
DECLARE @new_school_id2 INT = 1005;
EXEC TransferAthlete @athlete_id2, @new_school_id2;







DROP TABLE IF EXISTS Athlete;
DROP TABLE IF EXISTS Race;
DROP TABLE IF EXISTS Meet;
DROP TABLE IF EXISTS School;
DROP TABLE IF EXISTS Result;
DROP TABLE IF EXISTS Personal_Best;
DROP TABLE IF EXISTS Event;
DROP TABLE IF EXISTS School_Meet;
DROP TABLE IF EXISTS Score;

DROP PROCEDURE if exists TransferAthlete;
drop procedure if exists AddMeetResult;