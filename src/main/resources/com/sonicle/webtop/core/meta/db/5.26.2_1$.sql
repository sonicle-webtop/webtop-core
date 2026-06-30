@DataSource[default@com.sonicle.webtop.core]

CREATE OR REPLACE FUNCTION "public"."rrule_event_overlaps"(timestamptz, timestamptz, text, timestamptz, timestamptz)
  RETURNS "pg_catalog"."bool" AS $BODY$
DECLARE
  dtstart ALIAS FOR $1;
  dtend ALIAS FOR $2;
  repeatrule ALIAS FOR $3;
  in_mindate ALIAS FOR $4;
  in_maxdate ALIAS FOR $5;
  duration INTERVAL;
  mindate TIMESTAMP WITH TIME ZONE;
  maxdate TIMESTAMP WITH TIME ZONE;
BEGIN
  IF dtstart IS NULL THEN
    RETURN NULL;
  END IF;

  -- Event duration; zero when there's no dtend
  duration := COALESCE(dtend, dtstart) - dtstart;

  mindate := COALESCE(in_mindate, current_date - '10 years'::interval);
  maxdate := COALESCE(in_maxdate, current_date + '10 years'::interval);

  IF repeatrule IS NULL THEN
    RETURN (dtstart < maxdate AND (dtstart + duration) >= mindate);
  END IF;

  -- Expand the series anchored on DTSTART (correct per RFC 5545 for UNTIL/BYDAY/etc.);
  -- shift the lower bound of the search window back by the duration instead, so an
  -- instance whose END falls inside [mindate, maxdate) is still found.
  RETURN EXISTS (
    SELECT 1
    FROM rrule_event_instances_range(dtstart, repeatrule, mindate - duration, maxdate, 60) d
    WHERE (d + duration) >= mindate
  );

END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE
  PARALLEL SAFE
  COST 100