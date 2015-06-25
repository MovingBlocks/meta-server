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

<#if moduleId??>
        <ol class="breadcrumb">
          <li><a href="/modules/show">Modules</a></li>
          <li class="active">${moduleId}</li>
        </ol>
<#else>
        <ol class="breadcrumb">
          <li class="active">Modules</li>
        </ol>
</#if>

        <table class="table table-striped table-hover unselectable" id="server-list">

            <thead>
                <tr>
                    <th style="width:50%" class="name header">Name</th>
                    <th style="width:50%" class="name header">Version</th>
                </tr>
            </thead>
            <tbody>
<#list items?keys as key>
                <tr class="entry">
                    <td class="module-name">
<#if moduleId??>
                       ${key}
<#else>
                       <a href="/modules/show/${key}">${key}</a>
</#if>
                    </td>
                    <td class="module-version">

    <#list items[key] as artifact>
                      <a href="/modules/show/${key}/${artifact.version}">${artifact.version}</a>
    </#list>

                    </td>
                </tr>
</#list>

            </tbody>
        </table>
      </div>

  <br>

<#include "footer.ftl">

    </div> <!-- /container -->

</body></html>
