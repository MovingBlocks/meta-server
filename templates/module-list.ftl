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
                    <td class="module-name" >${key}</td>
                    <td class="module-version">

    <#list items[key] as artifact>
                      <a href="${key}/${artifact}">${artifact}</a> 
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