//@define Sonicle.calendar.data.CalendarMappings

/**
 * @class Sonicle.calendar.data.CalendarMappings
 * A simple object that provides the field definitions for Calendar records so that they can be easily overridden.
 *
 * To ensure the proper definition of Sonicle.calendar.data.EventModel the override should be
 * written like this:
 *
 *      Ext.define('MyApp.data.CalendarMappings', {
 *          override: 'Sonicle.calendar.data.CalendarMappings'
 *      },
 *      function () {
 *          // Update "this" (this === Sonicle.calendar.data.CalendarMappings)
 *      });
 */
Ext.ns('Sonicle.calendar.data');

Sonicle.calendar.data.CalendarMappings = {
    CalendarId: {
        name:    'CalendarId',
        mapping: 'id',
        type:    'int'
    },
    Title: {
        name:    'Title',
        mapping: 'title',
        type:    'string'
    },
    Description: {
        name:    'Description', 
        mapping: 'desc',   
        type:    'string' 
    },
    ColorId: {
        name:    'ColorId',
        mapping: 'color',
        type:    'int'
    },
    IsHidden: {
        name:    'IsHidden',
        mapping: 'hidden',
        type:    'boolean'
    }
};
