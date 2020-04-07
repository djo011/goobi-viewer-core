/**
 * This file is part of the Goobi viewer - a content presentation and management
 * application for digitized objects.
 * 
 * Visit these websites for more information. - http://www.intranda.com -
 * http://digiverso.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Module which includes mostly used helper functions.
 * 
 * @version 3.2.0
 * @module viewerJS.helper
 * @requires jQuery
 */
var viewerJS = ( function( viewer ) {
    'use strict';
    
    // default variables
    var _debug = false;
    
    viewer.iiif = {
            
            /**
             * parses the given element to return the appropriate String value for the given language.
             * If the given element is a String itself, that String is returned, if it is a single object, the property @value 
             * is returned, if it is an array of Strings, the first String is returned, if it is an array of objects,
             * the @value property of the first object with an @language property equals to the given language is returned
             *  
             * @param element   The js property value to parse, either a String, an object with properties @value and @language or an array of either of those
             * @param language  The preferred language String as a two digit code
             * @returns         The most appropriate String value found
             */
            getValue: function(element, locale) {
                if(element) {
                    if(typeof element === 'string') {
                        return element;
                    } else if (Array.isArray(element)) {
                       var fallback;
                        for (var index in element) {
                           var item = element[index];
                           if(typeof item === 'string') {
                               return item;
                           } else {
                               var value = item['@value'];
                               var language = item['@language'];
                               if(locale == language) {
                                   return value;
                               } else if(!fallback || language == 'en') {
                                   fallback = value;
                               }
                           }
                       }
                        return fallback;
                    } else {
                        return element['@value'];                
                    }
                }
            },
            
            /**
             * Method to retrieve metadata value of the metadata object with the given label and
             * within the given collection object.
             * 
             * @param collection {Object} The iiif-presentation collection object cotaining the
             * metadata.
             * @param label {String} The label property value of the metadata to return.
             * @returns {String} The count of works in the collection.
             */
            getMetadataValue: function( collection, label) {
                if ( _debug ) {
                    console.log( '---------- _getMetadataValue() ----------' );
                    console.log( '_getMetadataValue: collection = ', collection );
                    console.log( '_getMetadataValue: label = ', label );
                }
                
                var value = '';
                
                collection.metadata.forEach( function( metadata ) {
                    if ( _getValue(metadata.label, _defaults.displayLanguage) == label ) {
                        value = _getValue(metadata.value, _defaults.displayLanguage);
                    }
                } );
                
                return value;
            },
            
            /**
             * Returns the number of subcollections of a given iiif collection json element
             * The number is based in the service defined by '<rest-url>/api/collections/extent/context.json'
             * If no matching service is available, 0 is returned
             * 
             * @param collection
             * @returns the number of subcollections of a given iiif collection json element
             */
            getChildCollections: function(collection) {
                if(collection.service && Array.isArray(collection.service)) {
                    let extents = collection.service.filter( service => service['@context'].endsWith('api/context/collection/extent/context.json') );
                    if(extents && extents.length > 0) {
                        return extents[0].children;
                    }
                } else if(collection.service && collection.service['@context'].endsWith('api/context/collection/extent/context.json')) {
                     return collection.service.children;
                } else {
                    return 0;
                }
            },
            
            /**
             * Returns the number of contained works of a given iiif collection json element
             * The number is based in the service defined by '<rest-url>/api/collections/extent/context.json'
             * If no matching service is available, 0 is returned
             *
             * @param collection
             * @returns the number of contained works of a given iiif collection json element
             */
            getContainedWorks: function(collection) {
                if(collection.service && Array.isArray(collection.service)) {
                    let extents = collection.service.filter( service => service['@context'].endsWith('api/context/collection/extent/context.json') );
                    if(extents && extents.length > 0) {
                        return extents[0].containedWorks;
                    }
                } else if(collection.service && collection.service['@context'].endsWith('api/context/collection/extent/context.json')) {
                    return collection.service.containedWorks;
                } else {
                    return 0;
                }
            },
            
            /**
             * Returns the collection's related element with the given label
             * 
             * @param collection
             * @param label
             * @returns the collection's related element with the given label
             */
            getRelated: function(collection, label) {
                if(collection.related) {
                    if(Array.isArray(collection.related)) {                
                        for(var index in collection.related) {
                            var related = collection.related[index];
                            if(related.label == label) {
                                return related;
                            }
                        }
                    } else {
                        return collection.related;
                    }
                }
            }
    }
    
    return viewer;
    
} )( viewerJS || {}, jQuery );