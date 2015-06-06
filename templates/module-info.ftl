<!DOCTYPE html>
<html lang="en">

<#include "metainfo.ftl">

  <body>

    <div class="container">

<#assign tab = "modules">
<#include "navigation.ftl">

<#if message??>
      <div class="alert alert-success" role="alert">${message}</div>
</#if>

      <div>
        <h3>${meta.displayName} ${meta.version}</h3>
        <p>${meta.description}</p>
      </div>

  <br>

<#include "footer.ftl">

    </div> <!-- /container -->

</body></html>
