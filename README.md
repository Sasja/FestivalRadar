FestivalRadar
=============
this branch contains mock versions of the different components to enable easier develoment of separate components (eg. RadarDatabase.java, LocalisationSubService.java, CloudSubService.java)

in order to develop some component while mocking the others:

1. branch this branch, if class to develop has mock version, and you want to develop the real version: checkout the appropriate class from master (eg. 'git checkout master .../RadarDatabase.java')
2. develop on new branch, stick to the file as much as possible
3. Then pull that one file back to master branch and/or spoof branch and make it work, other (minor) changes in other files should also be pulled to master as well as spoof branch.
4. finaly delete branch (branch -d database; branch push origin :database)
 
Only keep mock versions for components worth mocking, eg. Activity classes should probably not be mocked.

may god be with you
