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
    <#if meta??>
        <div>
            <ol class="breadcrumb">
                <li><a href="/modules/show">Modules</a></li>
                <li><a href="/modules/show/${meta.id}">${meta.id}</a></li>
                <li class="active">${meta.version}</li>
            </ol>

            <h2>${meta.displayName}</h2>
            <h4>${meta.version}</h4>

            <br>
            <p>${meta.description}</p>

            <br>
            <h4>Last Updated</h4>
            <p>
                ${updated?date?string.long}<br>
                ${updated?time?string.long}
            </p>

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
                        <a href="/modules/show/${dep.id}">${dep.id}</a> between ${dep.minVersion} and ${dep.maxVersion}
                        <#if dep.optional>
                            (optional)
                        </#if>
                        <br>
                    </#list>
                </#if>
            </p>

            <br>
            <h4>Resolved by</h4>
            <p>
                <#if dependencies?size == 0>
                    <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> Unresolved
                <#else>
                    <#list dependencies as dep>
                        <a href="/modules/show/${dep.id}/${dep.version}">${dep.id} ${dep.version}</a><br>
                    </#list>
                    (there may be other combinations)
                </#if>
            </p>
            <br>

            <a href="${downloadUrl}" class="btn btn-lg btn-primary">
                <span class="glyphicon glyphicon-download-alt" aria-hidden="true"></span> Download (${downloadSize} kB)
            </a>

            <br><br>
            <br>

        </div>

    </#if>

    <#include "footer.ftl">

</div> <!-- /container -->

</body>
</html>
