-- Transfer athlete stored procedure
GO
CREATE or ALTER PROCEDURE TransferAthlete
    @athlete_id INT,
    @new_school_id INT,
    @output_message NVARCHAR(500) OUTPUT  -- Output parameter
AS
BEGIN
    BEGIN TRANSACTION;
        -- Check if the athlete exists
        IF NOT EXISTS (SELECT 1 FROM Athlete WHERE id = @athlete_id)
        BEGIN
            SET @output_message = 'Invalid athlete_id.';
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- Check if the new school exists
        IF NOT EXISTS (SELECT 1 FROM School WHERE id = @new_school_id)
        BEGIN
            SET @output_message = 'Invalid school_id.';
            ROLLBACK TRANSACTION;
            RETURN;
        END

		-- Check if the athlete is already at the new school
		IF EXISTS (SELECT 1 FROM Athlete WHERE id = @athlete_id AND school_id = @new_school_id)
		BEGIN
			SET @output_message = 'No need to transfer: The athlete is already at the specified school.';
			ROLLBACK TRANSACTION;
			RETURN;
		END

        -- Check for ongoing participation in meets
        IF EXISTS (
            SELECT 1
            FROM Result r
            INNER JOIN Event e ON r.event_id = e.event_id
            INNER JOIN Meet m ON e.meet_id = m.id
            WHERE r.athlete_id = @athlete_id AND m.status = 'in progress'
        )
        BEGIN
            SET @output_message = 'You must wait before transferring the athlete as they are currently competing in an event.';
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- Update the athlete's school
        UPDATE Athlete
        SET school_id = @new_school_id
        WHERE id = @athlete_id;

        SET @output_message = 'Transfer successful: Athlete has been transferred to the new school.';
        COMMIT TRANSACTION;
END;
GO

-- Add meet result stored procedure
GO
CREATE or ALTER PROCEDURE AddMeetResult
    @athlete_id INT,
    @event_id INT,
    @time TIME,
    @place INT,
    @output_message NVARCHAR(500) OUTPUT  -- Output parameter to hold the message
AS
BEGIN
    BEGIN TRANSACTION;
		
    -- Check if the athlete exists
    IF NOT EXISTS (SELECT 1 FROM Athlete WHERE id = @athlete_id)
    BEGIN
        SET @output_message = 'The athlete ID provided does not exist.';
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Check if the event exists
    IF NOT EXISTS (SELECT 1 FROM Event WHERE event_id = @event_id)
    BEGIN
        SET @output_message = 'The event ID provided does not exist.';
        ROLLBACK TRANSACTION;
        RETURN;
    END

	-- Check if a result already exists for this athlete and event (to avoid PK violation)
	IF EXISTS (SELECT 1 FROM Result WHERE athlete_id = @athlete_id AND event_id = @event_id)
	BEGIN
		SET @output_message = 'A result for this athlete and event already exists.';
		ROLLBACK TRANSACTION;
		RETURN;
	END

    -- Check meet status
    DECLARE @meet_id INT;
    SELECT @meet_id = meet_id FROM Event WHERE event_id = @event_id;
    DECLARE @status VARCHAR(50);
    SELECT @status = status FROM Meet WHERE id = @meet_id;

    IF @status = 'upcoming'
    BEGIN
        SET @output_message = 'Cannot add results to an upcoming meet.';
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Insert the result
    INSERT INTO Result(athlete_id, event_id, time, place)
    VALUES (@athlete_id, @event_id, @time, @place);

    SET @output_message = 'Result added successfully.';
    COMMIT TRANSACTION;
END;
GO

-- Create trigger to check personal best
GO
CREATE OR ALTER TRIGGER CheckPersonalBest
ON Result
AFTER INSERT
AS
BEGIN
    DECLARE @athlete_id INT, @race_id INT, @time TIME, @event_id INT;

	SELECT @athlete_id = inserted.athlete_id, @time = inserted.time, @event_id = inserted.event_id
    FROM inserted;
	SELECT @race_id = race_id FROM Event WHERE event_id = @event_id;

    -- Check if it's a personal best
    IF NOT EXISTS(SELECT 1 FROM Personal_Best WHERE athlete_id = @athlete_id AND race_id = @race_id AND time <= @time)
    BEGIN
        UPDATE Personal_Best
        SET time = @time
        WHERE athlete_id = @athlete_id AND race_id = @race_id;

        IF @@ROWCOUNT = 0
            INSERT INTO Personal_Best(athlete_id, race_id, time)
            VALUES (@athlete_id, @race_id, @time);
    END
END;
GO

-- find all schools in an event
GO
CREATE or ALTER PROCEDURE FindSchoolsInEvent
    @event_id INT
AS
BEGIN
    SELECT DISTINCT s.id, s.name
    FROM Event e
    INNER JOIN Meet m ON e.meet_id = m.id
    INNER JOIN School_Meet sm ON sm.meet_id = m.id
    INNER JOIN School s ON s.id = sm.school_id
    WHERE e.event_id = @event_id;
END;

-- calculate the score for an event and school
GO
CREATE OR ALTER PROCEDURE CalculateScore
    @event_id INT,
    @school_id INT,
    @score INT OUTPUT,
    @message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;

    -- Check meet status
    DECLARE @status VARCHAR(50);
    SELECT @status = m.status FROM Event e
    INNER JOIN Meet m ON e.meet_id = m.id
    WHERE e.event_id = @event_id;

    IF @status != 'completed'
    BEGIN
        SET @message = 'Cannot calculate score of an unfinished race.';
        ROLLBACK TRANSACTION;
        RETURN;
    END

	IF EXISTS (SELECT score FROM Score WHERE event_id = @event_id AND school_id = @school_id)
	BEGIN
		SELECT @score = score FROM Score WHERE event_id = @event_id AND school_id = @school_id;
		SET @message = 'Score already exists.'
		ROLLBACK TRANSACTION;
		RETURN;
	END;

    -- Prepare a table variable to hold top finishers
    DECLARE @finishers TABLE (place INT);

    -- Populate the table variable with the top 5 results
    INSERT INTO @finishers (place)
    SELECT TOP(5) r.place
    FROM Result r
    INNER JOIN Athlete a ON r.athlete_id = a.id
    WHERE r.event_id = @event_id AND a.school_id = @school_id
    ORDER BY r.time ASC;

    -- Check if enough finishers are available
    DECLARE @rowCount INT;
    SELECT @rowCount = COUNT(*) FROM @finishers;

    IF @rowCount < 5
    BEGIN
        SET @score = NULL;  -- Not enough finishers to calculate score
        SET @message = 'Not enough athletes finished the event.';
    END
    ELSE
    BEGIN
        -- Calculate score from the places of finishers
        SELECT @score = SUM(place) FROM @finishers;

        -- Insert the calculated score into the Score table
        INSERT INTO Score (event_id, school_id, score)
        VALUES (@event_id, @school_id, @score);

        SET @message = 'Score calculated and recorded successfully.';
    END

    COMMIT TRANSACTION;
END;
GO

GO



CREATE or ALTER PROCEDURE GetScores
	@event_id INT
AS
BEGIN
	SELECT s.school_id, s.score
	FROM Score s
	WHERE s.event_id = @event_id;
END;
GO


-- find top performers for a season
GO
CREATE OR ALTER PROCEDURE FindTopPerformers
    @season INT,
    @race_id INT,
    @rows INT
AS
BEGIN
    BEGIN TRANSACTION;
        IF NOT EXISTS (SELECT 1 FROM Race WHERE id = @race_id)
        BEGIN
            PRINT 'Invalid Race ID';
            ROLLBACK TRANSACTION;
            RETURN;
        END;

        SELECT TOP(@rows) CONCAT(a.lname, ', ', a.fname) as name, res.time
        FROM Meet m
        INNER JOIN Event e ON m.id = e.meet_id
        INNER JOIN Result res ON res.event_id = e.event_id
        INNER JOIN Athlete a ON a.id = res.athlete_id
        WHERE m.season = @season AND e.race_id = @race_id
        ORDER BY res.time ASC;
    COMMIT TRANSACTION;
END;
GO


-- Create meet stored procedure
CREATE OR ALTER PROCEDURE CreateMeet
    @host_school_id INT,
    @meet_name VARCHAR(255),
    @start_date DATE,
    @start_time TIME,
    @season INT,
    @status VARCHAR(50),
    @output_message VARCHAR(500) OUTPUT  -- Output parameter to return the message
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION; -- Start the transaction

    -- Validate date
    IF @start_date <= CAST(GETDATE() AS DATE)
    BEGIN
        SET @output_message = 'Error: Start date must be in the future.';
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Check if the host school exists
    IF NOT EXISTS (SELECT 1 FROM School WHERE id = @host_school_id)
    BEGIN
        SET @output_message = 'Error: Host school does not exist.';
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Attempt to insert the new meet
    INSERT INTO Meet (host, name, start_date, start_time, season, status)
    VALUES (@host_school_id, @meet_name, @start_date, @start_time, @season, @status);

    SET @output_message = 'Meet created successfully.';
    COMMIT TRANSACTION; -- Commit the transaction
END;
GO
--Delete Athlete and all corresponding races (if an athlete happens to be banned from competition)
CREATE OR ALTER PROCEDURE DeleteAthleteAndResults
    @AthleteID INT,
    @output_message NVARCHAR(500) OUTPUT  -- Output parameter to return the message
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRANSACTION; -- Start transaction

        -- Check if the athlete exists
        IF NOT EXISTS (SELECT 1 FROM Athlete WHERE ID = @AthleteID)
        BEGIN
            SET @output_message = 'Error: Athlete ID does not exist.';
            ROLLBACK TRANSACTION; -- Rollback transaction because of invalid ID
            RETURN;
        END

        -- Delete athlete's results
        DELETE FROM Result
        WHERE athlete_id = @AthleteID;

        -- Delete athlete's personal record
        DELETE FROM Personal_Best
        WHERE athlete_id = @AthleteID;

        -- Delete athlete from Athlete table
        DELETE FROM Athlete
        WHERE ID = @AthleteID;

        -- If all operations are successful, commit the transaction
        COMMIT TRANSACTION;
        SET @output_message = 'Athlete and associated records deleted successfully.';
END;

