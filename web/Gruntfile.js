'use strict';

// http://gruntjs.com/sample-gruntfile
module.exports = function(grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    // create file header from template
    template: {
      jsheader: {
        options: {
          data: {
            'year': grunt.template.today('yyyy'),
            'authors': 'ultical developers',
            'cmtstart': '//',
            'cmt': '//',
            'cmtend': '//'
          }
        },
        files: {
          'config/header.js': ['config/header.template']
        }
      }
    },
    // add banners to js files
    usebanner: {
      jsdist: {
        options: {
          replace: true,
          position: 'top',
          banner: grunt.file.read('config/header.js'),
          linebreak: true
        },
        files: [{
          src: ['components/**'],
          dest: 'components'
        }]
      }
    }
  });

  grunt.loadNpmTasks('grunt-template');
  grunt.loadNpmTasks('grunt-banner');

  // syncronize name and version between bower.json and package.json
  grunt.loadNpmTasks('grunt-sync-pkg');

  grunt.registerTask('default', ['template', 'sync']); //, ['usebanner']]);
};
