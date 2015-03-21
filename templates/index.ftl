<!DOCTYPE html>
<html lang="en"><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">
    <!--link rel="icon" href="http://getbootstrap.com/favicon.ico"-->

    <title>Terasology Master Server</title>

    <!-- Bootstrap core CSS -->
    <link href="/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="/css/jumbotron-narrow.css" rel="stylesheet">

  </head>

  <body>

    <div class="container">
      <div class="header clearfix">
        <nav>
          <ul class="nav nav-pills pull-right">
            <li role="presentation" class="active"><a href="#">Servers</a></li>
          </ul>
        </nav>
        <h3 class="text-muted"><img src="/img/sweet-gooey-smallish.png" height="66" alt="Gooey Sweet">  Terasology Game Servers</h3>
      </div>

      <div class="server-list">
        <table class="table table-striped table-hover unselectable" id="playlist">
        
            <thead>
                <tr>
                    <th class="name header">Name</th>
                    <th class="name header">Address</th>
                    <th class="name header">Port</th>
                    <th class="name header">Location</th>
                </tr>
            </thead>
            <tbody>
<#list items as item>
                <tr class="entry">
                    <td>${item.name}</td>
                    <td>${item.address}</td>
                    <td>${item.port?c}</td>
                    <td>${item.country!"?"}</td>
                </tr>
</#list>
                    
            </tbody>
        </table>
      </div>

      <footer class="footer">
        <p>&#169; MovingBlocks</p>
      </footer>

    </div> <!-- /container -->  

</body></html>
