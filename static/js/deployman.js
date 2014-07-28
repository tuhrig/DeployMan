var deployman = angular.module('deploymanApp',['ngRoute', 'chieffancypants.loadingBar', 'ui.bootstrap']);

deployman.config(function($routeProvider) {
	$routeProvider
		.when('/instance-view/:id', { templateUrl: 'templates/instance.html' })
		.when('/instances-view', { templateUrl: 'templates/instances.html' })
		.when('/routes-view', { templateUrl: 'templates/routes.html' })
		.when('/job-view/:id', { templateUrl: 'templates/job.html' })
		.when('/jobs-view', { templateUrl: 'templates/jobs.html' })
		.when('/help-view', { templateUrl: 'templates/help.html' })
		.when('/settings-view', { templateUrl: 'templates/settings.html' })
		.when('/setups-view', { templateUrl: 'templates/setups.html' })
		.when('/databases-view', { templateUrl: 'templates/databases.html' })
    	.when('/images-view', { templateUrl: 'templates/images.html' })
    	.when('/inits-view', { templateUrl: 'templates/inits.html' })
    	.when('/configs-view', { templateUrl: 'templates/configs.html' })
    	.when('/formation-view/:id', { templateUrl: 'templates/formation.html' })
    	.when('/formations-view', { templateUrl: 'templates/formations.html' })
    	.when('/container-view/:instance/:container', { templateUrl: 'templates/container.html' })
    	.when('/', { templateUrl: 'templates/home.html' })
    	.otherwise({ redirectTo: '/' });
})

deployman.controller('CloudInitCtrl', function($scope, $http, $routeParams){
	$http.get('/instances/'+$routeParams.id+'/cloud-init').then(function(response) {
		$scope.cloudInit = response.data;
	});
});

deployman.controller('JobsCtrl', function($scope, $http){
	$http.get('/jobs').then(function(response) {
		$scope.jobs = response.data;
	});
});

deployman.controller('JobCtrl', function($scope, $http, $routeParams){
	$http.get('/jobs/'+$routeParams.id).then(function(response) {
		$scope.job = response.data;
	});
});


deployman.controller('ContainerCtrl', function($scope, $http, $routeParams){
	$http.get('/docker/'+$routeParams.instance+'/containers/'+$routeParams.container).then(function(response) {
		$scope.container = response.data;
	});
});

deployman.controller('InitsCtrl', function($scope, $http){
	$http.get('/inits').then(function(response) {
		$scope.inits = response.data;
	});
});

deployman.controller('InstancesCtrl', function($scope, $http){
	$http.get('/instances').then(function(response) {
		$scope.instances = response.data;
	});
});

deployman.controller('InstanceCtrl', function($scope, $http, $routeParams){

	$scope.refreshInstance = function() {

		$http.get('/instances/'+$routeParams.id).then(function(response) {
	        $scope.instance = response.data;
	        $scope.predicate = '-state.name';
		});
	}

	$scope.splitMountString = function(string, nb) {
	    $scope.array = string.split(':');
	    return $scope.result = $scope.array[nb];
	}

	$http.get('/instances/'+$routeParams.id).then(function(response) {
        $scope.instance = response.data;
        $scope.predicate = '-state.name';
	});
});

deployman.controller('DatabasesCtrl', function($scope, $http){
	$http.get('/databases').then(function(response) {
		$scope.databases = response.data;
	});
});

deployman.controller('RoutesCtrl', function($scope, $http){
	$http.get('/routes').then(function(response) {
		$scope.routes = response.data;
	});
});

deployman.controller('SetupsCtrl', function($scope, $http){
	$http.get('/repo/locale/setups').then(function(response) {
		$scope.setups = response.data;
	});
});

deployman.controller('FormationCtrl', function($scope, $http, $routeParams){
	$http.get('/repo/locale/formations/'+$routeParams.id).then(function(response) {
		$scope.formation = response.data;
	});
});

deployman.controller('FormationsCtrl', function($scope, $http){
	$http.get('/repo/locale/formations').then(function(response) {
		$scope.formations = response.data;
	});
});

deployman.controller('SettingsCtrl', function($scope, $http){
	$http.get('/repo/locale/settings').then(function(response) {
		$scope.settings = response.data;
	});
});

deployman.controller('ImagesCtrl', function($scope, $http){
	$http.get('/repo/remote/images').then(function(response) {
		$scope.images = response.data;
	});
});

deployman.controller('ConfigsCtrl', function($scope, $http){
	$http.get('/repo/remote/configs').then(function(response) {
		$scope.configs = response.data;
	});
});

// from: https://gist.github.com/thomseddon/3511330
deployman.filter('bytes', function() {
	return function(bytes, precision) {
		if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) return '-';
		if (typeof precision === 'undefined') precision = 1;
		var units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'],
			number = Math.floor(Math.log(bytes) / Math.log(1024));
		return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) +  ' ' + units[number];
	}
});

deployman.filter('duration', function() {
	return function(string) {
		if(string !== undefined) {
			var time = string.split('done in ');

			return new Date(null, null, null, null, null, time[1]).toTimeString().match(/\d{2}:\d{2}:\d{2}/)[0]
		}
		return "";
	}
});

// from http://stackoverflow.com/questions/18095727/how-can-i-limit-the-length-of-a-string-that-displays-with-when-using-angularj
angular.module('ng').filter('cut', function () {

    return function (value, max, tail) {

        if (!value)
        	return '';

        max = parseInt(max, 10);

        if (!max)
        	return value;

        if (value.length <= max)
        	return value;

        value = value.substr(0, max);
        return value + (tail || ' …');
    };
});