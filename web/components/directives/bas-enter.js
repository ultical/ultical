'use strict';

app.directive('basEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 13) {
                scope.$apply(function (){
                    scope.$eval(attrs.basEnter);
                });

                event.preventDefault();
            }
        });
    };
});
