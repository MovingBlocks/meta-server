<!DOCTYPE html>
<html lang="en">

<#include "metainfo.ftl">

  <body>

    <div class="container">

<#include "navigation.ftl">

<#if error??>
      <div class="alert alert-danger" role="alert">${error}</div>
</#if>

      <h3>Add Entry</h3>

<div id="actions">

<form action="add" method="post" class="form-horizontal">
  <div class="form-group">
    <label class="col-sm-2 control-label" for="inputName">Server Name</label>
    <div class="col-sm-10">
      <input type="text" name="name" class="form-control" id="inputName" placeholder="My Server">
    </div>
  </div>
  <div class="form-group">
    <label class="col-sm-2 control-label" for="inputAddress">Address</label>
    <div class="col-sm-6">
      <input type="text" name="address" class="form-control" id="inputAddress" placeholder="myGameServer.org">
    </div>
    <label class="col-sm-2 control-label" for="inputPort">Port</label>
    <div class="col-sm-2">
      <input type="number" name="port" class="form-control" id="inputPort" placeholder="25777" value="25777">
    </div>
  </div>
  <div class="form-group">
    <label class="col-sm-2 control-label" for="inputOwner">Owner</label>
    <div class="col-sm-10">
      <input type="text" name="owner" class="form-control" id="inputOwner" placeholder="MyAccountName">
    </div>
  </div>  
  <div class="form-group">
    <label class="col-sm-2 control-label" for="inputSecret">Secret</label>
    <div class="col-sm-10">
      <input type="password" name="secret" class="form-control" id="inputSecret" placeholder="secret key">
    </div>
  </div>

  <div class="row">
    <div class="col-sm-4 col-sm-offset-4">
      <button type="submit" class="btn btn-primary pull-right"><span class="glyphicon glyphicon-plus"></span> Add Server</button>
    </div>
  </div>

</form>

</div>

<br>

<#include "footer.ftl">

    </div> <!-- /container -->  

</body></html>
