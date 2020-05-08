<geoLocationQuestion>
	
	<div if="{this.showInstructions()}" class="annotation_instruction">
		<label>{Crowdsourcing.translate("crowdsourcing__help__create_rect_on_image")}</label>
	</div>
	<div if="{this.showAddMarkerInstructions()}" class="annotation_instruction">
		<label>{Crowdsourcing.translate("crowdsourcing__help__add_marker_to_image")}</label>
	</div>
	<div if="{this.showInactiveInstructions()}" class="annotation_instruction annotation_instruction_inactive">
		<label>{Crowdsourcing.translate("crowdsourcing__help__make_active")}</label>
	</div>
	
	<div id="geoMap_{opts.index}" class="geo-map"></div>
	
	<div id="annotation_{index}" each="{anno, index in this.annotations}">
		
	</div>

<script>


this.question = this.opts.question;
this.annotationToMark = null;
this.addMarkerActive = !this.question.isRegionTarget() && !this.opts.item.isReviewMode();

this.on("mount", function() {
	this.opts.item.onItemInitialized( () => {	    
	    this.question.initializeView((anno) => new Crowdsourcing.Annotation.GeoJson(anno), this.addAnnotation, this.updateAnnotation, this.focusAnnotation);	    
	    this.initMap();
	    this.opts.item.onImageOpen(() => this.resetFeatures());
	    this.opts.item.onAnnotationsReload(() => this.resetFeatures());
	})
});

setView(view) {
    this.map.setView(view.center, view.zoom);
}

resetFeatures() {
    this.setFeatures(this.question.annotations);
    if(this.geoMap.getMarkerCount() > 0) {
        let zoom = 12;
        if(this.geoMap.getMarkerCount() == 1) {
            let marker = this.geoMap.getMarker(this.question.annotations[0].markerId);
            if(marker) {                
            	zoom = marker.feature.view.zoom;
            }
        }
        let featureView = this.geoMap.getViewAroundFeatures(zoom);
	    this.geoMap.setView(featureView);
    }
}

setFeatures(annotations) {
    this.geoMap.resetMarkers();
    annotations.filter(anno => !anno.isEmpty()).forEach((anno) => {
        let marker = this.geoMap.addMarker(anno.body);
        anno.markerId = marker.getId();
    });
}

addAnnotation(anno) {
   this.addMarkerActive = true; 
   this.annotationToMark = anno;
   if(this.question.areaSelector) {
       this.question.areaSelector.disableDrawer();
   }
   this.update();
}

updateAnnotation(anno) {
    this.focusAnnotation(this.question.getIndex(anno));
}

/**
 * focus the annotation with the given index
 */
focusAnnotation(index) {
    let anno = this.question.getByIndex(index);
    if(anno) {
        let marker = this.geoMap.getMarker(anno.markerId);
        if(marker) {            
	        console.log("focus ", anno, marker);
	        
        }
    }
}

/**
 * check if instructions on how to create a new annotation should be shown
 */
showInstructions() {
    return !this.addMarkerActive && !this.opts.item.isReviewMode() &&  this.question.active && this.question.isRegionTarget();
}

/**
 * check if instructions to acivate this question should be shown, in order to be able to create annotations for this question
 */
showInactiveInstructions() {
    return !this.opts.item.isReviewMode() &&  !this.question.active && this.question.isRegionTarget() && this.opts.item.questions.filter(q => q.isRegionTarget()).length > 1;

}

showAddMarkerInstructions() {
    return this.addMarkerActive && !this.opts.item.isReviewMode() &&  this.question.active && this.question.isRegionTarget() ;

}

/**
 * check if a button to add new annotations should be shown
 */
showAddAnnotationButton() {
    return !this.question.isReviewMode() && !this.question.isRegionTarget() && this.question.mayAddAnnotation();
}

/**
 * template method to change the body of an annotation based on an event
 */
setNameFromEvent(event) {
    event.preventUpdate = true;
    if(event.item.anno) {            
        anno.setName(event.target.value);
        this.question.saveToLocalStorage();
    } else {
        throw "No annotation to set"
    }
}

initMap() {
    this.geoMap = new viewerJS.GeoMap({
        mapId : "geoMap_" + this.opts.index,
        initialView : {
            zoom: 5,
            center: [11.073397, 49.451993] //long, lat
        },
        allowMovingFeatures: !this.opts.item.isReviewMode(),
        language: Crowdsourcing.translator.language,
        popover: undefined,
        emptyMarkerMessage: undefined,
        popoverOnHover: false,
    })
    this.geoMap.init();

    this.geoMap.onFeatureMove.subscribe(feature => this.moveFeature(feature));
    this.geoMap.onFeatureClick.subscribe(feature => this.removeFeature(feature));
    this.geoMap.onMapClick.subscribe(geoJson => {
        if(this.addMarkerActive && (this.question.targetFrequency == 0 || this.geoMap.getMarkerCount() < this.question.targetFrequency)) {
            let marker = this.geoMap.addMarker(geoJson);
            if(this.annotationToMark) {
                this.annotationToMark.markerId = marker.getId();
                this.updateFeature(marker.getId());
            } else {        
            	this.addFeature(marker.getId());
            }
	        this.addMarkerActive = !this.question.isRegionTarget();
	        if(this.question.areaSelector) {
	            this.question.areaSelector.enableDrawer();
	        }
        }
    })
}

getAnnotation(id) {
    return this.question.annotations.find(anno => anno.markerId == id);
}

updateFeature(id) {
    let annotation = this.getAnnotation(id);
    let marker = this.geoMap.getMarker(annotation.markerId);
    annotation.setBody(marker.feature);
    annotation.setView(marker.feature.view);
    this.question.saveToLocalStorage();
}

/**
 * Add a new marker. If the marker doesn't exist as an annotation, it is added as well
 */
addFeature(id) {
    let marker = this.geoMap.getMarker(id);
    let annotation = this.question.addAnnotation();
    annotation.markerId = id;
    annotation.setBody(marker.feature);
    annotation.setView(marker.feature.view);
    this.question.saveToLocalStorage();
}

/**
 * Change the location of a feature
 */
moveFeature(feature) {
    let annotation = this.getAnnotation(feature.id);
    if(annotation) {
        annotation.setGeometry(feature.geometry);
        annotation.setView(feature.view);
    }
    this.question.saveToLocalStorage();
}

/**
 * Remove a marker. If the marker exists as an annotation, it is removed as well
 */
removeFeature(feature) {
    this.geoMap.removeMarker(feature);
	let annotation = this.getAnnotation(feature.id);
    if(annotation) {      
	    this.question.deleteAnnotation(annotation);
	    this.question.saveToLocalStorage();
    }
}


</script>


</geoLocationQuestion>

