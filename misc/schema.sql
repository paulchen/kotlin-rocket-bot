CREATE TABLE venue (
    id INT NOT NULL,
    name VARCHAR(255),
    city VARCHAR(255),
    country VARCHAR(255),
    capacity VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE fixture (
    id INT NOT NULL,
    league_id INT NOT NULL,
    season INT NOT NULL,
    date TIMESTAMP WITH TIME ZONE NOT NULL,
    round VARCHAR(255) NOT NULL,
    team_home VARCHAR(255) NOT NULL,
    team_away VARCHAR(255) NOT NULL,
    status VARCHAR(10) NOT NULL,
    goals_halftime_home INT,
    goals_halftime_away INT,
    goals_fulltime_home INT,
    goals_fulltime_away INT,
    goals_extratime_home INT,
    goals_extratime_away INT,
    goals_penalty_home INT,
    goals_penalty_away INT,
    events_processed INT NOT NULL,
    venue_id INT,
    end_date TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id),
    FOREIGN KEY (venue_id) REFERENCES venue (id)
);
