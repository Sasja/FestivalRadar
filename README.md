FestivalRadar
=============
this branch contains mock versions of the different components to enable easier develoment of separate components (eg. RadarDatabase.java, LocalisationSubService.java, CloudSubService.java)

1. branch this branch, develop the chosen component until done.
2. Then merge to master and to spoof branch
  * to master: the fully developped component + any changes to interfaces
  * to spoof : changes to interfaces + adapt spoof-branch mockversion of component to use new interface and emulate any new behaviour
3. finaly delete branch
 
Only make mock versions for components worth mocking, eg. developed Activity classes should probably be fully merged to master branch as well as spoof branch.

may god be with you
