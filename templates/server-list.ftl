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

      <div>
        <table class="table table-striped table-hover unselectable" id="server-list">

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
                <tr class="entry <#if item.active == true><#else>disabled</#if>">
                    <td class="server-name" >${item.name}</td>
                    <td class="server-address">${item.address}</td>
                    <td class="server-port">${item.port?c}</td>
                    <td class="server-owner">${item.owner}</td>
                    <td class="server-location">
                    <#if item.country??>
                        <img src="/img/flags/${item.country?lower_case}.png" alt="${item.country}" title="${item.country}">
                    </#if>
                    ${item.city!}
                    </td>
                    <td class="edit-remove" title="Edit/Remove">
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
