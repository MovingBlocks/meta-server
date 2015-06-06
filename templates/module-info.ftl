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
        <h2>${meta.displayName}</h4>
        <h4>${meta.version}</h4>

        <br>
        <p>${meta.description}</p>

        <br>
        <h4>Required Permissions</h4>
        <p><#if meta.requiredPermissions?size == 0>
           none
           <#else>
           ${meta.requiredPermissions?join(", ")}
           </#if>
        </p>

        <br>
        <h4>Dependencies</h4>
        <p><#if meta.dependencies?size == 0>
           none
           <#else>
    <#list meta.dependencies as dep>
           ${dep.id} between ${dep.minVersion} and ${dep.maxVersion}
      <#if dep.optional>
           (optional)
      </#if>
           <br>
    </#list>
           </#if>
        </p>

        <br>
        <p><b>Last Updated:</b> ${updated?datetime}</p>

        <br>
        <p><a style="font-size:20px" href="${downloadUrl}">
          <span class="glyphicon glyphicon-download" aria-hidden="true" title="Download"></span>
          Download (${downloadSize} kB)</a>
        </p>

        <br>

<#include "footer.ftl">

    </div> <!-- /container -->

</body></html>
