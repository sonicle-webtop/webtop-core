@DataSource[default@com.sonicle.webtop.core]

CREATE OR REPLACE FUNCTION "public"."rrule_event_overlaps_legacy"(timestamptz, timestamptz, text, timestamptz, timestamptz)
  RETURNS "pg_catalog"."bool" AS $BODY$
DECLARE
  dtstart ALIAS FOR $1;
  dtend ALIAS FOR $2;
  repeatrule ALIAS FOR $3;
  in_mindate ALIAS FOR $4;
  in_maxdate ALIAS FOR $5;
  base_date TIMESTAMP WITH TIME ZONE;
  mindate TIMESTAMP WITH TIME ZONE;
  maxdate TIMESTAMP WITH TIME ZONE;
BEGIN

  IF dtstart IS NULL THEN
    RETURN NULL;
  END IF;
  IF dtend IS NULL THEN
    base_date := dtstart;
  ELSE
    base_date := dtend;
  END IF;

  IF in_mindate IS NULL THEN
    mindate := current_date - '10 years'::interval;
  ELSE
    mindate := in_mindate;
  END IF;

  IF in_maxdate IS NULL THEN
    maxdate := current_date + '10 years'::interval;
  ELSE
    -- If we add the duration onto the event, then an overlap occurs if dtend <= increased end of range.
    maxdate := in_maxdate + (base_date - dtstart);
  END IF;

  IF repeatrule IS NULL THEN
    RETURN (dtstart < maxdate AND base_date >= mindate);
  END IF;

  SELECT d INTO mindate FROM rrule_event_instances_range( base_date, repeatrule, mindate, maxdate, 60 ) d LIMIT 1;
  RETURN FOUND;

END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE
  PARALLEL SAFE