<!DOCTYPE html>
<html lang="en">

<#include "metainfo.ftl">

  <body>

    <div class="container">

<#assign tab = "servers">
<#include "navigation.ftl">

<#if message??>
      <div class="alert alert-success" role="alert">${message}</div>
</#if>

      <div class="server-list">
        <table class="table table-striped table-hover unselectable" id="playlist">

            <thead>
                <tr>
                    <th class="name header">Name</th>
                    <th class="name header">Address</th>
                    <th class="name header">Port</th>
                    <th class="name header">Owner</th>
                    <th class="name header">Location</th>
                    <th class="name header">Edit</th>
                </tr>
            </thead>
            <tbody>
<#list items as item>
                <tr class="entry">
                    <td>${item.name}</td>
                    <td>${item.address}</td>
                    <td>${item.port?c}</td>
                    <td>${item.owner}</td>
                    <td>
                    <#if item.country??>
                        <img src="/img/flags/${item.country?lower_case}.png" title="${item.country}"/>
                    </#if>
                    ${item.city!}
                    </td>
                    <td align="center" title="Edit/Remove">
                      <a href="edit?index=${item_index}">
                        <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
                      </a>
                    </td>
                </tr>
</#list>

            </tbody>
        </table>
      </div>

  <div class="row">
    <div class="col-sm-12">
      <a href="add" class="btn btn-success pull-right"><span class="glyphicon glyphicon-plus"></span> Add new server</a>
    </div>
  </div>

  <br>

<#include "footer.ftl">

    </div> <!-- /container -->

</body></html>
