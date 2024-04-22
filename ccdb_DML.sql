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
CREATE PROCEDURE FindSchoolsInEvent
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
CREATE PROCEDURE CalculateScore
	@event_id INT,
	@school_id INT,
	@score INT OUTPUT
AS
BEGIN
	BEGIN TRANSACTION;

	-- Check meet status
	DECLARE @status VARCHAR(50);
	SELECT @status = m.status FROM Event e
    INNER JOIN Meet m ON e.meet_id = m.id
    WHERE e.event_id = @event_id;
    
	IF @status != 'completed'
	BEGIN
    	ROLLBACK TRANSACTION;
    	PRINT 'Cannot calculate score of an unfinished race.';
    	RETURN;
	END;
	ELSE
	BEGIN
        -- check if score has already been calculated
   	    DECLARE @existingScore INT;
    	DECLARE @rowCount INT;
    	SELECT @existingScore = score FROM Score WHERE event_id = @event_id AND school_id = @school_id;

    	IF @existingScore IS NOT NULL
    	BEGIN
        	SET @score = @existingScore;
   		    PRINT 'Score already exists';
   		    ROLLBACK TRANSACTION;
   		    RETURN;
    	END;

    	-- calculate score
    	ELSE
    	BEGIN
            -- set score null if less than 5 athletes finish
   		    WITH finishers (place) AS (
            	-- get top five results for the event and school
            	SELECT r.place
            	FROM Result r
            	INNER JOIN Athlete a ON r.athlete_id = a.id
            	INNER JOIN School s ON a.school_id = s.id
            	WHERE r.event_id = @event_id AND s.id = @school_id
   		)
        	SELECT @rowCount = COUNT(*)
   		FROM finishers

        	IF @rowCount < 5
            	SET @score = null;
        	ELSE
   			WITH topFiveScores (place) AS (
   				-- get top five results for the event and school
   				SELECT TOP(5) r.place
   				FROM Result r
   				INNER JOIN Athlete a ON r.athlete_id = a.id
   				INNER JOIN School s ON a.school_id = s.id
   				WHERE r.event_id = @event_id AND s.id = @school_id
   				ORDER BY r.time ASC
   			)
            	SELECT @score = SUM(place)
            	FROM topFiveScores;

        	-- insert new row to scores table with calculated score
        	INSERT INTO Score(event_id, school_id, score)
        	VALUES (@event_id, @school_id, @score);
   		    COMMIT TRANSACTION;
    	END;
    END;
END;

-- get all scores for an event
GO
CREATE PROCEDURE GetScores
	@event_id INT
AS
BEGIN
	SELECT s.school_id, s.score
	FROM Score s
	WHERE s.event_id = @event_id;
END;

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

-- Create meet stored procedure
GO
CREATE OR ALTER PROCEDURE CreateMeet
    @host_school_id INT,
    @meet_name VARCHAR(255),
    @start_date DATE,
    @start_time TIME,
    @season VARCHAR(20),
    @status VARCHAR(50),
    @output_message VARCHAR(500) OUTPUT  -- Output parameter to return the message
AS
BEGIN
    SET NOCOUNT ON;

    -- Validate date
    IF @start_date <= CAST(GETDATE() AS DATE)
    BEGIN
        SET @output_message = 'Error: Start date must be in the future.';
        RETURN;
    END

    -- Check if the host school exists
    IF NOT EXISTS (SELECT 1 FROM School WHERE id = @host_school_id)
    BEGIN
        SET @output_message = 'Error: Host school does not exist.';
        RETURN;
    END

    -- Attempt to insert the new meet
    BEGIN TRY
        INSERT INTO Meet (host, name, start_date, start_time, season, status)
        VALUES (@host_school_id, @meet_name, @start_date, @start_time, @season, @status);
        SET @output_message = 'Meet created successfully.';
    END TRY
    BEGIN CATCH
        IF ERROR_NUMBER() = 2627  -- Handling primary key or unique constraint violation
        BEGIN
            SET @output_message = 'Error: A meet with the same details already exists.';
        END ELSE
        BEGIN
            SET @output_message = ERROR_MESSAGE();  -- Capture SQL Server error message
        END
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE FindKTopPerformers
    @season VARCHAR(255), 
    @race_id INT,
    @k INT
AS
BEGIN
    SELECT TOP (@k) 
        CONCAT(a.fname, ' ', a.lname) AS AthleteName,
        r.time AS ResultTime
    FROM 
        Meet m
    INNER JOIN 
        Event e ON m.id = e.meet_id
    INNER JOIN 
        Result r ON r.event_id = e.event_id
    INNER JOIN 
        Athlete a ON r.athlete_id = a.id
    WHERE 
        m.season = @season 
        AND e.race_id = @race_id
    ORDER BY 
        r.time ASC;
END;
GO
CREATE OR ALTER PROCEDURE DeleteResult
    @athlete_id INT,
    @event_id INT
AS
BEGIN
    BEGIN TRANSACTION;
    
    -- Check if the result exists
    IF EXISTS (SELECT 1 FROM Result WHERE athlete_id = @athlete_id AND event_id = @event_id)
    BEGIN
        -- Delete the result
        DELETE FROM Result WHERE athlete_id = @athlete_id AND event_id = @event_id;
        
        PRINT 'Result deleted successfully.';
    END
    ELSE
    BEGIN
        PRINT 'Result does not exist for the specified athlete and event.';
    END
    
    COMMIT TRANSACTION;
END;
