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
 * Module which generates a download modal which dynamic content.
 * 
 * @version 3.2.0
 * @module viewerJS.downloadModal
 * @requires jQuery
 * 
 */
var viewerJS = ( function( viewer ) {
    'use strict';
    
    // default variables
    var _debug = false;
    var _defaults = {
        dataType: null,
        dataTitle: null,
        dataId: null,
        dataPi: null,
        downloadBtn: null,
        reCaptchaSiteKey: '',
        useReCaptcha: true,
        path: '',
        apiUrl: '',
        userEmail: null,
        modal: {
            id: '',
            label: '',
            string: {
                title: '',
                body: '',
                closeBtn: '',
                saveBtn: '',
            }
        },
        messages: {
            reCaptchaText: 'Bitte bestätigen Sie uns, dass Sie ein Mensch sind.',
            rcInvalid: 'Die Überprüfung war nicht erfolgreich. Bitte bestätigen Sie die reCAPTCHA Anfrage.',
            rcValid: 'Vielen Dank. Sie können nun ihre ausgewählte Datei generieren lassen.',
            eMailText: 'Wenn Sie über den Fortschritt ihrer Datei informiert werden möchten, dann teilen Sie uns ihre E-Mail Adresse mit.',
            eMailTextLoggedIn: 'Sie werden über Ihre registrierte E-Mail Adresse von uns über den Fortschritt des Downloads informiert.',
            eMail: ''
        }
    };
    
    viewer.downloadModal = {
        /**
         * Method to initialize the download modal mechanic.
         * 
         * @method init
         * @param {Object} config An config object which overwrites the defaults.
         * @param {String} config.dataType The data type of the current file to download.
         * @param {String} config.dataTitle The title of the current file to download.
         * @param {String} config.dataId The LOG_ID of the current file to download.
         * @param {String} config.dataPi The PI of the current file to download.
         * @param {Object} config.downloadBtn A collection of all buttons with the class
         * attribute 'download-modal'.
         * @param {String} config.reCaptchaSiteKey The site key for the google reCAPTCHA,
         * fetched from the viewer config.
         * @param {String} config.path The current application path.
         * @param {String} config.apiUrl The URL to trigger the ITM download task.
         * @param {String} config.userEmail The current user email if the user is logged
         * in. Otherwise the one which the user enters or leaves blank.
         * @param {Object} config.modal A configuration object for the download modal.
         * @param {String} config.modal.id The ID of the modal.
         * @param {String} config.modal.label The label of the modal.
         * @param {Object} config.modal.string An object of strings for the modal content.
         * @param {String} config.modal.string.title The title of the modal.
         * @param {String} config.modal.string.body The content of the modal as HTML.
         * @param {String} config.modal.string.closeBtn Buttontext
         * @param {String} config.modal.string.saveBtn Buttontext
         * @param {Object} config.messages An object of strings for the used text
         * snippets.
         * @example
         * 
         * <pre>
         * var downloadModalConfig = {
         *     downloadBtn: $( '.download-modal' ),
         *     path: '#{navigationHelper.applicationUrl}',
         *     userEmail: $( '#userEmail' ).val(),
         *     messages: {
         *         reCaptchaText: '#{msg.downloadReCaptchaText}',
         *         rcInvalid: '#{msg.downloadRcInvalid}',
         *         rcValid: '#{msg.downloadRcValid}',
         *         eMailText: '#{msg.downloadEMailText}',
         *         eMailTextLoggedIn: '#{msg.downloadEMailTextLoggedIn}',
         *         eMail: '#{msg.downloadEmail}',
         *         closeBtn: '#{msg.downloadCloseModal}',
         *         saveBtn: '#{msg.downloadGenerateFile}',
         *     }
         * };
         * 
         * viewerJS.downloadModal.init( downloadModalConfig );
         * </pre>
         */
        init: function( config ) {
            if ( _debug ) {
                console.log( '##############################' );
                console.log( 'viewer.downloadModal.init' );
                console.log( '##############################' );
                console.log( 'viewer.downloadModal.init: config = ', config );
            }
            
            $.extend( true, _defaults, config );
            
            _defaults.downloadBtn.on( 'click', function() {
                _defaults.dataType = $( this ).attr( 'data-type' );
                _defaults.dataTitle = $( this ).attr( 'data-title' );
                _defaults.dataId = $( this ).attr( 'data-id' );
                _defaults.dataPi = $( this ).attr( 'data-pi' );
                
                _defaults.modal = {
                    id: _defaults.dataId + 'Modal',
                    label: _defaults.dataId + 'Label',
                    string: {
                        title: _defaults.dataTitle,
                        body: viewer.downloadModal.renderModalBody( _defaults.dataType, _defaults.dataTitle ),
                        closeBtn: _defaults.messages.closeBtn,
                        saveBtn: _defaults.messages.saveBtn,
                    }
                };
                
                // check datatype
                if ( _defaults.dataType === 'pdf' ) {
                    if ( _debug ) {
                        console.log( '---------- PDF Download ----------' );
                        console.log( 'Title = ', _defaults.dataTitle );
                        console.log( 'ID = ', _defaults.dataId );
                        console.log( 'PI = ', _defaults.dataPi );
                    }
                    
                    viewer.downloadModal.initModal( _defaults );
                }
                else {
                    if ( _debug ) {
                        console.log( '---------- ePub Download ----------' );
                        console.log( 'Title = ', _defaults.dataTitle );
                        console.log( 'ID = ', _defaults.dataId );
                        console.log( 'PI = ', _defaults.dataPi );
                    }
                    
                    viewer.downloadModal.initModal( _defaults );
                }
            } );
        },
        /**
         * Method which initializes the download modal and its content.
         * 
         * @method initModal
         * @param {Object} params An config object which overwrites the defaults.
         */
        initModal: function( params ) {
            if ( _debug ) {
                console.log( '---------- viewer.downloadModal.initModal() ----------' );
                console.log( 'viewer.downloadModal.initModal: params = ', params );
            }
            $( 'body' ).append( viewer.helper.renderModal( params.modal ) );
            
            // disable submit button
            $( '#submitModal' ).attr( 'disabled', 'disabled' );
            
            // show modal
            $( '#' + params.modal.id ).modal( 'show' );
            
            // render reCAPTCHA to modal
            $( '#' + params.modal.id ).on( 'shown.bs.modal', function( e ) {
                if ( _defaults.useReCaptcha ) {
                    var rcWidget = grecaptcha.render( 'reCaptchaWrapper', {
                        sitekey: _defaults.reCaptchaSiteKey,
                        callback: function() {
                            var rcWidgetResponse = viewer.downloadModal.validateReCaptcha( grecaptcha.getResponse( rcWidget ) );
                            
                            if ( rcWidgetResponse ) {
                                $( '#modalAlerts' ).append( viewer.helper.renderAlert( 'alert-success', _defaults.messages.rcValid, true ) );
                                
                                // enable submit button
                                $( '#submitModal' ).removeAttr( 'disabled' ).on( 'click', function() {
                                    _defaults.userEmail = $( '#recallEMail' ).val();
                                    
                                    _defaults.apiUrl = viewer.downloadModal
                                            .buildAPICall( _defaults.path, _defaults.dataType, _defaults.dataPi, _defaults.dataId, _defaults.userEmail );
                                    
                                    window.location.href = _defaults.apiUrl;
                                } );
                            }
                            else {
                                $( '#modalAlerts' ).append( viewer.helper.renderAlert( 'alert-danger', _defaults.messages.rcInvalid, true ) );
                            }
                        }
                    } );
                }
                else {
                    // hide paragraph
                    $( this ).find( '.modal-body h4' ).next( 'p' ).hide();
                    
                    // enable submit button
                    $( '#submitModal' ).removeAttr( 'disabled' ).on( 'click', function() {
                        _defaults.userEmail = $( '#recallEMail' ).val();
                        
                        _defaults.apiUrl = viewer.downloadModal.buildAPICall( _defaults.path, _defaults.dataType, _defaults.dataPi, _defaults.dataId, _defaults.userEmail );
                        
                        window.location.href = _defaults.apiUrl;
                    } );
                }
            } );
            
            // remove modal from DOM after closing
            $( '#' + params.modal.id ).on( 'hidden.bs.modal', function( e ) {
                $( this ).remove();
            } );
        },
        /**
         * Method which returns a HTML-String to render the download modal body.
         * 
         * @method renderModalBody
         * @param {String} type The current file type to download.
         * @param {String} title The title of the current download file.
         * @returns {String} The HTML-String to render the download modal body.
         */
        renderModalBody: function( type, title ) {
            if ( _debug ) {
                console.log( '---------- viewer.downloadModal.renderModalBody() ----------' );
                console.log( 'viewer.downloadModal.renderModalBody: type = ', type );
                console.log( 'viewer.downloadModal.renderModalBody: title = ', title );
            }
            var rcResponse = null;
            var modalBody = '';
            
            modalBody += '';
            // alerts
            modalBody += '<div id="modalAlerts"></div>';
            // Title
            if ( type === 'pdf' ) {
                modalBody += '<h4>';
                modalBody += '<i class="fa fa-file-pdf-o" aria-hidden="true"></i> PDF-Download: ';
                modalBody += title + '</h4>';
            }
            else {
                modalBody += '<h4>';
                modalBody += '<i class="fa fa-file-text-o" aria-hidden="true"></i> ePub-Download: ';
                modalBody += title + '</h4>';
            }
            // reCAPTCHA
            if ( _defaults.useReCaptcha ) {
                modalBody += '<p>' + _defaults.messages.reCaptchaText + '</p>';
                modalBody += '<div id="reCaptchaWrapper"></div>';
            }
            // E-Mail
            modalBody += '<hr />';
            modalBody += '<form class="email-form">';
            modalBody += '<div class="form-group">';
            modalBody += '<label for="recallEMail">' + _defaults.messages.eMail + '</label>';
            if ( _defaults.userEmail != undefined ) {
                modalBody += '<input type="email" class="form-control" id="recallEMail" value="' + _defaults.userEmail + '" disabled="disabled" />';
                modalBody += '<p class="help-block">' + _defaults.messages.eMailTextLoggedIn + '</p>';
            }
            else {
                modalBody += '<input type="email" class="form-control" id="recallEMail" />';
                modalBody += '<p class="help-block">' + _defaults.messages.eMailText + '</p>';
            }
            modalBody += '</div>';
            modalBody += '</form>';
            
            return modalBody;
        },
        /**
         * Method which checks the reCAPTCHA response.
         * 
         * @method validateReCaptcha
         * @param {String} response The reCAPTCHA response.
         * @returns {Boolean} Returns true if the reCAPTCHA sent a response.
         */
        validateReCaptcha: function( response ) {
            if ( _debug ) {
                console.log( '---------- viewer.downloadModal.validateReCaptcha() ----------' );
                console.log( 'viewer.downloadModal.validateReCaptcha: response = ', response );
            }
            if ( response == 0 ) {
                return false;
            }
            else {
                return true;
            }
        },
        /**
         * Method which returns an URL to trigger the ITM download task.
         * 
         * @method buildAPICall
         * @param {String} path The current application path.
         * @param {String} type The current file type to download.
         * @param {String} pi The PI of the current work.
         * @param {String} logid The LOG_ID of the current work.
         * @param {String} email The current user email.
         * @returns {String} The URL to trigger the ITM download task.
         */
        buildAPICall: function( path, type, pi, logid, email ) {
            if ( _debug ) {
                console.log( '---------- viewer.downloadModal.buildAPICall() ----------' );
                console.log( 'viewer.downloadModal.buildAPICall: path = ', path );
                console.log( 'viewer.downloadModal.buildAPICall: type = ', type );
                console.log( 'viewer.downloadModal.buildAPICall: pi = ', pi );
                console.log( 'viewer.downloadModal.buildAPICall: logid = ', logid );
                console.log( 'viewer.downloadModal.buildAPICall: email = ', email );
            }
            var url = '';
            
            url += path + 'rest/download';
            
            if ( type == '' ) {
                url += '/-';
            }
            else {
                url += '/' + type;
            }
            if ( pi == '' ) {
                url += '/-';
            }
            else {
                url += '/' + pi;
            }
            if ( logid == '' ) {
                url += '/-';
            }
            else {
                url += '/' + logid;
            }
            if ( email == '' || email == undefined ) {
                url += '/-/';
            }
            else {
                url += '/' + email + '/';
            }
            
            return encodeURI( url );
        },
    
    };
    
    return viewer;
    
} )( viewerJS || {}, jQuery );
