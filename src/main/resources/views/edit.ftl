<!DOCTYPE html>
<html lang="en">

<#include "metainfo.ftl">

  <body>

    <div class="container">

<#assign tab = "servers">
<#include "navigation.ftl">

<#if error??>
      <div class="alert alert-danger" role="alert">${error}</div>
</#if>

      <h3>Edit Entry</h3>

<div id="actions">

<form method="post" class="form-horizontal">

<#assign disabled = "yes">
<#include "edit-server-controls.ftl">

  <div class="row">
    <div class="col-sm-2 col-sm-offset-4">
      <button type="submit" formaction="update" class="btn btn-primary pull-right"><span class="glyphicon glyphicon-pencil"></span> Update</button>
    </div>
    <div class="col-sm-2">
      <button type="submit" formaction="remove" class="btn btn-danger pull-right"><span class="glyphicon glyphicon-minus"></span> Remove</button>
    </div>
  </div>

</form>

</div>

<br>

<#include "footer.ftl">

    </div> <!-- /container -->

</body></html>
