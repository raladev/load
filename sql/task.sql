CREATE TABLE stations(station_id serial PRIMARY KEY, station_name varchar(255) NOT NULL);
CREATE TABLE trains(train_id serial PRIMARY KEY, train_code varchar(50) NOT NULL);
CREATE TABLE schedules(schedule_id serial PRIMARY KEY, station_id bigint not null, train_id bigint not null, arrive_time time, departure_time time);

ALTER TABLE schedules ADD CONSTRAINT train_fk FOREIGN KEY (train_id) REFERENCES trains (train_id);
ALTER TABLE schedules ADD CONSTRAINT station_fk FOREIGN KEY (station_id) REFERENCES stations (station_id);

CREATE INDEX schedules_arrive_time on schedules(arrive_time);
CREATE INDEX schedules_departure_time on schedules(departure_time);

INSERT INTO trains (train_code) values ('t1'), ('t2'), ('t3'), ('t4'), ('t5'), ('t6');
INSERT INTO stations(station_name) values ('s1'), ('s2'), ('s3'), ('s4'), ('s5'), ('s6');

CREATE OR REPLACE FUNCTION add_schedule(_station_name varchar(255),
_train_code varchar(50),
_arrive_time time,
_departure_time time) RETURNS VOID AS
$$
BEGIN
INSERT INTO schedules (station_id, train_id, arrive_time, departure_time)
(select (select station_id from stations where stations.station_name = _station_name) as station_id,
(select train_id from trains where train_code = _train_code) as train_id,
_arrive_time as arrive_time,
_departure_time as departure_time);
END
$$
LANGUAGE 'plpgsql';

DO $$ BEGIN
truncate schedules;
PERFORM add_schedule('s1', 't1', '00:00', '23:59');
PERFORM add_schedule('s1', 't2', '00:00', '23:59');
PERFORM add_schedule('s2', 't3', '10:00', '10:59');
PERFORM add_schedule('s1', 't3', '09:00', '09:59');
PERFORM add_schedule('s3', 't4', '06:00', '15:59');
PERFORM add_schedule('s3', 't4', '20:00', '22:59');
PERFORM add_schedule('s2', 't6', '00:00', '13:59');
PERFORM add_schedule('s3', 't6', '15:00', '18:59');
END $$;

SELECT station_name FROM 
(SELECT station_id FROM 
(SELECT station_id, MAX(count) FROM 
(SELECT station_id, COUNT(train_id) FROM 
(SELECT a.station_id, a.train_id 
FROM schedules AS a 
LEFT JOIN schedules AS b using (station_id) 
WHERE b.schedule_id>a.schedule_id AND a.arrive_time<=b.departure_time AND b.arrive_time<=a.departure_time) AS abba 
GROUP BY abba.station_id, train_id) AS baab 
GROUP BY baab.station_id) AS finish 
ORDER BY max DESC 
LIMIT 1) AS last 
LEFT JOIN 
stations USING (station_id);
