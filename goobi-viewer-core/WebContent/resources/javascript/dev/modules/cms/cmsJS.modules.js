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
 * @version 3.4.0
 * @module cmsJS.modules
 * @requires jQuery, jQuery UI
 * @description Module which enables the cms module functionality.
 */
var cmsJS = ( function( cms ) {
    'use strict';
    
    var _debug = false;
    
    cms.modules = {
    		/**
             * @description Method which initializes the cms modules.
             * @method init
             * @param {Object} config The config object.
             */
        init: function() {
            if ( _debug ) {
                console.log( '##############################' );
                console.log( 'cmsJS.modules.init' );
                console.log( '##############################' );
            }
            this.initEventListeners();
            this.cleanUp();
            
            // jsf ajax event
            jsf.ajax.addOnEvent( function( data ) {
				var ajaxstatus = data.status;

				switch (ajaxstatus) {
					case 'begin':
						break;
					case 'complete':
						break;
					case 'success':
						if ( $( '.cms-module__option-message' ).length > 0 ) {
							cmsJS.modules.setValidationStatus( data.source.id );							
						}
						
						break;
				}
			});
        },
        /**
		 * @description Method to set the validation status.
		 * @method setValidationStatus
		 * @param {String} source The id of the element which fired the ajax request,
		 */
        setValidationStatus: function( source ) {
        	if ( _debug ) {
        		console.log( 'EXECUTE: setValidationStatus' );
        		console.log( '--> source: ', source );
        	}
        	
        	var stripedSource,
        		status;
        	
        	if ( source.indexOf( 'j_idt' ) > -1 ) {
        		stripedSource = source.match(/.*:(.*)/)[1];        		
        		status = $( '[id*="' + stripedSource + '"]' ).parents( '.cms-module__option-control' ).find( '.cms-module__option-message-status' );
        	}
        	else {
        		stripedSource = source;       		
        		status = $( '#' + stripedSource ).parents( '.cms-module__option-control' ).find( '.cms-module__option-message-status' );
        	}

        	if ( status.hasClass( 'success' ) ) {
        		$( '[id*="' + stripedSource + '"]' ).parents( '.cms-module__option-control' ).find( '.cms-module__option-message-mark' ).addClass( 'success' );
        		$( '[id*="' + stripedSource + '"]' ).addClass( 'success' );
        	}
        	else if ( status.hasClass( 'warning' ) ) {
        		$( '[id*="' + stripedSource + '"]' ).parents( '.cms-module__option-control' ).find( '.cms-module__option-message-mark' ).addClass( 'warning' );
        		$( '[id*="' + stripedSource + '"]' ).addClass( 'warning' );
        	}
        	else if ( status.hasClass( 'danger' ) ) {
        		$( '[id*="' + stripedSource + '"]' ).parents( '.cms-module__option-control' ).find( '.cms-module__option-message-mark' ).addClass( 'danger' );
        		$( '[id*="' + stripedSource + '"]' ).addClass( 'danger' );
        	}
        },
        /**
         * @description Method to clean up modules.
         * @method cleanUp
         */
        cleanUp: function() {
        	if ( _debug ) {
        		console.log( 'EXECUTE: cleanUp' );
        	}
        	
        	if ( $( '.cms-module__option-message ul' ).length > 0 ) {
        		$( '.cms-module__option-message ul' ).empty();
        	}
        },
        /**
		 * @description Method to reload all event listeners.
		 * @method onReload
		 * @param {Object} data The ajax data object from jsf.
		 */
        onReload: function( data ) {
            if ( data && data.status == 'begin' ) {
            	cms.modules.removeEventListeners();
            } 
            else if ( !data || data.status == 'success' ) {
            	cms.modules.initEventListeners();
            }
        },
        /**
		 * @description Method to remove all event listeners.
		 * @method removeEventListeners
		 */
        removeEventListeners: function() {
            $( '[data-toggle="helptext"]' ).off( 'click' );
            $( '[data-toggle="available-items"]' ).off( 'click' );
            $( '[data-toggle="option-dropdown"]' ).off( 'click' );
        },
        /**
         * @description Method to initialize all event listeners.
         * @method initEventListeners
         */
        initEventListeners: function() {
            // toggle input helptext
            $( '[data-toggle="helptext"]' ).on( 'click', function() {
            	$( this ).toggleClass( 'in' );
            	
            	var $input = $( this ).closest( '.cms-module__option-group' ).find( '.cms-module__option-control, .cms-module__option-dropdown' );
            	$input.toggleClass( 'in' );
            	$input.find( '.cms-module__option-control-helptext' ).toggleClass( 'in' );
            } );
            
            // toggle add new item accordeon
            $( '[data-toggle="available-items"]' ).on( 'click', function() {
            	if ( $( this ).hasClass( 'in' ) ) {
            		$( this ).toggleClass( 'in' );
                	$( this ).parent().find( '.cms-menu__available-items-toggle' ).slideToggle( 'fast', function() {
                		// focus first input if available
                		$( '.cms-menu__available-items-toggle .cms-module__option-group' ).first().find( '.form-control' ).focus();
                	} );
            	}
            	else {
            		$( '[data-toggle="available-items"], .cms-menu__available-items-toggle' ).each( function() {
            			$( '[data-toggle="available-items"]' ).removeClass( 'in' );
            			$( '.cms-menu__available-items-toggle' ).slideUp( 'fast' );
            		} );
            		
            		$( this ).toggleClass( 'in' );
            		$( this ).parent().find( '.cms-menu__available-items-toggle' ).slideToggle( 'fast', function() {
            			// focus first input if available
            			$( '.cms-menu__available-items-toggle .cms-module__option-group' ).first().find( '.form-control' ).focus();
            		} );            		
            	}
            } );
            
            // toggle option dropdown
            $( '[data-toggle="option-dropdown"]' ).on( 'click', function() {
            	$( this ).next().slideToggle( 'fast' );
            } );
            $( document ).on( 'click', function( event ) {
            	if ( $( event.target ).closest( '.cms-module__option-dropdown' ).length ) {	
            		return;
            	}
            	else {            		
            		$( '.cms-module__option-dropdown' ).find( 'ul' ).hide();
            	}
            } );
        }
    };
    
    return cms;
    
} )( cmsJS || {}, jQuery );
