var dragAndDropUpload = function(targetID, fileSelectorID, actionURL, enterFunction, exitFunction, overFunction, completeFunction, completeAllFunction, errorFunction) {
	var dropTarget = document.getElementById(targetID),
		fileSelectTarget = document.getElementById(fileSelectorID),
		upload = function(file) {
			var xhr = new XMLHttpRequest(),
				formData = new FormData(),
				progressBar,
				progress,
				label;
			
			// create a progress element
			progressBar = document.createElement('progress');
			progressBar.value = 0;
			progressBar.max = 100;
			progressBar.textContent = '0%';
			
			progress = document.createElement('div');
			progress.className = 'uploadProgress';
			
			label = document.createElement('span');
			label.appendChild(document.createTextNode(file.name));
			
			progress.appendChild(label);
			progress.appendChild(progressBar);
			dropTarget.appendChild(progress);
			
			xhr.open('POST', actionURL, true);
			xhr.onload = function(e) {
				// remove the progress element
				dropTarget.removeChild(progress);
				
				if(this.status >= 400) {
					// indicate error
					if(errorFunction) {
						errorFunction(e, file);
					}
				}
				
				// execute a function indicating the upload finished
				if(completeFunction) {
					completeFunction(e, file);
				}
			};
			xhr.upload.onprogress = function(event) {
				if (event.lengthComputable) {
					progressBar.value = (event.loaded / event.total) * 100;
					progressBar.textContent = progressBar.value + '%';
				}
			}
			
			formData.append(targetID,file);
			xhr.send(formData);
		},
		processFiles = function(files) {
			var count = files.length,
				i;

			for(i = 0; i < count; i++) {
				if(files.item) {
					//webkit and mozilla
					upload(files.item(i));
				} else {
					//presto
					upload(files[i]);
				}
			}

			// execute a function indicating all uploads are finished
			if(completeAllFunction) {
				completeAllFunction(event);
			}
			
			return false;
		},
		noopHandler = function(event) {
			event.stopPropagation();
			event.preventDefault();
			return false;
		},
		dropHandler = function(event) {
			event.stopPropagation();
			event.preventDefault();
			
			processFiles(event.dataTransfer.files);
			return false;
		},
		fileSelectionHandler = function(event) {
			event.stopPropagation();
			event.preventDefault();
			
			processFiles(event.target.files);
			return false;
		}, 
		body = document.getElementsByTagName('body')[0];

	
	dropTarget.addEventListener("dragenter", enterFunction?enterFunction:noopHandler, false);
	dropTarget.addEventListener("dragexit", exitFunction?exitFunction:noopHandler, false);
	dropTarget.addEventListener("dragover", overFunction?overFunction:noopHandler, false);
	dropTarget.addEventListener("drop", dropHandler, false);
	if (fileSelectTarget) {
		fileSelectTarget.addEventListener("change", fileSelectionHandler, false);
		fileSelectTarget.addEventListener("drop", dropHandler, false);
	}
	/*
	 * If the user misses the drop target, the browser will attempt to load
	 * the dropped file. That is undesirable, so use the noopHandler function
	 * on the body element to prevent it. The body may still need to be
	 * stretched to cover the entire viewport to achieve this.
	 */
	body.addEventListener("drop", noopHandler, false);
}