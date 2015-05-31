
      <div class="header clearfix">
        <nav>
          <ul class="nav nav-pills pull-right">
            <li role="presentation"
                <#if tab == "modules">class="active"</#if>
                ><a href="/modules/show">Modules</a>
            </li>
            <li role="presentation"
                <#if tab == "servers">class="active"</#if>
                ><a href="/servers/show">Servers</a>
            </li>
            <li role="presentation"
                <#if tab == "about">class="active"</#if>
                ><a href="/home"><span class="glyphicon glyphicon-home" aria-hidden="true" title="Home"></span></a>
            </li>
          </ul>
        </nav>
        <h3 class="text-muted">
          <img src="/img/sweet-gooey-smallish.png" height="66" alt="Gooey Sweet">
          Terasology Web Server
        </h3>
      </div>
