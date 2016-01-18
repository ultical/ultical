app.provider("flipConfig", function() {

	var cssString =
		"<style> \
		.{{flip}} {float: left; overflow: hidden; width: {{width}}; height: {{height}}; }\
		.{{flip}}Panel { \
		position: absolute; \
		width: {{width}}; height: {{height}}; \
		-webkit-backface-visibility: hidden; \
		backface-visibility: hidden; \
		transition: -webkit-transform {{tempo}}; \
		transition: transform {{tempo}}; \
		-webkit-transform: perspective( 800px ) rotateX( 0deg ); \
		transform: perspective( 800px ) rotateX( 0deg ); \
		} \
		.{{flip}}HideBack { \
		-webkit-transform:  perspective(800px) rotateX( 180deg ); \
		transform:  perspective(800px) rotateX( 180deg ); \
		} \
		.{{flip}}HideFront { \
		-webkit-transform:  perspective(800px) rotateX( -180deg ); \
		transform:  perspective(800px) rotateX( -180deg ); \
		} \
		</style> \
		";

	var _tempo = "1s";
	var _width = "100px";
	var _height = "100px";

	var _flipClassName = "flip";

	var _flipsOnClick = true;

	this.setTempo = function(tempo) {
		_tempo = tempo;
	};

	this.setDim = function(dim) {
		_width = dim.width;
		_height = dim.height;
	}

	this.setClassName = function(className) {
		_flipClassName = className;
	};

	this.flipsOnClick = function(bool){
		_flipsOnClick = bool;
	}

	this.$get = function($interpolate) {

		var interCss = $interpolate(cssString);
		var config = {
				width: _width,
				height: _height,
				tempo: _tempo,
				flip: _flipClassName
		};

		document.head.insertAdjacentHTML("beforeend", interCss(config));

		return {
			classNames: {
				base: _flipClassName,
				panel: _flipClassName + "Panel",
				hideFront: _flipClassName + "HideFront",
				hideBack: _flipClassName + "HideBack"
			},
			flipsOnClick : _flipsOnClick
		}
	};
});

app.config(function(flipConfigProvider){
	flipConfigProvider.setClassName("flipperCosmic");
	flipConfigProvider.setTempo("0.5s");
	flipConfigProvider.setDim({width:"100px", height:"40px"});
	flipConfigProvider.flipsOnClick(false);
});

app.directive("flip", function(flipConfig) {

	function setDim(element, width, height) {
		if (width) {
			element.style.width = width;
		}
		if (height) {
			element.style.height = height;
		}
	}

	return {
		restrict: "E",
		controller: function($scope, $element, $attrs) {

			$attrs.$observe("flipShow", function(newValue){
				if(newValue === "front"){
					showFront();
				} else if(newValue === "back"){
					showBack();
				} else {
					console.warn("FLIP: Unknown side.");
				}
			});

			var self = this;
			self.front = null,
			self.back = null;


			function showFront() {
				self.front.removeClass(flipConfig.classNames.hideFront);
				self.back.addClass(flipConfig.classNames.hideBack);
			}

			function showBack() {
				self.back.removeClass(flipConfig.classNames.hideBack);
				self.front.addClass(flipConfig.classNames.hideFront);
			}

			self.init = function() {
				self.front.addClass(flipConfig.classNames.panel);
				self.back.addClass(flipConfig.classNames.panel);

				showFront();

				if(flipConfig.flipsOnClick){
					self.front.on("click", showBack);
					self.back.on("click", showFront);
				}
			}

		},

		link: function(scope, element, attrs, ctrl) {

			var width = attrs.flipWidth,
			height = attrs.flipHeight;

			element.addClass(flipConfig.classNames.base);

			if (ctrl.front && ctrl.back) {
				[element, ctrl.front, ctrl.back].forEach(function(el) {
					setDim(el[0], width, height);
				});
				ctrl.init();
			} else {
				console.error("FLIP: 2 panels required.");
			}
		}
	}

});

app.directive("flipPanel", function() {
	return {
		restrict: "E",
		require: "^flip",
		link: function(scope, element, attrs, flipCtr) {
			if (!flipCtr.front) {
				flipCtr.front = element;
			} else if (!flipCtr.back) {
				flipCtr.back = element;
			} else {
				console.error("FLIP: Too many panels.");
			}
		}
	}
});