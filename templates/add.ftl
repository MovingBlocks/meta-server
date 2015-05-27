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

      <h3>Add Entry</h3>

<div id="actions">

<form action="add" method="post" class="form-horizontal">

<#include "edit-server-controls.ftl">

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
